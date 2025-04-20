package com.littlebank.finance.global.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FileUploadResponse {

    private String path;
    private String url;

    public static FileUploadResponse of(String path, String url) {
        return FileUploadResponse.builder()
                .path(path)
                .url(url)
                .build();
    }

}
