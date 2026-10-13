# Diagramas UML · SISPRO3D

Diagramas generados desde la implementación actual (`src/main/java`). Renderizan
en GitHub, VS Code o con `mermaid-cli`. Ver también `docs/architecture.md` para el
diagrama de capas y el modelo de datos.

## 1. Diagrama de clases

### 1.1 Capas de aplicación (presentaciones y dominio)

```mermaid
classDiagram
    direction LR

    %% ---------- Presentación WEB ----------
    class ServiceCatalogController
    class QuoteRequestController
    class ClientQuotesController
    class ClientOrdersController
    class ExpertDashboardController
    class AdminDashboardController

    %% ---------- Presentación REST ----------
    class QuoteApiController
    class WorkOrderApiController
    class ReviewApiController
    class DeliverableApiController
    class PreviewApiController
    class ThreadApiController
    class MessageApiController

    %% ---------- API services ----------
    class ApiQuoteService {
        +create(request)
        +findAll()
        +findByStatus(status)
        +reply(id, request)
        +accept(id) QuoteAcceptanceDTO
        +reject(id)
        +expire(id)
    }
    class ApiWorkOrderService {
        +create(request)
        +findAll()
        +findByClient/Expert/Status()
        +updateStatus(id, status)
    }
    class ApiReviewService
    class ApiDeliverableService
    class ApiPreviewService
    class ApiThreadService
    class ApiMessageService
    class ApiOrderAccess {
        +currentAccountId()
        +requireParticipant(order)
        +requireExpertOwner(order)
    }

    %% ---------- Servicios de dominio ----------
    class CrudService~Req, Res, ID~ {
        <<interface>>
        +create(Req) Res
        +findById(ID) Optional~Res~
        +update(ID, Req) Res
        +delete(ID)
    }
    class QuoteService {
        <<interface>>
        +respond(id, expertId, req)
        +accept(id, clientId) QuoteAcceptance
        +reject(id, clientId)
        +expire(id)
        +findByClientId/Service/Expert/Status()
    }
    class QuoteServiceImpl
    class WorkOrderService {
        <<interface>>
        +start(id, expertId)
        +markInReview(id, expertId)
        +requestChanges(id, clientId)
        +complete(id, clientId)
        +cancel(id, clientId)
    }
    class WorkOrderServiceImpl
    class ReviewService {
        <<interface>>
        +create(req)
        +findByOfferedServiceId()
    }
    class ReviewServiceImpl

    %% ---------- Seguridad ----------
    class SecurityConfig {
        +apiSecurityFilterChain()
        +webSecurityFilterChain()
    }
    class JwtAuthenticationFilter {
        <<OncePerRequestFilter>>
    }
    class JwtTokenService {
        +issueAccessToken(id, role)
        +issueRefreshToken(id, role)
        +parseAccessToken(token)
    }
    class ApiCurrentUser {
        <<static>>
        +requireAccountId() Long
    }
    class ApiAuthService
    class CurrentUser
    class RoleBasedAuthenticationSuccessHandler

    %% ============================================================
    %% Relaciones
    %% ============================================================

    %% Web -> dominio
    ServiceCatalogController --> QuoteService
    QuoteRequestController --> QuoteService
    ClientQuotesController --> QuoteService
    ClientOrdersController --> WorkOrderService
    ExpertDashboardController --> QuoteService
    ExpertDashboardController --> WorkOrderService
    QuoteRequestController ..> CurrentUser : id()/role()
    ClientQuotesController ..> CurrentUser
    ClientOrdersController ..> CurrentUser
    AdminDashboardController --> OfferedServiceService

    %% API controllers -> API services
    QuoteApiController --> ApiQuoteService
    WorkOrderApiController --> ApiWorkOrderService
    ReviewApiController --> ApiReviewService
    DeliverableApiController --> ApiDeliverableService
    PreviewApiController --> ApiPreviewService
    ThreadApiController --> ApiThreadService
    MessageApiController --> ApiMessageService

    %% API services -> dominio (delegación)
    ApiQuoteService --> QuoteService
    ApiQuoteService ..> ApiOrderAccess
    ApiWorkOrderService --> WorkOrderService
    ApiWorkOrderService ..> ApiOrderAccess
    ApiReviewService --> ReviewService
    ApiDeliverableService ..> ApiOrderAccess
    ApiPreviewService ..> ApiOrderAccess
    ApiThreadService ..> ApiOrderAccess
    ApiMessageService ..> ApiOrderAccess
    ApiQuoteService ..> ApiCurrentUser
    ApiWorkOrderService ..> ApiCurrentUser

    %% Dominio
    QuoteService <|.. QuoteServiceImpl : implementa
    WorkOrderService <|.. WorkOrderServiceImpl : implementa
    ReviewService <|.. ReviewServiceImpl : implementa
    CrudService <|-- QuoteService
    CrudService <|-- WorkOrderService
    CrudService <|-- ReviewService

    %% Aceptar una cotización crea la orden
    QuoteServiceImpl --> WorkOrderService : accept() crea WorkOrder

    %% Seguridad
    SecurityConfig --> JwtAuthenticationFilter
    JwtAuthenticationFilter ..> JwtTokenService : parseAccessToken
    ApiQuoteService ..> ApiCurrentUser
    ApiCurrentUser ..> SecurityContextHolder
    ApiAuthService ..> JwtTokenService
    ApiAuthService ..> AccountRepository
    RoleBasedAuthenticationSuccessHandler ..> RequestCache
```

