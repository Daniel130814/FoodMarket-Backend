package com.uade.tpo.foodmarketplace.repository;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.uade.tpo.foodmarketplace.entity.CategoryRequest;
import java.util.Arrays;

@Repository
public class CategoryRepository {
    public ArrayList<CategoryRequest> categories = new ArrayList<CategoryRequest>(
            Arrays.asList(CategoryRequest.builder().id(1).description("Electronica").build(),
                    CategoryRequest.builder().id(2).description("sillas").build(),
                    CategoryRequest.builder().id(3).description("colchones").build()));

    public ArrayList<CategoryRequest> getCategories() {
        return this.categories;
    }

    public Optional<CategoryRequest> getCategoryById(int categoryID) {
        for (CategoryRequest category : categories) {
            if (category.getId() == categoryID) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }

    public String createCategory(CategoryRequest categoryRequest) {
        int nextId = 0;

        for (CategoryRequest category : categories) {
            if (category.getId() > nextId) {
                nextId = category.getId();
            }
        }

        nextId++;
        categoryRequest.setId(nextId);
        categories.add(categoryRequest);
        return "Categoría creada correctamente";
    }
}
