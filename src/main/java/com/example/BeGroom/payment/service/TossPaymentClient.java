package com.example.BeGroom.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TossPaymentClient {

    private static final String CONFIRM_URL =
            "https://api.tosspayments.com/v1/payments/confirm";

    @Value("${toss.secret-key}")
    private String secretKey;

    public void confirm(String paymentKey, String orderId, Long amount) {
        try {
            String auth = Base64.getEncoder()
                    .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

            URL url = new URL(CONFIRM_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Basic " + auth);
            conn.setRequestProperty("Content-Type", "application/json");

            String body = """
                {
                  "paymentKey": "%s",
                  "orderId": "%s",
                  "amount": %d
                }
                """.formatted(paymentKey, orderId, amount);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();

            if (code != 200) {
                throw new RuntimeException("토스 결제 승인 실패. code=" + code);
            }

        } catch (Exception e) {
            throw new RuntimeException("토스 결제 승인 중 오류", e);
        }
    }
}

