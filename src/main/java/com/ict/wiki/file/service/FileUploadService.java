package com.ict.wiki.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Value("${app.upload.path}")
    private String uploadPath;

    /**
     * 이미지 저장 후 접근 URL 반환
     */
    public String saveImage(MultipartFile file) {
        validateImage(file);

        String extension = getExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "." + extension;

        try {
            Path dir = Paths.get(uploadPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            file.transferTo(dir.resolve(storedName));
            log.info("이미지 저장 완료 - {}", storedName);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }

        return "/api/files/" + storedName;
    }

    /**
     * 저장된 파일 바이트 반환
     */
    public byte[] loadImage(String filename) {
        // 경로 탐색 공격 방지
        if (filename.contains("..") || filename.contains("/")) {
            throw new IllegalArgumentException("잘못된 파일명");
        }
        try {
            Path path = Paths.get(uploadPath).resolve(filename);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("파일 없음: " + filename, e);
        }
    }

    public String getContentType(String filename) {
        String ext = getExtension(filename).toLowerCase();
        return switch (ext) {
            case "png"  -> "image/png";
            case "gif"  -> "image/gif";
            case "webp" -> "image/webp";
            default     -> "image/jpeg";
        };
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다 (jpg/png/gif/webp만 가능)");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}