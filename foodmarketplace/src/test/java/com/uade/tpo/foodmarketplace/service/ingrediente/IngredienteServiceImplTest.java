package com.uade.tpo.foodmarketplace.service.ingrediente;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.exceptions.ingrediente.IngredienteDuplicateException;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;

class IngredienteServiceImplTest {
    private final IngredienteRepository repository = mock(IngredienteRepository.class);
    private final IngredienteServiceImpl service = new IngredienteServiceImpl();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "ingredienteRepository", repository);
    }

    @Test void altaNormalUsaFlush() {
        when(repository.saveAndFlush(any(Ingrediente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals("Tomate", service.createIngrediente("Tomate", "Rojo").getNombre());
    }

    @Test void duplicadoCaseInsensitiveSeRechazaAntesDeGuardar() {
        when(repository.existsByNombreIgnoreCase("tomate")).thenReturn(true);
        assertThrows(IngredienteDuplicateException.class, () -> service.createIngrediente("tomate", "Rojo"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test void constraintConocidaSeTraduceYDesconocidaSePropaga() {
        when(repository.saveAndFlush(any(Ingrediente.class))).thenThrow(violation("uk_ingrediente_nombre"));
        assertThrows(IngredienteDuplicateException.class, () -> service.createIngrediente("Tomate", "Rojo"));
        reset(repository);
        DataIntegrityViolationException unknown = violation("fk_otra_tabla");
        when(repository.saveAndFlush(any(Ingrediente.class))).thenThrow(unknown);
        assertSame(unknown, assertThrows(DataIntegrityViolationException.class,
                () -> service.createIngrediente("Tomate", "Rojo")));
    }

    @Test void updateDuplicadoPreservaLaEntidadExistente() {
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setNombre("Cebolla");
        when(repository.findById(2L)).thenReturn(Optional.of(ingrediente));
        when(repository.existsByNombreIgnoreCaseAndIdNot("Tomate", 2L)).thenReturn(true);
        assertThrows(IngredienteDuplicateException.class,
                () -> service.updateIngrediente(2L, "Tomate", "Rojo"));
        assertEquals("Cebolla", ingrediente.getNombre());
    }

    private DataIntegrityViolationException violation(String name) {
        return new DataIntegrityViolationException("integrity",
                new ConstraintViolationException("duplicate", new SQLException(), "insert", name));
    }
}
