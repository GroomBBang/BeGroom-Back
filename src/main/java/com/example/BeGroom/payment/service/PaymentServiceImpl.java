package com.example.BeGroom.payment.service;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentFailReason;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.dto.PaymentConfirmReqDto;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.product.exception.InsufficientStockException;
import com.example.BeGroom.usecase.checkout.dto.CheckoutResDto;
import com.example.BeGroom.usecase.checkout.exception.CheckoutInsufficientBalanceException;
import com.example.BeGroom.usecase.checkout.exception.CheckoutInsufficientStockException;
import com.example.BeGroom.wallet.exception.InsufficientBalanceException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TossPaymentClient tossPaymentClient;

    @Override
    @Transactional
    public Payment create(Long orderId, PaymentMethod paymentMethod) {
        // 주문 조회
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("없는 주문입니다."));
        // 주문 검증 및 상태 변경
        order.markPaymentPending();
        // 결제 생성
        Payment payment = Payment.create(order, order.getTotalAmount(), paymentMethod, PaymentStatus.READY);
        // 결제 저장
        paymentRepository.save(payment);

        return payment;
    }

    @Transactional
    @Override
    public void approve(Long paymentId) {
        // 결제 조회
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("없는 결제입니다."));
        // 결제 검증 및 상태 변경 (PROCESSING -> APPROVED)
        payment.approve();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long paymentId, PaymentFailReason reason) {
        // 결제 조회
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("없는 결제입니다."));
        // 실패 처리
        payment.fail(reason);
    }

    @Override
    @Transactional
    public CheckoutResDto confirm(PaymentConfirmReqDto reqDto) {
        // 결제 조회
        Payment payment = paymentRepository.findById(reqDto.getPaymentId()).orElseThrow(() -> new EntityNotFoundException("없는 결제입니다."));
        // 결제 상태 변경 (PROCESSING)
        payment.markProcessing();
        // 주문 조회
        Order order = payment.getOrder();
        try {
            // 토스에게 승인 요청
            tossPaymentClient.confirm(
                    reqDto.getPaymentKey(),
                    reqDto.getOrderId(),   // orderId (문자열)
                    reqDto.getAmount());

            // 결제 승인
            approve(payment.getId());
            // 상품 재고 감소
            for(OrderProduct orderProduct : order.getOrderProductList()) {
                orderProduct.getProductDetail()
                        .decreaseStock(orderProduct.getQuantity());
            }
            // 주문 완료 처리
            order.complete();
            // 성공 반환
            return CheckoutResDto.completed(order.getId(), payment.getId());
        } catch (InsufficientBalanceException e) {
            // 실패 처리 (잔액 부족)
            fail(reqDto.getPaymentId(), PaymentFailReason.INSUFFICIENT_BALANCE);
            // 예외 반환
            throw new CheckoutInsufficientBalanceException(order.getId(), payment.getId());
        } catch (InsufficientStockException e) {
            // 실패 처리 (재고 부족)
            fail(payment.getId(), PaymentFailReason.INSUFFICIENT_STOCK);
            // 예외 반환
            throw new CheckoutInsufficientStockException(order.getId(), payment.getId());
        }
    }
}
