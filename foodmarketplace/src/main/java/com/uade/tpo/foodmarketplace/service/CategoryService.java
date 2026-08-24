package com.uade.tpo.foodmarketplace.service;

import java.util.ArrayList;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.CategoryRequest;

public interface CategoryService {

    ArrayList<CategoryRequest> getCategories();

    Optional<CategoryRequest> getCategoryById(int categoryId);

    String createCategory(CategoryRequest categoryRequest);
}