### 1.2 Entidades de dominio (JPA)

```mermaid
classDiagram
    direction LR

    class Account {
        <<Entity>>
        Long idUser
        String name
        String email
        String password
        Role role
        String specialty
    }
    class Category {
        <<Entity>>
        Long id
        String name
    }
    class OfferedService {
        <<Entity>>
        Long id
        String title
        BigDecimal basePrice
        ServiceStatus status
    }
    class Quote {
        <<Entity>>
        Long id
        QuoteStatus status
        BigDecimal totalAmount
        LocalDate validUntil
        String description
    }
    class WorkOrder {
        <<Entity>>
        Long id
        WorkOrderStatus status
        LocalDateTime startedAt
        LocalDateTime completedAt
    }
    class Deliverable {
        <<Entity>>
        Long id
        String name
        String urlFile
        String fileType
    }
    class Preview {
        <<Entity>>
        Long id
        String caption
        String urlFile
        LocalDateTime createdAt
    }
    class Thread {
        <<Entity>>
        Long id
    }
    class Message {
        <<Entity>>
        Long id
        String content
        LocalDateTime timestamp
    }
    class Review {
        <<Entity>>
        Long id
        Integer rating
        String comment
    }
    class FavoriteService {
        <<Entity·N:M>>
        Long id
    }
    class RevokedToken {
        <<Entity>>
        String idToken
    }
    class ServiceStatus {
        <<enum>>
        PENDING
        APPROVED
        REJECTED
    }
    class QuoteStatus {
        <<enum>>
        PENDING
        ACCEPTED
        REJECTED
        EXPIRED
    }
    class WorkOrderStatus {
        <<enum>>
        PENDING
        IN_PROGRESS
        IN_REVIEW
        COMPLETED
        CANCELED
    }

    Account "1" --> "0..*" OfferedService : experto
    Category "1" --> "0..*" OfferedService
    Account "1" --> "0..*" Quote : cliente
    OfferedService "1" --> "0..*" Quote
    Quote "1" --> "0..1" WorkOrder : "aceptada → 1"
    WorkOrder "1" --> "0..*" Deliverable
    Deliverable "1" --> "0..*" Preview
    WorkOrder "1" --> "0..1" Thread
    Thread "1" --> "0..*" Message
    Account "1" --> "0..*" Message : autor
    Account "1" --> "0..*" Review : cliente
    OfferedService "1" --> "0..*" Review
    Account "1" --> "0..*" FavoriteService : cliente
    OfferedService "1" --> "0..*" FavoriteService
    OfferedService --> ServiceStatus
    Quote --> QuoteStatus
    WorkOrder --> WorkOrderStatus
```

## 2. Diagramas de secuencia

