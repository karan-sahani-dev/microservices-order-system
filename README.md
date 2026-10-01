# Microservices Order System

A Java backend project built with independently runnable Spring Boot services.
Development follows a day-by-day approach.

## Services and Current Progress

| Service | Port | Responsibility | Current status |
|---|---|---|---|
| order-service | 8081 | Create and fetch orders | Order APIs and MySQL persistence implemented |
| inventory-service | 8082 | Manage and reserve stock | Inventory APIs, validation and error handling implemented |
| notification-service | 8083 | Send order notifications | Basic application and health endpoint implemented |

## Repository Structure

- order-service/ — Independent Maven project
- inventory-service/ — Independent Maven project
- notification-service/ — Independent Maven project

Each service has its own pom.xml, application entry point and configuration.

## Current Architecture

Each service runs as a separate application.

Business APIs follow:
Controller → Service → Repository → MySQL

- order-service owns order_db.
- inventory-service owns inventory_db.
- Both databases currently run on the same local MySQL server.
- inventory-service uses a dedicated database account restricted to inventory_db.
- Inter-service business communication is not implemented yet.

## Planned Event Flow

1. order-service creates a pending order and publishes OrderPlaced.
2. inventory-service consumes the event and reserves stock.
3. inventory-service publishes the reservation result.
4. order-service updates the order status.
5. notification-service consumes relevant events and sends notifications.

Kafka integration, API Gateway, Redis and circuit breakers are planned.

## Technology

Implemented:
- Java 17
- Spring Boot
- Spring Data JPA / Hibernate
- MySQL
- Bean Validation
- Lombok

Planned:
- Apache Kafka
- Redis
- Spring Cloud Gateway
- Resilience4j
- Docker Compose
- GitHub Actions

## APIs

| Service | Method | Endpoint | Purpose |
|---|---|---|---|
| All three services | GET | /health | Basic service health |
| order-service | POST | /api/orders | Create a pending order |
| order-service | GET | /api/orders/{id} | Fetch an order |
| inventory-service | POST | /api/inventory | Create inventory or add stock |
| inventory-service | GET | /api/inventory/{productId} | Fetch stock details |
| inventory-service | POST | /api/inventory/reserve | Reserve available stock |

## Local Setup

1. Install Java 17 and run MySQL on localhost:3306.
2. Create order_db and inventory_db.
3. Configure the database accounts referenced by each service.
4. Set these environment variables in the corresponding run configurations:
    - order-service: DB_PASSWORD
    - inventory-service: INVENTORY_DB_PASSWORD
5. Run each service's application class independently.

Database passwords must not be stored in tracked configuration files.

## Inventory Rules

Available stock = quantity - reservedQuantity.

- Adding stock increases quantity.
- Reserving stock increases reservedQuantity.
- Reservation does not decrease total quantity.
- Conditional database updates prevent reservations beyond available stock.
- MySQL upsert creates new inventory or increments existing stock.

## Inventory Error Responses

| Status | Meaning |
|---|---|
| 400 | Invalid input |
| 404 | Inventory record not found |
| 409 | Insufficient stock |

## Day 3 Verification

Verified database access isolation and ten API test scenarios,
including stock creation, fetching, reservation, validation,
missing products, stock increment and boundary conditions.

A two-request parallel reservation smoke test returned one 200 and one 409.
Final stock was total 5, reserved 4, available 1.