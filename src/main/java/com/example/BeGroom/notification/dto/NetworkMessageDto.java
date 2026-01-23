package com.example.BeGroom.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NetworkMessageDto {
    private Long receiverId;
    private String eventId;
    private String eventName;
    private String message;
    private Object data;

    @Builder
    private NetworkMessageDto(Long receiverId, String eventId, String eventName, String message, Object data) {
        this.receiverId = receiverId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.message = message;
        this.data = data;
    }
}