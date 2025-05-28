package com.littlebank.finance.domain.sharing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class KakaoProfileResponseDto {
    private String nickname;
    private String profileImageUrl;
    private String thumbnailImageUrl;
    private String countryISO;
}
