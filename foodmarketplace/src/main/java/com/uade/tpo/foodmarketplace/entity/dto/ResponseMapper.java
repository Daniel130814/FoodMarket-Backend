package com.uade.tpo.foodmarketplace.entity.dto;

import java.math.BigDecimal;

import com.uade.tpo.foodmarketplace.entity.Category;
import com.uade.tpo.foodmarketplace.entity.ChefProfile;
import com.uade.tpo.foodmarketplace.entity.DetallePedido;
import com.uade.tpo.foodmarketplace.entity.Order;
import com.uade.tpo.foodmarketplace.entity.Pago;
import com.uade.tpo.foodmarketplace.entity.Plato;
import com.uade.tpo.foodmarketplace.entity.Resena;
import com.uade.tpo.foodmarketplace.entity.SubPedidoChef;

public final class ResponseMapper {

    private ResponseMapper() {
    }

    public static PlatoResponse plato(Plato plato) {
        return new PlatoResponse(plato.getId(), plato.getNombre(), plato.getDescripcion(), plato.getImagenUrl(), plato.getPrecio(),
                plato.getStockDisponible(), plato.getEstado(), plato.getChef().getId(),
                plato.getCategorias().stream().map(Category::getDescription).toList(),
                plato.getIngredientes().stream().map(i -> new IngredientePlatoResponse(i.getIngrediente().getId(),
                        i.getIngrediente().getNombre(), i.getCantidad(), i.getUnidadMedida())).toList());
    }
    public static DetallePedidoResponse detalle(DetallePedido detalle) {
        return new DetallePedidoResponse(detalle.getId(), detalle.getPlato().getId(), detalle.getPlato().getNombre(),
                detalle.getCantidad(), detalle.getPrecioUnitario(), detalle.getSubtotal());
    }

    public static SubPedidoChefResponse subPedido(SubPedidoChef subPedido) {
        return new SubPedidoChefResponse(subPedido.getId(), subPedido.getChef().getId(), subPedido.getEstado(),
                subPedido.getSubtotal(), subPedido.getDetalles().stream().map(ResponseMapper::detalle).toList());
    }

    public static OrderResponse order(Order order) {
        return new OrderResponse(order.getId(), order.getUser().getId(), order.getDomicilioEntrega().getId(), order.getFechaCreacion(),
                order.getEstado(), order.getPrecioFinal(), order.isPagoBloqueado(),
                order.getSubPedidos().stream().map(ResponseMapper::subPedido).toList());
    }

    public static PagoResponse pago(Pago pago) {
        return new PagoResponse(pago.getId(), pago.getPedido().getId(), pago.getMonto(), pago.getMedioPago(), pago.getEstado(),
                pago.getFechaCreacion(), pago.getFechaPago());
    }

    public static ResenaResponse resena(Resena resena) {
        return new ResenaResponse(resena.getId(), resena.getCalificacion(), resena.getComentario(), resena.getFechaCreacion(),
                resena.getCliente().getId(), resena.getPlato().getId());
    }

    public static ChefProfileResponse chefProfile(ChefProfile profile, BigDecimal reputacion) {
        return new ChefProfileResponse(profile.getId(), profile.getUser().getId(), profile.getBiografia(), profile.getEspecialidad(),
                profile.getFotoUrl(), profile.getDescripcion(), reputacion);
    }
}
