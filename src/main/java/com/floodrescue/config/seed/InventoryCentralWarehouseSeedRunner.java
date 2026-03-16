package com.floodrescue.config.seed;

import com.floodrescue.module.inventory.entity.ItemCategoryEntity;
import com.floodrescue.module.inventory.entity.ItemClassificationEntity;
import com.floodrescue.module.inventory.entity.ItemUnitEntity;
import com.floodrescue.module.inventory.entity.StockBalanceEntity;
import com.floodrescue.module.inventory.repository.ItemCategoryRepository;
import com.floodrescue.module.inventory.repository.ItemClassificationRepository;
import com.floodrescue.module.inventory.repository.ItemUnitRepository;
import com.floodrescue.module.inventory.repository.StockBalanceRepository;
import com.floodrescue.shared.enums.StockSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Seeds central-warehouse-like inventory test data (logical warehouse via stock_balances).
 * Idempotent: creates only missing master data / balances and does not overwrite existing quantities.
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class InventoryCentralWarehouseSeedRunner implements ApplicationRunner {

    private final ItemUnitRepository itemUnitRepository;
    private final ItemClassificationRepository itemClassificationRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final StockBalanceRepository stockBalanceRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.central-warehouse.enabled:true}")
    private boolean centralWarehouseSeedEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled || !centralWarehouseSeedEnabled) {
            return;
        }

        ItemUnitEntity unitPack = ensureUnit("GOI", "Goi");
        ItemUnitEntity unitBottle = ensureUnit("CHAI", "Chai");
        ItemUnitEntity unitBox = ensureUnit("THUNG", "Thung");
        ItemUnitEntity unitSet = ensureUnit("BO", "Bo");

        ItemClassificationEntity clsFood = ensureClassification("FOOD", "Luong thuc");
        ItemClassificationEntity clsWater = ensureClassification("WATER", "Nuoc uong");
        ItemClassificationEntity clsMedical = ensureClassification("MEDICAL", "Y te");
        ItemClassificationEntity clsUtility = ensureClassification("UTILITY", "Vat tu cuu tro");

        ItemCategoryEntity rice = ensureCategory("RICE_5KG", "Gao 5kg", unitPack.getCode(), clsFood);
        ItemCategoryEntity instantNoodle = ensureCategory("NOODLE_BOX", "Mi an lien thung", unitBox.getCode(), clsFood);
        ItemCategoryEntity bottledWater = ensureCategory("WATER_500ML", "Nuoc suoi 500ml", unitBottle.getCode(), clsWater);
        ItemCategoryEntity oralRehydration = ensureCategory("ORESOL_BOX", "Oresol hop", unitBox.getCode(), clsMedical);
        ItemCategoryEntity firstAid = ensureCategory("FIRST_AID_SET", "Bo so cuu", unitSet.getCode(), clsMedical);
        ItemCategoryEntity blanket = ensureCategory("BLANKET", "Chan men", unitPack.getCode(), clsUtility);

        ensureBalance(rice, StockSourceType.DONATION, new BigDecimal("320.00"));
        ensureBalance(rice, StockSourceType.PURCHASE, new BigDecimal("180.00"));

        ensureBalance(instantNoodle, StockSourceType.DONATION, new BigDecimal("240.00"));
        ensureBalance(instantNoodle, StockSourceType.PURCHASE, new BigDecimal("160.00"));

        ensureBalance(bottledWater, StockSourceType.DONATION, new BigDecimal("1200.00"));
        ensureBalance(bottledWater, StockSourceType.PURCHASE, new BigDecimal("800.00"));

        ensureBalance(oralRehydration, StockSourceType.DONATION, new BigDecimal("90.00"));
        ensureBalance(oralRehydration, StockSourceType.PURCHASE, new BigDecimal("60.00"));

        ensureBalance(firstAid, StockSourceType.DONATION, new BigDecimal("70.00"));
        ensureBalance(firstAid, StockSourceType.PURCHASE, new BigDecimal("45.00"));

        ensureBalance(blanket, StockSourceType.DONATION, new BigDecimal("210.00"));
        ensureBalance(blanket, StockSourceType.PURCHASE, new BigDecimal("110.00"));
    }

    private ItemUnitEntity ensureUnit(String code, String name) {
        String normalized = normalize(code);
        return itemUnitRepository.findAll().stream()
                .filter(u -> normalize(u.getCode()).equals(normalized))
                .findFirst()
                .map(existing -> {
                    boolean changed = false;
                    if (!name.equals(existing.getName())) {
                        existing.setName(name);
                        changed = true;
                    }
                    if (Boolean.FALSE.equals(existing.getIsActive())) {
                        existing.setIsActive(true);
                        changed = true;
                    }
                    return changed ? itemUnitRepository.save(existing) : existing;
                })
                .orElseGet(() -> itemUnitRepository.save(ItemUnitEntity.builder()
                        .code(code)
                        .name(name)
                        .isActive(true)
                        .build()));
    }

    private ItemClassificationEntity ensureClassification(String code, String name) {
        String normalized = normalize(code);
        return itemClassificationRepository.findAll().stream()
                .filter(c -> normalize(c.getCode()).equals(normalized))
                .findFirst()
                .map(existing -> {
                    boolean changed = false;
                    if (!name.equals(existing.getName())) {
                        existing.setName(name);
                        changed = true;
                    }
                    if (Boolean.FALSE.equals(existing.getIsActive())) {
                        existing.setIsActive(true);
                        changed = true;
                    }
                    return changed ? itemClassificationRepository.save(existing) : existing;
                })
                .orElseGet(() -> itemClassificationRepository.save(ItemClassificationEntity.builder()
                        .code(code)
                        .name(name)
                        .isActive(true)
                        .build()));
    }

    private ItemCategoryEntity ensureCategory(String code, String name, String unitCode, ItemClassificationEntity classification) {
        return itemCategoryRepository.findByCode(code)
                .map(existing -> {
                    boolean changed = false;
                    if (!name.equals(existing.getName())) {
                        existing.setName(name);
                        changed = true;
                    }
                    if (!unitCode.equalsIgnoreCase(existing.getUnit())) {
                        existing.setUnit(unitCode);
                        changed = true;
                    }
                    if (existing.getClassification() == null || !classification.getId().equals(existing.getClassification().getId())) {
                        existing.setClassification(classification);
                        changed = true;
                    }
                    if (Boolean.FALSE.equals(existing.getIsActive())) {
                        existing.setIsActive(true);
                        changed = true;
                    }
                    return changed ? itemCategoryRepository.save(existing) : existing;
                })
                .orElseGet(() -> itemCategoryRepository.save(ItemCategoryEntity.builder()
                        .code(code)
                        .name(name)
                        .unit(unitCode)
                        .classification(classification)
                        .isActive(true)
                        .build()));
    }

    private void ensureBalance(ItemCategoryEntity category, StockSourceType sourceType, BigDecimal initialQty) {
        stockBalanceRepository.findByItemCategoryAndSourceType(category, sourceType)
                .orElseGet(() -> stockBalanceRepository.save(StockBalanceEntity.builder()
                        .itemCategory(category)
                        .sourceType(sourceType)
                        .qty(initialQty)
                        .build()));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
