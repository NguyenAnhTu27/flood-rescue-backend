package com.floodrescue.module.attachment.controller;

import com.floodrescue.module.attachment.dto.response.AttachmentUploadResponse;
import com.floodrescue.module.attachment.service.AttachmentService;
import com.floodrescue.shared.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * Upload file(s). Common endpoint for all modules.
     * Accept: files (List<MultipartFile>)
     * Return: List of { fileUrl, fileType }
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResult<List<AttachmentUploadResponse>>> uploadFiles(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        List<MultipartFile> uploadFiles = (files != null && !files.isEmpty()) ? files : attachments;
        return ResponseEntity.ok(ApiResult.ok("Tải file lên thành công", attachmentService.uploadFiles(uploadFiles)));
    }
}
