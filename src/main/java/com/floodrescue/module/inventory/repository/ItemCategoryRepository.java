package com.floodrescue.module.inventory.repository;

import com.floodrescue.module.inventory.entity.ItemCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemCategoryRepository extends JpaRepository<ItemCategoryEntity, Integer> {

    Optional<ItemCategoryEntity> findByCode(String code);

    boolean existsByCode(String code);
}
