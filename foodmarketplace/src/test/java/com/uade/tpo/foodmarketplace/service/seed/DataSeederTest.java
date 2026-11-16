package com.uade.tpo.foodmarketplace.service.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uade.tpo.foodmarketplace.entity.category.Category;
import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.repository.category.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private IngredienteRepository ingredienteRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private DataSeeder seeder(String username, String password, String email) {
        return new DataSeeder(userRepository, categoryRepository, ingredienteRepository, passwordEncoder,
                username, password, email);
    }

    @BeforeEach
    void setUp() {
        // Nada que preparar: cada test stubbea solo lo que usa.
    }

    @Test
    void sinVariablesDeEntornoNoCreaAdmin() {
        seeder("", "", "").sembrarAdmin();

        verify(userRepository, never()).save(any());
    }

    @Test
    void creaElAdminConPasswordCodificadaYRolAdmin() {
        when(userRepository.existsByUsernameIgnoreCase("admin")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("admin@appetitos.local")).thenReturn(false);
        when(passwordEncoder.encode("secreto123")).thenReturn("HASH");

        seeder(" Admin ", "secreto123", "").sembrarAdmin();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("admin", captor.getValue().getUsername());
        assertEquals("HASH", captor.getValue().getPassword());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
    }

    @Test
    void noDuplicaElAdminSiYaExiste() {
        when(userRepository.existsByUsernameIgnoreCase("admin")).thenReturn(true);

        seeder("admin", "secreto123", "").sembrarAdmin();

        verify(userRepository, never()).save(any());
    }

    @Test
    void soloCreaLasCategoriasQueFaltan() {
        when(categoryRepository.existsByDescriptionIgnoreCase(any())).thenReturn(true);
        when(categoryRepository.existsByDescriptionIgnoreCase("Postres")).thenReturn(false);

        seeder("", "", "").sembrarCategorias();

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(captor.capture());
        assertEquals("Postres", captor.getValue().getDescription());
    }

    @Test
    void soloCreaLosIngredientesQueFaltan() {
        when(ingredienteRepository.findByNombreNormalizado(any())).thenReturn(Optional.of(new Ingrediente()));
        when(ingredienteRepository.findByNombreNormalizado("sal")).thenReturn(Optional.empty());

        seeder("", "", "").sembrarIngredientes();

        ArgumentCaptor<Ingrediente> captor = ArgumentCaptor.forClass(Ingrediente.class);
        verify(ingredienteRepository, times(1)).save(captor.capture());
        assertEquals("Sal", captor.getValue().getNombre());
    }
}
