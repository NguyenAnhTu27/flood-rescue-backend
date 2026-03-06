package com.floodrescue.module.inventory.repository;

import com.floodrescue.module.inventory.entity.InventoryIssueLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueLineRepository extends JpaRepository<InventoryIssueLineEntity, Long> {
}
