package com.littlebank.finance.global.mail;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MailPolicy {
    public final static String DEFAULT_SENDER_NAME = "Little Bank";

    // 이메일 확인 관련
    public final static String EMAIL_CHECK_SEND_TITLE = "[LittleBank] 리틀뱅크 이메일 확인 안내 메일입니다.";
    public final static String EMAIL_CHECK_SEND_FORM_PATH = "templates/check-email.html";

    // 신고 관련
    public static final String REPORT_MAIL_TITLE = "[LittleBank] 신고 접수 안내";
    public static final String REPORT_MAIL_TEMPLATE_PATH = "templates/report-email.html";
}