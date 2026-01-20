package com.example.BeGroom.notification.service;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest extends IntegrationTestSupport {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MemberNotificationRepository memberNotificationRepository;

    @DisplayName("로그인한 사용자가 자신의 알림 리스트를 가져온다.")
    @Test
    void test(){
        // given
        Long memberId = 1L;

        // memberNotificationRepository.save()

        // when
        notificationService.getMyNotifications(memberId);

        // then
    }
}