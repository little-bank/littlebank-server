package com.littlebank.finance.global.toss;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.global.toss.config.TossProperties;
import com.littlebank.finance.global.toss.dto.PaymentReadyResponse;
import com.littlebank.finance.global.toss.dto.TossPaymentRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TossService {
    private final TossProperties tossProperties;
    private RestClient restClient;

    @PostConstruct
    private void initRestClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Basic " + Base64.getEncoder().encodeToString((tossProperties.getSecretKey() + ":").getBytes())
                )
                .build();
    }

    public PaymentReadyResponse ready(User user, int amount) {

        Map<String, Object> body = Map.of(
                "amount", amount,
                "orderId", "order-" + user.getId() + "-" + System.currentTimeMillis(),
                "orderName", "포인트 충전",
                "customerName", user.getName(),
                "customerKey", "user-" + user.getId(),
                "successUrl", tossProperties.getSuccessUrl(),
                "failUrl", tossProperties.getFailUrl()
        );
        TossPaymentRequest request = new TossPaymentRequest(
                "order-" + UUID.randomUUID(),
                amount,
                "포인트 충전",
                user.getName(),
                "user-" + user.getId(),
                tossProperties.getSuccessUrl(),
                tossProperties.getFailUrl()
        );

        PaymentReadyResponse response = restClient.post()
                .uri("/v1/payments")
                .body(request)
                .retrieve()
                .body(PaymentReadyResponse.class);

        return response;
    }
}
