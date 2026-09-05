1) En `OrderServiceImpl`, método `cancelarOrder()`:

Antes se podía cancelar un pedido aunque ya estuviera confirmado, en preparación o enviado, siempre que no estuviera totalmente entregado.

Ahora solo se puede cancelar si la orden y todos sus subpedidos siguen en `PENDIENTE`; si no, se rechaza. Si se cancela, se repone el stock y se marcan orden + subpedidos como `CANCELADO`.

2) En `DomicilioServiceImpl`, método `updateDomicilio()`: 

Antes se podía editar un domicilio aunque ya estuviera asociado a pedidos, haciendo que cambiara retroactivamente la dirección de pedidos viejos.

Ahora, si el domicilio ya fue usado en un pedido, no se puede modificar y se debe crear uno nuevo.

3) En `PlatoServiceImpl` y `PlatoRepository`:

Antes los GET públicos devolvían platos sin importar su estado, por lo que podían aparecer `BORRADOR`, `PAUSADO` o `AGOTADO`.

El cambio se hizo en `PlatoServiceImpl`, específicamente en:
`getPlatos()`
`getPlatoById(Long platoId)`

En vez de usar `findAll()` y `findById()`, ahora llaman a consultas que filtran por `EstadoPlato.PUBLICADO`.

Para eso, en `PlatoRepository` se agregaron:
`findByEstado(EstadoPlato estado)`
`findByIdAndEstado(Long id, EstadoPlato estado)`

De esta forma, tanto el catálogo público como la búsqueda por ID devuelven únicamente platos publicados.


4) En `OrderServiceImpl` y `PlatoServiceImpl` ajusté la lógica para que el estado del plato quede sincronizado con el stock.

En `OrderServiceImpl`, metodo `createOrder()`, cuando una compra deja el stock en `0`, el plato pasa automáticamente de `PUBLICADO` a `AGOTADO`.

En `OrderServiceImpl`, metodo `cancelarOrder()`, si se repone stock de un plato que estaba `AGOTADO`, vuelve automáticamente a `PUBLICADO`.

En `PlatoServiceImpl`, metodo `actualizarDatosPlato()`, también se valida esa coherencia al crear o editar un plato, evitando combinaciones como `PUBLICADO` con stock `0` o `AGOTADO` con stock disponible.

Los estados manuales `BORRADOR` y `PAUSADO` no se modifican automáticamente.  