### 2.1 Autenticación JWT y llamada protegida a la API

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente REST
    participant AC as ApiAuthController
    participant AAS as ApiAuthService
    participant AR as AccountRepository
    participant JWT as JwtTokenService
    participant F as JwtAuthenticationFilter
    participant SC as SecurityContext

    C->>AC: POST /api/v1/auth/login {email, password}
    AC->>AAS: authenticate(email, password)
    AAS->>AR: findByEmail(email)
    AR-->>AAS: Account
    AAS->>AAS: passwordEncoder.matches(password)
    AAS->>JWT: issueAccessToken(id, role)
    AAS->>JWT: issueRefreshToken(id, role)
    JWT-->>AAS: access + refresh
    AAS-->>AC: TokenResponseDTO (Bearer, expiresIn)
    AC-->>C: 200 + {accessToken, refreshToken}

    Note over C,F: Llamada protegida con Authorization: Bearer <accessToken>
    C->>F: PUT /api/v1/quotes/{id}/accept (sin body)
    F->>JWT: parseAccessToken(header)
    JWT-->>F: JwtPrincipal(accountId, role)
    F->>SC: setAuthentication(JwtPrincipal, ROLE_*)
    F->>F: filterChain.doFilter()
```

### 2.2 Aceptación de cotización (crea la orden)

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente REST
    participant QCC as QuoteApiController
    participant AQS as ApiQuoteService
    participant QS as QuoteServiceImpl
    participant WOS as WorkOrderServiceImpl
    participant DB as "JPA / MariaDB"

    C->>QCC: PUT /api/v1/quotes/{id}/accept
    QCC->>AQS: accept(id)
    AQS->>AQS: ApiCurrentUser.requireAccountId()
    AQS->>QS: accept(id, clientId)
    QS->>QS: validar rol CLIENT + dueño de la cotización
    QS->>QS: status == PENDING y vigencia no vencida
    QS->>DB: guardar Quote ACCEPTED
    QS->>WOS: create(WorkOrderRequest{clientId, quoteId})
    WOS->>WOS: validar quote ACCEPTED + sin orden previa
    WOS->>DB: guardar WorkOrder PENDING
    WOS-->>QS: WorkOrderResponse
    QS-->>AQS: QuoteAcceptance(quote, order)
    AQS-->>QCC: QuoteAcceptanceDTO{quote, workOrder}
    QCC-->>C: 200 OK con quote + workOrder
```

### 2.3 Catálogo web: «Contratar» → login → formulario

```mermaid
sequenceDiagram
    autonumber
    participant N as Visitante anónimo
    participant SC as ServiceCatalogController
    participant SEC as Spring Security (web chain)
    participant LG as Login / RoleBasedSuccessHandler
    participant QRC as QuoteRequestController
    participant QS as QuoteService
    participant CU as CurrentUser

    N->>SC: GET /services/{id} (catálogo público)
    SC-->>N: 200 services/detail.html con «Contratar»

    N->>QRC: GET /services/{id}/quote (ruta autenticada)
    QRC-->>N: 302 a /login (saved request guardado)
    N->>LG: POST /login (email, password, _csrf)
    LG->>LG: éxito → restaurar saved request (o home por rol)
    LG-->>N: 302 a /services/{id}/quote
    N->>QRC: GET /services/{id}/quote (sesión CLIENT)
    QRC->>CU: id()/role() == CLIENT
    QRC-->>N: 200 services/quote-request.html

    N->>QRC: POST /services/{id}/quote (formulario + _csrf)
    QRC->>CU: id() = cliente de sesión
    QRC->>QS: create(QuoteRequest{clientId de sesión, serviceId, description})
    QS-->>QRC: QuoteResponse PENDING
    QRC-->>N: 302 a /client/quotes
```

