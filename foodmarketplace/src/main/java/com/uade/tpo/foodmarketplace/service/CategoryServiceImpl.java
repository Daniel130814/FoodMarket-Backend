package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.Category;
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
        return categoryRepository.findById(categoryId);
    }

    @Override
    public Category createCategory(String description)
            throws CategoryDuplicateException {

        List<Category> categories = categoryRepository.findAll();

        boolean categoryAlreadyExists = categories.stream().anyMatch(category ->
                category.getDescription().equalsIgnoreCase(description));

        if (categoryAlreadyExists) {
            throw new CategoryDuplicateException();
        }

        Category category = new Category(description);
        return categoryRepository.save(category);
    }
}
