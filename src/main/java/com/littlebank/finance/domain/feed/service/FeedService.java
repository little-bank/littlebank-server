package com.littlebank.finance.domain.feed.service;

import com.littlebank.finance.domain.feed.domain.*;
import com.littlebank.finance.domain.feed.domain.repository.*;
import com.littlebank.finance.domain.feed.dto.request.FeedCommentRequestDto;
import com.littlebank.finance.domain.feed.dto.request.FeedRequestDto;
import com.littlebank.finance.domain.feed.dto.request.FeedImageRequestDto;
import com.littlebank.finance.domain.feed.dto.response.*;
import com.littlebank.finance.domain.feed.exception.FeedException;
import com.littlebank.finance.domain.notification.domain.Notification;
import com.littlebank.finance.domain.notification.domain.NotificationType;
import com.littlebank.finance.domain.notification.domain.repository.NotificationRepository;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import com.littlebank.finance.global.firebase.FirebaseService;
import com.littlebank.finance.global.redis.RedisDao;
import com.littlebank.finance.global.redis.RedisPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
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
    private final NotificationRepository notificationRepository;
    private final FirebaseService firebaseService;
    private final RedissonClient redissonClient;
    private final RedisDao redisDao;
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
        return FeedResponseDto.of(savedFeed, imageEntities, 0,false);
    }

    public FeedResponseDto updateFeed(Long userId, Long feedId, FeedRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
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

        String likeSetKey = RedisPolicy.FEED_LIKE_SET_KEY_PREFIX + feed.getId();
        boolean liked = redisDao.isMemberOfSet(likeSetKey, userId.toString());
        int likeCount = redisDao.getSetSize(likeSetKey);

        return FeedResponseDto.of(feed, imageEntities, likeCount, liked);

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

        feedRepository.delete(feed);
    }

    @Transactional(readOnly = true)
    public Page<FeedResponseDto> getFeedsOrderByTime(Long userId, GradeCategory gradeCategory, SubjectCategory subjectCategory, TagCategory tagCategory, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        return feedRepositoryCustom.findAllByFiltersOrderByTime(gradeCategory, subjectCategory, tagCategory, pageable)
                .map(feed -> {
                    List<FeedImage> images = feedImageRepository.findByFeed(feed);

                    String likeSetKey = RedisPolicy.FEED_LIKE_SET_KEY_PREFIX + feed.getId();
                    boolean liked = redisDao.isMemberOfSet(likeSetKey, userId.toString());
                    int likeCount = redisDao.getSetSize(likeSetKey);
                    return FeedResponseDto.of(feed, images, likeCount, liked);
                });
    }

    @Transactional(readOnly = true)
    public Page<FeedResponseDto> getFeedsOrderByLikes(Long userId, GradeCategory gradeCategory, SubjectCategory subjectCategory, TagCategory tagCategory, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        return feedRepositoryCustom.findAllByFiltersOrderByLikes(gradeCategory, subjectCategory, tagCategory, pageable)
                .map(feed -> {
                    List<FeedImage> images = feedImageRepository.findByFeed(feed);

                    String likeSetKey = RedisPolicy.FEED_LIKE_SET_KEY_PREFIX + feed.getId();
                    boolean liked = redisDao.isMemberOfSet(likeSetKey, userId.toString());
                    int likeCount = redisDao.getSetSize(likeSetKey);
                    return FeedResponseDto.of(feed, images, likeCount, liked);
                });
    }

    @Transactional(readOnly = true)
    public Page<FeedResponseDto> getFeedsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Page<Feed> feeds = feedRepository.findByUserId(userId, pageable);
        return feeds.map(feed -> {
            List<FeedImage> images = feedImageRepository.findByFeed(feed);

            String likeSetKey = RedisPolicy.FEED_LIKE_SET_KEY_PREFIX + feed.getId();
            boolean liked = redisDao.isMemberOfSet(likeSetKey, userId.toString());
            int likeCount = redisDao.getSetSize(likeSetKey);
            return FeedResponseDto.of(feed, images, likeCount, liked);
        });
    }

    public FeedResponseDto getFeedDetail(Long userId, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        feed.increaseViewCount();

        List<FeedImage> images = feedImageRepository.findByFeed(feed);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        String likeSetKey = RedisPolicy.FEED_LIKE_SET_KEY_PREFIX + feed.getId();
        boolean liked = redisDao.isMemberOfSet(likeSetKey, userId.toString());
        int likeCount = redisDao.getSetSize(likeSetKey);
        return FeedResponseDto.of(feed, images, likeCount, liked);
    }

    public FeedLikeResponseDto likeFeed(Long userId, Long feedId) {
        String lockKey = RedisPolicy.FEED_LIKE_LOCK_KEY_PREFIX + feedId;
        RLock lock = redissonClient.getLock(lockKey);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        String likeSetKey = RedisPolicy.FEED_LIKE_SET_KEY_PREFIX + feedId;
        boolean isLiked = false;
        int updatedLikeCount = 0;

        try {
            if (lock.tryLock(2,5, TimeUnit.SECONDS)) {
                if (redisDao.isMemberOfSet(likeSetKey, userId.toString())) {
                    throw new FeedException(ErrorCode.ALREADY_LIKED);
                }
                redisDao.addToSetWithTTL(likeSetKey, userId.toString(), Duration.ofDays(7));
                feedLikeRepository.save(FeedLike.builder()
                                .feed(feed)
                                .user(user)
                                .build());
                updatedLikeCount = redisDao.getSetSize(likeSetKey);
                redisDao.addToSet("changedFeedIds", feedId.toString());
                isLiked = true;

            } else {
                throw new FeedException(ErrorCode.FAIL_TO_GET_LOCK);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FeedException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        if (isLiked && !user.getId().equals(feed.getUser().getId()) && updatedLikeCount <= 30){
            try {
                // 알림 생성
                Notification notification = notificationRepository.save(Notification.builder()
                        .receiver(feed.getUser())
                        .message(user.getName() + "님이 회원님의 피드를 좋아합니다.")
                        .type(NotificationType.FEED_LIKE)
                        .targetId(feed.getId())
                        .isRead(false)
                        .build());

                firebaseService.sendNotification(notification);
            } catch (DataIntegrityViolationException e) {
                log.warn("이미 동일한 알림이 존재합니다.");
            }
        }
        return FeedLikeResponseDto.of(feedId, updatedLikeCount, true);
    }

    public FeedLikeResponseDto unlikeFeed(Long userId, Long feedId) {
        String lockKey = RedisPolicy.FEED_LIKE_LOCK_KEY_PREFIX + feedId;
        RLock lock = redissonClient.getLock(lockKey);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        String likeSetKey = RedisPolicy.FEED_LIKE_SET_KEY_PREFIX + feedId;
        boolean unliked = false;
        int updatedLikeCount = 0;

        try {
            if (lock.tryLock(2, 5, TimeUnit.SECONDS)) {
                if (!redisDao.isMemberOfSet(likeSetKey, userId.toString())) {
                    throw new FeedException(ErrorCode.LIKE_NOT_FOUND);
                }
                redisDao.removeFromSet(likeSetKey, userId.toString());

                FeedLike feedLike = feedLikeRepository.findByFeedAndUser(feed, user)
                        .orElseThrow(() -> new FeedException(ErrorCode.LIKE_NOT_FOUND));
                feedLikeRepository.delete(feedLike);
                updatedLikeCount = redisDao.getSetSize(likeSetKey);
                redisDao.addToSet("changedFeedIds", feedId.toString());
                unliked = true;
            } else {
                throw new FeedException(ErrorCode.FAIL_TO_GET_LOCK);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FeedException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return FeedLikeResponseDto.of(feedId, updatedLikeCount, false);
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
        FeedComment feedComment = FeedComment.builder()
                .feed(feed)
                .user(user)
                .content(request.getContent())
                .parent(parent)
                .build();

        feedCommentRepository.save(feedComment);
        feed.increaseCommentCount();

        int likeCount = 0;
        boolean isLiked = false;

        // 알림
        if (!user.getId().equals(feed.getUser().getId())) {
            try {
                Notification notification = Notification.builder()
                        .receiver(feed.getUser())
                        .message(user.getName() + "님이 회원님의 피드에 댓글을 달았습니다.")
                        .type(NotificationType.FEED_COMMENT)
                        .targetId(feed.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
                firebaseService.sendNotification(notification);
            } catch (DataIntegrityViolationException e) {
                log.warn("이미 동일한 알림이 존재합니다.");
            }
        }

        return FeedCommentResponseDto.of(
                feedComment,
                feed.getId(),
                parent != null? parent.getId() : null,
                feedComment.getUser().getId(),
                user.getName(),
                user.getProfileImagePath(),
                feedComment.getContent(),
                likeCount,
                isLiked,
                0
        );
    }

    public FeedCommentResponseDto createReply(Long userId, Long feedId, FeedCommentRequestDto request) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        if (request.getParentId() == null) {
            throw new FeedException(ErrorCode.COMMENT_NOT_FOUND);
        }

        FeedComment parent = feedCommentRepository.findById(request.getParentId())
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));

        if (!parent.getFeed().getId().equals(feedId)) {
            throw new FeedException(ErrorCode.INVALID_PARENT_COMMENT);
        }

        FeedComment reply = FeedComment.builder()
                .feed(feed)
                .user(user)
                .content(request.getContent())
                .parent(parent)
                .build();

        feedCommentRepository.save(reply);
        feed.increaseCommentCount();

        //알림
        if (!user.getId().equals(parent.getUser().getId())) {
            try {
                Notification notification = Notification.builder()
                        .receiver(parent.getUser())
                        .message(user.getName() + "님이 회원님의 댓글에 답글을 남겼습니다.")
                        .type(NotificationType.COMMENT_REPLY)
                        .targetId(parent.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
                firebaseService.sendNotification(notification);
            } catch (DataIntegrityViolationException e) {
                log.warn("이미 동일한 알림이 존재합니다.");
            }
        }

        return FeedCommentResponseDto.of(
                reply,
                feedId,
                parent.getId(),
                parent.getUser().getId(),
                user.getName(),
                user.getProfileImagePath(),
                reply.getContent(),
                0,
                false,
                0
        );
    }

    public FeedCommentResponseDto updateComment(Long userId, Long commentId, FeedCommentRequestDto request) {
        FeedComment feedComment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));
        if (!feedComment.getUser().getId().equals(userId)) {
            throw new FeedException(ErrorCode.USER_NOT_EQUAL);
        }
        feedComment.update(request.getContent());
        feedCommentRepository.flush();
        int likeCount = feedComment.getLikeCount();
        boolean isLiked = feedComment.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(userId));
        int replyCount = (int) feedComment.getReplies().stream()
                .filter(reply -> !reply.getIsDeleted())
                .count();

        return FeedCommentResponseDto.of(
                feedComment,
                feedComment.getFeed().getId(),
                feedComment.getParent()!=null? feedComment.getParent().getId() : null,
                feedComment.getUser().getId(),
                feedComment.getUser().getName(),
                feedComment.getUser().getProfileImagePath(),
                feedComment.getContent(),
                likeCount,
                isLiked,
                replyCount
        );
    }

    public void deleteComment(Long userId, Long commentId) {
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new FeedException(ErrorCode.USER_NOT_EQUAL);
        }
        comment.getFeed().decreaseCommentCount();
        comment.setIsDeleted(true);
        if (comment.getReplies() != null) {
            for (FeedComment reply : comment.getReplies()) {
                reply.setIsDeleted(true);
            }
        }
    }

    public FeedCommentLikeResponseDto likeComment(Long userId, Long commentId) {
        String lockKey = RedisPolicy.FEED_COMMENT_LIKE_LOCK_KEY_PREFIX + commentId;
        RLock lock = redissonClient.getLock(lockKey);

        FeedComment feedComment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        String likeSetKey = RedisPolicy.FEED_COMMENT_LIKE_SET_KEY_PREFIX + commentId;
        boolean isLiked = false;
        int updatedLikeCount = 0;
        try {
            if (lock.tryLock(2, 5, TimeUnit.SECONDS)) {
                if (redisDao.isMemberOfSet(likeSetKey, userId.toString())) {
                    throw new FeedException(ErrorCode.ALREADY_LIKED);
                }
                redisDao.addToSetWithTTL(likeSetKey, userId.toString(), Duration.ofDays(7));

                FeedCommentLike like = FeedCommentLike.builder()
                        .feedComment(feedComment)
                        .user(user)
                        .build();
                feedCommentLikeRepository.save(like);

                updatedLikeCount = redisDao.getSetSize(likeSetKey);
                isLiked = true;
                redisDao.addToSet("changedCommentIds", commentId.toString());
            } else {
                throw new FeedException(ErrorCode.FAIL_TO_GET_LOCK);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FeedException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        // 알림
        if (isLiked && !user.getId().equals(feedComment.getUser().getId()) && updatedLikeCount <= 15) {
            try {
                Notification notification = Notification.builder()
                        .receiver(feedComment.getUser())
                        .message(user.getName() + "님이 회원님의 댓글을 좋아합니다.")
                        .type(NotificationType.COMMENT_LIKE)
                        .targetId(feedComment.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
                firebaseService.sendNotification(notification);
            } catch (DataIntegrityViolationException e) {
                log.warn("이미 동일한 알림이 존재합니다.");
            }
        }

        return FeedCommentLikeResponseDto.of(commentId, updatedLikeCount, true);
    }

    public FeedCommentLikeResponseDto unlikeComment(Long userId, Long commentId) {
        String lockKey = RedisPolicy.FEED_COMMENT_LIKE_LOCK_KEY_PREFIX + commentId;
        RLock lock = redissonClient.getLock(lockKey);

        FeedComment feedComment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        String likeSetKey = RedisPolicy.FEED_COMMENT_LIKE_SET_KEY_PREFIX + commentId;
        int updatedLikeCount = 0;
        try {
            if (lock.tryLock(2, 5, TimeUnit.SECONDS)) {
                if (!redisDao.isMemberOfSet(likeSetKey, userId.toString())) {
                    throw new FeedException(ErrorCode.LIKE_NOT_FOUND);
                }
                redisDao.removeFromSet(likeSetKey, userId.toString());
                FeedCommentLike feedCommentLike = feedCommentLikeRepository.findByFeedCommentAndUser(feedComment, user)
                        .orElseThrow(() -> new FeedException(ErrorCode.LIKE_NOT_FOUND));
                feedCommentLikeRepository.delete(feedCommentLike);
                updatedLikeCount = redisDao.getSetSize(likeSetKey);
                redisDao.addToSet("changedCommentIds", commentId.toString());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FeedException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return FeedCommentLikeResponseDto.of(commentId, updatedLikeCount, false);
    }

    @Transactional(readOnly = true)
    public Page<FeedCommentResponseDto> getTopLevelComments(Long feedId, int page, int size, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdDate"));
        Page<FeedComment> commentPage = feedCommentRepository.findByFeedAndParentIsNullAndIsDeletedFalse(feed, pageable);

        return commentPage.map(comment -> {
            String likeSetKey = RedisPolicy.FEED_COMMENT_LIKE_SET_KEY_PREFIX + comment.getId();
            int likeCount = redisDao.getSetSize(likeSetKey);
            boolean isLiked = redisDao.isMemberOfSet(likeSetKey, userId.toString());
            int replyCount = (int) comment.getReplies().stream()
                    .filter(reply -> !reply.getIsDeleted())
                    .count();

            return FeedCommentResponseDto.of(
                    comment,
                    feed.getId(),
                    null,
                    comment.getUser().getId(),
                    comment.getUser().getName(),
                    comment.getUser().getProfileImagePath(),
                    comment.getContent(),
                    likeCount,
                    isLiked,
                    replyCount
            );
        });
    }

    @Transactional(readOnly = true)
    public Page<FeedCommentResponseDto> getReplies(Long parentId, int page, int size, Long userId) {
        FeedComment parent = feedCommentRepository.findById(parentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").ascending());
        Page<FeedComment> replies = feedCommentRepository.findByParentAndIsDeletedFalse(parent, pageable);

        return replies.map(reply -> {
            String likeSetKey = RedisPolicy.FEED_COMMENT_LIKE_SET_KEY_PREFIX + reply.getId();
            int likeCount = redisDao.getSetSize(likeSetKey);
            boolean isLiked = redisDao.isMemberOfSet(likeSetKey, userId.toString());

            return FeedCommentResponseDto.of(
                    reply,
                    reply.getFeed().getId(),
                    parentId,
                    parent.getUser().getId(),
                    reply.getUser().getName(),
                    reply.getUser().getProfileImagePath(),
                    reply.getContent(),
                    likeCount,
                    isLiked,
                    0
            );
        });
    }

}
