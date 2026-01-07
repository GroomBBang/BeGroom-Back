package com.example.BeGroom.payment.service;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentFailReason;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.dto.PaymentConfirmReqDto;
import com.example.BeGroom.usecase.checkout.dto.CheckoutResDto;

public interface PaymentService {
    Payment create(Long orderId, PaymentMethod paymentMethod);
    void approve(Long paymentId);
    void fail(Long paymentId, PaymentFailReason reason);

    CheckoutResDto confirm(PaymentConfirmReqDto reqDto);
}
