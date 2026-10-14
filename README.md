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

## Autenticación y login (Spring Security)

El login usa **Spring Security** con usuarios en la base de datos (`account`) y contraseñas con hash BCrypt:

- Página de login: `http://localhost:8080/login` (formulario con `email` y `password`).
- Tras iniciar sesión aterrizas según tu rol: cliente → `/client/quotes`, experto → `/expert/dashboard`, admin → `/admin/dashboard`. Si entraste a `/login` desde un enlace protegido, la sesión te devuelve a ese destino.
- Cerrar sesión: botón en el header (POST `/logout`).

Áreas públicas (sin login): `/`, `/login`, `/register`, el **catálogo público** `/services` (listado) y `/services/{id}` (detalle con ficha del experto, estilo marketplace), archivos estáticos (`/css/**`, `/js/**`, `/vendor/**`) y la API REST `/api/v1/**`. El catálogo solo muestra servicios `APPROVED`; los no aprobados responden 404.
Áreas protegidas por rol: `/admin/**` solo `ADMIN`, `/expert/**` solo `EXPERT`; el resto (incluido el detalle de orden `/orders/{id}` y su chat) requiere sesión iniciada — el detalle de una orden solo lo ven sus dos partes, y los archivos en `/files/**` solo los descarga el cliente o el experto de la orden propietaria (el resto recibe 404).

**Páginas de error de la web:** los errores de la interfaz web (no de la API) se renderizan con plantillas propias en español — `templates/error/404.html`, `templates/error/500.html` y el fallback genérico `templates/error.html` (reemplaza la página Whitelabel). Reutilizan el header/footer y muestran un mensaje genérico con navegación, sin filtrar mensajes técnicos ni trazas; el contrato de error JSON de `/api/v1/**` (`ErrorDetail`) no cambia.

### Credenciales de demostración

El script `scripts/data.sql` incluye cuentas con contraseñas reales (hash BCrypt):

| Rol    | Correo                       | Contraseña |
|--------|------------------------------|------------|
| ADMIN  | `ricardo.solano@render3d.mx` | `Admin123!` |
| EXPERT | `emilio.vargas@email.com`    | `Expert123!` |
| CLIENT | `mariana.stein@estudio.com`  | `Client123!` |

> **Importante:** las cuentas sembradas usaban antes hashes placeholder (`$2a$10$dummyhash*`) que ya **no** validan. Si ya tenés una base de datos con esos datos, volvé a ejecutar `scripts/data.sql` sobre una BD nueva (o re-generá los hashes) para poder iniciar sesión.

Los assets de front-end (Tailwind, `css/sispro3d.css`) viven en `src/main/resources/static/` — **no** se usan CDN públicos.

## API REST

Los endpoints REST viven bajo `/api/v1` y devuelven DTOs planos (sin grafos de entidades).

**Autenticación:** toda la API (salvo `/api/v1/auth/**`) exige un token JWT de acceso: cabecera `Authorization: Bearer <accessToken>`. Los tokens se obtienen en `POST /api/v1/auth/login` y se renuevan/cancelan con `/refresh` y `/logout`.

**Actores desde el token:** los cuerpos de petición **ya no llevan** `clientId`/`expertId`/`adminId`. La identidad del usuario que actúa se toma del token y los endpoints validan rol, propiedad y participación; una violación de rol o de pertenencia responde **403** (antes 400/500).

Semántica de errores: `ErrorDetail` con `{timestamp, status, error, message, path, details[]}` — 400 validación/regla de negocio, **401 sin token o token inválido**, **403 rol o propiedad incorrecta**, 404 no encontrado, 409 conflicto de integridad/duplicado; creaciones responden 201 + `Location`, borrados 204 y `logout` también 204.

### Autenticación — `/api/v1/auth` (pública)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/auth/login` | `{email, password}` → `{accessToken, refreshToken}` (401 indiferenciado si no existen o no coinciden) |
| POST | `/api/v1/auth/refresh` | `{refreshToken}` → nuevo par de tokens (401 si revocado/desconocido) |
| POST | `/api/v1/auth/logout` | `{refreshToken}` revoca el refresh (204) |

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
| POST | `/api/v1/work-orders` | Crear orden a partir de una cotización `ACCEPTED` y no usada (201 + Location; 409 si ya tiene orden) |
| GET | `/api/v1/work-orders` | Listar órdenes del actor autenticado |
| GET | `/api/v1/work-orders/client/{clientId}` | Listar órdenes de un cliente |
| GET | `/api/v1/work-orders/expert/{expertId}` | Listar órdenes de un experto |
| GET | `/api/v1/work-orders/status/{status}` | Listar órdenes por estado |
| GET | `/api/v1/work-orders/{id}` | Consultar orden |
| PUT | `/api/v1/work-orders/{id}/status` | Cambiar estado (validado por transiciones: `start`, `mark-in-review`, `request-changes`, `complete`, `cancel`) |

