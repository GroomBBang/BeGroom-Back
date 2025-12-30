package com.example.BeGroom.notification.dto;

import com.example.BeGroom.notification.domain.MemberNotification;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetMemberNotificationResDto {

    private long unreadCount;
    private List<NotificationInfo> notifications;

    public static GetMemberNotificationResDto of(List<MemberNotification> entities, long unreadCount) {
        List<NotificationInfo> list = entities.stream()
                .map(NotificationInfo::from)
                .collect(Collectors.toList());

        return GetMemberNotificationResDto.builder()
                .unreadCount(unreadCount)
                .notifications(list)
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationInfo {
        private Long id;
        private String type;
        private String title;
        private String message;
        private String link;
        private boolean isRead;
        private LocalDateTime createdTime;

        public static NotificationInfo from(MemberNotification entity) {
            return NotificationInfo.builder()
                    .id(entity.getId())
                    .type(entity.getNotification().getType().name())
                    .title(entity.getNotification().getTitle())
                    .message(entity.getNotification().getMessage())
                    .link(entity.getNotification().getLink())
                    .isRead(entity.isRead())
                    .createdTime(entity.getCreatedTime())
                    .build();
        }
    }
}