package com.littlebank.finance.domain.chat.domain.repository;

import com.littlebank.finance.domain.chat.domain.ChatRoomParticipant;
import com.littlebank.finance.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long>, ChatRoomParticipantCustomRepository {
    @Query("SELECT CASE WHEN COUNT(p)>0 THEN true ELSE false END " +
            "FROM ChatRoomParticipant p WHERE p.chatRoom.id=:roomId AND p.user.id=:userId")
    boolean existsByRoomIdAndUserId(@Param("roomId") String roomId,
                                    @Param("userId") Long userId);
    void deleteAllByChatRoomId(String roomId);

    @Query("SELECT crp.user FROM ChatRoomParticipant crp WHERE crp.chatRoom.id = :roomId")
    List<User> findUsersByRoomId(@Param("roomId") String roomId);

    //Set<Long> user(User user);

    @Query("SELECT crp.chatRoom.id FROM ChatRoomParticipant crp WHERE crp.user.id = :userIds")
    List<String> findRoomIdsByUserId(@Param("userIds") Long userId);

}