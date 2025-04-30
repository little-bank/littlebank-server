package com.littlebank.finance.domain.feed.dto.request;

import com.littlebank.finance.domain.feed.domain.GradeCategory;
import com.littlebank.finance.domain.feed.domain.SubjectCategory;
import com.littlebank.finance.domain.feed.domain.TagCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.List;

@Getter
public class FeedRequestDto {
    @NotBlank
    @Schema(description = "피드 제목", example = "ltbk 흥해라")
    private String title;

    @NotBlank
    @Schema(description = "글 카테고리", example = "ALL")
    private GradeCategory gradeCategory;

    @NotBlank
    @Schema(description = "과목 카테고리", example = "MATH")
    private SubjectCategory subjectCategory;

    @NotBlank
    @Schema(description = "습관 형성", example = "HABIT_BUILDING")
    private TagCategory tagCategory;

    @NotBlank
    @Schema(description = "피드 내용", example = "null 허용해도 되지 않을까")
    private String content;

    private List<FeedImageRequestDto> images;
}
