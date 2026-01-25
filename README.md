# Microservices Demo Project

This is a multi-module Maven project demonstrating a microservices architecture with Spring Boot and Spring Cloud.

## Services

1. **Config Service** (Port: 8761) - Eureka Server & Configuration Server
2. **Gateway Service** (Port: 8080) - API Gateway using Spring Cloud Gateway
3. **User Service** (Port: 8081) - User management
4. **Product Service** (Port: 8082) - Product catalog management
5. **Order Service** (Port: 8083) - Order processing
6. **Payment Service** (Port: 8084) - Payment processing
7. **Inventory Service** (Port: 8085) - Inventory management
8. **Notification Service** (Port: 8086) - Notification handling

## Service Interactions

- **Order Service** communicates with User Service and Product Service
- **Payment Service** communicates with Order Service
- **Inventory Service** manages stock levels
- **Notification Service** handles email/SMS notifications
- **Gateway Service** routes requests to appropriate services
- **Config Service** serves as Eureka Server for service discovery

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Building and Running

### Build all services:

```bash
mvn clean compile
```

### Run services in order:

1. Start Config Service (Eureka Server):

```bash
cd config-service
mvn spring-boot:run
```

2. Start Gateway Service:

```bash
cd gateway-service
mvn spring-boot:run
```

3. Start all other services:

```bash
# In separate terminals
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

## API Endpoints

All services can be accessed through the Gateway at `http://localhost:8080`:

- Users: `GET|POST|PUT|DELETE /api/users`
- Products: `GET|POST|PUT|DELETE /api/products`
- Orders: `GET|POST|PUT /api/orders`
- Payments: `GET|POST|PUT /api/payments`
- Inventory: `GET|POST|PUT /api/inventory`
- Notifications: `GET|POST /api/notifications`

## Monitoring

- Eureka Dashboard: http://localhost:8761
- H2 Consoles available for each service on their respective ports
