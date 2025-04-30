package com.littlebank.finance.domain.feed.controller;

import com.littlebank.finance.domain.feed.dto.request.FeedRequestDto;
import com.littlebank.finance.domain.feed.dto.response.FeedResponseDto;
import com.littlebank.finance.domain.feed.service.FeedService;
import com.littlebank.finance.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
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
        FeedResponseDto response = feedService.updateFeed(feedId, currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long feedId,
                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        feedService.deleteFeed(currentUser.getId(), feedId);
        return ResponseEntity.ok().build();
    }

}
