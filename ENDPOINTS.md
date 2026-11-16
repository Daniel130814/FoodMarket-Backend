# Appetitos API — contrato de endpoints

Base URL: `http://localhost:4002`. Autenticación: `Authorization: Bearer <access_token>` (token devuelto por `/api/v1/auth/authenticate`).

## Convenciones (valen para TODAS las respuestas)

```json
{ "success": true|false, "message": "texto", "data": <objeto | array | null> }
```

- Las listas vacías llegan con `data: null`.
- Errores: `success:false`, `data:null`. Códigos: `400` validación/regla de negocio, `401` sin token o token inválido, `403` sin rol/ownership, `404` no existe, `409` conflicto, `500` error interno.
- Excepción: `GET /imagenes/{nombre}` devuelve el binario de la imagen en éxito (no JSON); sus errores sí son JSON.

## Roles por ruta (resumen)

| Ruta | Acceso |
|---|---|
| `/api/v1/auth/**` | público |
| `GET /platos`, `GET /platos/{id}`, `GET /chef-profiles/**`, `GET /resenas/**`, `GET /categories/**`, `GET /ingredientes/**`, `GET /imagenes/**` | público |
| `GET /platos/mis-platos` | CHEF |
| `POST /platos/**` (createPlato, imágenes) | CHEF |
| `PUT/PATCH/DELETE /platos/**` | CHEF o ADMIN (ownership en servicio) |
| `GET/PUT /users/me`, `PUT /users/me/password` | cualquier autenticado |
| `/users/**` (resto) | ADMIN |
| `POST/PUT /chef-profiles/**` | CHEF / CHEF o ADMIN |
| `/categories/**`, `/ingredientes/**` (escritura) | ADMIN |
| `POST /resenas/**` / `PUT, DELETE /resenas/**` | CLIENTE / CLIENTE o ADMIN |
| `/domicilios/**`, `/orders/**` | CLIENTE (POST) / CLIENTE o ADMIN |
| `/pagos/**` | CLIENTE (POST), ADMIN (PATCH), CLIENTE o ADMIN (resto) |
| `GET /subpedidos/me` | CHEF |
| `/subpedidos/**` (resto) | CHEF o ADMIN (ownership en servicio) |
| `/carrito/**` | CLIENTE |
| `/favoritos/**` | CLIENTE (CHEF y ADMIN reciben 403) |

`POST /ingredientes` sigue siendo ADMIN; el CHEF crea ingredientes nuevos indicando `nombre` al armar el plato (ver abajo).

Los endpoints no listados acá (auth, órdenes, pagos, categorías, ingredientes, domicilios) **no cambiaron**; los cambios de carrito, reseñas, chef-profiles y subpedidos están en "Cambios para el front".

---

## NUEVO — `GET /platos/mis-platos` (CHEF)

Platos del chef autenticado en **todos** los estados (BORRADOR, PUBLICADO, AGOTADO, PAUSADO), más nuevo primero. Cada elemento es un `PlatoResponse`.

- `200` con platos: `message: "Platos obtenidos correctamente"`, `data: PlatoResponse[]`.
- `200` sin platos: `message: "No tenés platos cargados"`, `data: null`.
- `401` sin token · `403` si no es CHEF.

## CAMBIO — `GET /platos/{platoId}` (público, token opcional)

- PUBLICADO / AGOTADO: visible para todos (igual que antes).
- BORRADOR / PAUSADO: `200` solo si llega token válido de **su chef dueño** o de un **ADMIN**; en cualquier otro caso (sin token, otro chef, cliente) `404 "El plato indicado no existe"`.
- Un token inválido/vencido sigue devolviendo `401` (comportamiento del filtro JWT, también en rutas públicas).

## NUEVO — `POST /platos/{platoId}/imagenes` (CHEF dueño)

