package com.littlebank.finance.domain.chat.service;

import com.littlebank.finance.domain.chat.dto.request.ChatMessageDto;
import com.littlebank.finance.domain.chat.dto.response.ChatMessageResponse;
import com.littlebank.finance.domain.chat.domain.ChatMessage;
import com.littlebank.finance.domain.chat.domain.repository.ChatMessageRepository;
import com.littlebank.finance.domain.chat.domain.repository.ChatRoomParticipantRepository;
import com.littlebank.finance.domain.chat.dto.response.ReadMessageDto;
import com.littlebank.finance.domain.chat.dto.response.UpdateUnreadCountResponse;
import com.littlebank.finance.domain.chat.exception.ChatException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.domain.user.service.UserService;
import com.littlebank.finance.global.error.exception.ErrorCode;
import com.littlebank.finance.global.security.CustomUserDetails;
import com.littlebank.finance.global.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    // 메세지 전송 (email, roomId, dto)
    public ChatMessageResponse sendChatMessage(String email, String roomId, ChatMessageDto dto) {
        User sender = userService.findUserByEmail(email);
        Long senderId = sender.getId();

        validateSender(senderId, dto.getSenderId());

        if (!isParticipant(roomId, senderId)) {
            throw new ChatException(ErrorCode.USER_NOT_FOUND);
        }

        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .sender(sender)
                .message(dto.getMessage())
                .type(dto.getType())
                .build();

        message.markAsRead(senderId);
        chatMessageRepository.save(message);

        int participantCount = chatRoomParticipantRepository.countParticipantsInRoom(roomId).intValue();

        return ChatMessageResponse.from(message, participantCount - 1);
    }

    // 메세지 읽음 처리
    public void readChatMessage(String email, Long messageId, String roomId) {
        User reader = userService.findUserByEmail(email);
        Long readerId = reader.getId();

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getReadUserIds().contains(readerId)) {
            message.markAsRead(readerId); //
            chatMessageRepository.save(message);

            int participantCount = chatRoomParticipantRepository.countParticipantsInRoom(roomId).intValue();
            int unreadCount = participantCount - message.getReadUserIds().size();

            messagingTemplate.convertAndSend("/topic/chat/read/" + roomId,
                    new UpdateUnreadCountResponse(message.getId(), unreadCount));
        }
    }

    // senderId 검증
    private void validateSender(Long tokenUserId, Long dtoSenderId) {
        if (dtoSenderId == null || !tokenUserId.equals(dtoSenderId)) {
            log.warn("🚫 인증된 사용자 ID와 메시지의 senderId 불일치: tokenUserId={}, dtoSenderId={}", tokenUserId, dtoSenderId);
            throw new ChatException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }

    // 채팅방 참여 여부 확인
    private boolean isParticipant(String roomId, Long userId) {
        return chatRoomParticipantRepository.existsByRoomIdAndUserId(roomId, userId);
    }
}