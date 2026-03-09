package com.floodrescue.module.rescue.controller;

import com.floodrescue.module.rescue.dto.request.AddNoteRequest;
import com.floodrescue.module.rescue.dto.request.RescueRequestCreateRequest;
import com.floodrescue.module.rescue.dto.request.RescueRequestUpdateRequest;
import com.floodrescue.module.rescue.dto.response.AttachmentUploadResponse;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.shared.enums.AttachmentFileType;
import com.floodrescue.module.rescue.service.RescueRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rescue/citizen")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CITIZEN')")
public class CitizenRescueController {

    private final RescueRequestService rescueRequestService;

    @Value("${app.upload.rescue-dir:uploads/rescue}")
    private String rescueUploadDir;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @PostMapping("/requests")
    public ResponseEntity<RescueRequestResponse> createRescueRequest(
            @Valid @RequestBody RescueRequestCreateRequest request,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.createRescueRequest(citizenId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<RescueRequestResponse>> getMyRescueRequests(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        Page<RescueRequestResponse> response = rescueRequestService.getRescueRequestsByCitizen(citizenId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<?> getRescueRequestById(@PathVariable Long id, Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.getRescueRequestById(id);

        if (response.getCitizenId() == null || !response.getCitizenId().equals(citizenId)) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền xem yêu cầu này"));
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<RescueRequestResponse> updateRescueRequest(
            @PathVariable Long id,
            @Valid @RequestBody RescueRequestUpdateRequest request,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.updateRescueRequest(id, citizenId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Map<String, String>> cancelRescueRequest(
            @PathVariable Long id,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        rescueRequestService.cancelRescueRequest(id, citizenId);
        return ResponseEntity.ok(Map.of("message", "Yêu cầu cứu hộ đã được hủy"));
    }

    @PostMapping("/requests/{id}/notes")
    public ResponseEntity<RescueRequestResponse> addNote(
            @PathVariable Long id,
            @Valid @RequestBody AddNoteRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.addNote(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attachments")
    public ResponseEntity<List<AttachmentUploadResponse>> uploadAttachments(
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Path uploadPath = Paths.get(rescueUploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<AttachmentUploadResponse> result = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            String newFileName = UUID.randomUUID() + ext;
            Path target = uploadPath.resolve(newFileName);
            file.transferTo(target);

            String fileUrl = "/uploads/rescue/" + newFileName;
            fileUrl = fileUrl.replaceAll("/+", "/");

            result.add(AttachmentUploadResponse.builder()
                    .fileUrl(fileUrl)
                    .fileType(AttachmentFileType.IMAGE)
                    .build());
        }

        return ResponseEntity.ok(result);
    }
}
