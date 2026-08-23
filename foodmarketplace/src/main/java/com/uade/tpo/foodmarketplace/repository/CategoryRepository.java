package com.uade.tpo.foodmarketplace.repository;

import java.util.ArrayList;
import com.uade.tpo.foodmarketplace.entity.CategoryRequest;
import java.util.Arrays;

public class CategoryRepository {
    public ArrayList<CategoryRequest> categories = new ArrayList<CategoryRequest>(
            Arrays.asList(CategoryRequest.builder().id(1).description("Electronica").build(),
                    CategoryRequest.builder().id(2).description("sillas").build(),
                    CategoryRequest.builder().id(3).description("colchones").build()));

    public ArrayList<CategoryRequest> getCategories() {
        return this.categories;
    }

    public String getCategoryById(int categoryID) {
        return null;
    }

    public String createCategory(int entity) {
        return null;
    }
}
