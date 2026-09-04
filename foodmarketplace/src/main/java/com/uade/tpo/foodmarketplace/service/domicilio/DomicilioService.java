package com.uade.tpo.foodmarketplace.service.domicilio;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.domicilio.Domicilio;
import com.uade.tpo.foodmarketplace.entity.dto.domicilio.DomicilioUpdateRequest;

public interface DomicilioService {

    List<Domicilio> getDomicilios();

    Optional<Domicilio> getDomicilioById(Long domicilioId);

    List<Domicilio> getDomiciliosByUsuarioId(Long usuarioId);

    Domicilio createDomicilio(String calle, String numero, String piso, String departamento,
            String ciudad, String provincia, String codigoPostal, String indicacionesEntrega,
            Long usuarioId);

    /**
     * Updates address fields without changing the user that owns the address.
     */
    Domicilio updateDomicilio(Long domicilioId, DomicilioUpdateRequest request);

    /**
     * Deletes an address when it has not been used by an order.
     */
    void deleteDomicilio(Long domicilioId);
}
