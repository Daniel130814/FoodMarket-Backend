package com.uade.tpo.foodmarketplace.service.category;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.category.Category;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.common.ResourceInUseException;
import com.uade.tpo.foodmarketplace.repository.category.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PlatoRepository platoRepository;

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

    /**
     * Updates a category after verifying its existence and description uniqueness.
     */
    @Override
    public Category updateCategory(Long categoryId, String description) {
        // The entity is loaded first so JPA updates the existing row instead of inserting one.
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        if (categoryRepository.existsByDescriptionIgnoreCaseAndIdNot(description, categoryId)) {
            throw new CategoryDuplicateException();
        }

        category.setDescription(description);
        return categoryRepository.save(category);
    }

    /**
     * Deletes an unused category while preserving the integrity of dish-category relations.
     */
    @Override
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        if (platoRepository.existsByCategoriasId(categoryId)) {
            throw new ResourceInUseException("No se puede eliminar una categoria asignada a uno o mas platos");
        }

        categoryRepository.delete(category);
    }
}
