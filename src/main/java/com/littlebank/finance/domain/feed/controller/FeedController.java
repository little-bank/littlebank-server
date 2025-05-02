package com.littlebank.finance.domain.feed.controller;

import com.littlebank.finance.domain.feed.domain.GradeCategory;
import com.littlebank.finance.domain.feed.domain.SubjectCategory;
import com.littlebank.finance.domain.feed.domain.TagCategory;
import com.littlebank.finance.domain.feed.dto.request.FeedRequestDto;
import com.littlebank.finance.domain.feed.dto.response.FeedResponseDto;
import com.littlebank.finance.domain.feed.service.FeedService;
import com.littlebank.finance.global.security.CustomUserDetails;
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
public class FeedController {
    private final FeedService feedService;

    @PostMapping("/create")
    public ResponseEntity<String> createFeed(@RequestBody FeedRequestDto request,
                                           @AuthenticationPrincipal CustomUserDetails currentUser){
        feedService.createFeed(currentUser.getId(), request);
        return ResponseEntity.ok("업로드 완료");
    }

    @PutMapping("/{feedId}")
    public ResponseEntity<FeedResponseDto> updateFeed(@PathVariable Long feedId,
                                                      @RequestBody FeedRequestDto request,
                                                      @AuthenticationPrincipal CustomUserDetails currentUser){
        FeedResponseDto response = feedService.updateFeed(currentUser.getId(), feedId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long feedId,
                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        feedService.deleteFeed(currentUser.getId(), feedId);
        return ResponseEntity.ok().build();
    }

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
}
