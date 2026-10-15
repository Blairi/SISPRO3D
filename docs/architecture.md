# Arquitectura de SISPRO3D

Visual del proyecto descrito en `AGENTS.md`, `requerimientos.md` y `README.md`.
Los diagramas son Mermaid y renderizan en GitHub, VS Code o `mermaid-cli`.

## 1. Diagrama de capas

```mermaid
flowchart TB
    subgraph Cliente["Clientes"]
        Nav["🖥 Navegador<br/>HTML + Tailwind local<br/>(sin CDN)"]
        ApiCli["🔌 Cliente REST / Postman"]
    end

    subgraph Seguridad["Seguridad · security/"]
        SecConfig["SecurityConfig<br/>dos cadenas independientes"]
        WebChain["Cadena Web<br/>form login · sesión · CSRF · roles"]
        ApiChain["Cadena API · /api/v1/**<br/>stateless · Bearer JWT"]
        Jwt["JwtTokenService<br/>JwtAuthenticationFilter<br/>ApiCurrentUser"]
        Rev["RevokedTokenRepository<br/>revocación en logout"]
        Session["CustomUserDetailsService<br/>SecurityAccount · CurrentUser<br/>SessionHelper → sessionUser"]
    end

    subgraph WebStack["Presentación Web · Thymeleaf"]
        WC["user/controller/<br/>Home · Login · Register · ServiceCatalog<br/>QuoteRequest · ClientQuotes · ClientOrders<br/>ExpertDashboard · AdminDashboard · AdminUsers · AdminReports · Category"]
        Templates["templates/*.html<br/>home · login · register · services/<br/>client/ · expert/ · admin/ · fragments/"]
    end

    subgraph ApiStack["Presentación REST · /api/v1/*"]
        AC["api/controller/<br/>Account · Category · Service · Review · Quote<br/>WorkOrder · Deliverable · Preview · Thread<br/>Message · Favorite · Auth"]
        ADTO["api/dto + api/mapper"]
        ASE["api/service · Api*Service"]
        AEH["GlobalExceptionHandler<br/>ErrorDetail · 400/401/403/404/409/500"]
    end

    subgraph Dominio["Dominio · paquetes de feature"]
        Feature["category · offeredservice · quote · workorder<br/>deliverable · preview · thread · message<br/>review · favorite · user · revokedtoken"]
        Layers["cada feature: domain/ · dto/ · mapper/<br/>repository/ · service/ (+ service/impl/)"]
        Core["core/<br/>CrudService{Req, Res, ID}<br/>Forbidden · Conflict · BadRequest ·<br/>ResourceNotFoundException"]
    end

    subgraph Datos["Persistencia"]
        Repos["Spring Data JPA<br/>@ManyToOne / @OneToOne LAZY"]
        DB[("MariaDB · sispro3d_db<br/>schema.sql · ddl-auto=update")]
        H2[("H2 · MODE=MySQL · profile test<br/>ddl-auto=create-drop<br/>NO carga schema.sql")]
    end

    Nav --> WebChain
    ApiCli --> ApiChain
    Jwt -.-> ApiChain
    Rev -.-> ApiChain
    Session -.-> WebChain
    SecConfig --> WebChain
    SecConfig --> ApiChain

    WebChain --> WC
    WC --> Templates
    Session --> WC
    WC --> Dominio

    ApiChain --> AC
    AC --> ADTO
    AC --> ASE
    ASE --> Dominio
    ASE -.-> Core
    ADTO --> ASE

    Dominio --> Repos
    Repos --> DB
    Repos --> H2
```

> **Nota clave**: las dos presentaciones son **paralelas y no comparten** DTOs,
> servicios ni mappers. El código web usa controladores en `user/controller/` y
> servicios de dominio; la API usa `api/controller`, `api/service` (`Api*Service`)
> y `api/mapper`, que **delegan** en los mismos servicios de dominio.

## 2. Modelo de datos (relaciones principales)

```mermaid
erDiagram
    ACCOUNT ||--o{ OFFERED_SERVICE : "crea como EXPERT"
    CATEGORY ||--o{ OFFERED_SERVICE : "clasifica"
    ACCOUNT ||--o{ QUOTE : "solicita como CLIENT"
    OFFERED_SERVICE ||--o{ QUOTE : "recibe solicitudes"
    OFFERED_SERVICE ||--o{ REVIEW : "recibe reseñas"
    ACCOUNT ||--o{ REVIEW : "publica (única por servicio)"
    QUOTE ||--|| WORK_ORDER : "al aceptar se crea"
    WORK_ORDER ||--o{ DELIVERABLE : "contiene"
    DELIVERABLE ||--o{ PREVIEW : "muestra"
    WORK_ORDER ||--o| THREAD : "chat único"
    ACCOUNT ||--o{ MESSAGE : "escribe"
    THREAD ||--o{ MESSAGE : "agrupa"
    ACCOUNT ||--o{ FAVORITE : "marca"
    OFFERED_SERVICE ||--o{ FAVORITE : "es marcado"
    ACCOUNT ||--o{ REVOKED_TOKEN : "cierra sesión"
```

| Entidad | Notas |
|---|---|
| `ACCOUNT` | usuarios con `Role` ADMIN / CLIENT / EXPERT; email = username |
| `OFFERED_SERVICE` | estatus `PENDING → APPROVED / REJECTED`, controlado por ADMIN |
| `QUOTE` | `PENDING → ACCEPTED / REJECTED / EXPIRED`; al aceptar crea la orden |
| `WORK_ORDER` | `PENDING → IN_PROGRESS → IN_REVIEW → COMPLETED`; cancelable antes de `IN_REVIEW` |
| `REVIEW` | requiere orden `COMPLETED`; `UNIQUE (id_client, id_offered_service)` |
| `FAVORITE` | entidad N:M cliente–servicio; su lógica vive en otros servicios |
| `REVOKED_TOKEN` | persistencia de refresh tokens invalidados (logout) |