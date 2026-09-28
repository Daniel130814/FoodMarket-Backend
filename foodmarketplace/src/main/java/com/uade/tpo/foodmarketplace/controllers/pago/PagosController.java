package com.uade.tpo.foodmarketplace.controllers.pago;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.dto.pago.EstadoPagoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.pago.PagoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.pago.PagoResponse;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;
import com.uade.tpo.foodmarketplace.exceptions.pago.PagoNotFoundException;
import com.uade.tpo.foodmarketplace.service.pago.PagoService;

@RestController
@RequestMapping("pagos")
public class PagosController {

    @Autowired
    private PagoService pagoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PagoResponse>>> getPagos() {
        List<PagoResponse> data=pagoService.getPagos().stream().map(ResponseMapper::pago).toList();
        return ResponseEntity.ok(ApiResponse.ok(data.isEmpty()?"No hay pagos registrados para mostrar.":"Pagos obtenidos correctamente", data.isEmpty()?null:data));
    }

    @GetMapping("/{pagoId}")
    public ResponseEntity<ApiResponse<PagoResponse>> getPagoById(@PathVariable("pagoId") Long pagoId) {
        return ResponseEntity.ok(ApiResponse.ok("Pago obtenido correctamente",pagoService.getPagoById(pagoId).map(ResponseMapper::pago).orElseThrow(PagoNotFoundException::new)));
    }

    @PostMapping("createPago")
    public ResponseEntity<ApiResponse<PagoResponse>> createPago(@Valid @RequestBody PagoRequest pagoRequest) {
        var result = pagoService.createPago(
                pagoRequest.getMedioPago(),
                pagoRequest.getPedidoId());

        return ResponseEntity
                .created(URI.create("/pagos/" + result.getId()))
                .body(ApiResponse.ok("Pago creado correctamente",ResponseMapper.pago(result)));
    }

    @PatchMapping("/{pagoId}/estado")
    public ResponseEntity<ApiResponse<PagoResponse>> actualizarEstadoPago(@PathVariable("pagoId") Long pagoId,
            @Valid @RequestBody EstadoPagoRequest estadoPagoRequest) {
        return ResponseEntity.ok(ApiResponse.ok("Pago actualizado correctamente",ResponseMapper.pago(pagoService.actualizarEstadoPago(pagoId, estadoPagoRequest.getEstado()))));
    }
}
