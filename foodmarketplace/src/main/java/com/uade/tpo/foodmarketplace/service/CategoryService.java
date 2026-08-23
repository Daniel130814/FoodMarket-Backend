package com.uade.tpo.foodmarketplace.service;

import java.util.ArrayList;
import java.util.Locale.Category;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.uade.tpo.foodmarketplace.model.Category;
import com.uade.tpo.foodmarketplace.repository.CategoryRepository;

public class CategoryService {

    public ArrayList<Category> getCategories() {
        CategoryRepository categoryRepository = new CategoryRepository();
        return categoryRepository.getCategories();
    }

    public String getCategoryById(int categoryId) {
        return new String();
    }

    public String createCategory(int entity) {
        // metodo
        return new String();
    }
}
