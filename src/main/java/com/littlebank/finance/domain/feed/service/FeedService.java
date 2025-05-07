package com.littlebank.finance.domain.feed.service;

import com.littlebank.finance.domain.feed.domain.*;
import com.littlebank.finance.domain.feed.domain.repository.*;
import com.littlebank.finance.domain.feed.dto.request.FeedCommentRequestDto;
import com.littlebank.finance.domain.feed.dto.request.FeedRequestDto;
import com.littlebank.finance.domain.feed.dto.request.FeedImageRequestDto;
import com.littlebank.finance.domain.feed.dto.response.FeedCommentLikeResponseDto;
import com.littlebank.finance.domain.feed.dto.response.FeedCommentResponseDto;
import com.littlebank.finance.domain.feed.dto.response.FeedResponseDto;
import com.littlebank.finance.domain.feed.dto.response.FeedLikeResponseDto;
import com.littlebank.finance.domain.feed.exception.FeedException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final FeedLikeRepository feedLikeRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final FeedCommentLikeRepository feedCommentLikeRepository;
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
        return FeedResponseDto.of(savedFeed, imageEntities, false);
    }

    public FeedResponseDto updateFeed(Long userId, Long feedId, FeedRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        boolean liked = feedLikeRepository.findByFeedAndUser(feed, user).isPresent();
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

        return FeedResponseDto.of(feed, imageEntities, liked);

    }

    public void deleteFeed(Long userId, Long feedId) {
        Feed feed = feedRepository.findWithCommentsById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        if (!feed.getUser().getId().equals(userId)) {
            throw new FeedException(ErrorCode.USER_NOT_EQUAL);
        }
        for (FeedComment comment : feed.getComments()) {
            comment.setFeed(null);
            comment.setIsDeleted(true); // 소프트 삭제
        }

        feed.getComments().clear();
        //feedCommentRepository.deleteAllByFeedId(feedId);
        feedRepository.delete(feed);
    }

    public Page<FeedResponseDto> getFeedsOrderByTime(Long userId, GradeCategory gradeCategory, SubjectCategory subjectCategory, TagCategory tagCategory, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        return feedRepositoryCustom.findAllByFiltersOrderByTime(gradeCategory, subjectCategory, tagCategory, pageable)
                .map(feed -> {
                    List<FeedImage> images = feedImageRepository.findByFeed(feed);
                    boolean liked = feedLikeRepository.findByFeedAndUser(feed, user).isPresent();
                    return FeedResponseDto.of(feed, images, liked);
                });
    }

    public Page<FeedResponseDto> getFeedsOrderByLikes(Long userId, GradeCategory gradeCategory, SubjectCategory subjectCategory, TagCategory tagCategory, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        return feedRepositoryCustom.findAllByFiltersOrderByLikes(gradeCategory, subjectCategory, tagCategory, pageable)
                .map(feed -> {
                    List<FeedImage> images = feedImageRepository.findByFeed(feed);
                    boolean liked = feedLikeRepository.findByFeedAndUser(feed, user).isPresent();
                    return FeedResponseDto.of(feed, images, liked);
                });
    }

    public Page<FeedResponseDto> getFeedsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Page<Feed> feeds = feedRepository.findByUserId(userId, pageable);
        return feeds.map(feed -> {
            List<FeedImage> images = feedImageRepository.findByFeed(feed);
            boolean liked = feedLikeRepository.findByFeedAndUser(feed, user).isPresent();
            return FeedResponseDto.of(feed, images, liked);
        });
    }

    public FeedResponseDto getFeedDetail(Long userId, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        feed.increaseViewCount();

        List<FeedImage> images = feedImageRepository.findByFeed(feed);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        boolean liked = feedLikeRepository.findByFeedAndUser(feed, user).isPresent();

        return FeedResponseDto.of(feed, images, liked);
    }

    public FeedLikeResponseDto likeFeed(Long userId, Long feedId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        boolean alreadyLiked = feedLikeRepository.findByFeedAndUser(feed, user).isPresent();
        if (alreadyLiked) {
            throw new FeedException(ErrorCode.ALREADY_LIKED);
        }

        FeedLike feedLike = FeedLike.builder()
                .feed(feed)
                .user(user)
                .build();

        feedLikeRepository.save(feedLike);
        feed.increaseLikeCount();
        return FeedLikeResponseDto.of(feedId, feed.getLikeCount(), true);
    }

    public FeedLikeResponseDto unlikeFeed(Long userId, Long feedId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        FeedLike feedLike = feedLikeRepository.findByFeedAndUser(feed, user)
                .orElseThrow(() -> new FeedException(ErrorCode.LIKE_NOT_FOUND));

        feedLikeRepository.delete(feedLike);
        feed.decreaseLikeCount();
        return FeedLikeResponseDto.of(feedId, feed.getLikeCount(), false);
    }

    public FeedCommentResponseDto createComment(Long userId, Long feedId, FeedCommentRequestDto request) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        FeedComment parent = null;
        if (request.getParentId() != null) {
            parent = feedCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));
        }
        FeedComment comment = FeedComment.builder()
                .feed(feed)
                .user(user)
                .content(request.getContent())
                .parent(parent)
                .build();

        feedCommentRepository.save(comment);
        feed.increaseCommentCount();

        int likeCount = 0;
        boolean isLiked = false;

        return FeedCommentResponseDto.of(
                comment.getId(),
                feed.getId(),
                parent != null? parent.getId() : null,
                user.getName(),
                user.getProfileImagePath(),
                comment.getContent(),
                likeCount,
                isLiked
        );
    }

    public FeedCommentResponseDto updateComment(Long userId, Long commentId, FeedCommentRequestDto request) {
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUser().getId().equals(userId)) {
            throw new FeedException(ErrorCode.USER_NOT_EQUAL);
        }
        comment.update(request.getContent());

        int likeCount = comment.getLikeCount();
        boolean isLiked = comment.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(userId));

        return FeedCommentResponseDto.of(
                comment.getId(),
                comment.getFeed().getId(),
                comment.getParent()!=null? comment.getParent().getId() : null,
                comment.getUser().getName(),
                comment.getUser().getProfileImagePath(),
                comment.getContent(),
                likeCount,
                isLiked
        );
    }

    public Page<FeedCommentResponseDto> getComments(Long feedId, int page, int size, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdDate"));
        Page<FeedComment> commentPage = feedCommentRepository.findByFeedAndIsDeletedFalse(feed, pageable);

        return commentPage.map(comment -> {
            int likeCount = comment.getLikes().size();
            boolean isLiked = comment.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(userId));

            return FeedCommentResponseDto.of(
                    comment.getId(),
                    feed.getId(),
                    comment.getParent() != null ? comment.getParent().getId() : null,
                    comment.getUser().getName(),
                    comment.getUser().getProfileImagePath(),
                    comment.getContent(),
                    likeCount,
                    isLiked
            );
        });
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new FeedException(ErrorCode.USER_NOT_EQUAL);
        }
        comment.getFeed().decreaseCommentCount();
        comment.setIsDeleted(true);
    }

    public FeedCommentLikeResponseDto likeComment(Long userId, Long commentId) {
        FeedComment feedComment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        if (feedCommentLikeRepository.findByFeedCommentAndUser(feedComment, user).isPresent()) {
            throw new FeedException(ErrorCode.ALREADY_LIKED);
        }

        FeedCommentLike like = FeedCommentLike.builder()
                .feedComment(feedComment)
                .user(user)
                .build();
        feedCommentLikeRepository.save(like);

        int likeCount = feedCommentLikeRepository.countByFeedComment(feedComment);
        return FeedCommentLikeResponseDto.of(commentId, likeCount, true);
    }

    public FeedCommentLikeResponseDto unlikeComment(Long userId, Long commentId) {
        FeedComment feedComment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        FeedCommentLike like = feedCommentLikeRepository.findByFeedCommentAndUser(feedComment, user)
                .orElseThrow(() -> new FeedException(ErrorCode.LIKE_NOT_FOUND));

        feedCommentLikeRepository.delete(like);
        int likeCount = feedCommentLikeRepository.countByFeedComment(feedComment);
        return FeedCommentLikeResponseDto.of(commentId, likeCount, false);
    }
}
