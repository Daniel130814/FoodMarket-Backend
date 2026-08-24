package com.uade.tpo.foodmarketplace.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.Category;
import com.uade.tpo.foodmarketplace.entity.dto.CategoryRequest;
import com.uade.tpo.foodmarketplace.exceptions.CategoryDuplicateException;
import com.uade.tpo.foodmarketplace.repository.CategoryRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
    }

    @Override
    public String createCategory(CategoryRequest categoryRequest) {
        for (Category category : categoryRepository.getCategories()) {
            if (category.getDescription().equalsIgnoreCase(categoryRequest.getDescription())) {
                throw new CategoryDuplicateException();
            }
        }

        Category category = Category.builder()
                .description(categoryRequest.getDescription())
                .build();

        return categoryRepository.createCategory(category);
    }
}
