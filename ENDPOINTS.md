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
| `/subpedidos/**` | CHEF o ADMIN |
| `/carrito/**` | CLIENTE |

`POST /ingredientes` sigue siendo ADMIN; el CHEF crea ingredientes nuevos indicando `nombre` al armar el plato (ver abajo).

Los endpoints no listados acá (auth, carrito, órdenes, pagos, reseñas, categorías, ingredientes, domicilios, chef-profiles, subpedidos) **no cambiaron**.

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

## `PlatoResponse` (sin cambios)

```json
{ "id": 1, "nombre": "", "descripcion": null, "imagenesUrls": ["http://localhost:4002/imagenes/<uuid>.png"],
  "precio": 10.00, "descuentoPorcentaje": 0.00, "precioConDescuento": 10.00, "stockDisponible": 5,
  "estado": "BORRADOR|PUBLICADO|AGOTADO|PAUSADO", "chefId": 5, "categorias": [], "ingredientes": [] }
```

## CORS

Origen permitido por defecto `http://localhost:5173` (variable `CORS_ALLOWED_ORIGINS`, lista separada por comas). Métodos `GET, POST, PUT, PATCH, DELETE, OPTIONS`; headers `Authorization, Content-Type`. Verificado: el preflight del POST multipart con `Authorization` responde 200 y el POST devuelve `Access-Control-Allow-Origin`.

## Configuración nueva

| Propiedad / variable | Default | Uso |
|---|---|---|
| `IMAGENES_DIR` (`app.imagenes.dir`) | `uploads/imagenes` | carpeta en disco (relativa al directorio de arranque) |
| `PUBLIC_BASE_URL` (`app.public-base-url`) | `http://localhost:4002` | prefijo de las URLs absolutas guardadas |
| `spring.servlet.multipart.max-file-size` / `max-request-size` | `5MB` / `6MB` | límites multipart |

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
