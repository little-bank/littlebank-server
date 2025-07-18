package com.littlebank.finance.domain.chat.domain.repository.impl;

import com.littlebank.finance.domain.chat.domain.QChatMessage;
import com.littlebank.finance.domain.chat.domain.QChatRoom;
import com.littlebank.finance.domain.chat.domain.QUserChatRoom;
import com.littlebank.finance.domain.chat.domain.UserChatRoom;
import com.littlebank.finance.domain.chat.domain.constant.RoomRange;
import com.littlebank.finance.domain.chat.domain.repository.CustomUserChatRoomRepository;
import com.littlebank.finance.domain.chat.dto.response.ChatRoomDetailsResponse;
import com.littlebank.finance.domain.chat.dto.response.ChatRoomSummaryResponse;
import com.littlebank.finance.domain.chat.exception.ChatException;
import com.littlebank.finance.domain.friend.domain.Friend;
import com.littlebank.finance.domain.friend.domain.QFriend;
import com.littlebank.finance.domain.user.domain.QUser;
import com.littlebank.finance.global.error.exception.ErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.littlebank.finance.domain.chat.domain.QChatMessage.chatMessage;
import static com.littlebank.finance.domain.chat.domain.QChatRoom.chatRoom;
import static com.littlebank.finance.domain.chat.domain.QUserChatRoom.userChatRoom;
import static com.littlebank.finance.domain.friend.domain.QFriend.friend;
import static com.littlebank.finance.domain.user.domain.QUser.user;

@RequiredArgsConstructor
public class CustomUserChatRoomRepositoryImpl implements CustomUserChatRoomRepository {
    private final static int MAX_UNREAD_MESSAGE_SHOW_COUNT = 300;
    private final JPAQueryFactory queryFactory;
    private QUser u = user;
    private QChatRoom cr = chatRoom;
    private QUserChatRoom ucr = userChatRoom;
    private QChatMessage cm = chatMessage;
    private QFriend f = friend;

    /**
     * 메시지 전송 시 displayIdx를 현재 시간으로 update, 벌크 연산 수행
     *
     * @param roomId 업데이트할 채팅방 식별 id
     */
    @Override
    public void updateDisplayIdxByRoomId(Long roomId) {
        queryFactory
                .update(ucr)
                .set(ucr.displayIdx, LocalDateTime.now())
                .where(ucr.room.id.eq(roomId))
                .execute();
    }

    /**
     * 특정 채팅방의 참여자 수를 조회하되, 지정된 사용자는 제외하고 계산
     * 메시지의 초기 readCount 값을 설정할 때 사용
     *
     * @param roomId           채팅방 식별 id
     * @param excludedUserId   제외할 사용자 식별 id (메시지 전송자)
     * @return                 제외된 사용자를 제외한 채팅방 참여자 수
     */
    @Override
    public int countParticipantsExcludingUser(Long roomId, Long excludedUserId) {
        Long count = queryFactory
                .select(ucr.count())
                .from(ucr)
                .where(
                        ucr.room.id.eq(roomId),
                        ucr.user.id.ne(excludedUserId)
                )
                .fetchOne();

        return count != null ? count.intValue() : 0;
    }

    /**
     * UserChatRoom 엔티티를 ChatRoom 엔티티를 페치조인 하여 조회
     *
     * @param roomId 채팅방 식별 id
     * @return
     */
    @Override
    public List<UserChatRoom> findAllWithFetchByRoomId(Long roomId) {
        return queryFactory
                .selectFrom(ucr)
                .join(ucr.room, cr).fetchJoin()
                .where(ucr.room.id.eq(roomId))
                .fetch();
    }

    /**
     * 특정 유저들을 제외한 채팅방 참여자들을 조회
     *
     * @param roomId 채팅방 식별 id
     * @param targetUserIds 조회에 제외할 특정 유저들의 식별 id 목록
     * @return
     */
    @Override
    public List<UserChatRoom> findAllWithFetchByRoomIdNotInTargetUserIds(Long roomId, List<Long> targetUserIds) {
        return queryFactory
                .selectFrom(ucr)
                .join(ucr.room, cr).fetchJoin()
                .where(ucr.room.id.eq(roomId),
                        ucr.user.id.notIn(targetUserIds)
                )
                .fetch();
    }

