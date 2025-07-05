package com.littlebank.finance.global.toss.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "tosspayments")
public class TossProperties {
    private String secretKey;
    private String successUrl;
    private String failUrl;
}
