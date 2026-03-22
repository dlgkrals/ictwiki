package com.ict.wiki.file.controller;

import com.ict.wiki.file.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 이미지 업로드
     * POST /api/files/upload
     * Content-Type: multipart/form-data
     * 반환: { "url": "/api/files/xxxx.jpg" }
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) {

        String url = fileUploadService.saveImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * 이미지 서빙
     * GET /api/files/{filename}
     */
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getFile(@PathVariable String filename) {
        byte[] data = fileUploadService.loadImage(filename);
        String contentType = fileUploadService.getContentType(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(data);
    }
}