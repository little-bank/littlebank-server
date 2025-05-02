package com.littlebank.finance.domain.feed.controller;

import com.littlebank.finance.domain.feed.domain.GradeCategory;
import com.littlebank.finance.domain.feed.domain.SubjectCategory;
import com.littlebank.finance.domain.feed.domain.TagCategory;
import com.littlebank.finance.domain.feed.dto.request.FeedRequestDto;
import com.littlebank.finance.domain.feed.dto.response.FeedResponseDto;
import com.littlebank.finance.domain.feed.service.FeedService;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<String> createFeed(@RequestBody FeedRequestDto request,
                                           @AuthenticationPrincipal CustomUserDetails currentUser){
        feedService.createFeed(currentUser.getId(), request);
        return ResponseEntity.ok("업로드 완료");
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
    public ResponseEntity<Page<FeedResponseDto>> getFeeds (
            @RequestParam(required = false) GradeCategory gradeCategory,
            @RequestParam(required = false) SubjectCategory subjectCategory,
            @RequestParam(required = false) TagCategory tagCategory,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)Pageable pageable
            ) {
        Page<FeedResponseDto> response = feedService.getFeeds(gradeCategory, subjectCategory, tagCategory, pageable);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<FeedResponseDto> getFeedDetail(@PathVariable Long feedId) {
        FeedResponseDto response = feedService.getFeedDetail(feedId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "좋아요 증가")
    @PostMapping("/{feedId}/like")
    public ResponseEntity<Void> likeFeed(@PathVariable Long feedId,
                                         @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        feedService.likeFeed(customUserDetails.getId(), feedId);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "좋아요 취소")
    @DeleteMapping("/{feedId}/like")
    public ResponseEntity<Void> unlikeFeed(@PathVariable Long feedId,
                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        feedService.unlikeFeed(customUserDetails.getId(), feedId);
        return ResponseEntity.ok().build();
    }
}
