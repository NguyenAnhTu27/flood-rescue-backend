package com.floodrescue.module.inventory.repository;

import com.floodrescue.module.inventory.entity.StockBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockBalanceRepository extends JpaRepository<StockBalanceEntity, Long> {
}
