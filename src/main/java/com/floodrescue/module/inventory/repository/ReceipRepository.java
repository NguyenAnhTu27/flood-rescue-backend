package com.floodrescue.module.inventory.repository;

import com.floodrescue.module.inventory.entity.InventoryReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceipRepository extends JpaRepository<InventoryReceiptEntity, Long> {
}
