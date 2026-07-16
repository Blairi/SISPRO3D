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

> **Importante:** El esquema se recrea en cada arranque (`ddl-auto=create`). Si borrás la base de datos o cambias de entorno, volvé a ejecutar este script.

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

## Estructura del proyecto

```
src/main/java/com/sispro3d/unam/
├── core/                  # Excepciones y interfaces genéricas (CrudService)
├── user/                  # Cuentas (ADMIN, CLIENT, EXPERT)
├── category/              # Categorías de servicio (stub)
├── offeredservice/        # Servicios ofrecidos con flujo de aprobación
├── review/                # Reseñas de clientes
├── quote/                 # Cotizaciones (solo POJO)
├── workorder/             # Órdenes de trabajo (solo POJO)
├── deliverable/           # Archivos entregables (solo POJO)
├── preview/               # Vistas previas (solo POJO)
├── thread/                # Hilos de conversación (solo POJO)
└── message/               # Mensajes de chat (solo POJO)
```

### Lógica de negocio implementada

El módulo `offeredservice` tiene un flujo de aprobación:

- **Crear servicio:** Solo usuarios con rol `EXPERT`. El estado se fuerza a `PENDING`.
- **Aprobar servicio:** Solo usuarios con rol `ADMIN`. El servicio debe estar en `PENDING`.
- **Rechazar servicio:** Misma validación que aprobar. Estado cambia a `REJECTED`.

Los módulos marcados como "solo POJO" tienen su tabla definida en `schema.sql` pero aún no tienen implementación JPA en Java.
