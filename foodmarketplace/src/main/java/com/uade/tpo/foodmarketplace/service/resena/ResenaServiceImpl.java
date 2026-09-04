package com.uade.tpo.foodmarketplace.service.resena;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.resena.Resena;
import com.uade.tpo.foodmarketplace.entity.dto.resena.ResenaUpdateRequest;
import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.resena.CalificacionInvalidaException;
import com.uade.tpo.foodmarketplace.exceptions.plato.PlatoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.resena.ResenaDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.resena.ResenaNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.repository.order.DetallePedidoRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.resena.ResenaRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

@Service
public class ResenaServiceImpl implements ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatoRepository platoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Override
    public List<Resena> getResenas() {
        return resenaRepository.findAll();
    }

    @Override
    public Optional<Resena> getResenaById(Long resenaId) {
        return resenaRepository.findById(resenaId);
    }

    @Override
    public List<Resena> getResenasByPlatoId(Long platoId) {
        if (!platoRepository.existsById(platoId)) {
            throw new PlatoNotFoundException();
        }

        return resenaRepository.findByPlatoId(platoId);
    }

    @Override
    public Resena createResena(Integer calificacion, String comentario, Long clienteId, Long platoId) {
        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new CalificacionInvalidaException();
        }

        User cliente = userRepository.findById(clienteId)
                .orElseThrow(UserNotFoundException::new);

        Plato plato = platoRepository.findById(platoId)
                .orElseThrow(PlatoNotFoundException::new);

        if (resenaRepository.existsByClienteIdAndPlatoId(clienteId, platoId)) {
            throw new ResenaDuplicateException();
        }
        if (!detallePedidoRepository.existsBySubPedidoChefPedidoUserIdAndPlatoIdAndSubPedidoChefEstado(
                clienteId, platoId, EstadoPedido.ENTREGADO)) {
            throw new BusinessRuleException("Solo puede resenar un plato comprado en un subpedido entregado");
        }

        Resena resena = new Resena();
        resena.setCalificacion(calificacion);
        resena.setComentario(comentario);
        resena.setFechaCreacion(LocalDateTime.now());
        resena.setCliente(cliente);
        resena.setPlato(plato);

        return resenaRepository.save(resena);
    }

    /**
     * Actualiza una reseña sin repetir la validación de compra requerida al crearla.
     */
    @Override
    public Resena updateResena(Long resenaId, ResenaUpdateRequest request) {
        if (request.getCalificacion() < 1 || request.getCalificacion() > 5) {
            throw new CalificacionInvalidaException();
        }

        // La reseña existente conserva su cliente, plato y fecha de creación originales.
        Resena resena = resenaRepository.findById(resenaId).orElseThrow(ResenaNotFoundException::new);
        resena.setCalificacion(request.getCalificacion());
        resena.setComentario(request.getComentario());
        return resenaRepository.save(resena);
    }

    /**
     * Elimina una reseña después de confirmar que existe.
     */
    @Override
    public void deleteResena(Long resenaId) {
        Resena resena = resenaRepository.findById(resenaId).orElseThrow(ResenaNotFoundException::new);
        resenaRepository.delete(resena);
    }
}
