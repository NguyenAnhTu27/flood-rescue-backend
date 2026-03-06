package com.floodrescue.module.inventory.repository;

import com.floodrescue.module.inventory.entity.InventoryReceiptLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptLineRepository extends JpaRepository<InventoryReceiptLineEntity, Long> {
}
