package com.uade.tpo.foodmarketplace.service.carrito;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.carrito.Carrito;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.repository.carrito.CarritoRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

/**
 * Aísla el único intento de creación del carrito para poder recuperar una carrera de UNIQUE sin contaminar la transacción llamadora.
 */
@Service
public class CarritoCreationService {

    private final CarritoRepository carritoRepository;
    private final UserRepository userRepository;

    public CarritoCreationService(CarritoRepository carritoRepository, UserRepository userRepository) {
        this.carritoRepository = carritoRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void crearCarrito(Long clienteId) {
        Carrito carrito = new Carrito();
        User cliente = userRepository.getReferenceById(clienteId);
        carrito.setCliente(cliente);
        carritoRepository.saveAndFlush(carrito);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Optional<Carrito> buscarCarritoCreado(Long clienteId) {
        return carritoRepository.findByClienteId(clienteId);
    }
}
