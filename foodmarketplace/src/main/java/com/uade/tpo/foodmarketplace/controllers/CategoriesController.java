package com.uade.tpo.foodmarketplace.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.service.CategoryService;
import com.uade.tpo.foodmarketplace.model.Category;

import java.util.ArrayList;
import java.util.Locale.Category;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("categories")
public class CategoriesController {

    @GetMapping
    public ArrayList<Category> getCategories() {
        CategoryService categoryService = new CategoryService();
        return categoryService.getCategories();
    }

    @GetMapping("/{categoryId}")
    public String getCategoryByID(@PathVariable int categoryID) {
        CategoryService categoryService = new CategoryService();
        return categoryService.getCategoryById(categoryID);
    }

    @PostMapping("createCategory")
    public String createCategory(@RequestBody int categoryID) {
        CategoryService categoryService = new CategoryService();
        return categoryService.createCategory(categoryID);
    }

}
