package com.example.BeGroom.notification.scheduler;

import com.example.BeGroom.notification.repository.EmitterRepository;
import com.example.BeGroom.notification.service.network.SseNotificationNetworkService;
import com.example.BeGroom.notification.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import static com.example.BeGroom.notification.domain.SseEventMessage.HEARTBEAT;

@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {
    private final EmitterRepository emitterRepository;
    private final SseNotificationNetworkService sseNotificationNetworkService;

    @Scheduled(fixedRate = 45000)
    public void sendHeartbeat() {
        Map<String, SseEmitter> emitters = emitterRepository.findAll();

        emitters.forEach((id, emitter) -> {
            sseNotificationNetworkService.sendBySse(emitter, id, HEARTBEAT.getEventName(),
                    MessageUtil.createMessageByHashMap(HEARTBEAT.getMessageTemplate()));
        });
    }
}