- `Content-Type: multipart/form-data`, campo **`file`**.
- Formatos: `image/jpeg`, `image/png`, `image/webp` (se valida también la firma real del archivo). Máximo **5 MB**.
- Éxito: `201`, `message: "Imagen subida correctamente"`, header `Location: /platos/{id}`, `data: PlatoResponse` completo con la nueva URL **absoluta** agregada al final de `imagenesUrls`, p. ej. `http://localhost:4002/imagenes/6cc41e1f-....png`.
- Errores:
  - `400` formato no permitido (`"Formato no permitido: solo se aceptan image/jpeg, image/png e image/webp"`), contenido que no coincide con el formato, archivo > 5 MB (`"La imagen supera el tamaño máximo de 5 MB"`), archivo vacío o falta el campo `file` / no es multipart (`"La solicitud debe ser multipart/form-data e incluir el archivo en el campo 'file'"`).
  - `401` sin token · `403` si no es CHEF o no es el dueño del plato (un ADMIN tampoco puede subir) · `404` si el plato no existe.
- Orden de chequeos: existencia (404) → ownership (403) → validación del archivo (400).
- El camino anterior sigue vigente: `imagenesUrls: string[]` en `POST /platos/createPlato` y `PUT /platos/{id}`. Ojo: el `PUT` **reemplaza** la lista completa; si el front edita un plato con imágenes subidas por archivo, debe reenviar esas URLs o se pierden.
- Un plato BORRADOR no exige imágenes; PUBLICADO/AGOTADO exige al menos una (regla existente).

## NUEVO — `PUT /chef-profiles/{id}/foto` (CHEF dueño o ADMIN)

- `Content-Type: multipart/form-data`, campo **`file`**. Mismos formatos y límite que las imágenes de platos (jpeg/png/webp, 5 MB).
- Éxito: `200`, `message: "Foto de perfil actualizada correctamente"`, `data: ChefProfileResponse` con `fotoUrl` apuntando a la URL **absoluta** de la imagen subida. La foto anterior subida por archivo se borra del disco.
- Errores: `400` archivo inválido (mismos mensajes que arriba) · `401` sin token · `403` si no es el dueño del perfil ni ADMIN · `404` si el perfil no existe.
- `fotoUrl` como texto sigue disponible en `POST /chef-profiles` y `PUT /chef-profiles/{id}`.

## NUEVO — `GET /imagenes/{nombre}` (público)

Sirve el archivo con su `Content-Type` real (`image/jpeg|png|webp`), `Cache-Control` de 30 días. `404` JSON (`"La imagen indicada no existe"`) si no existe o el nombre no es válido. Se usa directo en `<img src>`.

## NUEVO — `PUT /users/me/password` (autenticado)

Body:

```json
{ "actual": "string", "nueva": "string (mínimo 8)" }
```

- `200`: `message: "Contraseña actualizada correctamente"`, `data: null`. El token vigente sigue siendo válido; el login posterior exige la nueva.
- `400`: `"La contraseña actual es incorrecta"`, o validación (`"nueva: el tamaño debe estar entre 8 y 2147483647"`, campos en blanco).
- `401` sin token.

## CAMBIO — `DELETE /platos/{platoId}` (CHEF dueño o ADMIN)

`200`, `data: null` (sin cambios), pero el `message` ahora informa qué pasó:

- `"Plato eliminado"` → se borró físicamente.
- `"Plato pausado porque tiene historial"` → quedó en `PAUSADO` (tiene pedidos, reseñas o está en un carrito).

---

## CAMBIO — `ingredientes` de `POST /platos/createPlato` y `PUT /platos/{id}` (CHEF dueño)

Cada elemento de `ingredientes` acepta **exactamente una** de estas formas:

```json
{ "ingredienteId": 4, "cantidad": 120, "unidadMedida": "GRAMOS" }
{ "nombre": "Pimentón ahumado", "cantidad": 5, "unidadMedida": "GRAMOS" }
```

