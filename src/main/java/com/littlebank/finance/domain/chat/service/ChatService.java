package com.littlebank.finance.domain.chat.service;

import com.littlebank.finance.domain.chat.domain.ChatRoom;
import com.littlebank.finance.domain.chat.domain.repository.ChatRoomRepository;
import com.littlebank.finance.domain.chat.dto.request.ChatMessageDto;
import com.littlebank.finance.domain.chat.dto.response.ChatMessageResponse;
import com.littlebank.finance.domain.chat.domain.ChatMessage;
import com.littlebank.finance.domain.chat.domain.repository.ChatMessageRepository;
import com.littlebank.finance.domain.chat.domain.repository.ChatRoomParticipantRepository;
import com.littlebank.finance.domain.chat.dto.response.ChatRoomSummary;
import com.littlebank.finance.domain.chat.exception.ChatException;
import com.littlebank.finance.domain.user.domain.User;
import com.littlebank.finance.domain.user.domain.repository.UserRepository;
import com.littlebank.finance.domain.user.exception.UserException;
import com.littlebank.finance.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final ChatRoomRepository chatRoomRepository;

    public void handleChatMessage(String roomId, ChatMessageDto dto, String senderEmail) {
        // 유저 정보 조회
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        Long senderId = sender.getId();

        // 채팅방 참여 여부 확인
        if (!isParticipant(roomId, senderId)) {
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

    private void sendToParticipants(ChatMessageDto dto) {
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
                        .message(dto.getMessage())
                        .type(dto.getType())
                        .isRead(false)
                        .build();

                chatMessageRepository.save(message);
            }
        }
    }

    private boolean isParticipant(String roomId, Long userId) {
        return chatRoomParticipantRepository.existsByRoomIdAndUserId(roomId, userId);
    }

    public List<ChatRoomSummary> getChatRoomsForUser(Long userId) {
        List<String> roomIds = chatRoomParticipantRepository.findRoomIdsByUserId(userId);

        return roomIds.stream()
                .map(roomId -> {
                    ChatRoom room = chatRoomRepository.findById(roomId)
                            .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다. roomId: " + roomId));

                    ChatMessage lastMessage = chatMessageRepository.findTopByRoomIdOrderByCreatedDateDesc(roomId);

                    List<User> participants = chatRoomParticipantRepository.findUsersByRoomId(roomId);

                    // ⭐ 나를 제외한 다른 사람들의 프로필 사진 최대 4명만 추출
                    List<String> profileImageUrls = participants.stream()
                            .filter(user -> !user.getId().equals(userId)) // 나 제외
                            .map(User::getProfileImageUrl)
                            .limit(4) // 최대 4개까지만
                            .toList();

                    Long unreadCount = 0L;
                    if (lastMessage != null) {
                        int participantCount = participants.size();
                        unreadCount = (long) (participantCount - lastMessage.getReadUserIds().size());
                    }

                    return ChatRoomSummary.builder()
                            .roomId(room.getId())
                            .roomName(room.getName())
                            .lastMessage(lastMessage != null ? lastMessage.getMessage() : "")
                            .lastMessageTime(lastMessage != null ? lastMessage.getCreatedDate() : null)
                            .unreadCount(unreadCount)
                            .profileImageUrls(profileImageUrls)
                            .build();
                })
                .toList();
    }

    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ChatException(ErrorCode.USER_NOT_FOUND))
                .getId();
    }
}