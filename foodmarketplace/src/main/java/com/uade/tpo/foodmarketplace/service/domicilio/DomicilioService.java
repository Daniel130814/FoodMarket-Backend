package com.uade.tpo.foodmarketplace.service.domicilio;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.domicilio.Domicilio;

public interface DomicilioService {

    List<Domicilio> getDomicilios();

    Optional<Domicilio> getDomicilioById(Long domicilioId);

    List<Domicilio> getDomiciliosByUsuarioId(Long usuarioId);

    Domicilio createDomicilio(String calle, String numero, String piso, String departamento,
            String ciudad, String provincia, String codigoPostal, String indicacionesEntrega,
            Long usuarioId);
}
