package com.uade.tpo.foodmarketplace.controllers.category;
import java.net.URI; import java.util.List;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
import com.uade.tpo.foodmarketplace.entity.category.Category; import com.uade.tpo.foodmarketplace.entity.dto.category.CategoryRequest; import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse; import com.uade.tpo.foodmarketplace.exceptions.category.CategoryNotFoundException; import com.uade.tpo.foodmarketplace.service.category.CategoryService; import jakarta.validation.Valid;
@RestController @RequestMapping("categories") public class CategoriesController {
 private final CategoryService service; public CategoriesController(CategoryService service){this.service=service;}
 @GetMapping public ResponseEntity<ApiResponse<List<Category>>> getCategories(){List<Category>d=service.getCategories();return ResponseEntity.ok(ApiResponse.ok(d.isEmpty()?"No hay categorías disponibles":"Categorías obtenidas correctamente",d.isEmpty()?null:d));}
 @GetMapping("/{id}") public ResponseEntity<ApiResponse<Category>> get(@PathVariable Long id){return ResponseEntity.ok(ApiResponse.ok("Categoría obtenida correctamente",service.getCategoryById(id).orElseThrow(CategoryNotFoundException::new)));}
 @PostMapping("createCategory") public ResponseEntity<ApiResponse<Category>> create(@Valid @RequestBody CategoryRequest r){Category c=service.createCategory(r.getDescription());return ResponseEntity.created(URI.create("/categories/"+c.getId())).body(ApiResponse.ok("Categoría creada correctamente",c));}
 @PutMapping("/{id}") public ResponseEntity<ApiResponse<Category>> update(@PathVariable Long id,@Valid @RequestBody CategoryRequest r){return ResponseEntity.ok(ApiResponse.ok("Categoría actualizada correctamente",service.updateCategory(id,r.getDescription())));}
 @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){service.deleteCategory(id);return ResponseEntity.ok(ApiResponse.ok("Categoría eliminada correctamente",null));}
}
