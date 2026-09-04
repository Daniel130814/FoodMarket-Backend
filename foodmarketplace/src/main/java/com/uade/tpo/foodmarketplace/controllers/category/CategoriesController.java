package com.uade.tpo.foodmarketplace.controllers.category;

import java.net.URI;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.category.Category;
import com.uade.tpo.foodmarketplace.entity.dto.category.CategoryRequest;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryDuplicateException;
import com.uade.tpo.foodmarketplace.service.category.CategoryService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("categories")
public class CategoriesController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Category> getCategoryById(@PathVariable("categoryId") Long categoryId) {
        Optional<Category> category = categoryService.getCategoryById(categoryId);

        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("createCategory")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest categoryRequest)
            throws CategoryDuplicateException {
        Category result = categoryService.createCategory(
                categoryRequest.getDescription());

        return ResponseEntity
                .created(URI.create("/categories/" + result.getId()))
                .body(result);
    }

    /**
     * Actualiza la descripción de una categoría identificada por el id de la URL.
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<Category> updateCategory(@PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody CategoryRequest categoryRequest) {
        Category result = categoryService.updateCategory(categoryId, categoryRequest.getDescription());
        return ResponseEntity.ok(result);
    }

    /**
     * Elimina una categoría únicamente cuando no está asignada a un plato.
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

}
