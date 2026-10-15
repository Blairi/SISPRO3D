# Relaciones de la Base de Datos — SISPRO3D

Relaciones 1:N que se usan en los escenarios de evidencia de la API REST
(`/api/v1`). Están declaradas en las entidades JPA (`@ManyToOne` + `@JoinColumn`)
y en `schema.sql`.

1. **Account (EXPERT) 1 —— N OfferedService**
   → `offered_service.id_expert` → `account.id_user` (`NOT NULL`)

2. **Category 1 —— N OfferedService**
   → `offered_service.id_category` → `category.id_category` (`NOT NULL`)

3. **OfferedService 1 —— N Review**
   → `review.id_offered_service` → `offered_service.id_offered_service` (`NOT NULL`)
   - Unicidad: un cliente solo reseña un servicio una vez (`review.id_client` + `review.id_offered_service`)

4. **WorkOrder 1 —— N Deliverable**
   → `deliverable.id_order` → `work_order.id` (`NOT NULL`, `ON DELETE CASCADE`)

5. **OfferedService 1 —— N Quote**
   → `quote.id_service` → `offered_service.id_offered_service` (`NOT NULL`)
   - Una cotización es solicitada por una sola cuenta CLIENT (`quote.id_client` → `account.id_user`).

6. **WorkOrder 1 —— 1 Thread**
   → `thread.id_order` → `work_order.id` (`NOT NULL`, `UNIQUE`)
   - Restricción de unicidad: una orden solo tiene un hilo de conversación (una segunda creación devuelve 409).

7. **Thread 1 —— N Message**
   → `message.id_thread` → `thread.id` (`NOT NULL`, `ON DELETE CASCADE`)
   - `message.user_id` → `account.id_user` (`NOT NULL`): autor del mensaje.

8. **Deliverable 1 —— N Preview**
   → `preview.deliverable_id` → `deliverable.id` (`NOT NULL`)

9. **Account (CLIENT) N —— M OfferedService** (favoritos)
   → tabla intermedia `favorite_service`:
     - `favorite_service.id_client` → `account.id_user` (`NOT NULL`, `ON DELETE CASCADE`)
     - `favorite_service.id_offered_service` → `offered_service.id_offered_service` (`NOT NULL`, `ON DELETE CASCADE`)
   - Unicidad del par (`favorite_service.id_client` + `favorite_service.id_offered_service`,
     `uq_favorite_client_service`): un cliente no puede marcar dos veces el mismo servicio (un par duplicado devuelve 409).

> Nota: `scripts/data.sql` no se ejecuta automáticamente; cargar a mano contra
> MariaDB tras el primer arranque (ver `README.md`).
