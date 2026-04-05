# Event-Ticket-Platform — Interview Preparation Guide

> A comprehensive reference for technical interview preparation covering the architecture, authentication flows, design patterns, and key implementation decisions of the Event-Ticket-Platform project.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture Overview](#2-architecture-overview)
3. [Authentication & Authorization Flow](#3-authentication--authorization-flow)
4. [Core Features Deep-Dives](#4-core-features-deep-dives)
5. [Technology Stack Q&A](#5-technology-stack-qa)
6. [Security Patterns](#6-security-patterns)
7. [Common Implementation Patterns](#7-common-implementation-patterns)
8. [Database Schema & Entity Relationships](#8-database-schema--entity-relationships)
9. [Troubleshooting Common Issues](#9-troubleshooting-common-issues)
10. [Best Practices & Trade-offs](#10-best-practices--trade-offs)

---

## 1. Project Overview

The **Event-Ticket-Platform** is a full-stack web application that allows:

- **Organizers** to create, manage, and publish events with multiple ticket types.
- **Attendees** to browse published events, purchase tickets, and view their QR codes.
- **Staff** to validate tickets on-site by scanning QR codes or entering IDs manually.

### High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                          FRONTEND (React + Vite)                     │
│   react-oidc-context  ──►  React Router  ──►  ShadCN / TailwindCSS  │
└─────────────────────────────────┬────────────────────────────────────┘
                                  │  HTTP (Bearer JWT)
┌─────────────────────────────────▼────────────────────────────────────┐
│              BACKEND (Spring Boot 4 / Java 21)                       │
│   SecurityFilterChain  ──►  Controllers  ──►  Services  ──►  JPA    │
└───────────────┬─────────────────────────────────────┬────────────────┘
                │                                     │
┌───────────────▼──────────────┐   ┌──────────────────▼───────────────┐
│  Keycloak (Identity Provider)│   │  PostgreSQL (Relational Database) │
│  port 9090 / realm:          │   │  port 5432                        │
│  event-ticket-platform       │   │  schema: public                   │
└──────────────────────────────┘   └───────────────────────────────────┘
```

---

## 2. Architecture Overview

### 2.1 Three-Tier Architecture

| Tier | Technology | Responsibility |
|------|-----------|----------------|
| **Presentation** | React 19, TypeScript, Vite, TailwindCSS, ShadCN | UI, routing, OIDC login flow |
| **Application** | Spring Boot 4, Java 21, Spring Security | REST API, business logic, auth enforcement |
| **Data** | PostgreSQL 16, Spring Data JPA / Hibernate | Persistence, schema management |

### 2.2 API Endpoint Map

| Method | Endpoint | Role Required | Description |
|--------|----------|---------------|-------------|
| GET | `/api/v1/published-events` | Public | List/search published events |
| GET | `/api/v1/published-events/{id}` | Public | Get a single published event |
| GET | `/api/v1/events` | `ROLE_ORGANIZER` | List organizer's events |
| POST | `/api/v1/events/create` | `ROLE_ORGANIZER` | Create a new event |
| GET | `/api/v1/events/{id}` | `ROLE_ORGANIZER` | Get event details |
| PUT | `/api/v1/events/{id}` | `ROLE_ORGANIZER` | Update an event |
| DELETE | `/api/v1/events/{id}` | `ROLE_ORGANIZER` | Delete an event |
| POST | `/api/v1/events/{eid}/ticket-types/{tid}/tickets` | Authenticated | Purchase a ticket |
| GET | `/api/v1/tickets` | Authenticated | List user's tickets |
| GET | `/api/v1/tickets/{id}` | Authenticated | Get ticket details |
| GET | `/api/v1/tickets/{id}/qr-codes` | Authenticated | Download QR code image |
| POST | `/api/v1/ticket-validations` | Authenticated ⚠️ | Validate a ticket (see §3.4 for path mismatch note) |

### 2.3 Expected Interview Questions

**Q: Walk me through the overall system architecture.**

> The platform is a classic three-tier architecture. The frontend is a React 19 SPA built with Vite and TypeScript; it uses `react-oidc-context` to implement the OAuth 2.0 Authorization Code + PKCE flow against Keycloak. All protected API calls attach the resulting JWT as a `Bearer` token in the `Authorization` header.
>
> The backend is a stateless Spring Boot 4 REST API configured as an OAuth 2.0 Resource Server. It validates every incoming JWT against Keycloak's JWKS endpoint, extracts roles from the `realm_access.roles` claim, and enforces method-level RBAC.
>
> PostgreSQL is the relational database; Spring Data JPA with Hibernate manages the schema (DDL auto = update) and all queries.

**Q: Why is the backend stateless and how is that enforced?**

> `SessionCreationPolicy.STATELESS` in the `SecurityFilterChain` instructs Spring Security not to create or use `HttpSession`. Every request must carry a valid JWT — there are no cookies or server-side sessions. This simplifies horizontal scaling because any node can handle any request.

**Follow-up Q: What are the trade-offs of a stateless architecture?**

> Pros: easy horizontal scaling, no session affinity needed, works well with CDNs and API gateways.  
> Cons: cannot revoke a JWT before it expires (no session to invalidate); must rely on short-lived tokens and token refresh mechanisms.

---

## 3. Authentication & Authorization Flow

### 3.1 OAuth 2.0 Authorization Code + PKCE (Frontend)

```
User Browser          React App          Keycloak            Backend API
     │                    │                  │                    │
     │  Click "Login"     │                  │                    │
     │───────────────────►│                  │                    │
     │                    │ signinRedirect()  │                    │
     │                    │─────────────────►│                    │
     │◄───────────────────────────────────────                    │
     │  Redirect to Keycloak login page                           │
     │                    │                  │                    │
     │  Enter credentials │                  │                    │
     │─────────────────────────────────────►│                    │
     │                    │                  │                    │
     │◄─────────────────────────────────────│                    │
     │  Redirect to /callback?code=AUTH_CODE │                    │
     │                    │                  │                    │
     │  /callback reached │                  │                    │
     │───────────────────►│                  │                    │
     │                    │ POST /token       │                    │
     │                    │ code + code_verifier                  │
     │                    │─────────────────►│                    │
     │                    │◄─────────────────│                    │
     │                    │ access_token (JWT)│                    │
     │                    │ id_token          │                    │
     │                    │ refresh_token     │                    │
     │                    │                  │                    │
     │                    │  GET /api/v1/events                   │
     │                    │  Authorization: Bearer <access_token> │
     │                    │──────────────────────────────────────►│
     │                    │                  │                    │
     │                    │                  │  Validate JWT      │
     │                    │                  │  (JWKS endpoint)   │
     │                    │◄──────────────────────────────────────│
     │                    │  200 OK + JSON                        │
```

#### Frontend OIDC Configuration (`main.tsx`)

```typescript
const oidcConfig = {
    // Note: the authority points to port 5173 (Vite dev server), NOT directly to Keycloak's
    // port 9090. vite.config.ts proxies all /auth/* requests to http://localhost:9090 with a
    // path rewrite (/auth → /), so the OIDC library communicates with Keycloak transparently
    // through the proxy. This avoids CORS issues during development.
    authority: "http://localhost:5173/auth/realms/event-ticket-platform",
    client_id: "event-ticket-platform-app",
    redirect_uri: "http://localhost:5173/callback",
    response_type: "code",            // Authorization Code flow
    scope: "openid profile email",    // Requested claims
    onSigninCallback: () => {
        // Clean up OIDC params from the URL after callback
        window.history.replaceState({}, document.title, window.location.pathname);
    },
};
```

**Q: Why does the `authority` URL point to port 5173 (Vite) instead of port 9090 (Keycloak)?**

> `vite.config.ts` configures a proxy rule: any request to `/auth/*` is forwarded to `http://localhost:9090` with the `/auth` prefix stripped. So `http://localhost:5173/auth/realms/…` transparently reaches `http://localhost:9090/realms/…` on Keycloak. Routing OIDC traffic through the Vite dev server proxy avoids browser CORS errors because all requests appear same-origin from the browser's perspective.

**Q: Why use PKCE (Proof Key for Code Exchange)?**

> PKCE prevents authorization code interception attacks. The client generates a random `code_verifier`, hashes it to produce a `code_challenge`, and sends the challenge with the authorization request. When exchanging the code for tokens, the client proves it holds the original verifier. Because the SPA cannot securely store a client secret (it runs in the browser), PKCE replaces the secret as the proof of legitimacy.

**Q: What scopes are requested and why?**

> `openid` — required for OIDC; enables the `id_token`.  
> `profile` — provides `preferred_username` used in `UserProvisioningFilter`.  
> `email` — provides the user's email address, also used during provisioning.

### 3.2 JWT Validation (Backend)

Spring Boot auto-configures a JWT decoder using the `issuer-uri` property:

```properties
# application.properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/event-ticket-platform
```

At startup Spring fetches `{issuer-uri}/.well-known/openid-configuration` and then the JWKS endpoint to obtain the signing keys used to verify every incoming JWT.

### 3.3 Role Extraction — `JwtAuthenticationConverter`

Keycloak places roles inside `realm_access.roles` rather than the standard `authorities` claim. The custom converter bridges this gap:

```java
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    @Override
    public JwtAuthenticationToken convert(Jwt source) {
        Collection<GrantedAuthority> grantedAuthorities = extractAuthorities(source);
        return new JwtAuthenticationToken(source, grantedAuthorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .filter(role -> role.startsWith("ROLE_"))   // only app roles
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
```

**Q: Why filter for roles that start with `ROLE_`?**

> Keycloak injects several internal roles (e.g., `offline_access`, `uma_authorization`, `default-roles-*`) into every token. Filtering for the `ROLE_` prefix ensures that only application-specific roles (`ROLE_ORGANIZER`, `ROLE_ATTENDEE`, `ROLE_STAFF`) are turned into Spring Security `GrantedAuthority` objects. This prevents unintentional privilege escalation through Keycloak's built-in roles.

**Q: Why return a `Set` instead of a `List`?**

> Roles are unique per user in an authorization context. Using `Collectors.toSet()` guarantees there are no duplicate `GrantedAuthority` entries, which makes authority checks O(1) on average.

### 3.4 Security Filter Chain (`SecurityConfig`)

```java
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   UserProvisioningFilter userProvisioningFilter,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter) {
        httpSecurity
                .authorizeHttpRequests(authorize ->
                        authorize
                                // Public: anyone can browse published events
                                .requestMatchers(HttpMethod.GET, "/api/v1/published-events/**").permitAll()
                                // Organizer-only: event management
                                .requestMatchers("/api/v1/events").hasRole("ORGANIZER")
                                // Staff-only: ticket validation
                                // ⚠️ NOTE: this path uses the singular "ticket-validation" but the
                                // controller is mapped to the plural "ticket-validations". As a result
                                // this security rule does not match the actual controller endpoint.
                                .requestMatchers("/api/v1/ticket-validation").hasRole("STAFF")
                                // Everything else requires authentication
                                .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)          // stateless API — no CSRF needed
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer ->
                                jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                // Run user provisioning AFTER the token is validated
                .addFilterAfter(userProvisioningFilter, BearerTokenAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
```

**Q: Why is CSRF disabled?**

> CSRF attacks require the browser to automatically attach a session cookie to a cross-origin request. Because this API uses stateless JWT authentication (Bearer tokens in the `Authorization` header, not cookies), the browser will never attach authentication credentials automatically to a malicious cross-origin request. Therefore CSRF protection is unnecessary and is explicitly disabled to reduce overhead.

**Q: There is a path mismatch between `SecurityConfig` (`/api/v1/ticket-validation`) and `TicketValidationController` (`/api/v1/ticket-validations`). What is the effect?**

> The `hasRole("STAFF")` security rule uses the singular form `ticket-validation`, but the controller is mapped to the plural `ticket-validations`. Spring Security's `requestMatchers` does an exact path match, so the rule never fires. The actual `/api/v1/ticket-validations` endpoint falls through to `.anyRequest().authenticated()`, meaning **any** authenticated user (not just staff) can call it. This is a bug in the codebase. The fix is to align the paths: `.requestMatchers("/api/v1/ticket-validations").hasRole("STAFF")`.

**Q: Where exactly in the filter chain is `UserProvisioningFilter` inserted and why?**

> It is added *after* `BearerTokenAuthenticationFilter`. That filter is the one that reads the `Authorization: Bearer …` header, validates the JWT, and populates `SecurityContextHolder`. By running after it, `UserProvisioningFilter` can safely read the `Authentication` principal (the validated `Jwt`) without re-doing any verification.

### 3.5 User Auto-Provisioning — `UserProvisioningFilter`

When a user authenticates for the first time their account does not yet exist in the application database. `UserProvisioningFilter` transparently creates it:

```java
@Component
@RequiredArgsConstructor
public class UserProvisioningFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {

            // Keycloak user ID is the JWT "sub" (subject) claim
            // String.transform(Function) is a Java 12+ API that applies a function and returns its result
            UUID keycloakId = jwt.getSubject().transform(UUID::fromString);
            Map<String, Object> claims = jwt.getClaims();

            if (!userRepository.existsById(keycloakId)) {
                User user = new User();
                user.setId(keycloakId);
                user.setName(claims.get("preferred_username").toString());
                user.setEmail(claims.get("email").toString());
                userRepository.save(user);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

**Q: What is the purpose of `UserProvisioningFilter`?**

> Keycloak is the source of truth for authentication (who you are). The application database holds domain data linked to users (events, tickets). On the first authenticated request, the filter reads the `sub`, `preferred_username`, and `email` claims from the JWT and lazily creates a local `User` record. Subsequent requests skip the insert via `existsById`. This pattern is called *just-in-time user provisioning*.

**Q: What could go wrong if `preferred_username` or `email` is missing from the token?**

> `claims.get("preferred_username").toString()` would throw a `NullPointerException`. The `email` scope must be configured in Keycloak and requested by the client (`scope: "openid profile email"`). In a production system you would add null checks and either reject the request or fall back to the subject UUID.

**Q: Is there a race condition in the provisioning logic?**

> Yes — in a multi-node deployment two concurrent first-requests from the same user could both pass the `!existsById` check before either completes the insert, causing a primary key violation. Mitigation options include: wrapping the check-then-insert in a `@Transactional` method with an upsert/`saveIfAbsent` semantic, using a `ON CONFLICT DO NOTHING` native query, or accepting the constraint violation and catching the `DataIntegrityViolationException`.

### 3.6 Frontend Role Checking — `useRoles` Hook

```typescript
export const useRoles = (): UseRolesReturn => {
    const {isLoading: isAuthLoading, user} = useAuth();
    // ...
    useEffect(() => {
        if (isAuthLoading || !user?.access_token) { /* reset */ return; }

        try {
            const payload = jwtDecode<JwtPayload>(user?.access_token);
            const allRoles = payload.realm_access?.roles || [];
            // Mirror the backend filter: only ROLE_* roles matter
            const filteredRoles = allRoles.filter((role) => role.startsWith("ROLE_"));
            setIsOrganizer(filteredRoles.includes("ROLE_ORGANIZER"));
            setIsAttendee(filteredRoles.includes("ROLE_ATTENDEE"));
            setIsStaff(filteredRoles.includes("ROLE_STAFF"));
        } catch (error) { /* reset */ }
    }, [isAuthLoading, user?.access_token]);
    // ...
};
```

**Q: Does the frontend role check provide security?**

> No — it provides UX gating only (hiding/showing UI elements). The backend enforces the actual security. An attacker with a valid JWT of a different role who calls the API directly would be rejected at the server. Parsing roles on the frontend is purely cosmetic.

### 3.7 Protected Routes

```typescript
const ProtectedRoute: React.FC<ProtectedRouteProperties> = ({children}) => {
    const {isLoading, isAuthenticated} = useAuth();
    const location = useLocation();

    if (isLoading) return <p>Loading...</p>;

    if (!isAuthenticated) {
        // Persist the intended destination for post-login redirect
        localStorage.setItem("redirectPath",
            globalThis.location.pathname + globalThis.location.search);
        return <Navigate to="/login" state={{from: location}} replace/>;
    }

    return children;
};
```

**Q: How does the post-login redirect work?**

> Before redirecting to `/login`, the component saves the current path to `localStorage` under `redirectPath`. The `CallbackPage` (the OIDC redirect URI handler) reads this key after successful authentication and navigates to the saved path, then removes it from storage. This gives users a seamless "return to where you were" experience.

---

## 4. Core Features Deep-Dives

### 4.1 Event Management Lifecycle

Events follow a status machine:

```
DRAFT ──► PUBLISHED ──► COMPLETED
  │                         ▲
  └──────────────────────►──┘
         CANCELLED
```

| Status | Meaning |
|--------|---------|
| `DRAFT` | Created but not visible to attendees |
| `PUBLISHED` | Visible on the public events listing |
| `CANCELLED` | Cancelled by the organizer |
| `COMPLETED` | Event has ended |

**Q: How does the event update handle ticket types that are removed?**

> `migrateEventTicketTypes` in `EventServiceImpl` performs a three-step reconciliation:
>
> 1. Collect the IDs of all ticket types in the update request.
> 2. Remove any existing ticket types whose IDs are *not* in that set (uses `List.removeIf` combined with the orphan removal cascade).
> 3. For each item in the request: if no ID is provided, create a new ticket type; if an ID is present and found, update it; if an ID is present but not found, throw `TicketTypeNotFoundException`.

```java
private void migrateEventTicketTypes(Event existingEvent,
                                      List<UpdateTicketTypeRequest> updateTicketTypes) {
    Set<UUID> uuids = updateTicketTypes.stream()
            .map(UpdateTicketTypeRequest::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    // Step 1: remove ticket types no longer in the request
    existingEvent.getTicketTypes()
            .removeIf(ticketType -> !uuids.contains(ticketType.getId()));

    Map<UUID, TicketType> ticketTypeMap = existingEvent.getTicketTypes()
            .stream()
            .collect(Collectors.toMap(TicketType::getId, Function.identity()));

    for (UpdateTicketTypeRequest updateTicketType : updateTicketTypes) {
        if (updateTicketType.getId() == null) {
            // New ticket type
            existingEvent.getTicketTypes().add(TicketType.builder()/* ... */.build());
        } else if (ticketTypeMap.containsKey(updateTicketType.getId())) {
            // Update existing
            TicketType ticketType = ticketTypeMap.get(updateTicketType.getId());
            ticketType.setName(updateTicketType.getName());
            // ...
        } else {
            throw new TicketTypeNotFoundException(/* ... */);
        }
    }
}
```

**Q: Why is `@Transactional` needed on `updateEventForOrganizer`?**

> The method loads an `Event`, modifies its `ticketTypes` collection (which involves both deletes and inserts), and then saves. If any step fails, the entire operation must be rolled back to avoid partial updates (e.g., some ticket types deleted but no new ones created). `@Transactional` ensures atomicity.

### 4.2 Ticket Purchase Flow

```
Client                        TicketTypeController       TicketTypeServiceImpl
  │                                   │                          │
  │  POST /events/{eid}/              │                          │
  │      ticket-types/{tid}/tickets   │                          │
  │──────────────────────────────────►│                          │
  │                                   │  purchaseTicket(         │
  │                                   │    userId, ticketTypeId) │
  │                                   │─────────────────────────►│
  │                                   │                          │ findById(userId)
  │                                   │                          │──► UserRepo
  │                                   │                          │
  │                                   │                          │ findByIdWithLock(ticketTypeId)
  │                                   │                          │──► TicketTypeRepo (SELECT FOR UPDATE)
  │                                   │                          │
  │                                   │                          │ countByTicketTypeId(typeId)
  │                                   │                          │──► TicketRepo
  │                                   │                          │
  │                                   │                          │ if sold out → throw TicketSoldOutException
  │                                   │                          │
  │                                   │                          │ decrement totalAvailable
  │                                   │                          │ save Ticket (status=PURCHASED)
  │                                   │                          │ generateQrCode(ticket)
  │                                   │                          │──► QrCodeServiceImpl
  │                                   │                          │    (ZXing → Base64 PNG)
  │                                   │                          │
  │◄──────────────────────────────────│◄─────────────────────────│
  │  201 Created                      │                          │
```

**Q: How is the sold-out check made concurrency-safe?**

> `ticketTypeRepository.findByIdWithLock(ticketTypeId)` uses a `SELECT … FOR UPDATE` pessimistic lock (`@Lock(LockModeType.PESSIMISTIC_WRITE)`). This prevents two concurrent purchases from both passing the availability check before either decrements the count.

**Q: Why is `totalAvailable` decremented in addition to counting existing tickets?**

> The count query (`countByTicketTypeId`) counts tickets that have already been saved. During the current transaction the new ticket is not yet persisted, so the count may be stale. Decrementing `totalAvailable` at save time provides a secondary guard. Note: this means `totalAvailable` drifts from its initial value over time and acts as a capacity counter.

### 4.3 QR Code Generation & Validation

#### Generation (`QrCodeServiceImpl`)

```java
@Override
public QrCode generateQrCode(Ticket ticket) {
    try {
        UUID id = UUID.randomUUID();          // The QR payload is just a UUID
        String qrCodeImage = generateQrCodeImage(id);

        QrCode qrCode = QrCode.builder()
                .id(id)                       // The UUID IS the QR code ID
                .status(QrCodeStatusEnum.ACTIVE)
                .value(qrCodeImage)           // Base64-encoded PNG stored in DB
                .ticket(ticket)
                .build();

        return qrCodeRepository.saveAndFlush(qrCode);
    } catch (WriterException ex) {
        throw new QrCodeGenerationException("Failed to generate QR Code", ex);
    }
}

private String generateQrCodeImage(UUID uniqueId) throws WriterException {
    BitMatrix bitMatrix = qrCodeWriter.encode(
            uniqueId.toString(),              // Encode the UUID string into the QR
            BarcodeFormat.QR_CODE,
            300, 300);
    BufferedImage qrCodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        ImageIO.write(qrCodeImage, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```

**Q: What is encoded in the QR code?**

> A UUID string — the QR code's own database ID. When scanned, the scanner reads this UUID and sends it to `POST /api/v1/ticket-validations` with `method: QR_SCAN`. The server looks up the `QrCode` record and retrieves the linked `Ticket`.

**Q: Why store the QR image as Base64 in the database instead of on disk / object storage?**

> For simplicity in a demo or MVP. The trade-off is that QR images can be several KB each and storing BLOBs in PostgreSQL increases database size, slows backups, and can hurt query performance. In a production system you would store the binary in object storage (e.g., S3) and save only the URL in the database.

#### Validation (`TicketValidationServiceImpl`)

```java
@Override
public TicketValidation validateTicketByQrCode(UUID qrCodeId) {
    QrCode qrCode = qrCodeRepository.findQrCodeByIdAndStatus(qrCodeId, QrCodeStatusEnum.ACTIVE)
            .orElseThrow(() -> new QrCodeNotFoundException(/* ... */));

    return validateTicket(qrCode.getTicket(), TicketValidationMethod.QR_SCAN);
}

private TicketValidation validateTicket(Ticket ticket, TicketValidationMethod method) {
    TicketValidation ticketValidation = TicketValidation.builder()
            .validationMethod(method)
            .ticket(ticket)
            .build();

    // A ticket is VALID only on its first scan; subsequent scans are INVALID
    TicketValidationStatusEnum status = ticket.getTicketValidations()
            .stream()
            .filter(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()))
            .findFirst()
            .map(v -> TicketValidationStatusEnum.INVALID)   // already validated once
            .orElse(TicketValidationStatusEnum.VALID);       // first validation

    ticketValidation.setStatus(status);
    return ticketValidationRepository.save(ticketValidation);
}
```

**Q: How does the system prevent a ticket from being used more than once?**

> The validation service checks whether any previous `TicketValidation` record for the ticket has status `VALID`. If one exists, the new validation is marked `INVALID`. Every attempt is persisted, creating an audit trail. There is no hard block — the system records the invalid scan and returns `INVALID` to the caller (staff app), which displays a red ✗ overlay.

**Q: What are the two validation methods and when is each used?**

> `QR_SCAN` — the staff member scans the attendee's QR code using the camera; the raw QR value (a UUID) is sent directly to the API.  
> `MANUAL` — the staff member types a ticket ID manually into the input field; the API uses `validateTicketManually` which looks up the `Ticket` by its primary key directly.

### 4.4 Full-Text Event Search

```java
@Query(value = "SELECT * FROM events WHERE " +
        "status = 'PUBLISHED' AND " +
        "to_tsvector('english', COALESCE(name, '') || ' ' || COALESCE(venue, '')) " +
        "@@ plainto_tsquery('english', :searchTerm)",
        countQuery = "...",
        nativeQuery = true)
Page<Event> searchEvents(@Param("searchTerm") String searchTerm, Pageable pageable);
```

**Q: Why use PostgreSQL's full-text search instead of a `LIKE` query?**

> `plainto_tsquery` / `to_tsvector` provides:
> - **Stemming**: "run", "running", "runs" all match.
> - **Stop word removal**: common words like "the", "and" are ignored.
> - **Performance**: `tsvector` can be indexed with a GIN index for O(log n) lookups vs. O(n) for `LIKE '%term%'` scans.
>
> The trade-off is that it requires PostgreSQL specifically (not portable to H2) and needs a GIN index to be performant at scale.

---

## 5. Technology Stack Q&A

### 5.1 Spring Boot 4 / Java 21

**Q: What is an OAuth 2.0 Resource Server and how is it configured here?**

> A Resource Server is any server that accepts and validates OAuth 2.0 access tokens to protect its resources. In Spring Boot, it is configured via:

```java
.oauth2ResourceServer(oauth2 ->
    oauth2.jwt(jwtConfigurer ->
        jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter))
)
```

> Plus the `issuer-uri` in `application.properties`. Spring Boot auto-creates a `JwtDecoder` that fetches the JWKS from Keycloak and validates every incoming JWT.

**Q: What does `@RequiredArgsConstructor` do, and why is it preferred over `@Autowired`?**

> Lombok's `@RequiredArgsConstructor` generates a constructor for all `final` fields. Spring Boot detects a single constructor and auto-wires the dependencies without needing `@Autowired`. This enforces immutability of injected dependencies, makes unit testing easier (just call the constructor), and avoids circular dependency problems that field injection can mask.

**Q: Explain the `@EnableJpaAuditing` setup.**

> `JpaConfiguration` is annotated with `@EnableJpaAuditing`. This activates Spring Data JPA's auditing infrastructure which handles `@CreatedDate` and `@LastModifiedDate` annotations on entity fields. Without it, those fields are never populated.

**Q: How does Spring Data JPA derive queries from method names?**

> The repository method `findEventsByOrganizerId(UUID organizerId, Pageable pageable)` is parsed by Spring Data: `findBy` is the keyword, `OrganizerId` maps to `Event.organizer.id`, and `Pageable` adds pagination. The generated JPQL is equivalent to `SELECT e FROM Event e WHERE e.organizer.id = :organizerId`.

### 5.2 React 19 + TypeScript + OIDC

**Q: What is `react-oidc-context` and what does it provide?**

> `react-oidc-context` is a React context wrapper around `oidc-client-ts`. It manages the full OIDC lifecycle: initiating login (`signinRedirect`), handling the callback, storing and refreshing tokens, and exposing the current auth state (`isLoading`, `isAuthenticated`, `user`) via the `useAuth()` hook.

**Q: How does the app pass the JWT to API calls?**

> Every API function in `src/lib/api.ts` accepts an `accessToken` string and sets:
> ```typescript
> headers: { Authorization: `Bearer ${accessToken}` }
> ```
> The caller extracts the token from the OIDC context: `user?.access_token`.

**Q: How is TypeScript used for API response typing?**

> The `domain.ts` file defines TypeScript interfaces (e.g., `EventSummary`, `TicketDetails`, `SpringBootPagination<T>`) that mirror backend DTOs. API functions cast responses: `return responseBody as SpringBootPagination<EventSummary>`. This is a type assertion, not a runtime validation — a Zod schema or similar would add runtime safety.

### 5.3 MapStruct

**Q: What is MapStruct and how is it used here?**

> MapStruct is a compile-time annotation processor that generates type-safe mapper implementations. By declaring an interface:
>
> ```java
> @Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
> public interface EventMapper {
>     CreateEventRequest fromDto(CreateEventRequestDto dto);
>     CreateEventResponseDto toCreateEventResponseDto(Event event);
>     // ...
> }
> ```
>
> MapStruct generates a Spring `@Component` at compile time that copies matching field names. `unmappedTargetPolicy = ReportingPolicy.IGNORE` suppresses warnings for fields that exist on the target but not the source.

**Q: What is the Lombok–MapStruct ordering requirement?**

> MapStruct uses getter/setter methods generated by Lombok. If MapStruct's annotation processor runs before Lombok's, the getters/setters don't exist yet and the compilation fails. The `pom.xml` declares the processors in order: Lombok first, then MapStruct, then `lombok-mapstruct-binding` (which coordinates the two processors).

### 5.4 PostgreSQL & JPA

**Q: What strategy is used for primary keys and why UUIDs?**

> Entities use `@GeneratedValue(strategy = GenerationType.UUID)` (JPA 3.1 feature, generates random UUIDv4 values). UUID PKs avoid integer auto-increment contention on distributed inserts, are safe to generate client-side, and do not expose a sequential ID that reveals record counts to external callers. The `User` entity is an exception: its UUID is set explicitly to the Keycloak subject claim, making Keycloak the authoritative source of the user's identity.

**Q: Explain `FetchType.LAZY` on relationships and when it matters.**

> `LAZY` means the related entity is not loaded from the database until accessed. For example, `Ticket.ticketType` and `Ticket.purchaser` are lazy. Without this, fetching a list of tickets would join-load all purchasers and ticket types even if they are not needed, creating N+1 or Cartesian product performance problems. MapStruct accesses fields during DTO mapping, which triggers the lazy load inside an active Hibernate session — the `@Transactional` boundary on service methods ensures the session is open.

### 5.5 Docker / docker-compose

**Q: What services does `docker-compose.yaml` spin up?**

```yaml
services:
  db:         # PostgreSQL on port 5432
  adminer:    # DB admin UI on port 8888
  keycloak:   # Keycloak IAM server on port 9090
```

> Together they provide the complete infrastructure for local development without installing anything beyond Docker.

**Q: What does `spring-boot-docker-compose` do?**

> It is a Spring Boot dev-tools integration that automatically starts the `docker-compose.yaml` when the application starts in development mode and stops containers when the application shuts down. The service is declared as `runtime` scope so it is not packaged into the production JAR.

---

## 6. Security Patterns

### 6.1 JWT Token Validation

**Q: What claims are validated in the JWT?**

> Spring's `JwtDecoder` (backed by Keycloak's JWKS) validates:
> - **Signature** — using the RSA public key from JWKS.
> - **Expiry** (`exp` claim) — token must not be expired.
> - **Issuer** (`iss` claim) — must match `spring.security.oauth2.resourceserver.jwt.issuer-uri`.
> - **Not-before** (`nbf` claim) — if present.
>
> The application additionally reads `realm_access.roles`, `sub`, `preferred_username`, and `email`.

**Q: How are short-lived tokens handled?**

> `oidc-client-ts` (used by `react-oidc-context`) automatically refreshes the access token using the refresh token before expiry. The application does not need to handle token refresh explicitly.

### 6.2 CORS

**Q: How is CORS handled?**

> The current `SecurityConfig` does not define a `CorsConfigurationSource` bean. In development, the Vite dev server proxies requests to the Spring Boot backend (`/api/v1/**`), so the browser sees requests as same-origin and no CORS headers are needed. In production, a `CorsFilter` or `@CrossOrigin` annotations should be added to restrict allowed origins.

### 6.3 Input Validation

**Q: How is input validated on the backend?**

> `@Valid` is placed on `@RequestBody` parameters in controllers (e.g., `@RequestBody @Valid CreateEventRequestDto request`). Spring Boot calls the Bean Validation (Hibernate Validator) implementation. Violations throw `MethodArgumentNotValidException`, which `GlobalExceptionHandler` catches and returns as a 400 Bad Request with a descriptive message:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult().getFieldErrors()
            .stream().findFirst()
            .map(field -> field.getField() + " : " + field.getDefaultMessage())
            .orElse("Validation error occurred");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(errorMessage));
}
```

### 6.4 SQL Injection Prevention

**Q: How does the application prevent SQL injection?**

> Two mechanisms:
>
> 1. **Spring Data JPA derived queries** (`findEventsByOrganizerId`, etc.) use JPQL with parameterized queries underneath — user input never reaches SQL strings.
>
> 2. **Native query with named parameters** (`searchEvents`) uses `:searchTerm` via `@Param`, which is substituted as a prepared statement parameter by Hibernate. The `plainto_tsquery` function in PostgreSQL also sanitizes the input before evaluating it as a text search query.

### 6.5 CSRF Protection

**Q: Why is CSRF disabled, and is that safe?**

> CSRF attacks exploit the browser automatically attaching cookies to cross-origin requests. This API uses `Authorization: Bearer <JWT>` header-based authentication; the browser never automatically attaches this header to cross-origin requests initiated by a malicious page. Therefore, the standard CSRF protection (synchronizer token pattern) provides no additional security here and is safely disabled.

---

## 7. Common Implementation Patterns

### 7.1 Repository Pattern

```java
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    Page<Event> findEventsByOrganizerId(UUID organizerId, Pageable pageable);
    Optional<Event> findEventByIdAndOrganizerId(UUID eventId, UUID organizerId);
    Page<Event> findEventsByStatus(EventStatusEnum status, Pageable pageable);
    @Query(/* full-text search */)
    Page<Event> searchEvents(@Param("searchTerm") String searchTerm, Pageable pageable);
    Optional<Event> findEventByIdAndStatus(UUID id, EventStatusEnum status);
}
```

**Q: What does extending `JpaRepository<Event, UUID>` give you?**

> `JpaRepository` extends `CrudRepository` and `PagingAndSortingRepository`. You get: `save`, `findById`, `findAll`, `delete`, `existsById`, `count`, `flush`, `saveAndFlush`, and paginated/sorted list variants — all implemented by Spring Data automatically.

### 7.2 Service Layer Pattern

> All business logic lives in `*ServiceImpl` classes, never in controllers. Controllers are thin — they parse the request, call the service, map the result to a DTO, and return the HTTP response. This separation makes services independently testable with mock repositories.

**Q: Why use a service interface (`EventService`) with an implementation (`EventServiceImpl`)?**

> It enables programming to abstractions rather than concrete types, which:
> - Allows swapping implementations (e.g., a mock in tests or a different storage backend).
> - Makes it easy to add `@Transactional` proxy behaviour on the implementation without affecting the interface.
> - Follows the Dependency Inversion Principle (SOLID).

### 7.3 DTO Pattern

**Q: Why use DTOs instead of returning entities directly?**

> 1. **Security** — entity fields not intended for the client (e.g., internal IDs, passwords, audit timestamps) are excluded from the DTO.
> 2. **Decoupling** — the API contract is independent of the database schema; changing the entity does not automatically change the API.
> 3. **Avoiding N+1 in serialization** — returning a JPA entity to Jackson would trigger lazy-load on every relationship during serialization.
> 4. **Versioning** — different API versions can have different DTOs mapping to the same entity.

### 7.4 Global Exception Handling

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // Maps domain exceptions to specific HTTP status codes
    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTicketNotFoundException(/* ... */) { /* 400 */ }

    @ExceptionHandler(QrCodeNotFoundException.class)
    public ResponseEntity<ErrorDto> handleQrCodeNotFoundException(/* ... */) { /* 500 */ }

    @ExceptionHandler(Exception.class)  // Catch-all
    public ResponseEntity<ErrorDto> handleException(/* ... */) { /* 500 */ }
}
```

**Q: Why catch `Exception.class` as a fallback?**

> Without a catch-all handler, unexpected exceptions (e.g., a `NullPointerException`, a database timeout) would be returned as Spring's default error response, which can include stack traces that expose internal implementation details. The catch-all returns a generic 500 with a safe message and logs the full exception server-side.

### 7.5 `OncePerRequestFilter`

**Q: Why extend `OncePerRequestFilter` for `UserProvisioningFilter`?**

> In a standard Servlet container, a filter can be called multiple times per logical request (e.g., when a forward or include is performed). `OncePerRequestFilter` guarantees execution exactly once per request, which is important for side-effect operations like database writes in `UserProvisioningFilter`.

### 7.6 RESTful API Design

| Convention | Example |
|-----------|---------|
| Plural resource nouns | `/api/v1/events`, `/api/v1/tickets` |
| HTTP verbs for actions | `GET` list, `POST` create, `PUT` update, `DELETE` delete |
| Path params for resource IDs | `/api/v1/events/{eventId}` |
| Query params for filtering | `/api/v1/published-events?q=concert&page=0&size=4` |
| Sub-resources for relationships | `/api/v1/tickets/{id}/qr-codes` |
| Versioning in path | `/api/v1/…` |

---

## 8. Database Schema & Entity Relationships

### 8.1 Entity Relationship Diagram

```
┌──────────────────────┐
│ users                │
│ ─────────────────── │
│ id (UUID, PK)        │
│ name                 │
│ email                │
│ created_at           │
│ updated_at           │
└──────┬───────────────┘
       │ 1                    ┌─────────────────────────┐
       │ organizes            │ events                  │
       │◄────────────────────┤ ─────────────────────── │
       │                      │ id (UUID, PK)            │
       │                      │ name                     │
       │ M                    │ event_start              │
       ├─────────────────────►│ event_end                │
       │ attending (join tbl) │ venue                    │
       │ M                    │ sales_start              │
       ├─────────────────────►│ sales_end                │
         staffing (join tbl)  │ status (ENUM)            │
                              │ organizer_id (FK→users)  │
                              └────────┬────────────────┘
                                       │ 1
                                       │ has
                              ┌────────▼────────────────┐
                              │ ticket_types             │
                              │ ──────────────────────  │
                              │ id (UUID, PK)            │
                              │ name                     │
                              │ price                    │
                              │ description              │
                              │ total_available          │
                              │ event_id (FK→events)     │
                              └────────┬────────────────┘
                                       │ 1
                                       │ sold as
                              ┌────────▼────────────────┐
                              │ tickets                  │
                              │ ──────────────────────  │
                              │ id (UUID, PK)            │
                              │ status (ENUM)            │
                              │ ticket_type (FK→types)   │
                              │ purchase_id (FK→users)   │
                              └────┬───────┬────────────┘
                                   │       │
                      ┌────────────▼┐   ┌──▼─────────────────────┐
                      │ qr_codes    │   │ ticket_validations      │
                      │ ─────────── │   │ ────────────────────── │
                      │ id (UUID)   │   │ id (UUID, PK)           │
                      │ value (TEXT)│   │ validation_method       │
                      │ status      │   │ status (ENUM)           │
                      │ ticket_id   │   │ ticket_id (FK→tickets)  │
                      └─────────────┘   └─────────────────────────┘
```

### 8.2 Join Tables

| Table | Purpose |
|-------|---------|
| `user_attending_events` | Many-to-many: users ↔ events they attend |
| `user_staffing_events` | Many-to-many: users ↔ events they staff |

### 8.3 Enums

| Enum | Values |
|------|--------|
| `EventStatusEnum` | `DRAFT`, `PUBLISHED`, `CANCELLED`, `COMPLETED` |
| `TicketStatusEnum` | `PURCHASED`, `CANCELLED` |
| `QrCodeStatusEnum` | `ACTIVE`, `USED`, `EXPIRED` |
| `TicketValidationStatusEnum` | `VALID`, `INVALID`, `EXPIRED` |
| `TicketValidationMethod` | `QR_SCAN`, `MANUAL` |

---

## 9. Troubleshooting Common Issues

### 9.1 `401 Unauthorized` on Protected Endpoints

| Possible Cause | Resolution |
|----------------|------------|
| JWT expired | The frontend should silently refresh the token; check `react-oidc-context` configuration |
| Wrong `issuer-uri` | Verify `spring.security.oauth2.resourceserver.jwt.issuer-uri` matches your Keycloak realm URL |
| Keycloak not running | Start `docker-compose up keycloak` |
| Bearer token not attached | Check that `Authorization: Bearer <token>` header is present in the request |

### 9.2 `403 Forbidden` on Role-Protected Endpoints

| Possible Cause | Resolution |
|----------------|------------|
| Role not assigned in Keycloak | Assign `ROLE_ORGANIZER` / `ROLE_STAFF` to the user in the Keycloak Admin console |
| Role name mismatch | Keycloak roles must start with `ROLE_` (e.g., `ROLE_ORGANIZER`) to pass the filter in `JwtAuthenticationConverter` |
| `realm_access` claim missing | Enable the `realm_access` claim in the Keycloak client scope settings |

### 9.3 User Not Provisioned (`UserNotFoundException` on first request)

> The `UserProvisioningFilter` runs on every authenticated request; if it encounters an exception (e.g., database unreachable) during provisioning, the filter chain still continues (`filterChain.doFilter`). The service method then fails to find the user. Check database connectivity and the filter's log output.

### 9.4 QR Code Returns Blank Image

> If `qrCode.getValue()` is stored as a truncated or corrupted Base64 string, `Base64.getDecoder().decode()` throws `IllegalArgumentException`. `QrCodeServiceImpl.getQrCodeImageForUserAndTicket` catches this and throws `QrCodeNotFoundException`. Check for row-level size limits on the `value` column (`TEXT` type in PostgreSQL has no limit, so this should not occur).

### 9.5 Full-Text Search Returns No Results

> The `searchEvents` query uses `plainto_tsquery('english', :searchTerm)`. Ensure:
> - The search term is in English (or the language configuration matches the stored data).
> - The `status` column has value `'PUBLISHED'` (string comparison in native SQL, not enum comparison).
> - A GIN index on `to_tsvector(...)` exists for performance (not automatically created by Hibernate DDL auto).

---

## 10. Best Practices & Trade-offs

### 10.1 What Was Done Well

| Practice | How It Appears in the Code |
|----------|---------------------------|
| Stateless API | `SessionCreationPolicy.STATELESS` in `SecurityConfig` |
| Separation of concerns | Controller → Service → Repository layers |
| Immutable dependency injection | `@RequiredArgsConstructor` + `final` fields |
| Compile-time mapping safety | MapStruct instead of reflection-based mappers |
| Audit trail | `@CreatedDate` / `@LastModifiedDate` on all entities |
| Custom JWT role extraction | `JwtAuthenticationConverter` handles Keycloak's non-standard claim structure |
| Idempotent user provisioning | `existsById` check before insert in `UserProvisioningFilter` |
| Pessimistic locking on purchase | `SELECT FOR UPDATE` prevents overselling |

### 10.2 Known Limitations & Improvement Opportunities

| Limitation | Impact | Suggested Improvement |
|-----------|--------|-----------------------|
| QR image stored as Base64 TEXT in DB | High storage cost for scale | Move to object storage (S3/MinIO); store URL |
| `UserProvisioningFilter` race condition | Potential duplicate users in high-concurrency | Upsert / optimistic retry / unique constraint |
| No CORS configuration | Blocked in production | Add `CorsConfigurationSource` bean |
| No token revocation | Stolen tokens valid until expiry | Short TTL + Keycloak's token introspection endpoint |
| `spring.jpa.hibernate.ddl-auto=update` | Risk of unintended schema changes in production | Use Flyway or Liquibase for migration control |
| Lack of rate limiting | DoS risk on purchase endpoint | Add Spring Cloud Gateway / Bucket4j rate limiter |
| Single QR code per ticket | If QR is lost, no way to regenerate | Allow regeneration (invalidate old, create new ACTIVE QR) |
| No pagination on ticket validations in ticket entity | `getTicketValidations()` loads all validations | Lazy load validations or use a dedicated count query |

### 10.3 Trade-off Discussion: Keycloak vs. Custom Auth

**Q: Why Keycloak instead of rolling your own JWT auth?**

> Keycloak provides production-grade features out of the box: user storage, social login, MFA, token introspection, PKCE, client credential flows, and admin UI. Building equivalent functionality from scratch would require significant security expertise and ongoing maintenance. The trade-off is operational complexity (running Keycloak) and vendor lock-in to Keycloak-specific claims (like `realm_access.roles`).

### 10.4 Trade-off Discussion: Optimistic vs. Pessimistic Locking

**Q: Why pessimistic locking for ticket purchase instead of optimistic?**

> With **optimistic locking** (`@Version`) the transaction succeeds speculatively and fails with `OptimisticLockException` if a concurrent update is detected — requiring a client-side retry. For ticket purchase, the user experience of a silent retry is poor; they would need to handle "try again" errors. **Pessimistic locking** (`SELECT FOR UPDATE`) serializes concurrent purchase attempts for the same ticket type, making sold-out detection deterministic at the cost of reduced throughput under heavy load.

---

*This guide was generated from the actual codebase at `amirelkased/Event-Ticket-Platform`. All code snippets are taken verbatim from the repository.*
