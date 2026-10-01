package com.uade.tpo.foodmarketplace.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.uade.tpo.foodmarketplace.entity.dto.user.UserUpdateRequest;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

class UserUniqueConstraintTest {
    @Test
    void soloConstraintsExactasDeUserSeTraducen() {
        assertInstanceOf(UserDuplicateException.class,
                UserUniqueConstraint.translate(violation("user.uk_user_email")));
        assertInstanceOf(UserDuplicateException.class,
                UserUniqueConstraint.translate(violation("`user`.`uk_user_username`")));
        DataIntegrityViolationException unknown = violation("user.fk_otro_nombre");
        assertSame(unknown, UserUniqueConstraint.translate(unknown));
    }

    @Test
    void updateNormalizaEmailAntesDeBuscarYGuardar() {
        UserRepository repository = mock(UserRepository.class);
        UserServiceImpl service = new UserServiceImpl();
        ReflectionTestUtils.setField(service, "userRepository", repository);
        User user = new User();
        user.setId(3L);
        when(repository.findById(3L)).thenReturn(Optional.of(user));
        when(repository.saveAndFlush(user)).thenReturn(user);
        UserUpdateRequest request = new UserUpdateRequest();
        request.setNombre("Ana");
        request.setApellido("Perez");
        request.setEmail("  ANA@MAIL.COM  ");

        service.updateUser(3L, request);

        verify(repository).existsByEmailIgnoreCaseAndIdNot("ana@mail.com", 3L);
        assertEquals("ana@mail.com", user.getEmail());
        verify(repository).saveAndFlush(user);
    }

    private DataIntegrityViolationException violation(String name) {
        return new DataIntegrityViolationException("integrity",
                new ConstraintViolationException("duplicate", new SQLException(), "insert", name));
    }
}
