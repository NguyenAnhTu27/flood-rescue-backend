package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping("/system-settings")
    public ResponseEntity<?> getSettings() {
        return ResponseEntity.ok(systemSettingService.getSettings());
    }

    @PutMapping("/system-settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(systemSettingService.updateSettings(request));
    }
}
