1) En `OrderServiceImpl`, método `cancelarOrder()`:

Antes se podía cancelar un pedido aunque ya estuviera confirmado, en preparación o enviado, siempre que no estuviera totalmente entregado.

Ahora solo se puede cancelar si la orden y todos sus subpedidos siguen en `PENDIENTE`; si no, se rechaza. Si se cancela, se repone el stock y se marcan orden + subpedidos como `CANCELADO`.

