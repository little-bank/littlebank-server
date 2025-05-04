package com.littlebank.finance.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class FeedCommentRequestDto {
    @NotBlank
    @Size(max = 500)
    private String content;
}
