# Order Management REST API

A Spring Boot REST API that manages **sales to customers** and **purchases from suppliers** through one unified order flow.

## Tech Stack

- Java 17
- Spring Boot 3 (Web, Data JPA, Validation)
- Hibernate / JPA
- Lombok
- Maven
- MySQL (change to PostgreSQL if that's what you use)
- Postman for API testing

## Features

- CRUD for **customers**, **employees** and **suppliers**
- One `Order` entity handling both `SALE` and `PURCHASE` orders
- Order status updates (`PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`)
- Filter orders by type, by placer (customer or employee) and by supplier
- Request validation and global exception handling with clear HTTP status codes

## Design Decisions

- **Inheritance:** `Customer` and `Employee` extend an abstract `OrderPlacer` using `@Inheritance(strategy = JOINED)`. Shared fields (name, email, phone, address) live in one parent table, and subclass fields live in their own tables.
- **Unified orders:** sales and purchase orders share the same fields and status flow, so they are one `Order` entity with an `OrderType`.
- **Business rules in the service layer:** only a customer can place a `SALE`, and only an employee can place a `PURCHASE` (which also requires a supplier).
- **Lazy loading:** relationships use `FetchType.LAZY`, with `@EntityGraph` on repository queries to avoid `LazyInitializationException` and N+1 queries.
- **DTOs as Java records** for requests and responses, so entities are never exposed directly for orders.
- **Layered architecture:** controller, service interface plus `Impl`, repository.
- **`BigDecimal`** for money and **`EnumType.STRING`** for enums.
- **Global exception handler** (`@RestControllerAdvice`) returns consistent JSON errors.

## Entity Relationships

```
OrderPlacer (abstract)
 ├── Customer
 └── Employee

Order  ──ManyToOne──>  OrderPlacer   (required)
Order  ──ManyToOne──>  Supplier      (optional, only for PURCHASE)
```

## Project Structure

```
src/main/java/com/example/CRUD
 ├── Entity        # OrderPlacer, Customer, Employee, Supplier, Order, OrderType, OrderStatus
 ├── DTO           # OrderRequest, OrderResponse
 ├── repo          # Spring Data JPA repositories (interfaces)
 ├── service       # Service interfaces and Impl classes
 ├── controller    # REST controllers
 └── exception     # Custom exceptions and GlobalExceptionHandler
```

## API Endpoints

### Customers (`/api/customers`)

| Method | URL | Description |
|---|---|---|
| GET | `/api/customers` | List all customers |
| GET | `/api/customers/{id}` | Get one customer |
| POST | `/api/customers` | Create a customer |
| PUT | `/api/customers/{id}` | Update a customer |
| DELETE | `/api/customers/{id}` | Delete a customer |

### Employees (`/api/employees`)

Same endpoints as customers, with an extra `designation` field.

### Suppliers (`/api/suppliers`)

Same endpoints as customers, with an extra `contactPerson` field.

### Orders (`/api/orders`)

| Method | URL | Description |
|---|---|---|
| GET | `/api/orders` | List all orders |
| GET | `/api/orders?type=SALE` | List only sales (or `PURCHASE`) |
| GET | `/api/orders/{id}` | Get one order |
| GET | `/api/orders/placer/{placerId}` | Orders placed by a customer or employee |
| GET | `/api/orders/supplier/{supplierId}` | Orders sent to a supplier |
| POST | `/api/orders` | Create an order |
| PATCH | `/api/orders/{id}/status?status=SHIPPED` | Update order status |
| DELETE | `/api/orders/{id}` | Delete an order |

## Sample Requests

**Create a sale** (the placer must be a customer, and no supplier is allowed):

```json
{
  "type": "SALE",
  "placerId": 1,
  "totalAmount": 2500.50,
  "status": "PENDING"
}
```

**Create a purchase** (the placer must be an employee, and a supplier is required):

```json
{
  "type": "PURCHASE",
  "placerId": 4,
  "supplierId": 1,
  "totalAmount": 50000.00
}
```

**Example response:**

```json
{
  "id": 4,
  "type": "PURCHASE",
  "placerId": 4,
  "placerName": "Hamza Tariq",
  "supplierId": 1,
  "supplierName": "ABC Traders",
  "orderDate": "2026-10-05T11:47:51",
  "status": "PENDING",
  "totalAmount": 50000.00
}
```

## Error Responses

| Status | When |
|---|---|
| `400 Bad Request` | Missing or invalid field, supplier on a SALE, no supplier on a PURCHASE, invalid status value |
| `404 Not Found` | Record not found, or the wrong kind of placer (for example an employee id on a SALE) |
| `409 Conflict` | Duplicate email |

Error body format:

```json
{ "error": "Customer not found with id 999" }
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven (or use the included `mvnw`)
- MySQL running locally

### Setup

1. Clone the repository:
```bash
   git clone https://github.com/TahasCODE/crud-api.git
   cd crud-api
```
2. Create a database, for example `crud_db`.
3. Update `src/main/resources/application.properties` with your own values:
```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/crud_db
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   spring.jpa.hibernate.ddl-auto=update
```
4. Run the app:
```bash
   ./mvnw spring-boot:run
```
On Windows PowerShell: `.\mvnw spring-boot:run`
5. The API is available at `http://localhost:8080`.

## Testing

All endpoints were tested with Postman, covering CRUD, filters, status updates and error cases (duplicate email, wrong placer type, missing fields).

## Future Improvements

- `Product` and `OrderItem` entities, with totals calculated from line items
- Spring Security with roles (customer, employee)
- Pagination and sorting
- Swagger / OpenAPI documentation
- Unit and integration tests
- Docker setup

## Author

**Taha Waheed**
- GitHub: [TahasCODE](https://github.com/TahasCODE)
- LinkedIn: [taha-khan](https://www.linkedin.com/in/taha-khan-970a743bb)