### Entregables — bajo `/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/work-orders/{orderId}/deliverables` | Listar entregables de una orden |
| POST | `/api/v1/work-orders/{orderId}/deliverables` | Subir entregable (EXPERT propietario, orden IN_PROGRESS/IN_REVIEW) |
| GET | `/api/v1/deliverables/{id}` | Consultar entregable (solo participantes) |
| PUT | `/api/v1/deliverables/{id}` | Actualizar entregable |
| DELETE | `/api/v1/deliverables/{id}` | Eliminar entregable (borra sus vistas previas) |

### Cotizaciones — `/api/v1/quotes`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/quotes` | Solicitar cotización (CLIENT + servicio APPROVED) |
| GET | `/api/v1/quotes` | Listar cotizaciones del actor autenticado |
| GET | `/api/v1/quotes/status/{status}` | Listar cotizaciones por estado |
| GET | `/api/v1/quotes/service/{serviceId}` | Listar cotizaciones de un servicio |
| GET | `/api/v1/quotes/{id}` | Consultar cotización |
| PUT | `/api/v1/quotes/{id}/reply` | Responder con monto y vigencia (EXPERT propietario) |
| PUT | `/api/v1/quotes/{id}/accept` | Aceptar (CLIENT solicitante) — crea la orden en el mismo commit |
| PUT | `/api/v1/quotes/{id}/reject` | Rechazar (CLIENT solicitante) |
| PUT | `/api/v1/quotes/{id}/expire` | Expirar una cotización vencida (regla de `validUntil`) |
| DELETE | `/api/v1/quotes/{id}` | Eliminar cotización |

### Hilos de chat — bajo `/api/v1` (acceso restringido a participantes)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/work-orders/{orderId}/thread` | Abrir hilo de la orden (409 si ya existe) |
| GET | `/api/v1/work-orders/{orderId}/thread` | Consultar hilo por orden |
| GET | `/api/v1/threads/{id}` | Consultar hilo |
| DELETE | `/api/v1/threads/{id}` | Eliminar hilo |

### Mensajes — bajo `/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/threads/{threadId}/messages` | Publicar mensaje (solo participantes; rechaza órdenes COMPLETED/CANCELED) |
| GET | `/api/v1/threads/{threadId}/messages` | Listar mensajes del hilo (ascendente) |
| GET | `/api/v1/messages/{id}` | Consultar mensaje |
| DELETE | `/api/v1/messages/{id}` | Eliminar mensaje |

### Vistas previas — bajo `/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/deliverables/{deliverableId}/previews` | Adjuntar vista previa (EXPERT propietario, orden IN_PROGRESS/IN_REVIEW) |
| GET | `/api/v1/deliverables/{deliverableId}/previews` | Listar vistas previas de un entregable |
| GET | `/api/v1/previews/{id}` | Consultar vista previa |
| PUT | `/api/v1/previews/{id}` | Actualizar vista previa |
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
- **Aceptar/Rechazar cotización:** Solo el `CLIENT` solicitante sobre cotizaciones vigentes (`PENDING` y no vencidas). **Aceptar crea la orden de trabajo** en el mismo commit transaccional; la respuesta incluye la cotización y la nueva orden.
- **Expirar cotización:** Una cotización cuyo `validUntil` ya pasó se marca `EXPIRED` y ya no puede aceptarse ni rechazarse.

El módulo `workorder` gestiona la orden de trabajo:

- **Crear orden:** Al aceptar una cotización `ACCEPTED` sin orden asociada (una orden por cotización).
- **Iniciar orden / Marcar en revisión:** Solo el `EXPERT` propietario.
- **Solicitar cambios / Completar / Cancelar:** Solo el `CLIENT`.

