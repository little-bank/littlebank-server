package com.littlebank.finance.domain.point.dto.response;

import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.global.toss.config.TossProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AmountTempSaveResponse {
    private String orderId;
    private String orderName;
    private String successUrl;
    private String failUrl;
    private String customerEmail;
    private String customerName;
    private String customerMobilePhone;

    public static AmountTempSaveResponse of(String orderId, String orderName, TossProperties properties, User user) {
        return AmountTempSaveResponse.builder()
                .orderId(orderId)
                .orderName(orderName)
                .successUrl(properties.getSuccessUrl())
                .failUrl(properties.getFailUrl())
                .customerEmail(user.getEmail())
                .customerName(user.getName())
                .customerMobilePhone(user.getPhone())
                .build();
    }

}
