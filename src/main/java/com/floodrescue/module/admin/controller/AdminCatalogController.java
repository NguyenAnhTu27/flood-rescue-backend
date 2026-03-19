package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.dto.request.AdminCatalogGroupUpdateRequest;
import com.floodrescue.module.admin.dto.request.AdminCatalogRequest;
import com.floodrescue.module.admin.service.AdminCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    @GetMapping("/catalogs")
    public ResponseEntity<List<Map<String, Object>>> getCatalogs() {
        return ResponseEntity.ok(adminCatalogService.getCatalogs());
    }

    @PostMapping("/catalogs")
    public ResponseEntity<Map<String, Object>> createCatalog(@Valid @RequestBody AdminCatalogRequest payload) {
        return adminCatalogService.createCatalog(payload);
    }

    @PutMapping("/catalogs/{id}")
    public ResponseEntity<Map<String, Object>> updateCatalog(@PathVariable Long id, @Valid @RequestBody AdminCatalogRequest payload) {
        return adminCatalogService.updateCatalog(id, payload);
    }

    @DeleteMapping("/catalogs/{id}")
    public ResponseEntity<Map<String, Object>> deleteCatalog(@PathVariable Long id) {
        return adminCatalogService.deleteCatalog(id);
    }

    @PatchMapping("/catalogs/{id}/active")
    public ResponseEntity<Map<String, Object>> toggleCatalogActive(@PathVariable Long id) {
        return adminCatalogService.toggleCatalogActive(id);
    }

    @GetMapping("/catalog-groups")
    public ResponseEntity<List<Map<String, Object>>> getCatalogGroups() {
        return ResponseEntity.ok(adminCatalogService.getCatalogGroups());
    }

    @PutMapping("/catalog-groups/{groupCode}")
    public ResponseEntity<Map<String, Object>> updateCatalogGroupName(
            @PathVariable String groupCode,
            @Valid @RequestBody AdminCatalogGroupUpdateRequest payload
    ) {
        return adminCatalogService.updateCatalogGroupName(groupCode, payload);
    }

    @DeleteMapping("/catalog-groups/{groupCode}")
    public ResponseEntity<Map<String, Object>> deleteCatalogGroup(@PathVariable String groupCode) {
        return adminCatalogService.deleteCatalogGroup(groupCode);
    }
}
