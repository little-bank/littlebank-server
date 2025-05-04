package com.littlebank.finance.domain.feed.dto.response;

import com.littlebank.finance.domain.feed.domain.*;
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
    private TagCategory tagCategory;
    private SubjectCategory subjectCategory;
    private GradeCategory gradeCategory;
    private String writerName;
    private String writerProfileImageUrl;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private boolean liked;

    public static FeedResponseDto of (Feed feed, List<FeedImage> images, boolean liked) {
        List<String> imageUrls = images.stream()
                .map(FeedImage::getUrl)
                .toList();
        return FeedResponseDto.builder()
                .feedId(feed.getId())
                .title(feed.getTitle())
                .imageUrls(imageUrls)
                .content(feed.getContent())
                .tagCategory(feed.getTagCategory())
                .subjectCategory(feed.getSubjectCategory())
                .gradeCategory(feed.getGradeCategory())
                .writerName(feed.getUser().getName())
                .writerProfileImageUrl(feed.getUser().getProfileImagePath())
                .viewCount(feed.getViewCount())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .liked(liked)
                .build();
    }
}
