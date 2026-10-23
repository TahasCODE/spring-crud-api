
# Order Management REST API

A Spring Boot backend for managing customers, employees, suppliers and orders. It focuses on the rules real order systems need: role-based security, safe concurrent updates, duplicate protection, refunds with limits, and background jobs.

## Features

### Orders
- **Sales and purchases**: sales are placed by customers, purchases by staff from a supplier (single-table inheritance: `SaleOrder`, `PurchaseOrder`)
- **Status workflow**: `PENDING -> CONFIRMED -> SHIPPED -> DELIVERED`, cancellable only before shipping; invalid moves return `400`
- **Dynamic search**: one endpoint filters by type, status, placer, supplier, amount range and date range, with whitelisted sorting and paging (page size capped at 100)
- **Cancellation rules**: a customer may cancel only their own sale order, only while it is `PENDING`, and only within 15 minutes; managers may cancel any order that is still cancellable; the status change is a conditional `UPDATE`, so two clicks cannot both succeed
- **Pricing quote engine**: `POST /api/quotes` returns line totals, the best discount (5% from 10,000 and 10% from 50,000, or a coupon; discounts never stack), then tax, with `BigDecimal` rounding (`HALF_UP`)
- **Refunds**: partial refunds on delivered sale orders within a time window; approved and pending refunds can never exceed the order total; the order row is locked while a request is checked, and a request can be decided only once
- **Order PDFs**: generated on create and update, stored for download
- **CSV export** and a **manager dashboard** (cached in Redis)

### Safety and concurrency
- **Idempotent creation**: an optional `Idempotency-Key` header replays the original response instead of creating a second order; without a key, an identical order from the same user within 60 seconds is rejected (SHA-256 fingerprint claimed atomically in Redis with `SET NX`)
- **Optimistic locking**: orders carry a `version`; `PATCH /api/orders/{id}/status` accepts an `If-Match: <version>` header and answers `409 Conflict` if someone else changed the order first
- **Row locking** for refunds (`SELECT ... FOR UPDATE`) and **compare-and-set updates** for cancellations and refund decisions

### Security
- JWT login with short-lived access tokens (60 min) and single-use refresh tokens (7 days, stored in Redis); logout revokes the refresh token
- Roles: `MANAGER`, `CASHIER`, `EMPLOYEE`, `CUSTOMER`; list and report endpoints are role-restricted, and customers see only their own orders (`/api/orders/mine`); an order someone may not see returns the same `404` as a missing one, so ids cannot be probed
- Login attempt blocking, Redis rate limiting (`429` with `Retry-After`), JSON `401`/`403` responses, CORS allow-list
- Secrets come from environment variables, not from the repository

### Background work and events
- **Auto-expire stale orders**: an hourly scheduled job cancels `PENDING` orders older than a configurable age; it runs in dry-run mode by default, logs what it would do, and uses a Redis lock so only one instance runs it
- **Domain events**: order created and cancelled events are handled after the database commit; a slow customer notification runs in the background (`@Async`) and a Redis-backed activity feed keeps the last 50 events (`GET /api/activity`, manager only)

### Observability
- Request ID in every log line and in the `X-Request-Id` response header
- Actuator health, plus custom Micrometer counters: `orders.created`, `orders.status.changed`, `orders.cancelled`, `orders.expired`, `refunds.requested`, `refunds.decided` (`/actuator/metrics`, manager only)
- Swagger UI for the API docs
- GitHub Actions builds the project on every push and pull request

## Tech stack

Java 17, Spring Boot 4, Spring Security (OAuth2 resource server / JWT), Spring Data JPA (Hibernate), PostgreSQL, Redis, Micrometer, Springdoc OpenAPI, Apache PDFBox, Lombok, Maven, GitHub Actions

## Getting started

### Prerequisites

- Java 17+
- PostgreSQL with an empty database, for example `crud_db`
- Redis running on `localhost:6379`

### Run locally

```bash
git clone https://github.com/TahasCODE/spring-crud-api.git
cd spring-crud-api
```

1. Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties`
2. Set these environment variables (in IntelliJ: Run, Edit Configurations, Environment variables):

| Variable | Meaning |
|---|---|
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Random text of at least 32 characters used to sign tokens |
| `PDF_PASSWORD` | Owner password for generated order PDFs |
| `BOOTSTRAP_MANAGER_PASSWORD` | Password of the first manager account, created on first start |

3. Start the app:

```bash
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`

The first manager account is created from the `app.bootstrap.manager.*` properties; log in with it to create staff accounts.

> Never commit real passwords or secrets. `application.properties` is git-ignored; only the `.example` file is committed.

## API overview

Send the token from login as `Authorization: Bearer <token>`.

### Authentication (`/api/auth`)

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register/customer` | public |
| POST | `/api/auth/register/employee` | manager |
| POST | `/api/auth/login` | public |
| POST | `/api/auth/refresh` | public (refresh token) |
| POST | `/api/auth/logout` | public (refresh token) |
| GET | `/api/auth/me` | logged in |

### Orders (`/api/orders`)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Create an order (duplicate protected, `Idempotency-Key` optional) |
| GET | `/api/orders/mine` | The caller's own orders, newest first (customers) |
| GET | `/api/orders/{id}` | One order (only if the caller may see it) |
| GET | `/api/orders` | All orders, paginated (staff) |
| GET | `/api/orders/search` | Dynamic search with filters, sorting and paging (manager, cashier) |
| GET | `/api/orders/sales`, `/purchases` | Filter by type (staff) |
| GET | `/api/orders/placer/{id}`, `/supplier/{id}` | Filter by placer or supplier (staff) |
| GET | `/api/orders/date-range?from=&to=` | Orders between two dates (staff) |
| PATCH | `/api/orders/{id}/status?status=` | Move to the next status, optional `If-Match: <version>` (manager) |
| POST | `/api/orders/{id}/cancel` | Cancel under the cancellation rules |
| DELETE | `/api/orders/{id}` | Delete an order |
| GET | `/api/orders/summary` | Order counts by status (manager) |
| GET | `/api/orders/export?from=&to=` | CSV export (manager) |
| GET/POST | `/api/orders/{id}/pdf` | Download or generate the order PDF |

### Quotes, refunds, reports and activity

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/quotes` | logged in |
| POST | `/api/orders/{id}/refunds` | customer (owner of the order) |
| GET | `/api/orders/{id}/refunds` | manager or the owning customer |
| GET | `/api/refunds?status=` | manager |
| POST | `/api/refunds/{id}/approve`, `/reject` | manager |
| GET | `/api/reports/dashboard` | manager |
| GET | `/api/activity?limit=` | manager |

Customers, employees and suppliers have their own endpoints; see Swagger UI for the full list.

## Design notes

- **Who may do what** is checked in three layers: role rules in `SecurityConfig`, ownership checks in the service layer, and state and time rules in policy classes (for example `CancellationPolicy`).
- **Three ways to stay safe under concurrency**: a version column (users editing stale screens), a row lock (refund limits that must never be exceeded), and a conditional `UPDATE ... WHERE status = :from` (decisions that may happen only once).
- **Events fire after commit** (`@TransactionalEventListener`), so a rolled-back change never triggers a notification. In-process events are lost if the server crashes right after the commit; a transactional outbox or a message broker is the next step for critical messages.
- **Money** is always `BigDecimal`, compared with `compareTo`, with two decimals and `HALF_UP` rounding at defined points.
- The dashboard is cached in Redis for 60 seconds, so numbers can be up to a minute old.
- The rate limiter uses a fixed-window counter in Redis (`INCR` + `EXPIRE`) and lets requests through if Redis is unavailable.

## Roadmap

- Transactional outbox or message queue for order events
- Order status history (audit trail)
- Audit log in MongoDB
- Cloud deployment

## Author

- Taha Waheed, [LinkedIn](https://www.linkedin.com/in/taha-khan-970a743bb)
- GitHub: [TahasCODE](https://github.com/TahasCODE)
