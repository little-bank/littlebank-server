package com.littlebank.finance.domain.chat.domain.repository;

import com.littlebank.finance.domain.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {

    //무한 스크롤 조회
    @Query("SELECT c FROM ChatMessage c WHERE c.roomId=:roomId AND " +
            "(:cursor IS NULL OR c.id<:cursor) ORDER BY c.id DESC")
    List<ChatMessage> findMessages(@Param("roomId") String roomId, @Param("cursor") Long cursor, Pageable pageable);
    //읽음 처리
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage c SET c.isRead=true WHERE c.roomId=:roomId " +
            "AND c.receiver=:receiver AND c.isRead=false")
    void markAsRead(@Param("roomId") String roomId, @Param("receiver") String receiver);


    ChatMessage findTopByRoomIdOrderByCreatedDateDesc(String roomId);
    List<ChatMessage> findByRoomId(String roomId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.roomId = :roomId AND :userId NOT MEMBER OF cm.readUserIds")
    Long countUnreadMessagesByRoomIdAndUserId(@Param("roomId") String roomId, @Param("userId") Long userId);

}