# Microservices Dependency Impact Analysis

## Executive Summary
- **Total Services**: 13
- **Total Dependencies**: 25
- **Analysis Date**: 2026-01-26T08:46:05.391517

## Services Overview
| Service | Port | Framework | Dependencies |
|---------|------|-----------|--------------|
| product-service | 8082 | null | 0 |
| config-service | 8761 | null | 0 |
| notification-service | 8086 | null | 0 |
| dependency-analyzer-enhanced | null | null | 0 |
| payment-service | 8084 | null | 2 |
| gateway-service | 8080 | null | 11 |
| logging-service | 8088 | null | 0 |
| order-service | 8083 | null | 3 |
| analytics-service | 8090 | null | 4 |
| email-service | 8087 | null | 0 |
| reporting-service | 8089 | null | 3 |
| user-service | 8081 | null | 2 |
| inventory-service | 8085 | null | 0 |

## Dependency Details
### payment-service → order-service
- **Type**: feign-client
- **Source**: src/main/java/com/demo/microservices/payment/client/OrderServiceClient.java

### payment-service → email-service
- **Type**: rest-template
- **Endpoint**: http://email-service:8087/api/email/send
- **Source**: src/main/java/com/example/paymentservice/service/PaymentNotificationService.java

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

### analytics-service → user-service
- **Type**: rest-template
- **Endpoint**: http://user-service:8081/api/users/analytics
- **Source**: src/main/java/com/example/analyticsservice/service/BusinessAnalyticsService.java

### analytics-service → order-service
- **Type**: rest-template
- **Endpoint**: http://order-service:8083/api/orders/analytics
- **Source**: src/main/java/com/example/analyticsservice/service/BusinessAnalyticsService.java

### analytics-service → payment-service
- **Type**: rest-template
- **Endpoint**: http://payment-service:8084/api/payments/analytics
- **Source**: src/main/java/com/example/analyticsservice/service/BusinessAnalyticsService.java

### analytics-service → inventory-service
- **Type**: rest-template
- **Endpoint**: http://inventory-service:8085/api/inventory/analytics
- **Source**: src/main/java/com/example/analyticsservice/service/BusinessAnalyticsService.java

### reporting-service → user-service
- **Type**: rest-template
- **Endpoint**: http://user-service/api/users/count
- **Source**: src/main/java/com/example/reportingservice/service/ReportService.java

### reporting-service → payment-service
- **Type**: feign-client
- **Source**: src/main/java/com/example/reportingservice/client/PaymentServiceClient.java

### reporting-service → order-service
- **Type**: feign-client
- **Source**: src/main/java/com/example/reportingservice/client/OrderServiceClient.java

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

### gateway-service → inventory-service
- **Type**: gateway

