package com.uade.tpo.foodmarketplace.service;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.CategoryRequest;
import com.uade.tpo.foodmarketplace.repository.CategoryRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository = new CategoryRepository();

    public ArrayList<CategoryRequest> getCategories() {
        return categoryRepository.getCategories();
    }

    public String getCategoryById(int categoryId) {
        return categoryRepository.getCategoryById(categoryId);
    }

    public String createCategory(CategoryRequest categoryRequest) {
        return categoryRepository.createCategory(categoryRequest);
    }
}
