## Purpose

Expose Favorites as a secure, consumer-friendly REST API that uses JWT identity and enforces ownership and role constraints. This lets external clients manage a client's favorite services via the API while preventing cross-client access.

## Requirements

### Requirement: Favorites API requires JWT authentication and CLIENT role

The system SHALL require a valid JWT access token for all Favorites API endpoints (both `/api/v1/clients/{clientId}/favorite-services/**` and `/api/v1/me/favorite-services/**`). The acting account SHALL be identified from the token. Only accounts whose role is `CLIENT` SHALL be allowed to access Favorites endpoints; other roles SHALL receive an authorization failure (403).

#### Scenario: Unauthenticated request is rejected
- **WHEN** a request to any Favorites endpoint is made without an `Authorization: Bearer` token
- **THEN** the response status SHALL be 401

#### Scenario: Non-CLIENT role is denied
- **WHEN** a user with role `EXPERT` or `ADMIN` calls a Favorites endpoint with a valid token
- **THEN** the response status SHALL be 403

#### Scenario: CLIENT with valid token is allowed
- **WHEN** a user with role `CLIENT` calls a Favorites endpoint with a valid token
- **THEN** access SHALL be granted (subject to ownership rules)

### Requirement: Ownership is enforced for path-based client endpoints

For requests to `/api/v1/clients/{clientId}/favorite-services/**`, the `clientId` in the path SHALL be equal to the `accountId` extracted from the JWT token. If the referenced client does not exist, the system SHALL return 404. If the referenced client exists but its id differs from the token's `accountId`, the system SHALL return 403. The system SHALL continue to validate the target service exists (404) and reject duplicates (409).

#### Scenario: Cross-client access is denied
- **WHEN** a CLIENT token for account A calls `/api/v1/clients/{clientIdB}/favorite-services`
- **THEN** the response status SHALL be 403

#### Scenario: Same-client access is allowed
- **WHEN** a CLIENT token for account A calls `/api/v1/clients/{clientIdA}/favorite-services`
- **THEN** the operation SHALL proceed according to its contract

### Requirement: /me endpoints use token identity

The system SHALL expose `/api/v1/me/favorite-services` (GET) and `/api/v1/me/favorite-services/{serviceId}` (POST, DELETE) to manage the authenticated CLIENT's favorites. These endpoints SHALL derive the client identity from the JWT token (no clientId in the path). They SHALL behave equivalently to the path-based endpoints for the same client (201 on associate with Location, 204 on delete, 200 on list, 404 if service/client not found, 409 if duplicate).

#### Scenario: Associate favorite via /me
- **WHEN** an authenticated CLIENT POSTs to `/api/v1/me/favorite-services/{serviceId}`
- **THEN** the service SHALL be added to that CLIENT's favorites
- **AND** the response SHALL be 201 with `Location` header

#### Scenario: List favorites via /me
- **WHEN** an authenticated CLIENT GETs `/api/v1/me/favorite-services`
- **THEN** the response SHALL be 200 with a list of their favorite services

#### Scenario: Remove favorite via /me
- **WHEN** an authenticated CLIENT DELETEs `/api/v1/me/favorite-services/{serviceId}`
- **THEN** the favorite association SHALL be removed
- **AND** the response SHALL be 204

#### Scenario: /me returns 404 when service unknown
- **WHEN** an authenticated CLIENT POSTs `/api/v1/me/favorite-services/{serviceId}` with unknown serviceId
- **THEN** the response SHALL be 404

#### Scenario: /me returns 409 on duplicate
- **WHEN** an authenticated CLIENT associates the same service twice via `/api/v1/me/favorite-services/{serviceId}`
- **THEN** the response SHALL be 409

### Requirement: Documentation reflects JWT usage and /me endpoints

The API documentation (`docs/api-rest-endpoints.md`) and Postman collection (`docs/postman_collection.json`) SHALL document Favorites endpoints as requiring `Authorization: Bearer <access_token>`, list the new `/api/v1/me/favorite-services` endpoints, and include examples for 401, 403, 404, 409 cases where relevant.

#### Scenario: API docs document JWT and /me
- **WHEN** the documentation is reviewed
- **THEN** `docs/api-rest-endpoints.md` SHALL mention Bearer token authentication and the `/api/v1/me/favorite-services` endpoints

#### Scenario: Postman collection updated
- **WHEN** the Postman collection is reviewed
- **THEN** `docs/postman_collection.json` SHALL include requests for `/api/v1/me/favorite-services` with Authorization header
