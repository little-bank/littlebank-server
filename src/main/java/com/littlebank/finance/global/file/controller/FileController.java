package com.littlebank.finance.global.file.controller;

import com.littlebank.finance.global.file.dto.FileUploadResponse;
import com.littlebank.finance.global.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-user/file")
@RequiredArgsConstructor
@Tag(name = "File")
public class FileController {
    private final FileService fileService;

    @Operation(summary = "파일 업로드 pre-signed url 발급 API")
    @GetMapping("/upload")
    public ResponseEntity<List<FileUploadResponse>> getPreSignedUrls(
            @Parameter(
                    name = "mimeType",
                    description = "파일의 MIME 타입 (예: image/png)",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "image/png"
            )
            @RequestParam("mimeType") String mimeType,

            @Parameter(
                    name = "type",
                    description = "업로드 파일 유형 (image, video, thumbnail, file)",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "image"
            )
            @RequestParam("type") String type,

            @Parameter(
                    name = "imageUploadTarget",
                    description = "업로드 대상 (예: profile, chat, mission, feed, ...)",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "profile"
            )
            @RequestParam(name = "imageUploadTarget", required = false) String target,

            @Parameter(
                    name = "num",
                    description = "필요한 업로드 URL 개수",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "3"
            )
            @RequestParam(name = "num", defaultValue = "1") int num
    ) {
        List<FileUploadResponse> result = fileService.generatePreSignedUrls(mimeType, type, target, num);
        return ResponseEntity.ok(result);
    }
}
