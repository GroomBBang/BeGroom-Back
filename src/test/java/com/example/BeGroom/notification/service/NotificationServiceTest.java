package com.example.BeGroom.notification.service;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import com.example.BeGroom.notification.dto.CreateNotificationReqDto;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class NotificationServiceTest extends IntegrationTestSupport {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberNotificationRepository memberNotificationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @DisplayName("관리자가 알림을 생성한다.")
    @Test
    void createNotificationService(){
        // given
        CreateNotificationReqDto createNotificationReqDto = CreateNotificationReqDto.builder()
                .type(NotificationType.ORDER).title("테스트 제목").message("테스트 메시지").link("test url").build();

        // when
        Notification result = notificationService.createNotification(createNotificationReqDto);

        // then
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
        assertThat(result.getMessage()).isEqualTo("테스트 메시지");
        assertThat(result.getType()).isEqualTo(NotificationType.ORDER);
    }

    @DisplayName("로그인한 사용자가 자신의 알림 리스트를 가져온다.")
    @Test
    void getMyNotifications(){
        // given
        Member member = creteMember();
        memberRepository.save(member);
        Notification notification = createNotification();
        notificationRepository.save(notification);
        MemberNotification memberNotification = createMemberNotification(member, notification);
        memberNotificationRepository.save(memberNotification);

        // when
        List<GetMemberNotificationResDto.NotificationInfo> response = notificationService.getMyNotifications(member.getId()).getNotifications();

        // then
        assertThat(response).hasSize(1).extracting("title", "isRead").containsExactlyInAnyOrder(
                tuple("테스트 제목", false)
        );
    }

    @DisplayName("")
    @Test
    void readNotification(){
        // given

        // when

        // then
    }

    @DisplayName("")
    @Test
    void readAllNotifications(){
        // given

        // when

        // then
    }

    @DisplayName("")
    @Test
    void send(){
        // given

        // when

        // then
    }

    @DisplayName("")
    @Test
    void sendToAllMembers(){
        // given

        // when

        // then
    }

    private Member creteMember(){
        return Member.builder()
                .email("user")
                .name("user")
                .password("1234")
                .phoneNumber("01012341234")
                .build();
    }

    private Notification createNotification(){
        return Notification.builder()
                .type(NotificationType.ORDER)
                .title("테스트 제목")
                .message("메세지")
                .link("url")
                .build();
    }

    private MemberNotification createMemberNotification(Member member, Notification notification){
        return MemberNotification.builder()
                .member(member)
                .notification(notification)
                .metaData("테스트입니다.")
                .isRead(false)
                .build();
    }
}