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


### Day 4 — Kafka Infrastructure and CLI Testing

- Started Apache Kafka 4.1.2 locally using Docker Compose in KRaft mode.
- Configured localhost:9092 for host clients and kafka:19092 for Docker clients.
- Added a named Docker volume for Kafka data.
- Created order-placed and inventory-reserved topics:
    - 3 partitions per topic
    - Replication factor: 1
- Published a sample order event using the console producer.
- Read the event using the inventory-demo consumer group.
- Verified independent consumption using the notification-demo consumer group.

Kafka testing currently uses CLI tools. Spring Boot producer and consumer
integration will be implemented in the following days.

This is a single-broker local setup without broker-level redundancy.

### Day 5 — Kafka Producer in Order Service

- Added Spring Boot Kafka integration.
- Created OrderPlacedEvent and OrderEventPublisher.
- Published order-placed events with order ID as the record key.
- Configured JSON serialization and acks=all.
- Added bounded acknowledgement waiting and metadata/buffer blocking timeout.

Verified:
- POST /api/orders returned 201 with PENDING status.
- CLI consumer received matching order ID, key and event data.
- GET /api/orders/3 returned the saved order.
- Kafka-down request returned 500; SQL verification found no test order.
- Service remained UP and publishing recovered after Kafka restart.

Known limitation:
MySQL commit and Kafka publishing are not atomic. A timeout can leave
delivery uncertain. Transactional outbox is planned for reliable delivery.
Inventory consumer integration is pending.

### Day 6 — Kafka Consumer and Inventory Reservation

- Consumed order-placed events using @KafkaListener.
- Mapped JSON payloads to local event objects and validated inputs.
- Added inventory_reservations with order_id as the primary key.
- Reserved stock and saved the reservation in one MySQL transaction.
- Added duplicate detection and conflicting-payload checks.
- Published inventory-reserved events after reservation commit.
- Configured record acknowledgements and a 2-second retry backoff.

Verified:
- Retained order events were consumed and stock was reserved.
- Inventory downtime did not stop order-service.
- Order 6 was processed after inventory-service restarted.
- Order 7 requested 86 units with only 85 available:
  stock stayed unchanged and no reservation row persisted.
- After adding 1 unit, retry succeeded and published the result.
- Final product 101 stock: total 109, reserved 109, available 0.

Limitations:
- Unlimited retries are a local learning policy; permanent failures
  can delay further consumption. Rejection events/DLT are pending.
- Reservation results can be published more than once.
- MySQL, Kafka publishing and offset commits are not atomic.
- Order confirmation consumer and transactional outbox are pending.

### Day 7 — Week 1 Review

- Verified order creation → OrderPlaced → stock reservation → InventoryReserved.
- Created order 8 for product 303 with quantity 2.
- Replayed the same OrderPlaced event using Kafka key 8.
- Verified stock remained: total 10, reserved 2, available 8.
- Verified exactly one reservation row exists for order 8.
- InventoryReserved was published again on replay; downstream consumers must handle duplicates.
- Orders remain PENDING; order confirmation is not implemented yet.

### Day 8 — Notification Service

- Added Kafka consumers for order-placed and inventory-reserved.
- Used an independent notification-service consumer group.
- Added JSON event mapping and explicit Bean Validation.
- Simulated notifications through console logs.
- Verified both notifications for order 9.
- Created order 10 while notification-service was stopped.
- Verified both notifications for order 10 after restart.

Known limitations:
- Console simulation only; no real email/SMS delivery.
- Duplicate events or retries can repeat notifications.
- No ordering guarantee across the two topics.
- Unlimited retries can block progress on invalid events; bounded retries and DLT are pending.
- Order confirmation is not implemented; orders remain PENDING.

### Day 9 — API Gateway

- Added api-gateway using Spring Cloud Gateway Server WebFlux on port 8080.
- Routed /api/orders and /api/orders/** to order-service on port 8081.
- Routed /api/inventory and /api/inventory/** to inventory-service on port 8082.
- Preserved request paths; backend services retain their business logic.
- Made destination URLs configurable through ORDER_SERVICE_URL and INVENTORY_SERVICE_URL.

Manual verification:
- Inventory GET through gateway returned 200.
- Order GET through gateway returned 200.
- Order POST through gateway returned 201 and created order 11.
- Product 303 stock changed from reserved 5 / available 5 to reserved 6 / available 4.
- With inventory-service stopped, inventory GET returned 500 while order GET still returned 200.
- Inventory route recovered after inventory-service restarted without restarting the gateway.

Known limitations:
- No gateway authentication, circuit breaker, or custom backend-error mapping yet.
- Backend ports remain directly accessible in the local setup.

### Day 10 — Circuit Breaker

- Added Resilience4j circuit breaker to the gateway inventory route.
- Configured a 10-call sliding window, minimum 5 calls, and 50% failure threshold.
- Configured a 10-second OPEN wait, 2 HALF_OPEN trial calls, and a 3-second timeout.
- Added a fallback returning HTTP 503 with a clear inventory-unavailable message.
- Added circuit-breaker state-transition logging.

Manual testing:
- Healthy inventory request returned 200.
- Requests while inventory-service was stopped returned the 503 fallback.
- Observed CLOSED_TO_OPEN.
- Restarted inventory-service without restarting the gateway.
- Observed OPEN_TO_HALF_OPEN and HALF_OPEN_TO_CLOSED during recovery.

Notes:
- A 503 fallback does not indicate zero stock.
- Fallback responses alone do not prove the circuit is OPEN.
- Circuit breaker protects gateway HTTP calls; Kafka processing is separate.