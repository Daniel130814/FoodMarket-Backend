package com.uade.tpo.foodmarketplace.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;
import com.uade.tpo.foodmarketplace.service.ingrediente.IngredienteResolver;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class IngredientePorNombreMySqlTest extends MySqlIntegrationTestBase {
    @Autowired IngredienteResolver resolver;
    @Autowired IngredienteRepository repository;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void creaElIngredienteConElNombreDelChefYLoReutilizaSinImportarFormato() {
        String base = "Pimentón" + UUID.randomUUID().toString().substring(0, 8);
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        Ingrediente creado = tx.execute(s -> resolver.obtenerOCrearPorNombre("  " + base + "  "));
        assertEquals(base, creado.getNombre());

        Long mismoId = tx.execute(s -> resolver.obtenerOCrearPorNombre(
                base.toUpperCase().replace("Ó", "O") + "   "))
                .getId();
        assertEquals(creado.getId(), mismoId);
        assertEquals(1, repository.findAll().stream()
                .filter(i -> Ingrediente.normalizar(base).equals(i.getNombreNormalizado())).count());
    }

    @Test
    void siLaTransaccionFallaElIngredienteNuevoNoQuedaCreado() {
        String nombre = "Rollback" + UUID.randomUUID().toString().substring(0, 8);
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThrows(IllegalStateException.class, () -> tx.executeWithoutResult(s -> {
            resolver.obtenerOCrearPorNombre(nombre);
            throw new IllegalStateException("plato invalido");
        }));

        assertFalse(repository.findByNombreNormalizado(Ingrediente.normalizar(nombre)).isPresent());
    }

    @Test
    void dosCreacionesConcurrentesDelMismoNombreResuelvenAlMismoIngrediente() throws Exception {
        String nombre = "Carrera" + UUID.randomUUID().toString().substring(0, 8);
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        CountDownLatch listos = new CountDownLatch(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Long> a = pool.submit(() -> {
                listos.countDown();
                listos.await();
                return tx.execute(s -> resolver.obtenerOCrearPorNombre(nombre)).getId();
            });
            Future<Long> b = pool.submit(() -> {
                listos.countDown();
                listos.await();
                return tx.execute(s -> resolver.obtenerOCrearPorNombre(nombre.toUpperCase())).getId();
            });
            assertEquals(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
        assertTrue(repository.findByNombreNormalizado(Ingrediente.normalizar(nombre)).isPresent());
    }
}
