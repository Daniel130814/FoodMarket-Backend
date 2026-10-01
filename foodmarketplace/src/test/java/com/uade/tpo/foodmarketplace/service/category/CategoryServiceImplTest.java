package com.uade.tpo.foodmarketplace.service.category;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.uade.tpo.foodmarketplace.entity.category.Category;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryDuplicateException;
import com.uade.tpo.foodmarketplace.repository.category.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;

class CategoryServiceImplTest {
    private final CategoryRepository repository = mock(CategoryRepository.class);
    private final CategoryServiceImpl service = new CategoryServiceImpl();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "categoryRepository", repository);
        ReflectionTestUtils.setField(service, "platoRepository", mock(PlatoRepository.class));
    }

    @Test void altaNormalUsaFlushParaDetectarLaConstraint() {
        when(repository.saveAndFlush(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals("Bebidas", service.createCategory("Bebidas").getDescription());
        verify(repository).saveAndFlush(any(Category.class));
    }

    @Test void duplicadoCaseInsensitiveSeRechazaAntesDeGuardar() {
        when(repository.existsByDescriptionIgnoreCase("bebidas")).thenReturn(true);
        assertThrows(CategoryDuplicateException.class, () -> service.createCategory("bebidas"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test void constraintConocidaSeTraduceYDesconocidaSePropaga() {
        when(repository.saveAndFlush(any(Category.class))).thenThrow(violation("uk_category_description"));
        assertThrows(CategoryDuplicateException.class, () -> service.createCategory("Bebidas"));
        reset(repository);
        DataIntegrityViolationException unknown = violation("fk_otra_tabla");
        when(repository.saveAndFlush(any(Category.class))).thenThrow(unknown);
        assertSame(unknown, assertThrows(DataIntegrityViolationException.class,
                () -> service.createCategory("Bebidas")));
    }

    @Test void updateDuplicadoPreservaLaEntidadExistente() {
        Category category = new Category("Postres");
        when(repository.findById(2L)).thenReturn(Optional.of(category));
        when(repository.existsByDescriptionIgnoreCaseAndIdNot("Bebidas", 2L)).thenReturn(true);
        assertThrows(CategoryDuplicateException.class, () -> service.updateCategory(2L, "Bebidas"));
        assertEquals("Postres", category.getDescription());
    }

    private DataIntegrityViolationException violation(String name) {
        return new DataIntegrityViolationException("integrity",
                new ConstraintViolationException("duplicate", new SQLException(), "insert", name));
    }
}
