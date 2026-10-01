package com.uade.tpo.foodmarketplace.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.uade.tpo.foodmarketplace.entity.category.Category;
import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.ingrediente.IngredienteDuplicateException;
import com.uade.tpo.foodmarketplace.repository.category.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;
import com.uade.tpo.foodmarketplace.service.category.CategoryService;
import com.uade.tpo.foodmarketplace.service.ingrediente.IngredienteService;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class UniqueConstraintsMySqlTest extends MySqlIntegrationTestBase {
    @Autowired CategoryService categories;
    @Autowired IngredienteService ingredientes;
    @Autowired CategoryRepository categoryRepository;
    @Autowired IngredienteRepository ingredienteRepository;

    @Test
    void categoryCreateSequentialCaseInsensitiveAndUpdateConflict() {
        String name = unique();
        Category a = categories.createCategory(name);
        assertNotNull(a.getId());
        assertThrows(CategoryDuplicateException.class, () -> categories.createCategory(name));
        assertThrows(CategoryDuplicateException.class, () -> categories.createCategory(name.toUpperCase()));
        String other = unique();
        Category b = categories.createCategory(other);
        assertThrows(CategoryDuplicateException.class, () -> categories.updateCategory(b.getId(), name));
        assertEquals(other, categoryRepository.findById(b.getId()).orElseThrow().getDescription());
    }

    @Test
    void ingredienteCreateSequentialCaseInsensitiveAndUpdateConflict() {
        String name = unique();
        Ingrediente a = ingredientes.createIngrediente(name, "test");
        assertNotNull(a.getId());
        assertThrows(IngredienteDuplicateException.class, () -> ingredientes.createIngrediente(name, "test"));
        assertThrows(IngredienteDuplicateException.class, () -> ingredientes.createIngrediente(name.toUpperCase(), "test"));
        String other = unique();
        Ingrediente b = ingredientes.createIngrediente(other, "test");
        assertThrows(IngredienteDuplicateException.class, () -> ingredientes.updateIngrediente(b.getId(), name, "test"));
        assertEquals(other, ingredienteRepository.findById(b.getId()).orElseThrow().getNombre());
    }

    @Test
    void categoryConcurrentInsertKeepsOneRow() throws Exception {
        String name = unique();
        Result result = race(() -> categories.createCategory(name), () -> categories.createCategory(name.toUpperCase()));
        assertEquals(1, result.successes(), result.toString());
        assertEquals(1, result.failuresOf(CategoryDuplicateException.class), result.toString());
        assertEquals(1, categoryRepository.findAll().stream()
                .filter(c -> c.getDescription().equalsIgnoreCase(name)).count());
    }

    @Test
    void ingredienteConcurrentInsertKeepsOneRow() throws Exception {
        String name = unique();
        Result result = race(() -> ingredientes.createIngrediente(name, "test"),
                () -> ingredientes.createIngrediente(name.toUpperCase(), "test"));
        assertEquals(1, result.successes(), result.toString());
        assertEquals(1, result.failuresOf(IngredienteDuplicateException.class), result.toString());
        assertEquals(1, ingredienteRepository.findAll().stream()
                .filter(i -> i.getNombre().equalsIgnoreCase(name)).count());
    }

    private String unique() { return "item" + UUID.randomUUID().toString().replace("-", ""); }

    private Result race(Callable<?> first, Callable<?> second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Throwable> a = pool.submit(() -> attempt(first, ready, start));
            Future<Throwable> b = pool.submit(() -> attempt(second, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            return new Result(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private Throwable attempt(Callable<?> action, CountDownLatch ready, CountDownLatch start) {
        try {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("start timed out");
            action.call();
            return null;
        } catch (Throwable failure) {
            return failure;
        }
    }

    private record Result(Throwable first, Throwable second) {
        int successes() { return (first == null ? 1 : 0) + (second == null ? 1 : 0); }
        int failuresOf(Class<? extends Throwable> type) {
            return (type.isInstance(first) ? 1 : 0) + (type.isInstance(second) ? 1 : 0);
        }
    }
}
