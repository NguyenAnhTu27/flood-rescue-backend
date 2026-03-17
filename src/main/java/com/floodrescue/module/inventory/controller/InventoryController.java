package com.floodrescue.module.inventory.controller;

import com.floodrescue.module.inventory.dto.request.ItemCategoryCreateRequest;
import com.floodrescue.module.inventory.dto.request.ItemClassificationCreateRequest;
import com.floodrescue.module.inventory.dto.request.ItemUnitCreateRequest;
import com.floodrescue.module.inventory.dto.response.ItemCategoryResponse;
import com.floodrescue.module.inventory.dto.response.ItemClassificationResponse;
import com.floodrescue.module.inventory.dto.response.ItemUnitResponse;
import com.floodrescue.module.inventory.dto.response.StockBalanceItemResponse;
import com.floodrescue.module.inventory.service.InventoryCatalogService;
import com.floodrescue.shared.dto.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class InventoryController {

    private final InventoryCatalogService inventoryCatalogService;

    @PostMapping("/items")
    public ResponseEntity<ApiResult<ItemCategoryResponse>> createItemCategory(
            @Valid @RequestBody ItemCategoryCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResult.ok("Tạo danh mục vật tư thành công", inventoryCatalogService.createItemCategory(request)));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResult<List<ItemCategoryResponse>>> getItemCategories(
            @RequestParam(required = false) Integer classificationId
    ) {
        return ResponseEntity.ok(ApiResult.ok(inventoryCatalogService.getItemCategories(classificationId)));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResult<Void>> deleteItemCategory(@PathVariable Integer id) {
        inventoryCatalogService.deleteItemCategory(id);
        return ResponseEntity.ok(ApiResult.ok("Đã xoá danh mục vật tư"));
    }

    @PostMapping("/item-classifications")
    public ResponseEntity<ApiResult<ItemClassificationResponse>> createItemClassification(
            @Valid @RequestBody ItemClassificationCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResult.ok("Tạo nhóm vật tư thành công", inventoryCatalogService.createItemClassification(request)));
    }

    @GetMapping("/item-classifications")
    public ResponseEntity<ApiResult<List<ItemClassificationResponse>>> getItemClassifications() {
        return ResponseEntity.ok(ApiResult.ok(inventoryCatalogService.getItemClassifications()));
    }

    @DeleteMapping("/item-classifications/{id}")
    public ResponseEntity<ApiResult<Void>> deleteItemClassification(@PathVariable Integer id) {
        inventoryCatalogService.deleteItemClassification(id);
        return ResponseEntity.ok(ApiResult.ok("Đã xoá nhóm vật tư"));
    }

    @PostMapping("/item-units")
    public ResponseEntity<ApiResult<ItemUnitResponse>> createItemUnit(
            @Valid @RequestBody ItemUnitCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResult.ok("Tạo đơn vị tính thành công", inventoryCatalogService.createItemUnit(request)));
    }

    @GetMapping("/item-units")
    public ResponseEntity<ApiResult<List<ItemUnitResponse>>> getItemUnits() {
        return ResponseEntity.ok(ApiResult.ok(inventoryCatalogService.getItemUnits()));
    }

    @DeleteMapping("/item-units/{id}")
    public ResponseEntity<ApiResult<Void>> deleteItemUnit(@PathVariable Integer id) {
        inventoryCatalogService.deleteItemUnit(id);
        return ResponseEntity.ok(ApiResult.ok("Đã xoá đơn vị tính"));
    }

    @GetMapping("/stock")
    public ResponseEntity<ApiResult<List<StockBalanceItemResponse>>> getStockBalances() {
        return ResponseEntity.ok(ApiResult.ok(inventoryCatalogService.getStockBalances()));
    }
}
