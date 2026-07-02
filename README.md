# AI Gateway

A production-shaped Spring Boot backend for a multi-tenant **AI-as-a-Service** platform. Built as a learning project that touches every important Spring Boot concept: REST, JPA, Flyway, Security, JWT, API keys, rate limiting, config profiles, exception handling, OpenAPI, and third-party HTTP integration.

Users register, receive a JWT, generate API keys, and call a `/chat` endpoint that proxies to any OpenAI-compatible LLM (OpenAI, Groq, Together, OpenRouter, Ollama...). Every request is authenticated, rate-limited by plan, persisted as a conversation, and metered.

---

## Tech Stack

| Concern            | Choice                                                         |
| ------------------ | -------------------------------------------------------------- |
| Runtime            | Java 17, Spring Boot 3.5.16                                    |
| Web                | Spring Web (REST), Bean Validation                             |
| Persistence        | Spring Data JPA, Hibernate, **PostgreSQL** (prod) / **H2** (dev) |
| Migrations         | Flyway                                                         |
| Security           | Spring Security + JWT (jjwt), BCrypt, custom API-key filter    |
| Rate limiting      | Bucket4j (in-memory; swap to Redis for horizontal scale)       |
| LLM integration    | `RestClient` \u2192 OpenAI-compatible `/chat/completions`         |
| Docs               | Springdoc OpenAPI + Swagger UI                                 |
| Build              | Maven                                                          |

---

## Quick Start (Dev, H2, No API key required)

Prerequisites: **JDK 17+**, **Maven 3.9+**, Git.

```bash
mvn spring-boot:run
```

That's it. The app boots with an embedded H2 database and a mock AI provider that echoes your messages back. Seed users are created automatically:

| Email                       | Password    | Role  | Plan       |
| --------------------------- | ----------- | ----- | ---------- |
| `admin@aigateway.local`     | `Admin123!` | ADMIN | ENTERPRISE |
| `demo@aigateway.local`      | `Demo1234!` | USER  | FREE       |

Then open:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- H2 console: <http://localhost:8080/h2-console> (JDBC URL: `jdbc:h2:file:./data/ai-gateway`)
- Health:     <http://localhost:8080/actuator/health>

### Try the API in 30 seconds

```bash
# 1. Login as the demo user
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@aigateway.local","password":"Demo1234!"}'
# -> { "accessToken": "eyJhbGci...", ... }

# 2. Call the AI (using the JWT from step 1)
curl -s -X POST http://localhost:8080/api/v1/chat \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello, world!"}'
```

---

## Using a real LLM (OpenAI / Groq / OpenRouter / Ollama)

Set two environment variables and restart:

```bash
# .env or shell exports
AI_PROVIDER=openai
OPENAI_API_KEY=sk-...
# Optional overrides:
OPENAI_BASE_URL=https://api.openai.com/v1        # or Groq / OpenRouter / http://localhost:11434/v1 for Ollama
OPENAI_MODEL=gpt-4o-mini
```

Any OpenAI-compatible `/chat/completions` endpoint works \u2014 the same code path calls all of them.

---

## Running with Postgres (Prod profile)

```bash
docker compose up -d postgres

export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET="$(openssl rand -base64 48)"      # or any 32+ char string
export AI_PROVIDER=openai
export OPENAI_API_KEY=sk-...

mvn spring-boot:run
```

Flyway runs migrations automatically on startup. See `.env.example` for the full list of variables.

---

## Endpoints

Base path: `/api/v1`

### Public
| Method | Path                | Purpose                              |
| ------ | ------------------- | ------------------------------------ |
| POST   | `/auth/register`    | Create an account, get a JWT         |
| POST   | `/auth/login`       | Login, get a JWT                     |

### Authenticated (Bearer JWT **or** `X-API-Key` header)
| Method | Path                                     | Purpose                                    |
| ------ | ---------------------------------------- | ------------------------------------------ |
| GET    | `/me`                                    | Current user profile                       |
| POST   | `/chat`                                  | Send a chat message                        |
| GET    | `/conversations`                         | List paginated conversations               |
| GET    | `/conversations/{id}`                    | Conversation with all messages             |
| DELETE | `/conversations/{id}`                    | Delete a conversation                      |
| GET    | `/api-keys`                              | List your API keys                         |
| POST   | `/api-keys`                              | Create a new API key (plaintext returned **once**) |
| DELETE | `/api-keys/{id}`                         | Revoke an API key                          |
| GET    | `/prompt-templates`                      | Your templates (`?includePublic=true` for public too) |
| POST   | `/prompt-templates`                      | Create a template                          |
| PUT    | `/prompt-templates/{id}`                 | Update your template                       |
| DELETE | `/prompt-templates/{id}`                 | Delete your template                       |
| GET    | `/usage/summary`                         | Your last-24h / last-30d totals            |