- `ingredienteId` y `nombre` a la vez, o ninguno de los dos → `400` ("Cada ingrediente debe indicar exactamente uno de: ingredienteId o nombre").
- Con `nombre`: se reutiliza el ingrediente existente o se crea uno nuevo. La búsqueda ignora mayúsculas, tildes y espacios sobrantes (`"tomate"`, `" Tomate "` y `"TOMATE"` son el mismo). Al crear se guarda el nombre tal cual lo mandó el chef (con trim) y `descripcion` queda igual al nombre.
- `nombre` vacío/en blanco o de más de 255 caracteres → `400`.
- Un mismo ingrediente repetido en el plato → `400`, incluso si una entrada usa `ingredienteId` y otra `nombre` (se verifica después de resolver los nombres).
- Los ingredientes nuevos se crean en la misma transacción que el plato: si el plato falla (validación, 403, etc.) no queda ninguno creado. Dos chefs creando el mismo nombre a la vez terminan usando el mismo ingrediente.
- `cantidad` > 0 y `unidadMedida` del enum actual, sin cambios. Quitar un ingrediente sigue siendo un `PUT` con la lista completa.
- La respuesta (`PlatoResponse.ingredientes`: `ingredienteId`, `nombre`, `cantidad`, `unidadMedida`) no cambia; los clientes que mandan solo `ingredienteId` siguen funcionando igual.
- Seguridad sin cambios: `POST/PUT/DELETE /ingredientes/**` siguen siendo ADMIN (el chef no renombra ni borra del catálogo); `DELETE /ingredientes/{id}` da `409` si lo usa algún plato; `GET /ingredientes` y `GET /ingredientes/{id}` son públicos (sirven para sugerir nombres).

## `PlatoResponse` (CAMBIO: se agrega `chefNombre`)

```json
{ "id": 1, "nombre": "", "descripcion": null, "imagenesUrls": ["http://localhost:4002/imagenes/<uuid>.png"],
  "precio": 10.00, "descuentoPorcentaje": 0.00, "precioConDescuento": 10.00, "stockDisponible": 5,
  "estado": "BORRADOR|PUBLICADO|AGOTADO|PAUSADO", "chefId": 5, "chefNombre": "Ana Gómez",
  "categorias": [], "ingredientes": [] }
```

`chefNombre` = nombre y apellido del chef dueño (si no los cargó, su `username`). Aplica a `GET /platos`, `GET /platos/{id}`, `GET /platos/mis-platos`, `GET /favoritos` y a las respuestas de `POST`, `PUT` y `PATCH` de platos.

---

# Cambios para el front (entrega 30/11)

## NUEVO — Favoritos (solo CLIENTE)

Un cliente solo ve y toca sus propios favoritos. Único por (cliente, plato). Se borran solos si se borra el plato o el usuario.

| Ruta | Éxito | Errores |
|---|---|---|
| `GET /favoritos` | `200`, `data: PlatoResponse[]` (más nuevo primero, solo platos `PUBLICADO`/`AGOTADO`). Sin favoritos: `200`, `message: "No tenés platos favoritos"`, `data: null` | `401`, `403` |
| `POST /favoritos/{platoId}` | `201` + `Location: /favoritos/{platoId}`, `message: "Plato agregado a favoritos"`, `data: PlatoResponse` | `404` plato inexistente o no visible, `409 "El plato ya está en tus favoritos"`, `401`, `403` |
| `DELETE /favoritos/{platoId}` | `200`, `message: "Plato quitado de favoritos"`, `data: null` | `404 "El plato no está en tus favoritos"`, `401`, `403` |

Si un plato favorito pasa a `PAUSADO`/`BORRADOR`, deja de aparecer en `GET /favoritos` (el favorito se conserva y reaparece si vuelve a publicarse).

## CAMBIO — Filtro de lenguaje ofensivo en reseñas

`POST /resenas/createResena` y `PUT /resenas/{resenaId}` validan `comentario` (opcional: `null` o vacío no se valida). Si contiene lenguaje ofensivo: `400`, `message: "El comentario contiene lenguaje no permitido"` (no se informa cuál fue la palabra). La comparación ignora mayúsculas, tildes, símbolos/números típicos (`p3l0tud0`), letras repetidas (`pelotuuudo`) y separadores (`p.e.l.o.t.u.d.o`), y es por **palabra entera** (no bloquea "pijama"). La lista está en `src/main/resources/palabras-prohibidas-es.txt` (una palabra por línea, minúscula y sin tildes) y se edita sin tocar Java; **pendiente de revisión de Daniel**.

