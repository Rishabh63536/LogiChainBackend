package com.cts.logichain360.service.impl;

import com.cts.logichain360.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/jpg");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("POD photo is required and cannot be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPEG/PNG images are allowed for POD photos. Received: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("POD photo exceeds the maximum allowed size of 5MB. Received: "+ (file.getSize() / (1024 * 1024)) + "MB.");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "photo.jpg" : file.getOriginalFilename());
            String extension = originalFilename.contains(".")? originalFilename.substring(originalFilename.lastIndexOf(".")): ".jpg";
            String storedFilename = UUID.randomUUID() + extension;

            Path targetPath = uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored POD photo as {} ({} bytes)", storedFilename, file.getSize());
            return storedFilename;

        } catch (IOException e) {
            log.error("Failed to store POD photo: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to store POD photo: " + e.getMessage(), e);
        }
    }
}