### Admin only (`ROLE_ADMIN`)
| Method | Path                                | Purpose                            |
| ------ | ----------------------------------- | ---------------------------------- |
| GET    | `/admin/users`                      | List all users                     |
| PUT    | `/admin/users/{id}/plan`            | Change a user's plan               |
| PUT    | `/admin/users/{id}/enabled`         | Enable/disable an account          |

---

## Architecture

```
src/main/java/com/aigateway/
  \u251c\u2500 AiGatewayApplication.java   \u2190 entry point
  \u251c\u2500 config/                     \u2190 Spring configuration classes (Security, OpenAPI, Properties, seed)
  \u251c\u2500 common/                     \u2190 base entity, error DTO, exceptions, global handler
  \u251c\u2500 security/                   \u2190 JwtService, JwtFilter, ApiKeyFilter, UserDetailsService, hashing
  \u251c\u2500 user/                       \u2190 User entity, Plan/Role enums, MeController
  \u251c\u2500 auth/                       \u2190 register/login
  \u251c\u2500 apikey/                     \u2190 machine-to-machine keys
  \u251c\u2500 prompt/                     \u2190 saved prompt templates (owned + public)
  \u251c\u2500 conversation/               \u2190 chat history persistence
  \u251c\u2500 ai/                         \u2190 provider abstraction (mock + OpenAI), ChatService, ChatController
  \u251c\u2500 ratelimit/                  \u2190 Bucket4j-based per-user limits
  \u251c\u2500 usage/                      \u2190 per-call metering + summary endpoint
  \u2514\u2500 admin/                      \u2190 admin-only user management
```

### Two authentication modes
- **JWT** (`Authorization: Bearer <token>`) \u2014 short-lived, ideal for browser/mobile.
- **API key** (`X-API-Key: aig_...`) \u2014 long-lived, ideal for servers and CLIs. Keys are hashed (SHA-256) before storage; only a short prefix is displayed for the UI.

### Rate limiting by plan
Each user has one of `FREE`, `PRO`, `ENTERPRISE`. Limits are configured in `application.yml` under `app.plans.*`:

| Plan       | Req/min | Req/day  | Max tokens/req |
| ---------- | ------: | -------: | -------------: |
| FREE       |       5 |      100 |          1,024 |
| PRO        |      60 |   10,000 |          4,096 |
| ENTERPRISE |     600 |1,000,000 |         32,000 |

Rate limits are enforced in-memory (Bucket4j). For a multi-instance deployment, swap the `RateLimitService` to use `LettuceBasedProxyManager` (Redis) \u2014 no other code changes needed.

---

## Running Tests

```bash
mvn test
```

Tests run against an in-memory H2 with the `mock` AI provider (see `src/test/resources/application-test.yml`).

---

## Suggested Learning Path

Work through these in order \u2014 each unlocks a new Spring Boot concept and makes the app more valuable.

**Fundamentals**
1. Read through `AiGatewayApplication` \u2192 `AuthController` \u2192 `AuthService` \u2192 `JwtService`. Follow one request from HTTP to DB.
2. Change the JWT expiration to 5 minutes and observe the 401s.
3. Add a `phoneNumber` field to `User` (entity + Flyway migration + register DTO).

**Data**
4. Add pagination to `/api/v1/prompt-templates`.
5. Add a search endpoint: `GET /prompt-templates/search?q=...` using a JPQL `LIKE` query.
6. Add soft-delete to `PromptTemplate` (a `deletedAt` column + custom `@Where` clause or Hibernate `@SQLRestriction`).

**Security**
7. Add refresh tokens (`/auth/refresh`), stored in a `refresh_tokens` table with rotation.
8. Add password reset via a one-time token + email (use Mailhog for local SMTP).
9. Add OAuth2 login (Google) using `spring-boot-starter-oauth2-client`.

**Reliability**
10. Add **Testcontainers** for a real Postgres in integration tests.
11. Add MockMvc / `@WebMvcTest` slice tests for `ChatController` and `AuthController`.
12. Add a `@RestControllerAdvice` test that verifies error DTO shape.

**AI features**
13. Add **streaming responses** (SSE) for `/chat` using `SseEmitter` or `Flux<String>`.
14. Add an **embeddings + vector-search** endpoint (RAG) using pgvector.
15. Add a background worker (Spring's `@Async` + `@Scheduled`) that summarizes old conversations to keep the DB slim.

**Product**
16. Add **Stripe** subscriptions \u2192 change a user's `plan` via a webhook.
17. Build a tiny React/Vue frontend that consumes this API.
18. Deploy to Fly.io / Render / Railway with a managed Postgres.

**Ops**
19. Add structured JSON logging and a request-id filter.
20. Add Prometheus metrics via Micrometer and a Grafana dashboard for tokens/sec, latency, error rate per plan.
21. Add distributed rate limiting via Redis (Bucket4j + Lettuce).

---

## License

MIT
