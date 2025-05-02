package com.littlebank.finance.domain.feed.domain;

public enum TagCategory {
    STUDY_CERTIFICATION("학습 인증"),
    HABIT_BUILDING("습관 형성"),
    INFORMATION("정보"),
    ALL("전체");
    private final String description;

    TagCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
