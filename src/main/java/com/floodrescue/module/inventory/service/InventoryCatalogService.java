package com.floodrescue.module.inventory.service;

import com.floodrescue.module.inventory.dto.request.ItemCategoryCreateRequest;
import com.floodrescue.module.inventory.dto.request.ItemClassificationCreateRequest;
import com.floodrescue.module.inventory.dto.request.ItemUnitCreateRequest;
import com.floodrescue.module.inventory.dto.response.ItemCategoryResponse;
import com.floodrescue.module.inventory.dto.response.ItemClassificationResponse;
import com.floodrescue.module.inventory.dto.response.ItemUnitResponse;
import com.floodrescue.module.inventory.dto.response.StockBalanceItemResponse;
import com.floodrescue.module.inventory.entity.ItemCategoryEntity;
import com.floodrescue.module.inventory.entity.ItemClassificationEntity;
import com.floodrescue.module.inventory.entity.ItemUnitEntity;
import com.floodrescue.module.inventory.entity.StockBalanceEntity;
import com.floodrescue.module.inventory.repository.ItemCategoryRepository;
import com.floodrescue.module.inventory.repository.ItemClassificationRepository;
import com.floodrescue.module.inventory.repository.ItemUnitRepository;
import com.floodrescue.module.inventory.repository.StockBalanceRepository;
import com.floodrescue.shared.enums.StockSourceType;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryCatalogService {

    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemClassificationRepository itemClassificationRepository;
    private final ItemUnitRepository itemUnitRepository;
    private final StockBalanceRepository stockBalanceRepository;

    @Transactional
    public ItemCategoryResponse createItemCategory(ItemCategoryCreateRequest request) {
        String code = request.getCode().trim();
        if (itemCategoryRepository.existsByCode(code)) {
            throw new BusinessException("Mã hàng đã tồn tại");
        }

        ItemClassificationEntity classification = itemClassificationRepository.findById(request.getClassificationId())
                .orElseThrow(() -> new NotFoundException("Phân loại hàng không tồn tại"));
        String requestedUnit = request.getUnit().trim();
        ItemUnitEntity unit = itemUnitRepository.findAll().stream()
                .filter(u -> requestedUnit.equalsIgnoreCase(u.getCode()) || requestedUnit.equalsIgnoreCase(u.getName()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Đơn vị không tồn tại trong danh mục đơn vị"));

        ItemCategoryEntity entity = ItemCategoryEntity.builder()
                .code(code)
                .name(request.getName().trim())
                .unit(unit.getCode())
                .classification(classification)
                .isActive(true)
                .build();

        return toCategoryResponse(itemCategoryRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ItemCategoryResponse> getItemCategories(Integer classificationId) {
        return itemCategoryRepository.findAllWithClassification(classificationId).stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteItemCategory(Integer id) {
        ItemCategoryEntity category = itemCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Danh mục hàng không tồn tại"));
        try {
            itemCategoryRepository.delete(category);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Không thể xóa danh mục đang phát sinh chứng từ nhập/xuất");
        }
    }

    @Transactional
    public ItemClassificationResponse createItemClassification(ItemClassificationCreateRequest request) {
        String code = request.getCode().trim();
        if (itemClassificationRepository.existsByCode(code)) {
            throw new BusinessException("Mã phân loại đã tồn tại");
        }

        ItemClassificationEntity entity = ItemClassificationEntity.builder()
                .code(code)
                .name(request.getName().trim())
                .isActive(true)
                .build();

        return toClassificationResponse(itemClassificationRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ItemClassificationResponse> getItemClassifications() {
        return itemClassificationRepository.findAll().stream()
                .map(this::toClassificationResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteItemClassification(Integer id) {
        if (itemCategoryRepository.countByClassification_Id(id) > 0) {
            throw new BusinessException("Không thể xóa phân loại đang được sử dụng trong danh mục hàng");
        }
        ItemClassificationEntity entity = itemClassificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phân loại hàng không tồn tại"));
        itemClassificationRepository.delete(entity);
    }

    @Transactional
    public ItemUnitResponse createItemUnit(ItemUnitCreateRequest request) {
        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();
        if (itemUnitRepository.existsByCodeOrName(code, name)) {
            throw new BusinessException("Đơn vị đã tồn tại");
        }

        ItemUnitEntity entity = itemUnitRepository.save(ItemUnitEntity.builder()
                .code(code)
                .name(name)
                .isActive(true)
                .build());

        return toUnitResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<ItemUnitResponse> getItemUnits() {
        return itemUnitRepository.findAll().stream()
                .map(this::toUnitResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteItemUnit(Integer id) {
        ItemUnitEntity entity = itemUnitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Đơn vị không tồn tại"));
        if (itemCategoryRepository.countByUnitIgnoreCase(entity.getCode()) > 0) {
            throw new BusinessException("Không thể xóa đơn vị đang được sử dụng trong danh mục hàng");
        }
        itemUnitRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<StockBalanceItemResponse> getStockBalances() {
        List<StockBalanceEntity> balances = stockBalanceRepository.findAll();

        Map<ItemCategoryEntity, List<StockBalanceEntity>> byItem = balances.stream()
                .collect(Collectors.groupingBy(StockBalanceEntity::getItemCategory));

        return byItem.entrySet().stream()
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

                    return StockBalanceItemResponse.builder()
                            .itemCategoryId(item.getId())
                            .code(item.getCode())
                            .name(item.getName())
                            .unit(item.getUnit())
                            .donationQty(donation)
                            .purchaseQty(purchase)
                            .totalQty(donation.add(purchase))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private ItemCategoryResponse toCategoryResponse(ItemCategoryEntity entity) {
        ItemClassificationEntity classification = entity.getClassification();
        return ItemCategoryResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .unit(entity.getUnit())
                .classificationId(classification == null ? null : classification.getId())
                .classificationCode(classification == null ? null : classification.getCode())
                .classificationName(classification == null ? null : classification.getName())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private ItemClassificationResponse toClassificationResponse(ItemClassificationEntity entity) {
        return ItemClassificationResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private ItemUnitResponse toUnitResponse(ItemUnitEntity entity) {
        return ItemUnitResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}