## CAMBIO — `ResenaResponse`

```json
{ "id": 1, "calificacion": 5, "comentario": "Muy rico", "fechaCreacion": "2026-11-16T10:00:00",
  "clienteId": 2, "clienteNombre": "Juan P.", "platoId": 5, "platoNombre": "Milanesa" }
```

`clienteNombre` = nombre + inicial del apellido (solo el nombre si no tiene apellido), por privacidad. Aplica a `GET /resenas`, `GET /resenas/{id}`, `GET /resenas/plato/{platoId}`, `GET /resenas/chef/{chefId}` y a las respuestas de `POST`/`PUT`. `DELETE /resenas/{id}` ya aceptaba ADMIN (probado en tests: ADMIN borra una reseña ajena, CLIENTE no puede borrar la de otro → `403`); la `reputacion` del chef se calcula en cada consulta como promedio de sus reseñas, así que queda al día tras el borrado.

## NUEVO — `GET /resenas/chef/{chefId}` (público)

Reseñas de todos los platos del chef, más nueva primero (`ResenaResponse[]`). Sin reseñas: `200`, `message: "Todavía no hay reseñas para este chef"`, `data: null`. `404 "El usuario indicado no existe"` si el id no existe o no es un CHEF.

## CAMBIO — `GET /platos?chefId={id}`

Nuevo filtro opcional, combinable con `nombre`, `categoriaId`, `precioMin` y `precioMax`. Un `chefId` sin platos visibles devuelve `200` con `data: null` (no `404`).

## CAMBIO — Items del carrito (`ItemCarritoResponse`)

Se agregan `imagenUrl` (primera imagen del plato o `null`), `stockDisponible` y `chefNombre`:

```json
{ "id": 4, "platoId": 3, "nombrePlato": "Milanesa", "cantidad": 2, "precioBase": 10.00,
  "descuentoPorcentaje": 0.00, "precioUnitarioActual": 10.00, "subtotalEstimado": 20.00,
  "imagenUrl": "http://localhost:4002/imagenes/a.png", "stockDisponible": 5, "chefNombre": "Ana Gómez" }
```

## CAMBIO — `SubPedidoChefResponse` (también dentro de `OrderResponse.subPedidos`)

```json
{ "id": 5, "pedidoId": 40, "fechaCreacion": "2026-11-01T12:00:00", "clienteNombre": "Juan Pérez",
  "chefId": 2, "chefNombre": "Ana", "estado": "CONFIRMADO", "subtotal": 10.00, "detalles": [] }
```

`fechaCreacion` es la del pedido; `clienteNombre` = nombre y apellido del dueño del pedido. Solo lo ven el chef dueño, el ADMIN y el propio cliente (dentro de su pedido).

## NUEVO — `PATCH /platos/{platoId}/stock` y `PATCH /platos/{platoId}/estado` (CHEF dueño o ADMIN)

```json
PATCH /platos/10/stock    { "stockDisponible": 20 }
PATCH /platos/10/estado   { "estado": "PAUSADO" }
```

Ambos devuelven `200` con el `PlatoResponse` actualizado (`message: "Stock actualizado correctamente"` / `"Estado actualizado correctamente"`) y aplican las mismas reglas que `PUT /platos/{id}`:

- Stock: `0` → `AGOTADO` y `> 0` → `PUBLICADO` solo si el plato estaba `PUBLICADO`/`AGOTADO`; `BORRADOR` y `PAUSADO` se respetan.
- Estado: `PUBLICADO`/`AGOTADO` se resuelven por stock (`0` → `AGOTADO`, `> 0` → `PUBLICADO`) y exigen al menos una imagen (`400 "Un plato publicado debe tener al menos una imagen"`); `BORRADOR`/`PAUSADO` se aplican tal cual.
- Errores: `400` stock negativo/nulo (`"stockDisponible: ..."`) o estado nulo/inválido · `401` · `403` si no es el dueño ni ADMIN · `404` plato inexistente.

## CAMBIO — `ChefProfileResponse` y requests

