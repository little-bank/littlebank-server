package com.littlebank.finance.domain.feed.service;

import com.littlebank.finance.domain.feed.domain.*;
import com.littlebank.finance.domain.feed.domain.repository.FeedImageRepository;
import com.littlebank.finance.domain.feed.domain.repository.FeedRepository;
import com.littlebank.finance.domain.feed.domain.repository.FeedRepositoryCustom;
import com.littlebank.finance.domain.feed.dto.request.FeedRequestDto;
import com.littlebank.finance.domain.feed.dto.request.FeedImageRequestDto;
import com.littlebank.finance.domain.feed.dto.response.FeedResponseDto;
import com.littlebank.finance.domain.feed.exception.FeedException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedService {
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final FeedImageRepository feedImageRepository;
    private final FeedRepositoryCustom feedRepositoryCustom;
    public FeedResponseDto createFeed(Long userId, FeedRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Feed feed = Feed.builder()
                .user(user)
                .title(request.getTitle())
                .gradeCategory(request.getGradeCategory())
                .subjectCategory(request.getSubjectCategory())
                .tagCategory(request.getTagCategory())
                .content(request.getContent())
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .build();

        Feed savedFeed = feedRepository.save(feed);

        List<FeedImage> imageEntities = new ArrayList<>();
        if (request.getImages() != null) {
            for (FeedImageRequestDto imageDto : request.getImages()) {
                FeedImage feedImage = FeedImage.builder()
                        .feed(savedFeed)
                        .url(imageDto.getUrl())
                        .build();
                feedImageRepository.save(feedImage);
                imageEntities.add(feedImage);
            }
        }
        return FeedResponseDto.of(savedFeed, imageEntities);
    }

    public FeedResponseDto updateFeed(Long userId, Long feedId, FeedRequestDto request) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        if (!feed.getUser().getId().equals(userId)) {
            throw new FeedException(ErrorCode.USER_NOT_EQUAL);
        }

        // 본문 업데이트
        feed.update(request.getTitle(), request.getGradeCategory(), request.getSubjectCategory(), request.getTagCategory(), request.getContent());

        // db 저장 목록
        List<FeedImage> currentImages = feedImageRepository.findByFeed(feed);
        Set<String> existingImageUrls = currentImages.stream()
                .map(FeedImage::getUrl)
                .collect(Collectors.toSet());

        // 사용자 요청 목록
        Set<String> incomingUrls = request.getImages().stream()
                .map(FeedImageRequestDto::getUrl)
                .collect(Collectors.toSet());

        // 삭제 대상
        for (FeedImage image : currentImages) {
            if (!incomingUrls.contains(image.getUrl())) {
                feedImageRepository.delete(image);
            }
        }
        // 추가 대상
        for (String incomingUrl : incomingUrls) {
            if (!existingImageUrls.contains(incomingUrl)) {
                FeedImage newImage = FeedImage.builder()
                        .feed(feed)
                        .url(incomingUrl)
                        .build();
                feedImageRepository.save(newImage);
            }
        }

        // 최종 목록
        List<FeedImage> imageEntities = feedImageRepository.findByFeed(feed);

        return FeedResponseDto.of(feed, imageEntities);

    }

    public void deleteFeed(Long userId, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        if (!feed.getUser().getId().equals(userId)) {
            throw new FeedException(ErrorCode.USER_NOT_EQUAL);
        }
        feedRepository.delete(feed);
    }

    public Page<FeedResponseDto> getFeeds(GradeCategory gradeCategory, SubjectCategory subjectCategory, TagCategory tagCategory, Pageable pageable) {
        return feedRepositoryCustom.findAllByFilters(gradeCategory, subjectCategory, tagCategory, pageable)
                .map(feed -> {
                    List<FeedImage> images = feedImageRepository.findByFeed(feed);
                    return FeedResponseDto.of(feed, images);
                });
    }


    public Page<FeedResponseDto> getFeedsByUser(Long userId, Pageable pageable) {
        Page<Feed> feeds = feedRepository.findByUserId(userId, pageable);
        return feeds.map(feed -> {
            List<FeedImage> images = feedImageRepository.findByFeed(feed);
            return FeedResponseDto.of(feed, images);
        });
    }

    public FeedResponseDto getFeedDetail(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        feed.increaseViewCount();
        return FeedResponseDto.of(feed, feedImageRepository.findByFeed(feed));
    }

}
