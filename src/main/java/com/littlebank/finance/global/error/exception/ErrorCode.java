package com.littlebank.finance.global.error.exception;

public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(400, "C001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(405, "C002", " Invalid Input Value"),
    INTERNAL_SERVER_ERROR(500, "C004", "Server Error"),
    INVALID_TYPE_VALUE(400, "C005", " Invalid Type Value"),
    HANDLE_ACCESS_DENIED(403, "C006", "Access is Denied"),
    NOT_AUTHENTICATED(401, "C007", "Unauthorized"),

    // User
    USER_NOT_FOUND(404, "U001", "유저가 존재하지 않습니다"),
    EMAIL_DUPLICATED(409, "U002", "중복된 이메일이 존재합니다"),
    PHONE_DUPLICATED(409, "U003", "전화번호가 중복되었습니다"),
    FORBIDDEN_PARENT_ONLY(403, "U004", "부모 역할만 권한이 있습니다"),

    // Friend
    FRIEND_NOT_FOUND(404, "F001", "친구가 존재하지 않습니다"),
    ALREADY_FRIEND_EXISTS(409, "F002", "이미 친구 추가 되어있습니다"),
    NO_PERMISSION_TO_MODIFY(403, "F003", "수정 권한이 없습니다"),

    // Family
    FAMILY_NOT_FOUND(404, "FA001", "가족이 존재하지 않습니다"),

    // FamilyMember
    FAMILY_MEMBER_NOT_FOUND(404, "FM001", "가족 맴버가 존재하지 않습니다"),
    FAMILY_INVITE_ALREADY_SENT(409, "FM002", "가족 맴버로 초대를 보낸 상태입니다"),
    FAMILY_MEMBER_ALREADY_EXISTS(409, "FM003", "가족 맴버로 소속되어 있습니다"),
    MULTIPLE_PARENTS_NOT_ALLOWED(422, "FM004", "부모 역할이 2개 이상 존재할 수 없습니다"),

    // Goal
    GOAL_NOT_FOUND(404, "G001", "목표 데이터가 존재하지 않습니다"),
    GOAL_WEEKLY_DUPLICATE(409, "G002", "이번 주에 이미 신청한 목표 유형입니다"),
    GOAL_END_DATE_EXPIRED(410, "G003", "설정한 목표 진행 기간이 지났습니다"),

    // Point
    PAYMENT_INVALID_STATUS(400, "P001", "결제 정보 저장 중 오류가 발생했습니다(지원하지 않는 결제 상태)"),
    PAYMENT_STATUS_NOT_PAID(400, "P002", "결제 된 상태가 아닙니다"),
    PAYMENT_ALREADY_EXISTS(409, "A003", "결제 정보가 이미 존재합니다"),

    // Auth
    PASSWORD_NOT_MATCHED(401, "A001", "비밀번호가 일치하지 않습니다"),
    INVALID_REFRESH_TOKEN(401, "A002", "리프레쉬 토큰이 유효하지 않습니다"),

    // File
    INVALID_MIMETYPE(415, "FI001", "유효하지 않은 mimetype 입니다"),

    // Mail
    MAIL_SEND_ERROR(500, "M001", "메일 전송 과정에서 오류가 발생했습니다"),

    // Chat
    CHAT_ROOM_NOT_FOUND(404,"C001","채팅방이 존재하지 않습니다"),
    FORBIDDEN_CHAT_DELETE(403,"C002","채팅방을 삭제할 수 없습니다"),

    // Feed
    FEED_NOT_FOUND(404, "F001","피드를 찾을 수 없습니다" ),
    USER_NOT_EQUAL(403, "F002", "접근 권한이 없습니다"),
    ALREADY_LIKED(400, "F003", "이미 좋아요 눌렀습니다"),
    LIKE_NOT_FOUND(404, "F004", "좋아요 정보가 존재하지 않습니다"),
    COMMENT_NOT_FOUND(404, "F005", "댓글을 찾을 수 없습니다"),
    INVALID_PARENT_COMMENT(404, "F006", "댓글 아이디를 찾을 수 없습니다"),
    LIKE_ALREADY_DELETED(400, "F008" , "좋아요 정보가 존재하지 않습니다"),
    FAIL_TO_GET_LOCK(409, "F009", "락 획득 실패했습니다"),

    // Challenge
    CHALLENGE_NOT_FOUND(404, "CH001", "챌린지를 찾을 수 없습니다"),
    ALREADY_JOINED(400, "CH002", "이미 참여 중입니다"),
    INVALID_PARTICIPATION_PERIOD(400, "CH003", "챌린지 참여 기간 다시 입력하세요"),
    LOCK_INTERUPPTED(500, "CH004", "챌린지 참여 처리 중 오류 발생했습니다"),
    CHALLENGE_FULL(400, "CH005", "챌린지 참여 인원을 초과했습니다"),
    INVALID_CHALLENGE_CATEGORY(400, "CH006", "카테고리가 타당하지 않습니다"),
    CHALLENGE_END_DATE_EXPIRED(400, "CH007", "챌린지 기한이 지났습니다"),
    NOT_FOUND_PARTICIPATION(400, "CH008", "자식이 챌린지에 참여하지 않았습니다"),
    ALREADY_ACCEPT(400, "CH009", "이미 아이의 챌리지 신청을 승낙했습니다"),
    UNVALID_MY_PARTICIPATION_TYPE( 400, "CH010", "타입은 진행중, 완료한 두 개만 존재합니다"),
    // GAME
    GAME_NOT_FOUND( 404, "G001", "게임을 찾을 수 없습니다"),
    ALREADY_VOTED(400, "G002", "이미 투표했습니다");

    private final String code;
    private final String message;
    private final int status;

    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }

}

