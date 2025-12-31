package com.example.BeGroom.notification.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.MemberCreateReqDto;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import com.example.BeGroom.notification.dto.CreateNotificationReqDto;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberNotificationRepository memberNotificationRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Override
    public GetMemberNotificationResDto getMyNotifications(Long memberId) {
        List<MemberNotification> notiList = memberNotificationRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
        long unreadCount = memberNotificationRepository.countByMemberIdAndIsReadFalse(memberId);
        return GetMemberNotificationResDto.of(notiList, unreadCount);
    }

    @Transactional(readOnly = false)
    @Override
    public void send(List<Long> receiverIds, Long templateId, Map<String, String> variables) {
        Notification template = notificationRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("해당 타입의 알림 템플릿이 없습니다."));

        String jsonMetaData;
        jsonMetaData = objectMapper.writeValueAsString(variables);

        List<Member> receivers = memberRepository.findAllById(receiverIds);

        List<MemberNotification> notifications = receivers.stream()
                .map(receiver -> new MemberNotification(receiver, template, jsonMetaData))
                .collect(Collectors.toList());

        memberNotificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = false)
    public void sendToAllMembers(Long templateId, Map<String, String> variables) {
        Notification template = notificationRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("해당 타입의 알림 템플릿이 없습니다."));

        String jsonMetaData;
        jsonMetaData = objectMapper.writeValueAsString(variables);

        List<Long> targetIds = memberRepository.findAllIds();

        List<MemberNotification> notifications = targetIds.stream()
                .map(targetId -> {
                    Member memberProxy = memberRepository.getReferenceById(targetId);
                    return new MemberNotification(memberProxy, template, jsonMetaData);
                })
                .collect(Collectors.toList());

        memberNotificationRepository.saveAll(notifications);
    }

    @Override
    public void readNotification(Long mappingId) {
        MemberNotification memberNoti = memberNotificationRepository.findById(mappingId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 알림입니다."));

        memberNoti.read();
    }

    @Override
    @Transactional(readOnly = false)
    public Notification createNotification(CreateNotificationReqDto reqDto) {

        Notification notification = Notification.createNotification(
                reqDto.getType(),
                reqDto.getTitle(),
                reqDto.getMessage(),
                reqDto.getLink()
        );
        notificationRepository.save(notification);

        return notification;
    }

}
