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
import com.uade.tpo.foodmarketplace.repository.domicilio.DomicilioRepository;
import com.uade.tpo.foodmarketplace.repository.order.OrderRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@Service
public class DomicilioServiceImpl implements DomicilioService {

    @Autowired
    private DomicilioRepository domicilioRepository;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Domicilio> getDomicilios() {
        User currentUser = authenticatedUserService.getCurrentUser();
        return authenticatedUserService.isAdmin(currentUser)
                ? domicilioRepository.findAll()
                : domicilioRepository.findByUsuarioId(currentUser.getId());
    }

    @Override
    public Optional<Domicilio> getDomicilioById(Long domicilioId) {
        User currentUser = authenticatedUserService.getCurrentUser();
        Optional<Domicilio> domicilio = domicilioRepository.findById(domicilioId);
        domicilio.ifPresent(value -> authenticatedUserService.requireOwnerOrAdmin(
                currentUser, value.getUsuario().getId()));
        return domicilio;
    }

    @Override
    public List<Domicilio> getDomiciliosByUsuarioId(Long usuarioId) {
        User currentUser = authenticatedUserService.getCurrentUser();
        authenticatedUserService.requireOwnerOrAdmin(currentUser, usuarioId);
        return domicilioRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Domicilio createDomicilio(String calle, String numero, String piso, String departamento,
            String ciudad, String provincia, String codigoPostal, String indicacionesEntrega) {

        User usuario = authenticatedUserService.getCurrentUser();

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

        Domicilio domicilio = domicilioRepository.findById(domicilioId)
                .orElseThrow(DomicilioNotFoundException::new);

        authenticatedUserService.requireOwnerOrAdmin(
                authenticatedUserService.getCurrentUser(),
                domicilio.getUsuario().getId()
        );

        if (orderRepository.existsByDomicilioEntregaId(domicilioId)) {
            throw new ResourceInUseException(
                    "No se puede modificar un domicilio asociado a un pedido. Cree un nuevo domicilio."
            );
        }

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
        authenticatedUserService.requireOwnerOrAdmin(authenticatedUserService.getCurrentUser(),
                domicilio.getUsuario().getId());
        if (orderRepository.existsByDomicilioEntregaId(domicilioId)) {
            throw new ResourceInUseException("No se puede eliminar un domicilio asociado a un pedido");
        }

        domicilioRepository.delete(domicilio);
    }
}
