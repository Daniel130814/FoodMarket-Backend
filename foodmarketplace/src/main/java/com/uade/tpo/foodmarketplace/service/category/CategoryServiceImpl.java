package com.uade.tpo.foodmarketplace.service.category;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.category.Category;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryDuplicateException;
import com.uade.tpo.foodmarketplace.repository.category.CategoryRepository;

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

        if (categoryRepository.existsByDescriptionIgnoreCase(description)) {
            throw new CategoryDuplicateException();
        }

        Category category = new Category(description);
        return categoryRepository.save(category);
    }
}