### 2.4 Reseña: validación con orden COMPLETED

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente REST
    participant RC as ReviewApiController
    participant ARS as ApiReviewService
    participant RS as ReviewServiceImpl
    participant RQ as WorkOrderRepository
    participant RR as ReviewRepository

    C->>RC: POST /api/v1/services/{serviceId}/reviews {rating, comment}
    RC->>ARS: create(serviceId, request)
    ARS->>ARS: ApiCurrentUser.requireAccountId()
    ARS->>RS: create(ReviewRequest{clientId, serviceId, ...})
    RS->>RS: rol del cliente == CLIENT
    RS->>RQ: findByClient_AndService_AndStatus(clientId, serviceId, COMPLETED)
    RQ-->>RS: orden COMPLETED existente
    RS->>RR: findByClient_IdUserAndOfferedService_Id(...) ¿ya reseñó?
    RR-->>RS: vacía → primera reseña
    RS->>RS: rating entre 1 y 5
    RS->>RR: save(Review)
    RS-->>ARS: ReviewResponse
    ARS-->>RC: ReviewResponseDTO
    RC-->>C: 201 Created + Location
```

## 3. Diagramas de actividad

### 3.1 Ciclo de contratación (actividad)

```mermaid
flowchart TD
    Inicio([Cliente ve servicio APPROVED]) --> CTA{¿Puede contratar?}
    CTA -- Anónimo --> Login[[redirect a /login]]
    Login --> Form
    CTA -- EXPERT/ADMIN --> FinX([✗ rechazado por rol])
    CTA -- CLIENT --> Form[Formulario de solicitud<br/>/services/{id}/quote]

    Form --> Guardar{¿Servicio APPROVED?}
    Guardar -- No --> Err1[[400/404 · no se crea nada]]
    Guardar -- Sí --> CreaQuote[quote PENDING guardada]

    CreaQuote --> Reply{¿Experto del servicio?}
    Reply -- No --> Err2[[403 · solo el experto propietario]]
    Reply -- Sí --> Responde[quote responde: monto + vigencia<br/>sigue PENDING]

    Responde --> Decide{¿Cotización PENDING y vigente?}
    Decide -- Vencida --> Err3[[rechazada · vigencia expirada]]
    Decide -- Sí --> Acepta{Cliente decide}
    Acepta -- Rechazar --> Rej[quote REJECTED · fin]
    Acepta -- Aceptar --> Orden[quote ACCEPTED +<br/>WorkOrder PENDING creada]

    Orden --> Trans{WFS en orden}
    Trans --> P[PENDING → IN_PROGRESS<br/>inicia el EXPERT]
    Trans --> C1[CANCELED · cliente<br/>desde PENDING o IN_PROGRESS]
    P --> IR[IN_PROGRESS → IN_REVIEW<br/>entrega el EXPERT]
    IR --> C2[IN_REVIEW → IN_PROGRESS<br/>solicita cambios el CLIENT]
    IR --> Done[IN_REVIEW → COMPLETED<br/>aprueba el CLIENT]
    Done --> Review((cliente con orden COMPLETED<br/>puede dejar review))
```

### 3.2 Máquinas de estado

**Orden de trabajo**

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> IN_PROGRESS : start() · EXPERT
    PENDING --> CANCELED : cancel() · CLIENT
    IN_PROGRESS --> IN_REVIEW : markInReview() · EXPERT
    IN_PROGRESS --> CANCELED : cancel() · CLIENT
    IN_REVIEW --> IN_PROGRESS : requestChanges() · CLIENT
    IN_REVIEW --> COMPLETED : complete() · CLIENT
    COMPLETED --> [*]
    CANCELED --> [*]
```

**Cotización**

```mermaid
stateDiagram-v2
    [*] --> PENDING : create()
    PENDING --> PENDING : respond() fija monto y vigencia
    PENDING --> ACCEPTED : accept() · genera WorkOrder PENDING
    PENDING --> REJECTED : reject()
    PENDING --> EXPIRED : expire() · vigencia vencida
    ACCEPTED --> [*]
    REJECTED --> [*]
    EXPIRED --> [*]
```

**Servicio (aprobación por ADMIN)**

```mermaid
stateDiagram-v2
    [*] --> PENDING : create() · EXPERT
    PENDING --> APPROVED : approve() · ADMIN
    PENDING --> REJECTED : reject() · ADMIN
    APPROVED --> PENDING : update() vuelve a revisión
    APPROVED --> [*]
    REJECTED --> [*]
```