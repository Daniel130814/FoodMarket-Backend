package com.uade.tpo.foodmarketplace.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.CategoryRequest;
import com.uade.tpo.foodmarketplace.service.CategoryService;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("categories")
public class CategoriesController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ArrayList<CategoryRequest>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryRequest> getCategoryById(@PathVariable("categoryId") int categoryId) {
        Optional<CategoryRequest> category = categoryService.getCategoryById(categoryId);

        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("createCategory")
    public String createCategory(@RequestBody CategoryRequest categoryRequest) {
        return categoryService.createCategory(categoryRequest);
    }

}
