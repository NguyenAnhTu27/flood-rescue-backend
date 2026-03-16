package com.floodrescue.module.inventory.controller;

import com.floodrescue.module.inventory.dto.request.ItemCategoryCreateRequest;
import com.floodrescue.module.inventory.dto.request.ItemClassificationCreateRequest;
import com.floodrescue.module.inventory.dto.request.ItemUnitCreateRequest;
import com.floodrescue.module.inventory.dto.response.ItemCategoryResponse;
import com.floodrescue.module.inventory.dto.response.ItemClassificationResponse;
import com.floodrescue.module.inventory.dto.response.ItemUnitResponse;
import com.floodrescue.module.inventory.dto.response.StockBalanceItemResponse;
import com.floodrescue.module.inventory.service.InventoryCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class InventoryController {

        private final InventoryCatalogService inventoryCatalogService;

    @PostMapping("/items")
    public ResponseEntity<ItemCategoryResponse> createItemCategory(
            @Valid @RequestBody ItemCategoryCreateRequest request
    ) {
        return ResponseEntity.ok(inventoryCatalogService.createItemCategory(request));
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemCategoryResponse>> getItemCategories(
            @RequestParam(required = false) Integer classificationId
    ) {
        return ResponseEntity.ok(inventoryCatalogService.getItemCategories(classificationId));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItemCategory(@PathVariable Integer id) {
                inventoryCatalogService.deleteItemCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/item-classifications")
    public ResponseEntity<ItemClassificationResponse> createItemClassification(
            @Valid @RequestBody ItemClassificationCreateRequest request
    ) {
                return ResponseEntity.ok(inventoryCatalogService.createItemClassification(request));
    }

    @GetMapping("/item-classifications")
    public ResponseEntity<List<ItemClassificationResponse>> getItemClassifications() {
                return ResponseEntity.ok(inventoryCatalogService.getItemClassifications());
    }

    @DeleteMapping("/item-classifications/{id}")
    public ResponseEntity<Void> deleteItemClassification(@PathVariable Integer id) {
                inventoryCatalogService.deleteItemClassification(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/item-units")
    public ResponseEntity<ItemUnitResponse> createItemUnit(
            @Valid @RequestBody ItemUnitCreateRequest request
    ) {
                return ResponseEntity.ok(inventoryCatalogService.createItemUnit(request));
    }

    @GetMapping("/item-units")
    public ResponseEntity<List<ItemUnitResponse>> getItemUnits() {
                return ResponseEntity.ok(inventoryCatalogService.getItemUnits());
    }

    @DeleteMapping("/item-units/{id}")
    public ResponseEntity<Void> deleteItemUnit(@PathVariable Integer id) {
                inventoryCatalogService.deleteItemUnit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stock")
    public ResponseEntity<List<StockBalanceItemResponse>> getStockBalances() {
        return ResponseEntity.ok(inventoryCatalogService.getStockBalances());
    }
}