    /**
     * 참여 중인 채팅방 목록을 조회
     *
     * 각 채팅방에 대해 다음 정보를 제공
     * - roomId: 채팅방 ID
     * - roomName: 채팅방 이름 (1:1 채팅인 경우 user 이름으로 설정, 친구 추가해 놨다면 설정한 친구 이름으로 설정)
     * - roomType: 채팅방 타입 (FRIEND로 한정)
     * - roomRange: 채팅방 공개 범위
     * - displayIdx: 사용자의 채팅방 정렬 기준 시간
     * - unreadMessageCount : 안 읽은 메시지 갯수 (300개 이상은 300개까지만 노출)
     * - participantNameList: 채팅방 참여자 이름 목록 (본인 제외)
     *      - 친구 관계가 있는 경우: Friend.customName 사용
     *      - 친구가 아닌 경우: User.name 사용
     *
     * 조건:
     * - 삭제되지 않은 채팅방(User, ChatRoom 엔티티의 @Where 조건 포함)이 기본적으로 필터링됨
     * - 1:1 채팅에서 내가 상대방을 차단한 경우, unreadMessageCount는 0으로 처리
     *
     * @param userId 조회할 사용자의 ID
     * @return 채팅방 요약 정보 리스트
     */
    @Override
    public List<ChatRoomSummaryResponse> findChatRoomSummaryList(Long userId) {
        List<Tuple> myRooms = queryFactory
                .select(cr.id, cr.name, cr.range, ucr.displayIdx, cr.lastMessageId, ucr.joinedDate)
                .from(ucr)
                .join(ucr.room, cr)
                .where(
                        ucr.user.id.eq(userId),
                        new BooleanBuilder()
                                .or(cr.createdBy.id.eq(userId))
                                .or(
                                        JPAExpressions
                                                .selectOne()
                                                .from(cm)
                                                .where(cm.room.id.eq(cr.id),
                                                        ucr.isJoined,
                                                        cm.timestamp.goe(ucr.joinedDate))
                                                .exists()
                                )
                )
                .fetch();

        return myRooms.stream().map(tuple -> {
            Long roomId = tuple.get(cr.id);
            String roomName = tuple.get(cr.name);
            RoomRange roomRange = tuple.get(cr.range);
            LocalDateTime displayIdx = tuple.get(ucr.displayIdx);
            Long lastMessageId = tuple.get(cr.lastMessageId);
            LocalDateTime ucrJoinedDate = tuple.get(ucr.joinedDate);

            UserChatRoom userChatRoom = queryFactory
                    .selectFrom(ucr)
                    .where(ucr.user.id.eq(userId).and(ucr.room.id.eq(roomId)))
                    .fetchOne();

            // 읽지 않은 메시지 갯수 조회
            int finalUnreadCount = 0;
            if (roomRange == RoomRange.PRIVATE) {
                Long opponentId = queryFactory
                        .select(u.id)
                        .from(ucr)
                        .join(ucr.user, u)
                        .where(ucr.room.id.eq(roomId), u.id.ne(userId))
                        .fetchFirst();

                Friend friend = queryFactory
                        .selectFrom(f)
                        .where(f.fromUser.id.eq(userId), f.toUser.id.eq(opponentId))
                        .fetchOne();

                boolean isBlocked = friend != null && friend.getIsBlocked();

                if (!isBlocked) {
                    finalUnreadCount = fetchUnreadCount(userId, roomId, lastMessageId, userChatRoom.getLastReadMessageId(), ucrJoinedDate);
                } else {
                    finalUnreadCount = 0;
                }

            } else if (roomRange == RoomRange.PRIVATE) {
                finalUnreadCount = fetchUnreadCount(userId, roomId, lastMessageId, userChatRoom.getLastReadMessageId(), ucrJoinedDate);
            }

            // 내 기준 참여자들과의 친구 관계 이름을 조회
            List<String> participantNames = queryFactory
                    .select(
                            new CaseBuilder()
                                    .when(f.id.isNotNull())
                                    .then(f.customName)
                                    .otherwise(u.name)
                    )
                    .from(ucr)
                    .join(ucr.user, u)
                    .leftJoin(f)
                    .on(f.fromUser.id.eq(userId)
                            .and(f.toUser.id.eq(u.id)))
                    .where(ucr.room.id.eq(roomId)
                            .and(u.id.ne(userId)))
                    .fetch();

            if (roomRange == RoomRange.PRIVATE && !participantNames.isEmpty()) {
                roomName = participantNames.get(0);
            }

            return ChatRoomSummaryResponse.builder()
                    .roomId(roomId)
                    .roomName(roomName)
                    .roomRange(roomRange)
                    .participantNameList(participantNames)
                    .displayIdx(displayIdx)
                    .unreadMessageCount(finalUnreadCount)
                    .build();
        }).collect(Collectors.toList());
    }

