package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.service.AdminCatalogService;
import com.floodrescue.shared.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    @GetMapping("/catalogs")
    public ResponseEntity<ApiResult<List<Map<String, Object>>>> getCatalogs() {
        return ResponseEntity.ok(ApiResult.ok(adminCatalogService.getCatalogs()));
    }

    @PostMapping("/catalogs")
    public ResponseEntity<ApiResult<Void>> createCatalog(@RequestBody Map<String, Object> payload) {
        adminCatalogService.createCatalog(payload);
        return ResponseEntity.ok(ApiResult.ok("Tạo catalog thành công"));
    }

    @PutMapping("/catalogs/{id}")
    public ResponseEntity<ApiResult<Void>> updateCatalog(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        adminCatalogService.updateCatalog(id, payload);
        return ResponseEntity.ok(ApiResult.ok("Cập nhật catalog thành công"));
    }

    @DeleteMapping("/catalogs/{id}")
    public ResponseEntity<ApiResult<Void>> deleteCatalog(@PathVariable Long id) {
        adminCatalogService.deleteCatalog(id);
        return ResponseEntity.ok(ApiResult.ok("Xóa catalog thành công"));
    }

    @PatchMapping("/catalogs/{id}/active")
    public ResponseEntity<ApiResult<Void>> toggleCatalogActive(@PathVariable Long id) {
        adminCatalogService.toggleCatalogActive(id);
        return ResponseEntity.ok(ApiResult.ok("Đã cập nhật trạng thái"));
    }

    @GetMapping("/catalog-groups")
    public ResponseEntity<ApiResult<List<Map<String, Object>>>> getCatalogGroups() {
        return ResponseEntity.ok(ApiResult.ok(adminCatalogService.getCatalogGroups()));
    }

    @PutMapping("/catalog-groups/{groupCode}")
    public ResponseEntity<ApiResult<Void>> updateCatalogGroupName(
            @PathVariable String groupCode,
            @RequestBody Map<String, Object> payload
    ) {
        adminCatalogService.updateCatalogGroupName(groupCode, payload);
        return ResponseEntity.ok(ApiResult.ok("Cập nhật nhóm danh mục thành công"));
    }

    @DeleteMapping("/catalog-groups/{groupCode}")
    public ResponseEntity<ApiResult<Void>> deleteCatalogGroup(@PathVariable String groupCode) {
        adminCatalogService.deleteCatalogGroup(groupCode);
        return ResponseEntity.ok(ApiResult.ok("Xóa nhóm danh mục thành công"));
    }
}
