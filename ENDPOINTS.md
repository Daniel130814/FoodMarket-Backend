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
