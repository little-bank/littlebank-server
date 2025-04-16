package com.littlebank.finance.domain.chat.controller;

import com.littlebank.finance.domain.chat.dto.ChatRoomRequest;
import com.littlebank.finance.domain.chat.entity.ChatRoom;
import com.littlebank.finance.domain.chat.entity.ChatRoomParticipant;
import com.littlebank.finance.domain.chat.repository.ChatRoomParticipantRepository;
import com.littlebank.finance.domain.chat.repository.ChatRoomRepository;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "채팅방 API", description = "채팅방 생성, 조회 등 기능 제공")
@RestController
@RequestMapping("/chat-room")
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

    @Operation(summary = "채팅방 생성", description = "채팅방을 생성하고 참여자들을 등록합니다.")
    @PostMapping //채팅방 생성 + 참여자 등록
    public ResponseEntity<ChatRoom> createRoom(@RequestBody ChatRoomRequest request) {
        String roomId = UUID.randomUUID().toString();
        ChatRoom chatRoom = ChatRoom.builder().id(roomId).name(request.getName()).build();
        chatRoomRepository.save(chatRoom);
        List<Long> participantLongIds = request.getParticipantIds().stream().map(Long::valueOf).toList();
        List<User> users = userRepository.findAllById(participantLongIds);
        for (User user : users) {
            participantRepository.save(ChatRoomParticipant.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .build());
        }
        return ResponseEntity.ok(chatRoom);
    }

    @Operation(summary = "전체 채팅방 조회", description = "모든 채팅방을 조회합니다.")
    @GetMapping //채팅방 전체 조회
    public List<ChatRoom> listRooms() {
        return chatRoomRepository.findAll();
    }
    @GetMapping("/{roomId}/is-participant")
    public ResponseEntity<Boolean> isParticipant(@PathVariable String roomId, @RequestParam String userId) {
        boolean result = participantRepository.existsByRoomIdAndUserId(roomId, userId);
        return ResponseEntity.ok(result);
    }
}