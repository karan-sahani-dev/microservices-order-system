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

## Day 11 — Synchronous Inventory Lookup

Added a WebClient-based inventory lookup in order-service.

### Endpoint

GET /api/orders/inventory/{productId}

- Direct access: http://localhost:8081/api/orders/inventory/303
- Gateway access: http://localhost:8080/api/orders/inventory/303

The gateway routes the request to order-service, which calls
inventory-service directly over HTTP.

### Configuration

- Inventory URL: INVENTORY_SERVICE_URL, default http://localhost:8082
- Request timeout: app.inventory.request-timeout-seconds=3

### Manual Tests

| Scenario | Result |
|----------|--------|
| Existing product | 200 OK |
| Missing product | 404 Not Found |
| Invalid product ID (0) | 400 Bad Request |
| Inventory-service stopped | 503 Service Unavailable |
| Recovery after inventory restart | 200 OK |
| Lookup through API Gateway | 200 OK |
| Delayed response using debugger breakpoint | 503 after approximately 3 seconds |
| Recovery after removing breakpoint and resuming | 200 OK |

### Design Notes

- Lookup is read-only and does not reserve stock.
- Available stock is a snapshot, not a reservation guarantee.
- Existing Kafka-based order creation remains unchanged.
- WebClient uses block() in the MVC application, so the calling
  request thread waits for the result within the configured timeout.
- The gateway inventory-route circuit breaker does not protect
  order-service's direct WebClient call.

## Day 12 — Redis Inventory Caching

Added cache-aside caching to inventory-service using Spring Cache
and Redis.

### Implementation

- Cache name: inventory-stock
- Cache key example: inventory-stock::303
- Cached value: InventoryResponse DTO serialized as JSON
- TTL: 5 minutes
- Cache hit: returns the cached DTO without an inventory database lookup
- Cache miss: reads MySQL and populates Redis
- Successful stock additions and reservations evict the product's cache entry
- Transaction-aware cache eviction is deferred until successful commit
- Failed reservations preserve the existing cache entry

### Manual Verification

| Scenario | Observed result |
|----------|-----------------|
| Redis connectivity | PONG |
| Cache population | Stock JSON present in Redis |
| Cache hit | 200 OK without a new inventory SELECT |
| Stock addition | Cache entry removed; next GET loaded updated stock |
| REST reservation | Cache entry removed; updated stock returned |
| Insufficient stock | 409 Conflict; cached stock unchanged |
| Kafka reservation | Cache key changed from present to absent; fresh stock returned |
| TTL expiry | TTL counted down, then returned -2 |
| Reload after expiry | Fresh JSON cached with a new TTL |

### Configuration

- REDIS_HOST: defaults to localhost
- REDIS_PORT: defaults to 6379
- spring.cache.redis.time-to-live=5m
- Redis connection and command timeouts: 2 seconds

### Limitations

MySQL remains the source of truth, and reservations use the atomic
database update. Cached availability is a snapshot and does not
guarantee reservation success.

Transaction-aware caching does not make MySQL and Redis atomic.
Concurrent reads and cache failures can still cause stale data.
Automatic fallback to MySQL when Redis is unavailable is not implemented.

## Day 13 — Order Outcomes Through Kafka

Orders start as PENDING. Inventory processing publishes an outcome
that updates the order asynchronously.

| Inventory outcome | Order status |
|---|---|
| Stock reserved | CONFIRMED |
| Insufficient stock | CANCELLED |
| Product not found | CANCELLED |
| Inventory service unavailable | PENDING until processing resumes |

### Implementation

- Inventory outcomes are persisted as RESERVED or REJECTED.
- Rejection reasons: INSUFFICIENT_STOCK and PRODUCT_NOT_FOUND.
- Stock reservation and inventory outcome share one MySQL transaction.
- Replayed events reuse the saved outcome without reserving stock again.
- Order updates validate event details and acquire a database row lock.
- Repeated matching outcomes leave the final order status unchanged.
- Conflicting outcomes are rejected.

### Kafka Setup

The inventory-rejected topic requires 3 partitions and replication
factor 1 for the local single-broker setup.

Create it from the project root:

    docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:19092 --create --if-not-exists --topic inventory-rejected --partitions 3 --replication-factor 1

### Existing Database Migration

Before starting the updated inventory-service against a pre-Day-13
database, apply:

docs/sql/day13-inventory-reservation-outcomes.sql

This is a one-time migration. Do not rerun it on an already migrated
database or a fresh schema created from the updated entities.

### Manual Verification

- Successful reservation confirmed order 13.
- Insufficient stock cancelled order 15.
- Order 16 remained PENDING during inventory-service downtime and
  became CONFIRMED after recovery.
- Missing product cancelled order 17 with PRODUCT_NOT_FOUND.
- Replaying order 16 did not reserve stock again.
- Replaying rejected order 15 after restocking preserved its rejection
  and left stock unchanged.

### Current Limitations

- MySQL commits and Kafka publishing are separate operations;
  a transactional outbox is not implemented.
- Duplicate event delivery remains possible.
- Unlimited retries can block processing on permanently invalid events;
  dead-letter topic handling is pending.

## Day 14 — Code Review and Reliability Findings

### Reviewed

- Order creation transaction and Kafka acknowledgement handling.
- Durable inventory outcomes and duplicate-event handling.
- Atomic conditional stock reservation.
- Order outcome validation, row locking and status transitions.
- Transaction-aware Redis cache eviction.
- Kafka consumer groups, offsets and retry configuration.
- Database UNIQUE index on inventory.product_id verified.

### Fixes

- Added HTTP 404 mapping for EntityNotFoundException when an order
  is missing.

### Remaining Reliability Work

1. Transactional Outbox for order creation:
   save the order and outgoing event record in one MySQL transaction.
2. Retry classification and Dead Letter Topics:
   preserve permanent failures without endlessly blocking processing.
3. Recovery policy for permanently missing orders and unexpected
   persisted PENDING reservations.
4. Cache recovery for failed eviction and concurrent stale refills.

### Guarantees and Limits

- Matching duplicate outcomes do not change final order status.
- Inventory replay reuses the saved reservation outcome.
- Kafka delivery may contain duplicates.
- Kafka acknowledgement does not imply MySQL commit.
- MySQL and Redis updates are not one atomic transaction.
- Concurrent consumer behaviour was reviewed; a dedicated concurrent
  load test has not yet been performed.

## Day 15 — Order Service Docker Packaging

The Dockerfile packages the prebuilt Spring Boot JAR with a Java 17 runtime.

From the project root in PowerShell, with Docker Desktop and Windows MySQL
running, stop the IntelliJ order-service instance to free port 8081.

```powershell
.\order-service\mvnw.cmd -f order-service/pom.xml package -DskipTests
docker build -t order-service:day15 ./order-service
docker compose up -d kafka

$kafkaId = docker compose ps -q kafka
$kafkaNetwork = (docker inspect $kafkaId | ConvertFrom-Json)[0].NetworkSettings.Networks.PSObject.Properties.Name | Select-Object -First 1

$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'MySQL password' -AsSecureString)).Password

docker run --rm --name order-service-day15 `
  --network $kafkaNetwork `
  -p 127.0.0.1:8081:8081 `
  -e DB_PASSWORD `
  -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/order_db" `
  -e "KAFKA_BOOTSTRAP_SERVERS=kafka:19092" `
  order-service:day15
```

Verified: `GET /health` returned `200 OK`, `GET /api/orders/13` read an
existing MySQL order, and the Kafka consumers received partition assignments.
Stop the foreground container with Ctrl+C.