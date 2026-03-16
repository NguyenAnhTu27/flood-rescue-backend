package com.floodrescue.module.publicapi.controller;

import com.floodrescue.module.publicapi.service.RuntimeSettingsService;
import com.floodrescue.shared.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class RuntimeSettingsController {

    private final RuntimeSettingsService runtimeSettingsService;

    @GetMapping("/runtime-settings")
    public ResponseEntity<ApiResult<Map<String, String>>> getRuntimeSettings() {
        return ResponseEntity.ok(ApiResult.ok(runtimeSettingsService.getRuntimeSettings()));
    }

    @GetMapping("/content-pages/{pageKey}")
    public ResponseEntity<ApiResult<Map<String, String>>> getContentPage(@PathVariable String pageKey) {
        Map<String, String> page = runtimeSettingsService.getContentPage(pageKey);
        if (page == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResult.ok(page));
    }
}
