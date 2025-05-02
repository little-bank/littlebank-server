package com.littlebank.finance.domain.chat.controller;

import com.littlebank.finance.domain.chat.dto.request.ChatRoomRequest;
import com.littlebank.finance.domain.chat.domain.ChatRoom;
import com.littlebank.finance.domain.chat.service.ChatRoomService;

import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api-user/chat-room")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Tag(name = "Chat", description = "채팅방 생성, 조회 등 기능 제공")
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 생성", description = "채팅방을 생성하고 참여자들을 등록합니다.")
    @PostMapping
    public ResponseEntity<ChatRoom> createRoom(@RequestBody @Valid ChatRoomRequest request,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId=currentUser.getId();
        ChatRoom createdRoom = chatRoomService.createRoom(request, userId);
        return ResponseEntity.ok(createdRoom);
    }

    @Operation(summary = "전체 채팅방 조회", description = "모든 채팅방을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ChatRoom>> listRooms(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            log.error("❌ currentUser is null");
            throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }
        Long userId=currentUser.getId();
        return ResponseEntity.ok(chatRoomService.getAllRooms(userId));
    }

    @Operation(summary="참여자 여부 확인")
    @GetMapping("/{roomId}/is-participant")
    public ResponseEntity<Boolean> isParticipant(@PathVariable String roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        boolean result = chatRoomService.isParticipant(roomId, currentUser.getId());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "채팅방 삭제", description = "채팅방 ID로 삭제합니다.")
    @Transactional
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        chatRoomService.deleteRoom(roomId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}