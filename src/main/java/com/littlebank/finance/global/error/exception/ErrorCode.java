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
    USER_NOT_FOUND(500, "U001", "유저가 존재하지 않습니다"),
    EMAIL_DUPLICATED(500, "U002", "중복된 이메일이 존재합니다"),

    // Auth
    PASSWORD_NOT_MATCHED(500, "A001", "비밀번호가 일치하지 않습니다"),

    // File
    INVALID_MIMETYPE(500, "I001", "유효하지 않은 mimetype 입니다"),

    // Chat
    CHAT_ROOM_NOT_FOUND(404,"C0H01","채팅방이 존재하지 않습니다"),
    FORBIDDEN_CHAT_DELETE(403,"CH002","채팅방을 삭제할 수 없습니다"),
    MESSAGE_NOT_FOUND(404, "CH003", "메세지를 찾을 수 없습니다");

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

