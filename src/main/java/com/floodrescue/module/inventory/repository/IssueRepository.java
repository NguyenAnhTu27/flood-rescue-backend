package com.floodrescue.module.inventory.repository;

import com.floodrescue.module.inventory.entity.InventoryIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<InventoryIssueEntity, Long> {
}
