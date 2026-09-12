## Cotizaciones — `/api/v1/quotes`

### POST `/api/v1/quotes`
Solicitar cotización. Solo CLIENT y servicio APPROVED; la cotización nace en PENDING (sin monto).

```
POST http://localhost:8080/api/v1/quotes
Body (raw JSON):
{
  "clientId": 5,
  "offeredServiceId": 2,
  "description": "Sculpt realista para cortometraje"
}
```

→ 201 Created + `Location: /api/v1/quotes/{id}`

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
  "expertId": 2,
  "totalAmount": 18500.00,
  "validUntil": "2026-12-31"
}
```

→ 200

### PUT `/api/v1/quotes/{id}/accept`
Aceptar la cotización. Solo el CLIENT solicitante.

```
PUT http://localhost:8080/api/v1/quotes/1/accept
Body (raw JSON):
{
  "clientId": 5
}
```

→ 200

### PUT `/api/v1/quotes/{id}/reject`
Rechazar la cotización. Solo el CLIENT solicitante.

```
PUT http://localhost:8080/api/v1/quotes/1/reject
Body (raw JSON):
{
  "clientId": 5
}
```

→ 200

### DELETE `/api/v1/quotes/{id}`
Eliminar una cotización.

```
DELETE http://localhost:8080/api/v1/quotes/1
```

→ 204 No Content

## Hilos de chat — bajo `/api/v1`

### POST `/api/v1/work-orders/{orderId}/thread`
Abrir el hilo de una orden. Solo CLIENT/EXPERT de la orden; 409 si ya existe. En el seed las órdenes 1–5 ya tienen hilo, así que `POST /work-orders/1/thread` responde 409 (evidencia de unicidad). Para ver el **201**, primero borrá el hilo existente y volvé a crearlo:

```
DELETE http://localhost:8080/api/v1/threads/1     -> 204 (borra hilo y sus mensajes)
POST http://localhost:8080/api/v1/work-orders/1/thread
Body (raw JSON):
{
  "actorId": 5
}
```

→ 201 Created + `Location: /api/v1/threads/{nuevoId}`

### GET `/api/v1/work-orders/{orderId}/thread`
Consultar el hilo de una orden por su id.

```
GET http://localhost:8080/api/v1/work-orders/1/thread
```

→ 200 `{"id":1,"workOrderId":1}`

### GET `/api/v1/threads/{id}`
Consultar un hilo por su id.

```
GET http://localhost:8080/api/v1/threads/1
```

→ 200

### DELETE `/api/v1/threads/{id}`
Eliminar un hilo.

```
DELETE http://localhost:8080/api/v1/threads/1
```

→ 204 No Content

## Mensajes — bajo `/api/v1`

### POST `/api/v1/threads/{threadId}/messages`
Publicar mensaje. Solo participantes del hilo; el timestamp lo asigna el servidor.

```
POST http://localhost:8080/api/v1/threads/1/messages
Body (raw JSON):
{
  "authorId": 5,
  "content": "Hola Emilio, ¿puedes adjuntar una vista previa?"
}
```

→ 201 Created

### GET `/api/v1/threads/{threadId}/messages`
Listar mensajes del hilo en orden cronológico.

```
GET http://localhost:8080/api/v1/threads/1/messages
```

→ 200

### GET `/api/v1/messages/{id}`
Consultar un mensaje.

```
GET http://localhost:8080/api/v1/messages/1
```

→ 200

### DELETE `/api/v1/messages/{id}`
Eliminar un mensaje.

```
DELETE http://localhost:8080/api/v1/messages/1
```

→ 204 No Content

## Vistas previas — bajo `/api/v1`

### POST `/api/v1/deliverables/{deliverableId}/previews`
Adjuntar vista previa. Solo el EXPERT propietario y con la orden IN_PROGRESS/IN_REVIEW. En el seed, la orden 3 (IN_PROGRESS, experto 4) tiene el entregable 9: úsalo para ver el 201.

```
POST http://localhost:8080/api/v1/deliverables/9/previews
Body (raw JSON):
{
  "expertId": 4,
  "caption": "Vista previa del rig",
  "urlFile": "https://files.render3d.mx/orden3/preview-rig.png"
}
```

→ 201 Created

### GET `/api/v1/deliverables/{deliverableId}/previews`
Listar vistas previas de un entregable.

```
GET http://localhost:8080/api/v1/deliverables/9/previews
```

→ 200

### GET `/api/v1/previews/{id}`
Consultar una vista previa.

```
GET http://localhost:8080/api/v1/previews/1
```

→ 200

### DELETE `/api/v1/previews/{id}`
Eliminar una vista previa.

```
DELETE http://localhost:8080/api/v1/previews/1
```

→ 204 No Content

## Favoritos (N:M cliente–servicio)

### POST `/api/v1/clients/{clientId}/favorite-services/{serviceId}`
Marcar servicio como favorito; 409 si el par ya existe.

```
POST http://localhost:8080/api/v1/clients/5/favorite-services/2
```

→ 201 Created + `Location: /api/v1/services/2`

### GET `/api/v1/clients/{clientId}/favorite-services`
Listar los servicios favoritos del cliente (más reciente primero).

```
GET http://localhost:8080/api/v1/clients/5/favorite-services
```

→ 200

### DELETE `/api/v1/clients/{clientId}/favorite-services/{serviceId}`
Quitar favorito; 404 si no existe la asociación.

```
DELETE http://localhost:8080/api/v1/clients/5/favorite-services/2
```

→ 204 No Content