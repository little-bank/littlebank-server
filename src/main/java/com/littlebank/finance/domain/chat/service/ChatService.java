package com.littlebank.finance.domain.chat.service;

import com.littlebank.finance.domain.chat.dto.request.ChatMessageDto;
import com.littlebank.finance.domain.chat.dto.response.ChatMessageResponse;
import com.littlebank.finance.domain.chat.domain.ChatMessage;
import com.littlebank.finance.domain.chat.domain.repository.ChatMessageRepository;
import com.littlebank.finance.domain.chat.domain.repository.ChatRoomParticipantRepository;
import com.littlebank.finance.domain.chat.exception.ChatException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void handleChatMessage(String roomId, ChatMessageDto dto, String senderEmail) {
        // 유저 정보 조회
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ChatException(ErrorCode.USER_NOT_FOUND));
        Long senderId = sender.getId();

        // 채팅방 참여 여부 확인
        if (!isParticipant(roomId, senderId.toString())) {
            throw new ChatException(ErrorCode.USER_NOT_FOUND); // or 커스텀 메시지
        }

        // DTO에 sender 정보 채움
        dto.setRoomId(roomId);
        dto.setSenderId(senderId);

        // 메시지 저장 및 브로드캐스트
        sendToParticipants(dto);

        ChatMessageResponse response = ChatMessageResponse.builder()
                .sender(String.valueOf(senderId))
                .message(dto.getMessage())
                .type(dto.getType().name())
                .build();

        String destination = "/topic/chat/" + roomId;
        messagingTemplate.convertAndSend(destination, response);
        log.info("📢 메시지 전송 완료: {}", destination);
    }

    public void sendToParticipants(ChatMessageDto dto) {
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new ChatException(ErrorCode.USER_NOT_FOUND));

        // 채팅방 참여자 전체 조회
        List<User> participants = chatRoomParticipantRepository.findUsersByRoomId(dto.getRoomId());

        // 참여자 중 sender가 아닌 사람에게만 메시지 저장
        for (User participant : participants) {
            if (!participant.getId().equals(sender.getId())) {
                ChatMessage message = ChatMessage.builder()
                        .roomId(dto.getRoomId())
                        .sender(sender)
                        .receiver(participant) // 저장은 receiver와 함께
                        .message(dto.getMessage())
                        .type(dto.getType())
                        .isRead(false)
                        .build();

                chatMessageRepository.save(message);
            }
        }
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