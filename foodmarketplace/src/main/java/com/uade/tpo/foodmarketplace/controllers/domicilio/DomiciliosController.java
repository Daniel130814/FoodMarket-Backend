package com.uade.tpo.foodmarketplace.controllers.domicilio;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uade.tpo.foodmarketplace.entity.domicilio.Domicilio;
import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;
import com.uade.tpo.foodmarketplace.entity.dto.domicilio.*;
import com.uade.tpo.foodmarketplace.exceptions.domicilio.DomicilioNotFoundException;
import com.uade.tpo.foodmarketplace.service.domicilio.DomicilioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("domicilios")
public class DomiciliosController {
    private final DomicilioService domicilioService;
    public DomiciliosController(DomicilioService domicilioService) { this.domicilioService = domicilioService; }
    @GetMapping public ResponseEntity<ApiResponse<List<DomicilioResponse>>> getDomicilios() { return lista(domicilioService.getDomicilios()); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<DomicilioResponse>> getDomicilioById(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok("Domicilio obtenido correctamente", map(domicilioService.getDomicilioById(id).orElseThrow(DomicilioNotFoundException::new)))); }
    @GetMapping("/usuario/{id}") public ResponseEntity<ApiResponse<List<DomicilioResponse>>> getDomiciliosByUsuarioId(@PathVariable Long id) { return lista(domicilioService.getDomiciliosByUsuarioId(id)); }
    @PostMapping("createDomicilio") public ResponseEntity<ApiResponse<DomicilioResponse>> createDomicilio(@Valid @RequestBody DomicilioRequest r) { Domicilio d=domicilioService.createDomicilio(r.getCalle(),r.getNumero(),r.getPiso(),r.getDepartamento(),r.getCiudad(),r.getProvincia(),r.getCodigoPostal(),r.getIndicacionesEntrega()); return ResponseEntity.created(URI.create("/domicilios/"+d.getId())).body(ApiResponse.ok("Domicilio creado correctamente",map(d))); }
    @PutMapping("/{id}") public ResponseEntity<ApiResponse<DomicilioResponse>> updateDomicilio(@PathVariable Long id,@Valid @RequestBody DomicilioUpdateRequest r) { return ResponseEntity.ok(ApiResponse.ok("Domicilio actualizado correctamente",map(domicilioService.updateDomicilio(id,r)))); }
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> deleteDomicilio(@PathVariable Long id) { domicilioService.deleteDomicilio(id); return ResponseEntity.ok(ApiResponse.ok("Domicilio eliminado correctamente",null)); }
    private ResponseEntity<ApiResponse<List<DomicilioResponse>>> lista(List<Domicilio> ds) { List<DomicilioResponse> data=ds.stream().map(this::map).toList(); return ResponseEntity.ok(ApiResponse.ok(data.isEmpty()?"No tenés domicilios registrados.":"Domicilios obtenidos correctamente",data.isEmpty()?null:data)); }
    private DomicilioResponse map(Domicilio d) { return new DomicilioResponse(d.getId(),d.getCalle(),d.getNumero(),d.getPiso(),d.getDepartamento(),d.getCiudad(),d.getProvincia(),d.getCodigoPostal(),d.getIndicacionesEntrega()); }
}
