package com.littlebank.finance.domain.chat.domain.repository.impl;

import com.littlebank.finance.domain.chat.domain.*;
import com.littlebank.finance.domain.chat.domain.repository.CustomChatRoomEventLogRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.littlebank.finance.domain.chat.domain.QChatMessage.chatMessage;
import static com.littlebank.finance.domain.chat.domain.QChatRoom.chatRoom;
import static com.littlebank.finance.domain.chat.domain.QChatRoomEventLog.chatRoomEventLog;
import static com.littlebank.finance.domain.chat.domain.QChatRoomEventLogDetail.chatRoomEventLogDetail;
import static com.littlebank.finance.domain.chat.domain.QUserChatRoom.userChatRoom;

@RequiredArgsConstructor
public class CustomChatRoomEventLogRepositoryImpl implements CustomChatRoomEventLogRepository {
    private final JPAQueryFactory queryFactory;
    private QChatRoomEventLog el = chatRoomEventLog;
    private QChatRoomEventLogDetail eld = chatRoomEventLogDetail;
    private QChatRoom cr = chatRoom;
    private QChatMessage cm = chatMessage;
    private QUserChatRoom ucr = userChatRoom;

    /**
     * 채팅방에서 이벤트 로그를 모두 조회
     *
     * @param roomId  조회할 채팅방의 식별 id
     * @return  발생한 이벤트 로그 목록
     */
    @Override
    public List<ChatRoomEventLog> findAllByRoomId(Long userId, Long roomId) {
        return queryFactory.selectFrom(el)
                .leftJoin(eld).on(eld.log.id.eq(el.id)).fetchJoin()
                .where(
                        el.room.id.eq(roomId),
                        el.createdDate.goe(getJoinedDate(roomId, userId))
                )
                .orderBy(el.createdDate.desc())
                .fetch();
    }

    /**
     * 채팅방 ID와 메시지 ID 범위를 기준으로 해당 범위 내에 발생한 채팅방 이벤트 로그를 조회
     *
     * @param roomId         조회할 채팅방의 식별 id
     * @param startMessageId 시작 메시지 식별 id (이 메시지의 createdDate 이상부터 포함)
     * @param endMessageId   마지막 메시지 식별 id (이 메시지의 createdDate 이하까지 포함)
     * @return 지정된 시간 범위 내에 발생한 이벤트 로그 목록
     */
    @Override
    public List<ChatRoomEventLog> findByRoomIdAndMessageIds(Long userId, Long roomId, Long startMessageId, Long endMessageId) {
        // 현재 페이지 가장 오래된 메시지와 다음 페이지 가장 최근 메시지 사이의 로그 메시지도 조회하기 위함
        Long realStartMessageId = queryFactory
                .select(cm.id)
                .from(cm)
                .where(cm.room.id.eq(roomId),
                        cm.id.lt(startMessageId)
                )
                .orderBy(cm.id.desc())
                .fetchOne();

        if (realStartMessageId == null) {
            realStartMessageId = startMessageId;
        }

        List<LocalDateTime> timeRange = queryFactory
                .select(cm.timestamp)
                .from(cm)
                .where(cm.id.in(realStartMessageId, endMessageId))
                .orderBy(cm.timestamp.asc())
                .fetch();

        LocalDateTime startTime;
        LocalDateTime endTime;
        if (timeRange.size() < 2) {
            LocalDateTime onlyTime = timeRange.isEmpty() ? null : timeRange.get(0);

            if (onlyTime == null) {
                return Collections.emptyList();
            }

            startTime = onlyTime;
            endTime = onlyTime;
        } else {
            startTime = timeRange.get(0);
            endTime = timeRange.get(1);
        }

        ChatRoom chatRoom = queryFactory
                .selectFrom(cr)
                .where(cr.id.eq(roomId))
                .fetchOne();

        if (chatRoom == null) {
            return Collections.emptyList();
        }

        BooleanBuilder condition = new BooleanBuilder()
                .and(
                        el.room.id.eq(roomId)
                                .and(el.createdDate.goe(getJoinedDate(roomId, userId)))
                );

        // 햔재 페이지 가장 오래된 메시지가 첫 번째 메시지일 때, 그 이전 로그 메시지들까지 전부 조회
        if (startMessageId.equals(chatRoom.getFirstMessageId())) {
            condition.and(el.createdDate.lt(endTime));

            // 햔재 페이지 가장 최근 메시지가 마지막 메시지일 때, 그 이후 로그 메시지들까지 전부 조회
        } else if (endMessageId.equals(chatRoom.getLastMessageId())) {
            condition.and(el.createdDate.goe(startTime));

            // 메시지들 사이에 존재하는 로그 메시지 조회
        } else {
            condition.and(el.createdDate.goe(startTime)
                    .and(el.createdDate.lt(endTime)));
        }

        return queryFactory.selectFrom(el)
                .leftJoin(eld).on(eld.log.id.eq(el.id)).fetchJoin()
                .where(condition)
                .orderBy(el.createdDate.desc())
                .fetch();
    }

    private LocalDateTime getJoinedDate(Long roomId, Long userId) {
        return queryFactory.select(ucr.joinedDate)
                .from(ucr)
                .where(
                        ucr.room.id.eq(roomId),
                        ucr.user.id.eq(userId)
                )
                .fetchOne();
    }

}
