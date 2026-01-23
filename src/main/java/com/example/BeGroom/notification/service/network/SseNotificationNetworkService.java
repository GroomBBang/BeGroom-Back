package com.example.BeGroom.notification.service.network;

import com.example.BeGroom.notification.dto.NetworkMessageDto;
import com.example.BeGroom.notification.dto.SseMessageDto;
import com.example.BeGroom.notification.repository.EmitterRepository;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static com.example.BeGroom.notification.domain.SseEventMessage.*;
import static com.example.BeGroom.notification.util.MessageUtil.makeEmitterId;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseNotificationNetworkService implements NotificationNetworkService {

    private final EmitterRepository emitterRepository;
    private final MemberNotificationRepository memberNotificationRepository;

    @Value("${sse.timeout}")
    private Long defaultTimeout;

    @Override
    public SseEmitter connect(Long memberId, LocalDateTime connectTime) {
        if(memberId == null || memberId <= 0L){
            throw new IllegalArgumentException("멤버 ID는 양수여야 합니다.");
        }

        long timestamp = connectTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String emitterId = memberId + "_" + timestamp;
        SseEmitter emitter = new SseEmitter(defaultTimeout);

        emitter.onCompletion(() -> {
            log.info("SSE Connection Completed: {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });
        emitter.onTimeout(() -> {
            emitter.complete();
            log.info("SSE Connection Timed Out: {}", emitterId);
        });
        emitter.onError((_) -> {
            emitter.complete();
        });

        emitterRepository.save(emitterId, emitter);

        return emitter;
    }

    @Override
    public void send(List<NetworkMessageDto> messages) {
        for (NetworkMessageDto message : messages) {

            // 2. 이 메시지를 받을 사람의 ID 확인
            Long receiverId = message.getReceiverId();

            // 3. 해당 유저의 Emitter들만 쏙 빼옵니다. (PC, 모바일 탭 동시 접속 고려)
            Map<String, SseEmitter> userEmitters = emitterRepository.findAllStartWithById(receiverId);

            // 4. 해당 유저의 모든 연결(탭)에 메시지를 발송합니다.
            userEmitters.forEach((emitterId, emitter) -> {
                sendBySse(
                        emitter,
                        message.getEventId(),   // DTO에 담겨온 DB PK (책갈피용 ID)
                        message.getEventName(), // "notification"
                        message.getData()       // 실제 알림 내용
                );
            });
        }
    }

    @Override
    public void disconnect(Long memberId) {
        if(memberId == null || memberId <= 0L){
            throw new IllegalArgumentException("멤버 ID는 양수여야 합니다.");
        }

        Map<String, SseEmitter> emitters = emitterRepository.findAllStartWithById(memberId);

        emitters.forEach((id, emitter) -> {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("[SSE] Disconnect failed : " + id);
            } finally {
                emitterRepository.deleteById(id);
            }
        });
    }

    public SseEmitter subscribeWithHistory(Long memberId, String lastEventId, LocalDateTime connectTime) {
        SseEmitter emitter = this.connect(memberId, connectTime);
        String emitterId = makeEmitterId(memberId, connectTime);
        this.sseConnectionMessage(memberId, lastEventId, emitterId, emitter);
        return emitter;
    }

    public void sseConnectionMessage(Long memberId, String lastEventId, String emitterId, SseEmitter emitter) {
        if (!lastEventId.isEmpty()) {
            try {
                long lastId = MessageUtil.parseMessageIdFromHeader(lastEventId);
                long lostCount = memberNotificationRepository.countByMemberIdAndIdGreaterThan(memberId, lastId);

                if (lostCount > 0) {
                    sendBySse(emitter, emitterId, "notification", RETRY_RECEIVE_NOTIFICATION_SUCCESS.format(lostCount));
                }
            } catch (Exception e) {
                log.error("재연결 데이터 처리 중 오류", e);
            }
        } else {
            long unreadCount = memberNotificationRepository.countByMemberIdAndIsReadFalse(memberId);

            if (unreadCount > 0) {
                sendBySse(emitter, emitterId, "notification", FIRST_CONNECT_UNREAD.format(unreadCount));
            } else {
                sendBySse(emitter, emitterId, "connect", CONNECT);
            }
        }
    }

    public void sendBySse(SseEmitter emitter, String id, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(id)
                    .name(eventName)
                    .data(data));
        } catch (IOException | IllegalStateException e) {
            emitterRepository.deleteById(id);
        }
    }
}