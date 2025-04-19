package com.littlebank.finance.domain.chat.service;

import com.littlebank.finance.domain.chat.dto.ChatMessageDto;
import com.littlebank.finance.domain.chat.dto.ChatMessageResponse;
import com.littlebank.finance.domain.chat.entity.ChatMessage;
import com.littlebank.finance.domain.chat.repository.ChatMessageRepository;
import com.littlebank.finance.domain.chat.repository.ChatRoomParticipantRepository;
import com.littlebank.finance.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;


    public ChatMessage saveMessage(ChatMessageDto dto) {
        ChatMessage message = ChatMessage.builder()
                .roomId(dto.getRoomId())
                .sender(loadUserById(dto.getSenderId()))
                .receiver(loadUserById(dto.getReceiverId()))
                .message(dto.getMessage())
                .type(dto.getType())
                .isRead(false)
                .build();
        return chatMessageRepository.save(message);
    }

    public boolean isParticipant(String roomId, String userId) {
        return chatRoomParticipantRepository.existsByRoomIdAndUserId(roomId, userId);
    }

    public void markAsRead(String roomId, String receiver) {
        chatMessageRepository.markAsRead(roomId, receiver);
    }

    public List<ChatMessageResponse> getMessages(String roomId, Long cursor, int size) {
        Pageable pageable= PageRequest.of(0,size, Sort.by("id").descending());
        return chatMessageRepository.findMessages(roomId,cursor,pageable).stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }
    private User loadUserById(Long userId) {
        // Placeholder for actual implementation to fetch or create a User entity
        // Replace this with the actual logic from your application (e.g., userRepository.findById(userId).orElseThrow())
        return User.builder().id(userId).build();
    }
}
