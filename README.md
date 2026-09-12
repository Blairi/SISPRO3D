# SISPRO3D

Plataforma de servicios de modelado 3D.

## Requisitos

- Java 21
- Maven
- MariaDB corriendo en `localhost:3306`
- IntelliJ IDEA

## Configurar la base de datos

Asegúrate de que MariaDB esté corriendo en `localhost:3306`. Luego crea el usuario y la base de datos:

```sql
CREATE DATABASE IF NOT EXISTS sispro3d_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'MiPasswordSegura123!';
GRANT ALL PRIVILEGES ON sispro3d_db.* TO 'appuser'@'localhost';
FLUSH PRIVILEGES;
```

## Arrancar el proyecto

### Desde IntelliJ

1. Abrir `SISPRO3DApplication.java` (`src/main/java/com/sispro3d/unam/SISPRO3DApplication.java`)
2. Hacer clic en el ícono de **Run** en la gutera (al lado del `main()`)
3. Cuando veas `===== SISPRO3D & Spring Boot =====` en la consola, el servidor está listo en `http://localhost:8080`

### Desde la terminal

```bash
mvn spring-boot:run
```

## Cargar datos de ejemplo (obligatorio la primera vez)

El archivo `scripts/data.sql` contiene datos de prueba (usuarios, categorías, servicios, reseñas, etc.). **No se ejecuta automáticamente** — debés correrlo manualmente después del primer arranque.

1. Abrir la terminal de IntelliJ (`View > Tool Windows > Terminal`)
2. Conectate a MariaDB y ejecutá el script:

```bash
mariadb -u <user> -p<password> sispro3d_db < scripts/data.sql
```

O ejecutalo desde la consola de MariaDB:

```sql
source scripts/data.sql;
```

> **Importante:** El esquema se sincroniza en cada arranque (`ddl-auto=update` junto con `schema.sql`). Si cambiás de entorno o base de datos, volvé a ejecutar este script.

## Correr tests

Los tests usan H2 en memoria (no necesitan MariaDB).

### Desde IntelliJ

Clic derecho sobre una clase o método de test > **Run**.

### Desde la terminal

```bash
mvn test                                           # todos los tests
mvn test -Dtest=AccountServiceImplTest             # una clase
mvn test -Dtest=AccountServiceImplTest#testCreate  # un método
```

## API REST

Los endpoints REST viven bajo `/api/v1` y devuelven DTOs planos (sin grafos de entidades). Semántica de errores: `ErrorDetail` con `{status, message, details[]}` — 400 validación/regla de negocio, 404 no encontrado, 409 conflicto de integridad; creaciones responden 201 + `Location` y borrados 204.

### Cuentas — `/api/v1/accounts`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/accounts/{id}` | Consultar cuenta |
| POST | `/api/v1/accounts` | Crear cuenta |
| PUT | `/api/v1/accounts/{id}` | Actualizar cuenta |
| DELETE | `/api/v1/accounts/{id}` | Eliminar cuenta |

### Categorías — `/api/v1/categories`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/categories` | Listar categorías |
| GET | `/api/v1/categories/{id}` | Consultar categoría |
| POST | `/api/v1/categories` | Crear categoría |
| PUT | `/api/v1/categories/{id}` | Actualizar categoría |
| DELETE | `/api/v1/categories/{id}` | Eliminar categoría (409 si tiene servicios) |

### Servicios — `/api/v1/services`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/services?categoryId=&expertId=` | Listar servicios (filtros opcionales) |
| GET | `/api/v1/services/{id}` | Consultar servicio |
| POST | `/api/v1/services` | Crear servicio (EXPERT, estado PENDING) |
| PUT | `/api/v1/services/{id}` | Actualizar servicio |
| DELETE | `/api/v1/services/{id}` | Eliminar servicio (409 si tiene reseñas/cotizaciones) |

### Reseñas — bajo `/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/services/{serviceId}/reviews` | Listar reseñas de un servicio |
| POST | `/api/v1/services/{serviceId}/reviews` | Crear reseña (409 si el cliente ya reseñó) |
| PUT | `/api/v1/reviews/{id}` | Actualizar reseña |
| DELETE | `/api/v1/reviews/{id}` | Eliminar reseña |

### Órdenes de trabajo — `/api/v1/work-orders`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/work-orders/{id}` | Consultar orden |
| PUT | `/api/v1/work-orders/{id}/status` | Cambiar estado (validado por transiciones) |

### Entregables — bajo `/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/work-orders/{orderId}/deliverables` | Listar entregables de una orden |
| POST | `/api/v1/work-orders/{orderId}/deliverables` | Subir entregable |
| PUT | `/api/v1/deliverables/{id}` | Actualizar entregable |
| DELETE | `/api/v1/deliverables/{id}` | Eliminar entregable (borra sus vistas previas) |

