package com.littlebank.finance.domain.chat.controller;

import com.littlebank.finance.domain.chat.dto.ChatRoomRequest;
import com.littlebank.finance.domain.chat.entity.ChatRoom;
import com.littlebank.finance.domain.chat.entity.ChatRoomParticipant;
import com.littlebank.finance.domain.chat.repository.ChatRoomParticipantRepository;
import com.littlebank.finance.domain.chat.repository.ChatRoomRepository;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Tag(name = "채팅방 API", description = "채팅방 생성, 조회 등 기능 제공")
@RestController
@SecurityRequirement(name = "BearerAuth")
@RequestMapping("/api-user/chat-room")
public class ChatRoomController {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public ChatRoomController(ChatRoomRepository chatRoomRepository,
                              ChatRoomParticipantRepository participantRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.participantRepository=participantRepository;
        this.userRepository = userRepository;
    }

    @Operation(summary = "채팅방 생성", description = "채팅방을 생성하고 참여자들을 등록합니다.", security=@SecurityRequirement(name="BearerAuth"))
    @PostMapping //채팅방 생성 + 참여자 등록
    public ResponseEntity<ChatRoom> createRoom(@RequestBody @Valid ChatRoomRequest request) {
        String roomId = UUID.randomUUID().toString();
        ChatRoom chatRoom = ChatRoom.builder().id(roomId).name(request.getName()).build();
        chatRoomRepository.save(chatRoom);
        //Long currentUserId=Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        CustomUserDetails userDetails=(CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId=userDetails.getId();
        Set<Long> allParticipantIds=new HashSet<>(request.getParticipantIds());
        allParticipantIds.add(currentUserId);
        //List<Long> participantLongIds = request.getParticipantIds().stream().map(Long::valueOf).toList();
        List<User> users = userRepository.findAllById(allParticipantIds.stream().toList());
        for (User user : users) {
            participantRepository.save(ChatRoomParticipant.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .build());
        }
        return ResponseEntity.ok(chatRoom);
    }

    @Operation(summary = "전체 채팅방 조회", description = "모든 채팅방을 조회합니다.", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping //채팅방 전체 조회
    public List<ChatRoom> listRooms() {
        return chatRoomRepository.findAll();
    }
    @GetMapping("/{roomId}/is-participant")
    public ResponseEntity<Boolean> isParticipant(@PathVariable String roomId, @RequestParam String userId) {
        boolean result = participantRepository.existsByRoomIdAndUserId(roomId, userId);
        return ResponseEntity.ok(result);
    }


    @Operation(summary = "채팅방 삭제", description = "채팅방 ID로 삭제합니다.", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        participantRepository.deleteAllByChatRoomId(roomId);
        chatRoomRepository.deleteById(roomId);
        return ResponseEntity.noContent().build();
    }
}