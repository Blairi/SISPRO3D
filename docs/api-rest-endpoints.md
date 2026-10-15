# API REST — endpoints y ejemplos (Postman/curl)

Toda la API cuelga de `/api/v1` y devuelve DTOs planos. **Salvo** `/api/v1/auth/**`, cada petición exige la cabecera:

```
Authorization: Bearer <accessToken>
```

La identidad del actor (**nunca se envía en el cuerpo**) — su `clientId`/`expertId`/`adminId` — sale del token. Las violaciones de rol, propiedad o participación responden **403**; sin token o token inválido, **401**.

## Autenticación — `/api/v1/auth` (pública)

### POST `/api/v1/auth/login`
Devuelve `{accessToken, refreshToken, tokenType, expiresIn, claims}`. El 401 es indiferenciado para email desconocido o contraseña errónea.

```
POST http://localhost:8080/api/v1/auth/login
Body (raw JSON):
{
  "email": "mariana.stein@estudio.com",
  "password": "Client123!"
}
```

→ 200

### POST `/api/v1/auth/refresh`
Intercambia un refresh token válido por un par nuevo.

```
POST http://localhost:8080/api/v1/auth/refresh
Body (raw JSON):
{
  "refreshToken": "<refreshToken del login>"
}
```

→ 200

### POST `/api/v1/auth/logout`
Revoca el refresh token (no puede volver a usarse en `/refresh`).

```
POST http://localhost:8080/api/v1/auth/logout
Body (raw JSON):
{
  "refreshToken": "<refreshToken del login>"
}
```

→ 204 No Content

## Cotizaciones — `/api/v1/quotes`

### POST `/api/v1/quotes`
Solicitar cotización. Solo CLIENT y servicio APPROVED; la cotización nace en PENDING (sin monto).

```
POST http://localhost:8080/api/v1/quotes
Body (raw JSON):
{
  "offeredServiceId": 2,
  "description": "Sculpt realista para cortometraje"
}
```

→ 201 Created + `Location: /api/v1/quotes/{id}`

### GET `/api/v1/quotes`
Lista las cotizaciones del actor autenticado.

```
GET http://localhost:8080/api/v1/quotes
```

→ 200

### GET `/api/v1/quotes/status/{status}`
Lista por estado (`PENDING`, `ACCEPTED`, `REJECTED`, `EXPIRED`).

### GET `/api/v1/quotes/service/{serviceId}`
Lista las cotizaciones de un servicio.

### GET `/api/v1/quotes/{id}`
Consultar una cotización.

```
GET http://localhost:8080/api/v1/quotes/1
```

→ 200

### PUT `/api/v1/quotes/{id}/reply`
Responder la cotización con monto y vigencia. Solo el EXPERT propietario del servicio.

```
PUT http://localhost:8080/api/v1/quotes/1/reply
Body (raw JSON):
{
  "totalAmount": 18500.00,
  "validUntil": "2026-12-31"
}
```

→ 200

### PUT `/api/v1/quotes/{id}/accept`
Aceptar la cotización. Solo el CLIENT solicitante y mientras siga vigente. Crea la orden de trabajo en el mismo commit y devuelve `{quote, workOrder}`.

```
PUT http://localhost:8080/api/v1/quotes/1/accept
```

→ 200

### PUT `/api/v1/quotes/{id}/reject`
Rechazar la cotización. Solo el CLIENT solicitante y mientras siga vigente.

```
PUT http://localhost:8080/api/v1/quotes/1/reject
```

→ 200

### PUT `/api/v1/quotes/{id}/expire`
Marcar `EXPIRED` una cotización cuyo `validUntil` ya pasó.

```
PUT http://localhost:8080/api/v1/quotes/1/expire
```

→ 200

### DELETE `/api/v1/quotes/{id}`
Eliminar una cotización.

```
DELETE http://localhost:8080/api/v1/quotes/1
```

→ 204 No Content

## Órdenes de trabajo — `/api/v1/work-orders`

### POST `/api/v1/work-orders`
Crear la orden para una cotización `ACCEPTED` no usada (409 si la cotización ya tiene orden).

```
POST http://localhost:8080/api/v1/work-orders
Body (raw JSON):
{
  "quoteId": 1
}
```

→ 201 Created + `Location: /api/v1/work-orders/{id}`

### GET `/api/v1/work-orders`
Lista las órdenes del actor autenticado (un cliente solo ve las suyas).

### GET `/api/v1/work-orders/client/{clientId}`
Lista las órdenes de un cliente.

### GET `/api/v1/work-orders/expert/{expertId}`
Lista las órdenes de un experto.

### GET `/api/v1/work-orders/status/{status}`
Lista por estado (`PENDING`, `IN_PROGRESS`, `IN_REVIEW`, `COMPLETED`, `CANCELED`).

### GET `/api/v1/work-orders/{id}`
Consultar una orden (solo sus dos partes).

```
GET http://localhost:8080/api/v1/work-orders/1
```

→ 200

### PUT `/api/v1/work-orders/{id}/status`
Transición validada por estado y actor del token: `IN_PROGRESS` (start, experto), `IN_REVIEW` (mark-in-review, experto), `IN_PROGRESS` de vuelta (request-changes, cliente), `COMPLETED` (cliente), `CANCELED` (cliente, solo PENDING/IN_PROGRESS).

```
PUT http://localhost:8080/api/v1/work-orders/1/status
Body (raw JSON):
{
  "status": "IN_PROGRESS"
}
```

→ 200

## Entregables — bajo `/api/v1`

### GET `/api/v1/work-orders/{orderId}/deliverables`
Listar entregables de una orden.

