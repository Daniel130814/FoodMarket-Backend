package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.DetallePedido;
import com.uade.tpo.foodmarketplace.entity.Order;
import com.uade.tpo.foodmarketplace.entity.WeeklyMenu;
import com.uade.tpo.foodmarketplace.exceptions.CantidadInvalidaException;
import com.uade.tpo.foodmarketplace.exceptions.PedidoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.WeeklyMenuNotFoundException;
import com.uade.tpo.foodmarketplace.repository.DetallePedidoRepository;
import com.uade.tpo.foodmarketplace.repository.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.WeeklyMenuRepository;

@Service
public class DetallePedidoServiceImpl implements DetallePedidoService {

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WeeklyMenuRepository weeklyMenuRepository;

    @Override
    public List<DetallePedido> getDetallesPedido() {
        return detallePedidoRepository.findAll();
    }

    @Override
    public Optional<DetallePedido> getDetallePedidoById(Long detallePedidoId) {
        return detallePedidoRepository.findById(detallePedidoId);
    }

    @Override
    public List<DetallePedido> getDetallesPedidoByPedidoId(Long pedidoId) {
        if (!orderRepository.existsById(pedidoId)) {
            throw new PedidoNotFoundException();
        }

        return detallePedidoRepository.findByPedidoId(pedidoId);
    }

    @Override
    public DetallePedido createDetallePedido(Integer cantidad, Long pedidoId, Long menuSemanalId) {
        if (cantidad == null || cantidad <= 0) {
            throw new CantidadInvalidaException();
        }

        Order pedido = orderRepository.findById(pedidoId)
                .orElseThrow(PedidoNotFoundException::new);

        WeeklyMenu menuSemanal = weeklyMenuRepository.findById(menuSemanalId)
                .orElseThrow(WeeklyMenuNotFoundException::new);

        DetallePedido detallePedido = new DetallePedido();
        detallePedido.setCantidad(cantidad);
        detallePedido.setPrecioUnitario(menuSemanal.getPrecio());
        detallePedido.setSubtotal(menuSemanal.getPrecio() * cantidad);
        detallePedido.setPedido(pedido);
        detallePedido.setMenuSemanal(menuSemanal);

        return detallePedidoRepository.save(detallePedido);
    }
}
