package com.example.BeGroom.notification.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.MemberCreateReqDto;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import com.example.BeGroom.notification.dto.CreateNotificationReqDto;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;

public interface NotificationService {
    public void send(Member receiver, NotificationType type, String title, String msg, String link);
    public GetMemberNotificationResDto getMyNotifications(Long memberId);
    public void readNotification(Long mappingId);
    Notification createNotification(CreateNotificationReqDto reqDto);
}
