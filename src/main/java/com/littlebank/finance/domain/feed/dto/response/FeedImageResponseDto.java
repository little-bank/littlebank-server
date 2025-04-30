package com.littlebank.finance.domain.feed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FeedImageResponseDto {
    private String url;
    public static FeedImageResponseDto of(String url) {
        return FeedImageResponseDto.builder()
                .url(url)
                .build();
    }
}
