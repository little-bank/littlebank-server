package com.littlebank.finance.domain.feed.controller;

import com.littlebank.finance.domain.feed.domain.GradeCategory;
import com.littlebank.finance.domain.feed.domain.SubjectCategory;
import com.littlebank.finance.domain.feed.domain.TagCategory;
import com.littlebank.finance.domain.feed.dto.request.FeedCommentRequestDto;
import com.littlebank.finance.domain.feed.dto.request.FeedRequestDto;
import com.littlebank.finance.domain.feed.dto.response.*;
import com.littlebank.finance.domain.feed.service.FeedService;
import com.littlebank.finance.global.common.CustomPageResponse;
import com.littlebank.finance.global.common.PaginationPolicy;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api-user/feed")
@RequiredArgsConstructor
@Tag(name = "Feed")
public class FeedController {
    private final FeedService feedService;

    @Operation(summary = "피드 생성")
    @PostMapping("/create")
    public ResponseEntity<FeedResponseDto> createFeed(@RequestBody FeedRequestDto request,
                                           @AuthenticationPrincipal CustomUserDetails currentUser){
        FeedResponseDto response = feedService.createFeed(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피드 수정")
    @PutMapping("/update/{feedId}")
    public ResponseEntity<FeedResponseDto> updateFeed(@PathVariable Long feedId,
                                                      @RequestBody FeedRequestDto request,
                                                      @AuthenticationPrincipal CustomUserDetails currentUser){
        FeedResponseDto response = feedService.updateFeed(currentUser.getId(), feedId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피드 삭제")
    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long feedId,
                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        feedService.deleteFeed(currentUser.getId(), feedId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "피드 전체 목록 조회")
    @GetMapping
    public ResponseEntity<CustomPageResponse<FeedResponseDto>> getFeeds (
            @RequestParam(required = false) GradeCategory gradeCategory,
            @RequestParam(required = false) SubjectCategory subjectCategory,
            @RequestParam(required = false) TagCategory tagCategory,
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            ) {
        Pageable pageable = PageRequest.of(pageNumber, PaginationPolicy.GENERAL_PAGE_SIZE);
        CustomPageResponse<FeedResponseDto> response = feedService.getFeedsOrderByTime(customUserDetails.getId() ,gradeCategory, subjectCategory, tagCategory, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피드 좋아요 순 전체 목록 조회")
    @GetMapping("/likes")
    public ResponseEntity<Page<FeedResponseDto>> getFeedsOrderByLikes (
            @RequestParam(required = false) GradeCategory gradeCategory,
            @RequestParam(required = false) SubjectCategory subjectCategory,
            @RequestParam(required = false) TagCategory tagCategory,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Page<FeedResponseDto> feeds = feedService.getFeedsOrderByLikes(customUserDetails.getId(), gradeCategory, subjectCategory, tagCategory, pageable);
        return ResponseEntity.ok(feeds);
    }


    @Operation(summary = "내가 쓴 피드 조회")
    @GetMapping("/my")
    public ResponseEntity<Page<FeedResponseDto>> getMyFeeds (
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<FeedResponseDto> response = feedService.getFeedsByUser(customUserDetails.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피드 상세 조회")
    @GetMapping("/{feedId}")
    public ResponseEntity<FeedResponseDto> getFeedDetail(@PathVariable Long feedId,
                                                         @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        FeedResponseDto response = feedService.getFeedDetail(customUserDetails.getId(), feedId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "좋아요 증가")
    @PostMapping("/{feedId}/like")
    public ResponseEntity<FeedLikeResponseDto> likeFeed(@PathVariable("feedId") Long feedId,
                                                        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        FeedLikeResponseDto response = feedService.likeFeed(customUserDetails.getId(), feedId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "좋아요 취소")
    @DeleteMapping("/{feedId}/like")
    public ResponseEntity<Void> unlikeFeed(@PathVariable("feedId") Long feedId,
                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        feedService.unlikeFeed(customUserDetails.getId(), feedId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 등록")
    @PostMapping("/{feedId}/comment")
    public ResponseEntity<FeedCommentResponseDto> createComment (
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long feedId,
            @RequestBody FeedCommentRequestDto request) {
        return ResponseEntity.ok(feedService.createComment(customUserDetails.getId(), feedId, request));
    }

    @Operation(summary = "대댓글 등록")
    @PostMapping("/{feedId}/comment/reply")
    public ResponseEntity<FeedCommentResponseDto> createReply (
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long feedId,
            @RequestBody FeedCommentRequestDto request
    ) {
        FeedCommentResponseDto response = feedService.createReply(customUserDetails.getId(), feedId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 좋아요 증가")
    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<FeedCommentLikeResponseDto> likeComment (@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                   @PathVariable Long commentId) {
        FeedCommentLikeResponseDto response = feedService.likeComment(customUserDetails.getId(), commentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 좋아요 취소")
    @DeleteMapping("/comment/{commentId}/like")
    public ResponseEntity<FeedCommentLikeResponseDto> unlikeComment (@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                     @PathVariable Long commentId) {
        FeedCommentLikeResponseDto response = feedService.unlikeComment(customUserDetails.getId(), commentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/comment/{commentId}")
    public ResponseEntity<FeedCommentResponseDto> updateComment (
            @PathVariable Long commentId,
            @RequestBody FeedCommentRequestDto request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(feedService.updateComment(customUserDetails.getId(), commentId, request));
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Void> deleteComment (
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        feedService.deleteComment(customUserDetails.getId(), commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "피드의 최상위 댓글 목록 조회")
    @GetMapping("/{feedId}/comments")
    public ResponseEntity<Page<FeedCommentResponseDto>> getTopLevelComments(
            @PathVariable Long feedId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        return ResponseEntity.ok(feedService.getTopLevelComments(feedId, page, size, customUserDetails.getId()));
    }

    @Operation(summary = "특정 댓글의 대댓글 목록 조회")
    @GetMapping("/comment/{parentId}/replies")
    public ResponseEntity<Page<FeedCommentResponseDto>> getRepliesByParent(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        return ResponseEntity.ok(feedService.getReplies(parentId, page, size, customUserDetails.getId()));
    }
}
