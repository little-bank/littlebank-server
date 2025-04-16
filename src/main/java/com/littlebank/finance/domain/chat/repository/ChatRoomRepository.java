package com.littlebank.finance.domain.chat.repository;

import com.littlebank.finance.domain.chat.dto.ChatRoomSummary;
import com.littlebank.finance.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface ChatRoomRepository extends JpaRepository<ChatRoom,String> {
    @Query(
            value = """
    SELECT 
        r.id AS roomId,
        r.room_name AS roomName,
        m.message AS lastMessage,
        m.created_at AS lastMessageTime,
        (
            SELECT COUNT(*) FROM chat_message m2
            WHERE m2.room_id = r.id AND m2.receiver = :userId AND m2.is_read = false
        ) AS unreadCount
    FROM chat_room r
    JOIN chat_message m
      ON m.id = (
         SELECT MAX(m1.id)
         FROM chat_message m1
         WHERE m1.room_id = r.id
      )
    WHERE r.id IN :roomIds
    """,
            nativeQuery = true)
    List<Object[]> findChatRoomSummariesWithLatestMessage(
            @Param("roomIds") List<String> roomIds,
            @Param("userId") String userId
    );
}
