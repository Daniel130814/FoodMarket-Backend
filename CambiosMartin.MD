1) Visibilidad de platos agotados

Se corrigió la lógica del catálogo público para que los platos AGOTADO sigan siendo visibles.

Cambios realizados:

PlatoServiceImpl.java → métodos getPlatos() y getPlatoById(): se agregó ESTADOS_PUBLICOS, que contiene PUBLICADO y AGOTADO.
PlatoRepository.java → buscarConFiltros(): la query pasó de filtrar por un solo estado con = a aceptar varios mediante IN.
PlatoRepository.java → findByIdAndEstadoIn(): el detalle también permite consultar PUBLICADO y AGOTADO.
PlatoServiceImplTest.java: se actualizaron los tests y se agregaron casos para verificar que AGOTADO sea visible y que BORRADOR/PAUSADO sigan ocultos.

Los filtros por nombre, categoría y precio siguen funcionando igual, pero ahora contemplan PUBLICADO + AGOTADO.

El carrito no se modificó: un plato AGOTADO sigue sin poder agregarse.

2) Múltiples fotografías por plato

Se reemplazó la imagen única de cada plato por una lista de imágenes y se agregó la regla de que los platos públicos tengan al menos una fotografía.

Cambios realizados:

Plato.java → campo imagenesUrls: el antiguo String imagenUrl se reemplazó por List<String> usando @ElementCollection, persistida en la tabla plato_imagenes con plato_id, imagen_url y orden.
PlatoRequest.java → imagenesUrls: ahora create/update reciben varias URLs y cada elemento debe ser no vacío.
PlatoResponse.java y ResponseMapper.plato() → ahora devuelven la lista completa de imágenes manteniendo su orden.
PlatoServiceImpl.actualizarDatosPlato() → reemplaza la colección de imágenes y valida el estado final:
BORRADOR: puede no tener imágenes.
PUBLICADO: mínimo 1.
AGOTADO: mínimo 1.
PAUSADO: puede no tener imágenes.
PlatoServiceImplTest.java → se agregaron tests de múltiples imágenes, orden y validaciones según estado.

No se tocaron endpoints, carrito, stock, pedidos, descuentos, filtros ni autenticación.