    private int fetchUnreadCount(Long userId, Long roomId, Long lastMessageId, Long lastReadMessageId, LocalDateTime joinedDate) {
        Long count = queryFactory
                .select(cm.id.count())
                .from(cm)
                .where(
                        cm.room.id.eq(roomId),
                        cm.sender.id.ne(userId),
                        cm.id.gt(lastReadMessageId),
                        cm.id.loe(lastMessageId),
                        cm.timestamp.goe(joinedDate)
                )
                .limit(MAX_UNREAD_MESSAGE_SHOW_COUNT + 1)
                .fetchOne();

        return (int) Math.min(count, MAX_UNREAD_MESSAGE_SHOW_COUNT);
    }

    /**
     * 단일 채팅방의 상세 정보를 조회
     *
     * 조건:
     * - 사용자가 참여 중인 채팅방이어야 함
     *
     * 응답:
     * - roomId: 채팅방 id
     * - roomName: 채팅방 이름 (PRIVATE 방일 경우 상대방 이름 혹은 customName)
     * - roomRange: 채팅방 공개 범위
     * - participants: 채팅방 참여자 정보 목록 (본인 포함)
     *      - userId, name, profileImageUrl, isFriend, friendId, customName, isBestFriend, isBlocked
     * - lastReadMessageId : 내가 마지막으로 읽은 메시지의 식별 id
     * - lastSendMessageId : 채팅방에서 마지막으로 올라온 메시지의 식별 id
     *
     * @param userId 현재 로그인한 사용자 id
     * @param roomId 조회할 채팅방 id
     * @return ChatRoomDetailsResponse (없으면 Optional.empty())
     */
    @Override
    public Optional<ChatRoomDetailsResponse> findChatRoomDetails(Long userId, Long roomId) {
        Tuple roomInfo = queryFactory
                .select(cr.id, cr.name, cr.range, ucr.lastReadMessageId, cr.lastMessageId)
                .from(ucr)
                .join(ucr.room, cr)
                .where(
                        cr.id.eq(roomId),
                        ucr.user.id.eq(userId),
                        ucr.isJoined.isTrue()
                )
                .fetchOne();

        if (roomInfo == null) throw new ChatException(ErrorCode.USER_CHAT_ROOM_NOT_FOUND);

        Long fetchedRoomId = roomInfo.get(cr.id);
        String roomName = roomInfo.get(cr.name);
        RoomRange roomRange = roomInfo.get(cr.range);
        Long lastReadMessageId = roomInfo.get(ucr.lastReadMessageId);
        Long lastSendMessageId = roomInfo.get(cr.lastMessageId);

        // 내 기준 참여자들과의 친구 관계 조회
        List<ChatRoomDetailsResponse.ParticipantInfo> participants = queryFactory
                .select(Projections.constructor(ChatRoomDetailsResponse.ParticipantInfo.class,
                        u.id,
                        u.name,
                        u.profileImagePath,
                        f.id.isNotNull(),
                        f.id,
                        f.customName,
                        f.isBestFriend,
                        f.isBlocked
                ))
                .from(ucr)
                .join(ucr.user, u)
                .leftJoin(f).on(
                        f.fromUser.id.eq(userId),
                        f.toUser.id.eq(u.id)
                )
                .where(ucr.room.id.eq(roomId))
                .fetch();

        // 1:1 채팅일 때 채팅방 이름 설정
        if (roomRange == RoomRange.PRIVATE) {
            ChatRoomDetailsResponse.ParticipantInfo participantInfo = participants.stream()
                    .filter(p -> !p.getUserId().equals(userId))
                    .findFirst().get();

            roomName = participantInfo.getIsFriend() ? participantInfo.getCustomName() : participantInfo.getName();
        }

        return Optional.of(new ChatRoomDetailsResponse(
                fetchedRoomId,
                roomName,
                roomRange,
                participants,
                lastReadMessageId,
                lastSendMessageId
        ));
    }

}
