package com.floodrescue.shared.storage;

import com.floodrescue.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.rescue-dir:uploads/rescue}")
    private String rescueUploadDir;

    @Value("${app.upload.rescue-public-prefix:/uploads/rescue}")
    private String rescuePublicPrefix;

    @Override
    public StoredFile storeRescueAttachment(MultipartFile file, String extension) throws IOException {
        if (extension == null || extension.isBlank()) {
            throw new BusinessException("Định dạng file không hợp lệ");
        }

        Path uploadPath = Paths.get(rescueUploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID() + extension;
        Path target = uploadPath.resolve(fileName).normalize();
        if (!target.startsWith(uploadPath)) {
            throw new BusinessException("Đường dẫn file không hợp lệ");
        }

        file.transferTo(target);

        String normalizedPrefix = rescuePublicPrefix.endsWith("/")
                ? rescuePublicPrefix.substring(0, rescuePublicPrefix.length() - 1)
                : rescuePublicPrefix;

        return new StoredFile(fileName, normalizedPrefix + "/" + fileName);
    }
}
