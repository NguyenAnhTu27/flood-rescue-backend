package com.floodrescue.module.inventory.repository;

import com.floodrescue.module.inventory.entity.ItemCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCategoryRepository extends JpaRepository<ItemCategoryEntity, Integer> {
}
