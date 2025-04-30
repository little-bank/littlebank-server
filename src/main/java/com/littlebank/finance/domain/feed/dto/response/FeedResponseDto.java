package com.littlebank.finance.domain.feed.dto.response;

import com.littlebank.finance.domain.feed.domain.Feed;
import com.littlebank.finance.domain.feed.domain.FeedImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FeedResponseDto {
    private Long feedId;
    private String title;
    private List<String> imageUrls;
    private String content;
    private String writerName;
    private String writerProfileImageUrl;
    private int viewCount;
    private int likeCount;
    private int commentCount;

    public static FeedResponseDto of (Feed feed, List<FeedImage> images) {
        List<String> imageUrls = images.stream()
                .map(FeedImage::getUrl)
                .toList();
        return FeedResponseDto.builder()
                .feedId(feed.getId())
                .title(feed.getTitle())
                .imageUrls(imageUrls)
                .content(feed.getContent())
                .writerName(feed.getUser().getName())
                .writerProfileImageUrl(feed.getUser().getProfileImagePath())
                .viewCount(feed.getViewCount())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .build();
    }
}
