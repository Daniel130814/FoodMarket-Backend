package com.uade.tpo.foodmarketplace.repository;

import java.util.ArrayList;
import java.util.Optional;
import com.uade.tpo.foodmarketplace.entity.Category;
import java.util.Arrays;
import org.springframework.data.jpa.repository.JpaRepository

@Repository
public interface CategoryRepository extends JpaRepository<Category,
Long>{


}
