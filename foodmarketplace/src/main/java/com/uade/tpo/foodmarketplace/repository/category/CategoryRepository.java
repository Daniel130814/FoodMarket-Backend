package com.uade.tpo.foodmarketplace.repository.category;
import com.uade.tpo.foodmarketplace.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long>{
    boolean existsByDescriptionIgnoreCase(String description);

    /**
     * Comprueba si otra categoría ya utiliza la descripción indicada.
     */
    boolean existsByDescriptionIgnoreCaseAndIdNot(String description, Long id);
}
