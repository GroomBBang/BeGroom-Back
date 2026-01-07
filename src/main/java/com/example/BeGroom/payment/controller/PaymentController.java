package com.example.BeGroom.payment.controller;

import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.dto.PaymentConfirmReqDto;
import com.example.BeGroom.payment.dto.PaymentCreateReqDto;
import com.example.BeGroom.payment.dto.PaymentCreateResDto;
import com.example.BeGroom.payment.service.PaymentService;
import com.example.BeGroom.usecase.checkout.dto.CheckoutResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@Tag(name = "Payment API", description = "결제 관련 API")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "결제 생성", description = "생성된 주문에 대한 결제를 생성한다.")
    public ResponseEntity<CommonSuccessDto<PaymentCreateResDto>> create(
            @Valid @RequestBody PaymentCreateReqDto reqDto
            ) {

        Payment payment = paymentService.create(reqDto.getOrderId(), reqDto.getPaymentMethod());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                new PaymentCreateResDto(payment.getId()),
                                HttpStatus.CREATED,
                                "결제 생성 성공"
                        )
                );
    }

    /**
     * PG 연동을 위한 임시 API
     */
    @PostMapping("/confirm")
    @Operation(summary = "PG 결제 승인", description = "PG로 생성한 결제를 승인한다.")
    public ResponseEntity<CommonSuccessDto<CheckoutResDto>> confirm(
            @ModelAttribute PaymentConfirmReqDto reqDto
            ) {
        CheckoutResDto checkoutResDto = paymentService.confirm(reqDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                checkoutResDto,
                                HttpStatus.OK,
                                "PG 결제 승인 성공"
                        )
                );
    }

}
