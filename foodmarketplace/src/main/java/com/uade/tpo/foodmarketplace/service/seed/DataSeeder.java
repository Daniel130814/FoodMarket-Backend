package com.uade.tpo.foodmarketplace.service.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.category.Category;
import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.repository.category.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

/**
 * Carga datos iniciales (ADMIN, categorías e ingredientes base) solo cuando faltan, así que es seguro
 * en cada arranque. Se apaga con app.seed.enabled=false. Las credenciales del ADMIN salen únicamente de
 * las variables de entorno ADMIN_USERNAME y ADMIN_PASSWORD (ADMIN_EMAIL es opcional).
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    static final List<String> CATEGORIAS = List.of("Entradas", "Platos principales", "Pastas", "Carnes",
            "Vegetarianos", "Postres", "Bebidas");

    static final List<String> INGREDIENTES = List.of("Sal", "Pimienta", "Aceite de oliva", "Ajo", "Cebolla",
            "Tomate", "Papa", "Zanahoria", "Morrón", "Perejil", "Albahaca", "Orégano", "Harina", "Huevo", "Leche",
            "Manteca", "Queso mozzarella", "Queso parmesano", "Crema de leche", "Azúcar", "Arroz", "Fideos",
            "Carne vacuna", "Pollo", "Cerdo", "Pescado", "Jamón", "Panceta", "Champiñones", "Espinaca", "Limón",
            "Chocolate", "Dulce de leche", "Vainilla", "Pan rallado");

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredienteRepository ingredienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminEmail;

    public DataSeeder(UserRepository userRepository, CategoryRepository categoryRepository,
            IngredienteRepository ingredienteRepository, PasswordEncoder passwordEncoder,
            @Value("${ADMIN_USERNAME:}") String adminUsername,
            @Value("${ADMIN_PASSWORD:}") String adminPassword,
            @Value("${ADMIN_EMAIL:}") String adminEmail) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminEmail = adminEmail;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        sembrarAdmin();
        sembrarCategorias();
        sembrarIngredientes();
    }

    void sembrarAdmin() {
        if (adminUsername.isBlank() || adminPassword.isBlank()) {
            log.info("Seed: ADMIN_USERNAME/ADMIN_PASSWORD no definidas; no se crea el usuario ADMIN inicial");
            return;
        }
        String username = adminUsername.trim().toLowerCase(java.util.Locale.ROOT);
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        String email = adminEmail.isBlank() ? username + "@appetitos.local"
                : adminEmail.trim().toLowerCase(java.util.Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.warn("Seed: el email del ADMIN inicial ya está en uso por otro usuario; no se crea");
            return;
        }
        User admin = new User();
        admin.setUsername(username);
        admin.setNombre("Admin");
        admin.setApellido("Appetitos");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Seed: usuario ADMIN '{}' creado", username);
    }

    void sembrarCategorias() {
        for (String descripcion : CATEGORIAS) {
            if (!categoryRepository.existsByDescriptionIgnoreCase(descripcion)) {
                categoryRepository.save(new Category(descripcion));
            }
        }
    }

    void sembrarIngredientes() {
        for (String nombre : INGREDIENTES) {
            if (ingredienteRepository.findByNombreNormalizado(Ingrediente.normalizar(nombre)).isEmpty()) {
                Ingrediente ingrediente = new Ingrediente();
                ingrediente.setNombre(nombre);
                ingredienteRepository.save(ingrediente);
            }
        }
    }
}
