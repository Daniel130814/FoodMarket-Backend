1) En `OrderServiceImpl`, método `cancelarOrder()`:

Antes se podía cancelar un pedido aunque ya estuviera confirmado, en preparación o enviado, siempre que no estuviera totalmente entregado.

Ahora solo se puede cancelar si la orden y todos sus subpedidos siguen en `PENDIENTE`; si no, se rechaza. Si se cancela, se repone el stock y se marcan orden + subpedidos como `CANCELADO`.

2) En `DomicilioServiceImpl`, método `updateDomicilio()`: 

Antes se podía editar un domicilio aunque ya estuviera asociado a pedidos, haciendo que cambiara retroactivamente la dirección de pedidos viejos.

Ahora, si el domicilio ya fue usado en un pedido, no se puede modificar y se debe crear uno nuevo.

