package com.littlebank.finance.domain.chat.service;

import com.littlebank.finance.domain.chat.dto.request.ChatMessageDto;
import com.littlebank.finance.domain.chat.dto.response.ChatMessageResponse;
import com.littlebank.finance.domain.chat.domain.ChatMessage;
import com.littlebank.finance.domain.chat.domain.repository.ChatMessageRepository;
import com.littlebank.finance.domain.chat.domain.repository.ChatRoomParticipantRepository;
import com.littlebank.finance.domain.chat.dto.response.UpdateUnreadCountResponse;
import com.littlebank.finance.domain.chat.exception.ChatException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageResponse handleChatMessage(String roomId, ChatMessageDto dto, String senderEmail) {
        // 유저 조회
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Long senderId = sender.getId();

        // 채팅방 참여 여부 확인
        if (!isParticipant(roomId, senderId)) {
            throw new ChatException(ErrorCode.USER_NOT_FOUND);
        }

        // 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .sender(sender)
                .message(dto.getMessage())
                .type(dto.getType())
                .build();
        message.markAsRead(senderId);
        chatMessageRepository.save(message);

        // 참여자 수 가져오기
        int participantCount = chatRoomParticipantRepository.countParticipantsInRoom(roomId).intValue();

        return ChatMessageResponse.from(message, participantCount - 1); // 본인 제외
    }

    private List<ChatMessage> sendToParticipants(ChatMessageDto dto, User sender) {
        List<User> participants = chatRoomParticipantRepository.findUsersByRoomId(dto.getRoomId());
        List<ChatMessage> savedMessages = new ArrayList<>();

        for (User participant : participants) {
            if (!participant.getId().equals(sender.getId())) {
                ChatMessage message = ChatMessage.builder()
                        .roomId(dto.getRoomId())
                        .sender(sender)
                        //.receiver(participant) // 꼭 receiver 설정!
                        .message(dto.getMessage())
                        .type(dto.getType())
                        .isRead(false)
                        .build();

                savedMessages.add(chatMessageRepository.save(message));
            }
        }

        return savedMessages;
    }


    private boolean isParticipant(String roomId, Long userId) {
        return chatRoomParticipantRepository.existsByRoomIdAndUserId(roomId, userId);
    }

    @Transactional
    public void markAsRead(Long messageId, Long readerId, String roomId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getReadUserIds().contains(readerId)) {
            message.markAsRead(readerId);
            chatMessageRepository.save(message);

            int participantCount = chatRoomParticipantRepository.countParticipantsInRoom(roomId).intValue();
            int unreadCount = participantCount - message.getReadUserIds().size();

            messagingTemplate.convertAndSend("/topic/chat/read/" + roomId,
                    new UpdateUnreadCountResponse(message.getId(), unreadCount));
        }
    }

}