El módulo `deliverable`/`preview` permite al `EXPERT` propietario subir entregables y vistas previas solo mientras la orden está `IN_PROGRESS` o `IN_REVIEW`. Desde el detalle de la orden (`/orders/{id}`) el experto también puede **eliminar** cada vista previa (una en una) o un entregable completo; al borrar un entregable se eliminan sus vistas previas y todos los archivos asociados.

Desde la interfaz web, el experto adjunta el archivo real desde el formulario del entregable (`.obj`, `.fbx`, `.stl` o `.blend`, o un paquete comprimido `.zip`, `.rar` o `.7z`) y la imagen de cada vista previa (`.png`, `.jpg`, `.jpeg` o `.webp`), hasta 25 MB para modelos e imágenes y hasta 100 MB para los comprimidos. El archivo se guarda en la carpeta configurada por `app.storage.location` (por defecto `uploads/`) bajo un nombre único (UUID) y la fila conserva solo la ruta relativa en `urlFile`; `fileType` se deduce de la extensión. Los archivos se sirven en `/files/**`, donde solo el cliente o el experto de la orden propietaria pueden descargarlos (cualquier otra cuenta, incluido un admin, recibe 404 para no revelar su existencia); se borran al eliminar o reemplazar el entregable o la vista previa. Las filas creadas por la API REST siguen guardando URLs externas, que las plantillas renderizan tal cual.

> **Nota:** la API REST `/api/v1` todavía recibe una referencia (`urlFile`) en lugar de un multipart; la subida de binarios solo aplica a la interfaz web. La referencia del archivo (`url_file`, `VARCHAR(255)`) se reutiliza sin cambios de esquema.

El módulo `thread`/`message` implementa el chat de la orden:

- **Crear hilo:** Solo el `CLIENT` o el `EXPERT` de la orden (un hilo por orden).
- **Enviar mensaje:** Solo los participantes del hilo (cliente o experto de la orden).

### Búsqueda y filtros del catálogo público

El catálogo `/services` admite búsqueda y filtros por parámetros de consulta (resultados compartibles por URL), siempre sobre servicios `APPROVED`: **texto libre** (`q`) que coincide, sin distinguir mayúsculas, como subcadena del título, la descripción o el nombre del experto; **categoría** (`categoryId`); **rango de precio** base inclusive (`minPrice`, `maxPrice`); **tiempo de entrega máximo** en días (`maxDeliveryDays`); y **orden de resultados** (`sort`: `NEWEST` por defecto, `PRICE_ASC` o `PRICE_DESC`). Los filtros se combinan con una conjunción. Cuando ningún servicio coincide, la página muestra un estado de «sin resultados» distinto del catálogo vacío; si `minPrice` es mayor que `maxPrice` se muestra un mensaje de validación y no se aplica el filtro de precio.

### Reportes de administración

Desde `/admin/reports` (solo `ADMIN`, con enlace en la navegación) el administrador ve métricas derivadas de los datos existentes: **servicios más solicitados** (ranking por número de cotizaciones recibidas, con las órdenes generadas), **desempeño de expertos** (servicios, cotizaciones respondidas, órdenes por estado, órdenes completadas y calificación promedio, o «sin reseñas») y **resumen de actividad** (cuentas por rol, servicios/cotizaciones/órdenes por estado y total de reseñas). El formulario acepta un rango opcional de fechas `from`/`to` que filtra por fecha de creación (inclusive en ambos extremos); si `from` es posterior a `to` se muestra un mensaje de validación y se presentan los totales históricos. La página es de solo lectura: no crea ni modifica registros.


1. Suite: mvn -o test → 329 verdes.
2. Web (necesita MariaDB + spring-boot:run).
3. 
3. Flujo completo verificado por curl: login CLIENT (bm/Client123!) 
→ «Contratar» → solicitud → login EXPERT (emilio.vargas@email.com/Expert123!) 
4. → responda en /expert/quotes → cliente acepta → orden → experto inicia/envía a revisión 
5. → registra entregable+preview → cliente completa → /orders/{id} muestra chat → reseña 
6. 5★ visible anónimamente. Rechazos comprobados: reseña duplicada, rating 0/6, mensaje en 
7. orden concluida, outsider 404, entregable en fase incorrecta.
8. 
3. API: POST /api/v1/auth/login → usar accessToken en Authorization: Bearer; verifiqué 401 sin token, refresh, logout y reuso del refresh revocado → 401.