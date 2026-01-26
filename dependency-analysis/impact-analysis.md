# Microservices Dependency Impact Analysis

## Executive Summary
- **Total Services**: 14
- **Total Dependencies**: 23
- **Analysis Date**: 2026-01-26T11:29:12.180187

## Services Overview
| Service | Port | Framework | Dependencies |
|---------|------|-----------|--------------|
| product-service | 8082 | null | 0 |
| config-service | 8761 | null | 0 |
| notification-service | 8086 | null | 0 |
| dependency-analyzer-enhanced | null | null | 0 |
| payment-service | 8084 | null | 2 |
| gateway-service | 8080 | null | 12 |
| logging-service | 8088 | null | 0 |
| order-service | 8083 | null | 4 |
| analytics-service | 8090 | null | 0 |
| email-service | 8087 | null | 0 |
| reporting-service | 8089 | null | 2 |
| user-service | 8081 | null | 3 |
| common-lib | null | null | 0 |
| inventory-service | 8085 | null | 0 |

## Dependency Details
### payment-service → order-service
- **Type**: feign-client
- **Source**: src/main/java/com/demo/microservices/payment/client/OrderServiceClient.java

### payment-service → email-service
- **Type**: rest-template
- **Endpoint**: http://email-service:8087/api/email/send
- **Source**: src/main/java/com/example/paymentservice/service/PaymentNotificationService.java

### order-service → common-lib
- **Type**: maven-dependency
- **Source**: pom.xml

### order-service → product-service
- **Type**: feign-client
- **Source**: src/main/java/com/demo/microservices/order/client/ProductServiceClient.java

### order-service → user-service
- **Type**: feign-client
- **Source**: src/main/java/com/demo/microservices/order/client/UserServiceClient.java

### order-service → email-service
- **Type**: rest-template
- **Endpoint**: http://email-service:8087/api/email/send
- **Source**: src/main/java/com/example/orderservice/service/OrderNotificationService.java

### reporting-service → payment-service
- **Type**: feign-client
- **Source**: src/main/java/com/example/reportingservice/client/PaymentServiceClient.java

### reporting-service → order-service
- **Type**: feign-client
- **Source**: src/main/java/com/example/reportingservice/client/OrderServiceClient.java

### user-service → common-lib
- **Type**: maven-dependency
- **Source**: pom.xml

### user-service → logging-service
- **Type**: rest-template
- **Endpoint**: http://logging-service:8088/api/logs/user-activity
- **Source**: src/main/java/com/example/userservice/service/UserActivityService.java

### user-service → email-service
- **Type**: rest-template
- **Endpoint**: http://email-service:8087/api/email/send
- **Source**: src/main/java/com/example/userservice/service/UserActivityService.java

### gateway-service → product-service
- **Type**: gateway

### gateway-service → notification-service
- **Type**: gateway

### gateway-service → dependency-analyzer-enhanced
- **Type**: gateway

### gateway-service → payment-service
- **Type**: gateway

### gateway-service → logging-service
- **Type**: gateway

### gateway-service → order-service
- **Type**: gateway

### gateway-service → analytics-service
- **Type**: gateway

### gateway-service → email-service
- **Type**: gateway

### gateway-service → reporting-service
- **Type**: gateway

### gateway-service → user-service
- **Type**: gateway

### gateway-service → common-lib
- **Type**: gateway

### gateway-service → inventory-service
- **Type**: gateway