### POST `/api/v1/work-orders/{orderId}/deliverables`
Registrar entregable. Solo el EXPERT propietario y con la orden IN_PROGRESS/IN_REVIEW.

```
POST http://localhost:8080/api/v1/work-orders/3/deliverables
Body (raw JSON):
{
  "name": "Rig definitivo",
  "urlFile": "https://files.render3d.mx/orden3/rig-v2.blend",
  "fileType": "BLEND"
}
```

→ 201 Created

### GET `/api/v1/deliverables/{id}`
Consultar entregable (solo participantes de la orden).

### PUT `/api/v1/deliverables/{id}`
Actualizar entregable.

### DELETE `/api/v1/deliverables/{id}`
Eliminar entregable (borra sus vistas previas).

## Vistas previas — bajo `/api/v1`

### POST `/api/v1/deliverables/{deliverableId}/previews`
Adjuntar vista previa. Solo el EXPERT propietario y con la orden IN_PROGRESS/IN_REVIEW.

```
POST http://localhost:8080/api/v1/deliverables/9/previews
Body (raw JSON):
{
  "caption": "Vista previa del rig",
  "urlFile": "https://files.render3d.mx/orden3/preview-rig.png"
}
```

→ 201 Created

### GET `/api/v1/deliverables/{deliverableId}/previews`
Listar vistas previas de un entregable (ordenadas por `createdAt`).

### GET `/api/v1/previews/{id}`
Consultar una vista previa.

### PUT `/api/v1/previews/{id}`
Actualizar vista previa.

### DELETE `/api/v1/previews/{id}`
Eliminar vista previa.

```
DELETE http://localhost:8080/api/v1/previews/1
```

→ 204 No Content

## Hilos de chat — bajo `/api/v1`

### POST `/api/v1/work-orders/{orderId}/thread`
Abrir el hilo de una orden. Solo CLIENT/EXPERT de la orden; 409 si ya existe.

```
POST http://localhost:8080/api/v1/work-orders/1/thread
Body (raw JSON): {}
```

→ 201 Created + `Location: /api/v1/threads/{nuevoId}`

### GET `/api/v1/work-orders/{orderId}/thread`
Consultar el hilo de una orden por su id (solo participantes).

```
GET http://localhost:8080/api/v1/work-orders/1/thread
```

→ 200 `{"id":1,"workOrderId":1}`

### GET `/api/v1/threads/{id}`
Consultar un hilo por su id.

### DELETE `/api/v1/threads/{id}`
Eliminar un hilo (solo participantes).

## Mensajes — bajo `/api/v1`

### POST `/api/v1/threads/{threadId}/messages`
Publicar mensaje. Solo participantes del hilo; el timestamp lo asigna el servidor. Rechaza envíos en órdenes `COMPLETED`/`CANCELED`.

```
POST http://localhost:8080/api/v1/threads/1/messages
Body (raw JSON):
{
  "content": "Hola Emilio, ¿puedes adjuntar una vista previa?"
}
```

→ 201 Created

### GET `/api/v1/threads/{threadId}/messages`
Listar mensajes del hilo en orden cronológico.

### GET `/api/v1/messages/{id}`
Consultar un mensaje.

### DELETE `/api/v1/messages/{id}`
Eliminar un mensaje.

## Favoritos (N:M cliente–servicio)

Requiere autenticación JWT: `Authorization: Bearer <access_token>`. Solo clientes (`CLIENT`) pueden acceder. 
Si el `clientId` indicado en la ruta no coincide con el del token, se devuelve `403`. 
La autorización se resuelve desde el token (no desde el cuerpo).

### POST `/api/v1/clients/{clientId}/favorite-services/{serviceId}`
Marcar servicio como favorito; 409 si el par ya existe. 403 si no coincide el propietario.

```
POST http://localhost:8080/api/v1/clients/5/favorite-services/2
Authorization: Bearer <access_token>
```

→ 201 Created + `Location: /api/v1/services/2`

### GET `/api/v1/clients/{clientId}/favorite-services`
Listar los servicios favoritos del cliente (más reciente primero). 403 si no coincide el propietario.

```
GET http://localhost:8080/api/v1/clients/5/favorite-services
Authorization: Bearer <access_token>
```

→ 200 OK

### DELETE `/api/v1/clients/{clientId}/favorite-services/{serviceId}`
Quitar favorito; 404 si no existe la asociación. 403 si no coincide el propietario.

```
DELETE http://localhost:8080/api/v1/clients/5/favorite-services/2
Authorization: Bearer <access_token>
```

→ 204 No Content

### POST `/api/v1/me/favorite-services/{serviceId}`
Marcar servicio como favorito para el cliente autenticado (identificado por token). 409 si ya existe.

```
POST http://localhost:8080/api/v1/me/favorite-services/2
Authorization: Bearer <access_token>
```

→ 201 Created + `Location: /api/v1/services/2`

### GET `/api/v1/me/favorite-services`
Listar favoritos del cliente autenticado.

```
GET http://localhost:8080/api/v1/me/favorite-services
Authorization: Bearer <access_token>
```

→ 200 OK

### DELETE `/api/v1/me/favorite-services/{serviceId}`
Quitar favorito para el cliente autenticado. 404 si no existe la asociación.

```
DELETE http://localhost:8080/api/v1/me/favorite-services/2
Authorization: Bearer <access_token>
```

→ 204 No Content

**Errores:** 401 (sin token), 403 (no CLIENT / acceso cruzado), 404 (cliente/servicio no encontrado o asociación inexistente), 409 (ya es favorito).