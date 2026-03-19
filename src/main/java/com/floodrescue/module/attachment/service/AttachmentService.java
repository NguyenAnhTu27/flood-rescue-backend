package com.floodrescue.module.attachment.service;

import com.floodrescue.module.attachment.dto.response.AttachmentUploadResponse;
import com.floodrescue.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    @Value("${file.upload.dir:uploads/}")
    private String uploadDir;

    @Value("${file.upload.base-url:/uploads/}")
    private String baseUrl;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip"
    );

    public List<AttachmentUploadResponse> uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException("Vui lòng chọn ít nhất 1 file để tải lên");
        }

        List<AttachmentUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            responses.add(uploadSingleFile(file));
        }

        return responses;
    }

    private AttachmentUploadResponse uploadSingleFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File không được để trống");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException("Không xác định được tên file");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File vượt quá kích thước tối đa 50MB");
        }

        // Validate file extension
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new BusinessException("Loại file không được phép: " + fileExtension);
        }

        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String storedFileName = UUID.randomUUID() + "." + fileExtension;
            Path filePath = uploadPath.resolve(storedFileName);

            // Save file
            Files.write(filePath, file.getBytes());

            return new AttachmentUploadResponse(
                    baseUrl + storedFileName,
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream"
            );
        } catch (IOException e) {
            throw new BusinessException("Lỗi khi tải file lên: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf('.');
        if (lastIndexOf == -1 || lastIndexOf == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }
}
