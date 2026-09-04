package com.uade.tpo.foodmarketplace.service.domicilio;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.domicilio.Domicilio;
import com.uade.tpo.foodmarketplace.entity.dto.domicilio.DomicilioUpdateRequest;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.common.ResourceInUseException;
import com.uade.tpo.foodmarketplace.exceptions.domicilio.DomicilioNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.domicilio.DomicilioRepository;
import com.uade.tpo.foodmarketplace.repository.order.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

@Service
public class DomicilioServiceImpl implements DomicilioService {

    @Autowired
    private DomicilioRepository domicilioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Domicilio> getDomicilios() {
        return domicilioRepository.findAll();
    }

    @Override
    public Optional<Domicilio> getDomicilioById(Long domicilioId) {
        return domicilioRepository.findById(domicilioId);
    }

    @Override
    public List<Domicilio> getDomiciliosByUsuarioId(Long usuarioId) {
        if (!userRepository.existsById(usuarioId)) {
            throw new UserNotFoundException();
        }

        return domicilioRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Domicilio createDomicilio(String calle, String numero, String piso, String departamento,
            String ciudad, String provincia, String codigoPostal, String indicacionesEntrega,
            Long usuarioId) {

        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(UserNotFoundException::new);

        Domicilio domicilio = new Domicilio();
        domicilio.setCalle(calle);
        domicilio.setNumero(numero);
        domicilio.setPiso(piso);
        domicilio.setDepartamento(departamento);
        domicilio.setCiudad(ciudad);
        domicilio.setProvincia(provincia);
        domicilio.setCodigoPostal(codigoPostal);
        domicilio.setIndicacionesEntrega(indicacionesEntrega);
        domicilio.setUsuario(usuario);

        return domicilioRepository.save(domicilio);
    }

    /**
     * Actualiza los campos de un domicilio existente conservando su propietario actual.
     */
    @Override
    public Domicilio updateDomicilio(Long domicilioId, DomicilioUpdateRequest request) {
        // Aquí no se recibe un usuario para impedir que un domicilio sea reasignado accidentalmente.
        Domicilio domicilio = domicilioRepository.findById(domicilioId).orElseThrow(DomicilioNotFoundException::new);
        domicilio.setCalle(request.getCalle());
        domicilio.setNumero(request.getNumero());
        domicilio.setPiso(request.getPiso());
        domicilio.setDepartamento(request.getDepartamento());
        domicilio.setCiudad(request.getCiudad());
        domicilio.setProvincia(request.getProvincia());
        domicilio.setCodigoPostal(request.getCodigoPostal());
        domicilio.setIndicacionesEntrega(request.getIndicacionesEntrega());

        return domicilioRepository.save(domicilio);
    }

    /**
     * Elimina un domicilio solo si no hay órdenes que lo requieran como historial.
     */
    @Override
    public void deleteDomicilio(Long domicilioId) {
        Domicilio domicilio = domicilioRepository.findById(domicilioId).orElseThrow(DomicilioNotFoundException::new);
        if (orderRepository.existsByDomicilioEntregaId(domicilioId)) {
            throw new ResourceInUseException("No se puede eliminar un domicilio asociado a un pedido");
        }

        domicilioRepository.delete(domicilio);
    }
}