### Cotizaciones — `/api/v1/quotes`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/quotes` | Solicitar cotización (CLIENT + servicio APPROVED) |
| GET | `/api/v1/quotes/{id}` | Consultar cotización |
| PUT | `/api/v1/quotes/{id}/reply` | Responder con monto y vigencia (EXPERT propietario) |
| PUT | `/api/v1/quotes/{id}/accept` | Aceptar (CLIENT solicitante) |
| PUT | `/api/v1/quotes/{id}/reject` | Rechazar (CLIENT solicitante) |
| DELETE | `/api/v1/quotes/{id}` | Eliminar cotización |

### Hilos de chat — bajo `/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/work-orders/{orderId}/thread` | Abrir hilo de la orden (409 si ya existe) |
| GET | `/api/v1/work-orders/{orderId}/thread` | Consultar hilo por orden |
| GET | `/api/v1/threads/{id}` | Consultar hilo |
| DELETE | `/api/v1/threads/{id}` | Eliminar hilo |

### Mensajes — bajo `/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/threads/{threadId}/messages` | Publicar mensaje (solo participantes) |
| GET | `/api/v1/threads/{threadId}/messages` | Listar mensajes del hilo (ascendente) |
| GET | `/api/v1/messages/{id}` | Consultar mensaje |
| DELETE | `/api/v1/messages/{id}` | Eliminar mensaje |

### Vistas previas — bajo `/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/deliverables/{deliverableId}/previews` | Adjuntar vista previa (EXPERT propietario, orden IN_PROGRESS/IN_REVIEW) |
| GET | `/api/v1/deliverables/{deliverableId}/previews` | Listar vistas previas de un entregable |
| GET | `/api/v1/previews/{id}` | Consultar vista previa |
| DELETE | `/api/v1/previews/{id}` | Eliminar vista previa |

### Favoritos (N:M cliente–servicio) — `/api/v1/clients/{clientId}/favorite-services`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/clients/{clientId}/favorite-services/{serviceId}` | Marcar servicio como favorito (409 si ya lo es) |
| GET | `/api/v1/clients/{clientId}/favorite-services` | Listar favoritos del cliente |
| DELETE | `/api/v1/clients/{clientId}/favorite-services/{serviceId}` | Quitar favorito (404 si no existe la asociación) |

## Estructura del proyecto

```
src/main/java/com/sispro3d/unam/
├── core/                  # Excepciones y interfaces genéricas (CrudService)
├── user/                  # Cuentas (ADMIN, CLIENT, EXPERT)
├── category/              # Categorías de servicio (stub)
├── offeredservice/        # Servicios ofrecidos con flujo de aprobación
├── review/                # Reseñas de clientes
├── quote/                 # Cotizaciones con flujo de aprobación
├── workorder/             # Órdenes de trabajo (nacen de cotización aceptada)
├── deliverable/           # Archivos entregables de una orden
├── preview/               # Vistas previas de un entregable
├── thread/                # Hilos de conversación de una orden
└── message/               # Mensajes de chat de un hilo
```

### Lógica de negocio implementada

El módulo `offeredservice` tiene un flujo de aprobación:

- **Crear servicio:** Solo usuarios con rol `EXPERT`. El estado se fuerza a `PENDING`.
- **Aprobar servicio:** Solo usuarios con rol `ADMIN`. El servicio debe estar en `PENDING`.
- **Rechazar servicio:** Misma validación que aprobar. Estado cambia a `REJECTED`.

El módulo `quote` tiene un flujo de cotización:

- **Solicitar cotización:** Solo usuarios con rol `CLIENT` sobre un servicio `APPROVED`. El estado se fuerza a `PENDING`.
- **Responder cotización:** Solo el `EXPERT` propietario del servicio. Establece `totalAmount` y `validUntil`.
- **Aceptar/Rechazar cotización:** Solo el `CLIENT` solicitante sobre cotizaciones `PENDING`.

El módulo `workorder` gestiona la orden de trabajo:

- **Crear orden:** Solo el `CLIENT` a partir de una cotización `ACCEPTED` (una orden por cotización).
- **Iniciar orden / Marcar en revisión:** Solo el `EXPERT` propietario.
- **Solicitar cambios / Completar / Cancelar:** Solo el `CLIENT`.

El módulo `deliverable`/`preview` permite al `EXPERT` propietario subir entregables y vistas previas solo mientras la orden está `IN_PROGRESS` o `IN_REVIEW`.

El módulo `thread`/`message` implementa el chat de la orden:

- **Crear hilo:** Solo el `CLIENT` o el `EXPERT` de la orden (un hilo por orden).
- **Enviar mensaje:** Solo los participantes del hilo (cliente o experto de la orden).
