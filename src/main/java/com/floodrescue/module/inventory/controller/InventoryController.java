package com.floodrescue.module.inventory.controller;

import com.floodrescue.module.inventory.dto.request.ItemCategoryCreateRequest;
import com.floodrescue.module.inventory.dto.response.ItemCategoryResponse;
import com.floodrescue.module.inventory.dto.response.StockBalanceItemResponse;
import com.floodrescue.module.inventory.entity.ItemCategoryEntity;
import com.floodrescue.module.inventory.entity.StockBalanceEntity;
import com.floodrescue.module.inventory.repository.ItemCategoryRepository;
import com.floodrescue.module.inventory.repository.StockBalanceRepository;
import com.floodrescue.shared.enums.StockSourceType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class InventoryController {

    private final ItemCategoryRepository itemCategoryRepository;
    private final StockBalanceRepository stockBalanceRepository;

    @PostMapping("/items")
    public ResponseEntity<ItemCategoryResponse> createItemCategory(
            @Valid @RequestBody ItemCategoryCreateRequest request
    ) {
        if (itemCategoryRepository.existsByCode(request.getCode().trim())) {
            return ResponseEntity.badRequest().build();
        }

        ItemCategoryEntity entity = ItemCategoryEntity.builder()
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .unit(request.getUnit().trim())
                .isActive(true)
                .build();

        entity = itemCategoryRepository.save(entity);

        return ResponseEntity.ok(toResponse(entity));
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemCategoryResponse>> getItemCategories() {
        List<ItemCategoryResponse> list = itemCategoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/stock")
    public ResponseEntity<List<StockBalanceItemResponse>> getStockBalances() {
        List<StockBalanceEntity> balances = stockBalanceRepository.findAll();

        Map<ItemCategoryEntity, List<StockBalanceEntity>> byItem = balances.stream()
                .collect(Collectors.groupingBy(StockBalanceEntity::getItemCategory));

        List<StockBalanceItemResponse> result = byItem.entrySet().stream()
                .map(entry -> {
                    ItemCategoryEntity item = entry.getKey();
                    BigDecimal donation = entry.getValue().stream()
                            .filter(b -> b.getSourceType() == StockSourceType.DONATION)
                            .map(StockBalanceEntity::getQty)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal purchase = entry.getValue().stream()
                            .filter(b -> b.getSourceType() == StockSourceType.PURCHASE)
                            .map(StockBalanceEntity::getQty)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal total = donation.add(purchase);

                    return StockBalanceItemResponse.builder()
                            .itemCategoryId(item.getId())
                            .code(item.getCode())
                            .name(item.getName())
                            .unit(item.getUnit())
                            .donationQty(donation)
                            .purchaseQty(purchase)
                            .totalQty(total)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private ItemCategoryResponse toResponse(ItemCategoryEntity entity) {
        return ItemCategoryResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .unit(entity.getUnit())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
