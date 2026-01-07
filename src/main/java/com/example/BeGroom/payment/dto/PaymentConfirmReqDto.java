package com.example.BeGroom.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmReqDto {

    private String orderId;
    private Long paymentId;
    private String paymentKey;
    private Long amount;

}