```json
{ "id": 7, "userId": 3, "nombre": "Ana", "apellido": "Gómez", "biografia": "...", "especialidad": "Pastas",
  "nacionalidad": "Argentina", "fotoUrl": null, "descripcion": "...", "reputacion": 4.50 }
```

`POST /chef-profiles` y `PUT /chef-profiles/{id}` aceptan `nacionalidad` (string opcional). Largos máximos: `especialidad` y `nacionalidad` 100; `biografia` y `descripcion` 2000 → `400` (`"especialidad: no puede superar los 100 caracteres"`).

## NUEVO — `GET /subpedidos/chef/{chefId}/pendientes` (ese chef o ADMIN)

`200`, `data: { "cantidad": 3 }` = subpedidos del chef en `PENDIENTE` o `CONFIRMADO`. Siempre devuelve el objeto (con `0` si no hay). `403` si es otro chef · `404` si el usuario no existe.

## NUEVO — `GET /chef-profiles/user/{userId}` (público)

Perfil por id de **usuario** (`ChefProfileResponse`). `404` si ese usuario no tiene perfil.

## NUEVO — `GET /subpedidos/me` (CHEF)

Atajo de `GET /subpedidos/chef/{miId}`: mismos `SubPedidoChefResponse[]`; sin subpedidos `200` con `data: null`. `403` si no es CHEF.

## NUEVO — Datos semilla

Al arrancar se crea lo que falte (idempotente): categorías base (Entradas, Platos principales, Pastas, Carnes, Vegetarianos, Postres, Bebidas), ingredientes base y un ADMIN inicial **solo** si están definidas `ADMIN_USERNAME` y `ADMIN_PASSWORD` (`ADMIN_EMAIL` opcional; por defecto `<username>@appetitos.local`). Se apaga con `SEED_ENABLED=false` (`app.seed.enabled`).

## CORS

Origen permitido por defecto `http://localhost:5173` (variable `CORS_ALLOWED_ORIGINS`, lista separada por comas). Métodos `GET, POST, PUT, PATCH, DELETE, OPTIONS`; headers `Authorization, Content-Type`. Verificado: el preflight del POST multipart con `Authorization` responde 200 y el POST devuelve `Access-Control-Allow-Origin`.

## Configuración nueva

| Propiedad / variable | Default | Uso |
|---|---|---|
| `IMAGENES_DIR` (`app.imagenes.dir`) | `uploads/imagenes` | carpeta en disco (relativa al directorio de arranque) |
| `PUBLIC_BASE_URL` (`app.public-base-url`) | `http://localhost:4002` | prefijo de las URLs absolutas guardadas |
| `spring.servlet.multipart.max-file-size` / `max-request-size` | `5MB` / `6MB` | límites multipart |
| `SEED_ENABLED` (`app.seed.enabled`) | `true` | datos semilla al arrancar |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` / `ADMIN_EMAIL` | vacías | ADMIN inicial (sin usuario y contraseña no se crea) |

## Lo que falta

- Revisar la lista `palabras-prohibidas-es.txt` (es una propuesta inicial).
- `GET /favoritos` oculta los platos no visibles pero conserva el favorito; no hay endpoint para limpiarlos.
- Los tests HTTP que requieren Docker/MySQL (`FavoritosSecurityTest` y el resto de integración) no se pudieron correr en la máquina de desarrollo; se omiten solos sin Docker.

---

# Cambio de rol por ADMIN

Este archivo se creó para documentar esta operación. **No es el inventario completo de la API.**

## Solicitud

```http
PATCH /users/{userId}/role
Authorization: Bearer <access_token_de_ADMIN>
Content-Type: application/json

