package com.thecodereveal.shopease.repositories;

import com.thecodereveal.shopease.entities.Category;
import com.thecodereveal.shopease.entities.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryTypeRepository extends JpaRepository<CategoryType, UUID> {
    CategoryType findByCategoryAndCode(Category category, String code);
}
