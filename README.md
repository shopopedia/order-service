# Order Service

Spring Boot REST service for creating and retrieving customer orders.

## Overview

- Application name: `order-service`
- Main package: `com.shopopedia.order`
- Java version: 21
- Spring Boot: 4.0.6
- Base API path: `/api/orders`
- Default server port: `8086`
- Default database: H2 in-memory database in Oracle compatibility mode

The service currently supports:

- Creating a new order
- Fetching an order by order id
- Fetching all orders for a user id
- Validation error handling
- Not-found error handling

## Tech Stack

- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Boot Actuator
- Lombok
- H2 Database
- Oracle JDBC driver support

## Running The Service

Use a local Gradle installation, because this repository does not include the Gradle wrapper.

### Start With The Default H2 Profile

```bash
gradle bootRun
```

### Build The Application

```bash
gradle clean build
```

### Start With The Oracle Profile

```bash
SPRING_PROFILES_ACTIVE=oracle gradle bootRun
```

## Configuration

### `src/main/resources/application.yml`

Default development configuration:

- Server port: `8086`
- Datasource: `jdbc:h2:mem:order_service;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- Username: `sa`
- Password: empty
- Driver: `org.h2.Driver`
- JPA schema mode: `ddl-auto: update`
- SQL logging enabled
- Actuator endpoints exposed: `health`, `info`

### `src/main/resources/application-oracle.yml`

Oracle profile configuration:

- Datasource URL: `jdbc:oracle:thin:@localhost:1521/FREEPDB1`
- Username: `order_service`
- Password: `order_service_pass`
- Driver: `oracle.jdbc.OracleDriver`
- Hibernate dialect: `org.hibernate.dialect.OracleDialect`

## API Summary

All endpoints are under the `/api/orders` base path.

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/orders` | Create a new order |
| `GET` | `/api/orders/{orderId}` | Fetch one order by id |
| `GET` | `/api/orders/user/{userId}` | Fetch all orders for a user |

## Response Envelope

All controller responses use the `ApiResponse<T>` wrapper:

```json
{
  "timestamp": "2026-06-03T12:34:56.123",
  "status": 200,
  "message": "Order fetched successfully",
  "data": {}
}
```

Fields:

- `timestamp`: server time when the response was created
- `status`: HTTP status code as an integer
- `message`: human-readable message
- `data`: payload or error details

## API Details

### Create Order

`POST /api/orders`

Request body:

```json
{
  "userId": 101,
  "items": [
    {
      "productId": 501,
      "quantity": 2,
      "price": 199.99
    },
    {
      "productId": 777,
      "quantity": 1,
      "price": 349.0
    }
  ]
}
```

Validation rules:

- `userId` is required
- `items` is required and cannot be empty
- Each item is validated recursively
- `productId` is required
- `quantity` is required and must be at least `1`
- `price` is required and must be at least `1`

Successful response:

- HTTP status: `201 Created`
- Body message: `Order created successfully`

Example response:

```json
{
  "timestamp": "2026-06-03T12:34:56.123",
  "status": 201,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "userId": 101,
    "status": "CREATED",
    "totalAmount": 749.98,
    "createdAt": "2026-06-03T12:34:56.000",
    "items": [
      {
        "id": 1,
        "productId": 501,
        "quantity": 2,
        "price": 199.99,
        "lineAmount": 399.98
      },
      {
        "id": 2,
        "productId": 777,
        "quantity": 1,
        "price": 349.0,
        "lineAmount": 349.0
      }
    ]
  }
}
```

Business rules during create:

- Order status is set to `CREATED` automatically before persistence
- `createdAt` is set automatically on persist
- `lineAmount` is calculated as `price * quantity`
- `totalAmount` is calculated as the sum of all item line amounts
- Order items are persisted through the order aggregate cascade

### Get Order By Id

`GET /api/orders/{orderId}`

Example:

```bash
curl http://localhost:8086/api/orders/1
```

Successful response:

- HTTP status: `200 OK`
- Body message: `Order fetched successfully`

If the order does not exist:

- HTTP status: `404 Not Found`
- Body message: the exception message, for example `Order not found with id: 1`
- `data`: `null`

### Get Orders By User Id

`GET /api/orders/user/{userId}`

Example:

```bash
curl http://localhost:8086/api/orders/user/101
```

Successful response:

- HTTP status: `200 OK`
- Body message: `User orders fetched successfully`

The `data` field is a list of `OrderResponse` objects.

## Error Handling

The service has a global exception handler.

### Validation Errors

Returned when request validation fails.

- HTTP status: `400 Bad Request`
- Body message: `Validation failed`
- `data`: a map of field name to validation message

Example:

```json
{
  "timestamp": "2026-06-03T12:34:56.123",
  "status": 400,
  "message": "Validation failed",
  "data": {
    "userId": "User id is required",
    "items": "Order items are required"
  }
}
```

### Order Not Found

Returned when an order id does not exist.

- HTTP status: `404 Not Found`
- `data`: `null`

## Data Model

### `orders`

- `id`: primary key, auto-generated
- `userId`: required
- `status`: required enum, stored as string
- `totalAmount`: calculated order total
- `createdAt`: set on persist
- `items`: one-to-many relationship to order items

### `order_items`

- `id`: primary key, auto-generated
- `productId`: required
- `quantity`: required
- `price`: item unit price
- `lineAmount`: `price * quantity`
- `order_id`: foreign key to `orders`

## Domain Rules

- `OrderStatus` currently contains `CREATED`, `CONFIRMED`, and `CANCELLED`
- New orders are always created in `CREATED` status
- `Order.items` uses `cascade = CascadeType.ALL`
- `Order.items` uses `orphanRemoval = true`
- `OrderItem.order` is a lazy `ManyToOne` relationship

## Repository Layer

- `OrderRepository` extends `JpaRepository<Order, Long>`
- `OrderRepository` adds `findByUserId(Long userId)`
- `OrderItemRepository` extends `JpaRepository<OrderItem, Long>`

## Project Structure

```text
src/main/java/com/shopopedia/order
  controller/        REST endpoints
  dto/               API request and response records
  entity/            JPA entities and enums
  exception/         Global exception handling
  repository/        Spring Data repositories
  service/           Business logic
  OrderServiceApplication.java
src/main/resources
  application.yml
  application-oracle.yml
```

## Actuator

The application exposes:

- `GET /actuator/health`
- `GET /actuator/info`

## Notes

- There is no update or cancel endpoint in the current API.
- There is no authentication or authorization layer in the current codebase.
- The default H2 datasource runs in Oracle compatibility mode to reduce differences between local development and Oracle.