{"role":"CHEF"}
```

- `userId`: identificador del usuario destinatario.
- `role`: obligatorio, no nulo. Valores: `CLIENTE`, `CHEF` o `ADMIN`.
- La autorización existente de `/users/**` exige ADMIN. El servicio también comprueba el rol actual del solicitante.
- Solo se modifica `User.role`. Se conservan contraseña/hash, datos personales, perfil de chef, publicaciones, carritos e historial.
- El rol reemplaza al anterior: no hay permisos combinados ni herencia de permisos entre roles.
- Asignar CHEF no exige ni crea automáticamente un perfil. Si ya existía, se conserva y puede reutilizarse al volver a CHEF.

## Respuesta exitosa

Devuelve `200 OK` con `ApiResponse<UserResponse>`, sin contraseña ni tokens:

```json
{
  "success": true,
  "message": "Rol del usuario actualizado correctamente",
  "data": {
    "id": 12,
    "username": "ana",
    "nombre": "Ana",
    "apellido": "Paz",
    "email": "ana@example.com",
    "role": "CHEF"
  }
}
```

Los datos del ejemplo son ilustrativos: la respuesta conserva los datos reales del destinatario.

Repetir el rol actual devuelve el mismo formato y `200`, sin guardar cambios ni comprobar actividad de chef. Un administrador puede solicitar ADMIN para sí mismo, pero no puede quitarse ese rol.

## Restricciones al salir de CHEF

Tanto CHEF → CLIENTE como CHEF → ADMIN devuelven `409` si el usuario tiene:

- Algún plato en `PUBLICADO` o `AGOTADO`.
- Algún subpedido en `PENDIENTE`, `CONFIRMADO`, `EN_PREPARACION` o `ENVIADO`.

Si únicamente tiene platos `BORRADOR`/`PAUSADO` y subpedidos `ENTREGADO`/`CANCELADO`, el cambio está permitido. No se borran ni modifican esos registros.

```json
{
  "success": false,
  "message": "No se puede quitar el rol CHEF mientras tenga platos publicados o agotados, o subpedidos sin terminar",
  "data": null
}
```

## Errores

| Estado | Motivo |
| --- | --- |
| `400` | Falta `role`, es nulo/desconocido o el JSON es inválido. |
| `400` | El administrador intenta cambiar su propio rol a CLIENTE o CHEF. |
| `401` | Falta autenticación, o el JWT es inválido/venció. |
| `403` | El solicitante es CLIENTE o CHEF. |
| `404` | El destinatario no existe. |
| `409` | La actividad del chef impide quitarle el rol. |

Los errores usan `{"success":false,"message":"...","data":null}`. Los rechazos no guardan cambios.

## JWT y compatibilidad

Los JWT actuales identifican al usuario por `username`, sin incluir el rol. El filtro recarga sus autoridades desde la persistencia en cada solicitud. Después de confirmar el cambio, **el mismo JWT válido usa el nuevo rol en la siguiente solicitud**, sin otro login. Una solicitud ya en curso puede conservar la autorización inicial.

Se mantienen la configuración CORS, el registro público (CLIENTE/CHEF, nunca ADMIN) y la edición propia de perfil sin acceso a `role` ni contraseña. Este endpoint no crea el primer administrador.

## Ejemplo en Postman

1. Definir `baseUrl` con la dirección del backend. Para una instancia local con el puerto predeterminado de Spring Boot: `http://localhost:8080`.
2. Obtener un JWT de una cuenta que ya sea ADMIN mediante:

   ```http
   POST {{baseUrl}}/api/v1/auth/authenticate
   Content-Type: application/json
   ```

   Authorization: **No Auth**. Body → **raw → JSON**, reemplazando las credenciales por las de esa cuenta:

   ```json
   {
     "username": "tu_admin",
     "password": "tu_password"
   }
   ```

3. Copiar `access_token` de la respuesta y guardarlo como variable `adminToken`.
4. Definir `userId` con el ID de otro usuario existente. Enviar:

   - Método: **PATCH**.
   - URL: `{{baseUrl}}/users/{{userId}}/role`.
   - Authorization → **Bearer Token** → Token: `{{adminToken}}` (sin escribir `Bearer` en ese campo).
   - Header: `Content-Type: application/json`.
   - Body → **raw → JSON**:

   ```json
   {
     "role": "CHEF"
   }
   ```

5. Esperar `200` y `data.role = "CHEF"`. Repetir la misma solicitud devuelve `200` sin guardar. Para comprobar la restricción de actividad, un cambio posterior a CLIENTE o ADMIN devuelve `409` si ese chef tiene publicaciones comprables o trabajo pendiente.
