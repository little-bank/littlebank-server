package com.littlebank.finance.global.file.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.littlebank.finance.global.error.exception.ErrorCode;
import com.littlebank.finance.global.file.dto.FileUploadResponse;
import com.littlebank.finance.global.file.exception.FileException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final AmazonS3 s3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<FileUploadResponse> generatePreSignedUrls(String mimeType, String type, String target, int num) {
        if (type.equals("image") && !mimeType.startsWith("image/")) {
            throw new FileException(ErrorCode.INVALID_MIMETYPE);
        }

        List<FileUploadResponse> result = new ArrayList<>();
        String ext = mimeType.split("/")[1];
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        for (int i = 0; i < num; i++) {
            String uuid = UUID.randomUUID().toString();
            String key = uuid + "." + ext;
            String path = resolvePath(type, target, datePath, key);

            URL url = s3Client.generatePresignedUrl(new GeneratePresignedUrlRequest(bucket, path)
                    .withMethod(HttpMethod.PUT)
                    .withContentType(mimeType)
                    .withExpiration(new Date(System.currentTimeMillis() + 60 * 1000)));

            result.add(FileUploadResponse.of(path, url.toString()));
        }

        return result;
    }

    private String resolvePath(String type, String target, String datePath, String key) {
        return switch (type) {
            case "image" -> target.equals("profile") ?
                    String.format("images/origin/profile/%s", key) :
                    String.format("images/origin/%s/%s/%s", target, datePath, key);
            case "video" -> String.format("videos/%s/%s/%s", target, datePath, key);
            case "thumbnail" -> String.format("videos/thumbnail/%s/%s/%s", target, datePath, key);
            case "file" -> String.format("files/%s/%s/%s", target, datePath, key);
            default -> key;
        };
    }
}
