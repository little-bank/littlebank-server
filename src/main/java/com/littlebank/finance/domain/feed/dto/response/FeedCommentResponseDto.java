package com.littlebank.finance.domain.feed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FeedCommentResponseDto {
    private Long commentId;
    private Long feedId;
    private String writerName;
    private String writerProfileUrl;
    private String content;
    public static FeedCommentResponseDto of(Long commentId, Long feedId, String writerName, String writerProfileUrl, String content) {
        return FeedCommentResponseDto.builder()
                .commentId(commentId)
                .feedId(feedId)
                .writerName(writerName)
                .writerProfileUrl(writerProfileUrl)
                .content(content)
                .build();
    }
}
