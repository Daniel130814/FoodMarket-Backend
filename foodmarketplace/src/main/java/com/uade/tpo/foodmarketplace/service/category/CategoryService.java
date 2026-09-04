package com.uade.tpo.foodmarketplace.service.category;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.category.Category;

public interface CategoryService {

    List<Category> getCategories();

    Optional<Category> getCategoryById(Long categoryId);

    Category createCategory(String description);

}
