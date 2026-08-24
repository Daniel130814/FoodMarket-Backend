package com.uade.tpo.foodmarketplace.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.CategoryRequest;
import com.uade.tpo.foodmarketplace.exceptions.CategoryDuplicateException;
import com.uade.tpo.foodmarketplace.repository.CategoryRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public ArrayList<CategoryRequest> getCategories() {
        return categoryRepository.getCategories();
    }

    @Override
    public Optional<CategoryRequest> getCategoryById(int categoryId) {
        return categoryRepository.getCategoryById(categoryId);
    }

    @Override
    public String createCategory(CategoryRequest categoryRequest) {
        for (CategoryRequest category : categoryRepository.getCategories()) {
            if (category.getDescription().equalsIgnoreCase(categoryRequest.getDescription())) {
                throw new CategoryDuplicateException();
            }
        }

        return categoryRepository.createCategory(categoryRequest);
    }
}
