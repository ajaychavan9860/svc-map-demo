# Microservices Dependency Impact Analysis

## Executive Summary
- **Total Services**: 14
- **Total Dependencies**: 34
- **Analysis Date**: 2026-01-25T11:34:09.116336

## Services Overview
| Service | Port | Framework | Dependencies |
|---------|------|-----------|--------------|
| product-service | 8082 | null | 0 |
| config-service | 8761 | null | 0 |
| notification-service | 8086 | null | 0 |
| dependency-analyzer-enhanced | null | null | 0 |
| payment-service | 8084 | null | 1 |
| gateway-service | 8080 | null | 12 |
| .. | null | null | 11 |
| logging-service | 8088 | null | 0 |
| order-service | 8083 | null | 2 |
| analytics-service | 8090 | null | 4 |
| email-service | 8087 | null | 0 |
| reporting-service | 8089 | null | 2 |
| user-service | 8081 | null | 2 |
| inventory-service | 8085 | null | 0 |

## Dependency Details
### payment-service → email-service
- **Type**: rest-template
- **Endpoint**: http://email-service:8087/api/email/send
- **Source**: src/main/java/com/example/paymentservice/service/PaymentNotificationService.java

### .. → email-service
- **Type**: rest-template
- **Endpoint**: http://email-service:8087/api/email/send
- **Source**: payment-service/src/main/java/com/example/paymentservice/service/PaymentNotificationService.java

### .. → email-service
- **Type**: rest-template
- **Endpoint**: http://email-service:8087/api/email/send
- **Source**: order-service/src/main/java/com/example/orderservice/service/OrderNotificationService.java

### .. → email-service
- **Type**: rest-template
- **Endpoint**: http://email-service:8087/api/email/send
- **Source**: order-service/src/main/java/com/example/orderservice/service/OrderNotificationService.java

### .. → user-service
- **Type**: rest-template
- **Endpoint**: http://user-service:8081/api/users/analytics
- **Source**: analytics-service/src/main/java/com/example/analyticsservice/service/BusinessAnalyticsService.java

### .. → order-service
- **Type**: rest-template
- **Endpoint**: http://order-service:8083/api/orders/analytics
- **Source**: analytics-service/src/main/java/com/example/analyticsservice/service/BusinessAnalyticsService.java

### .. → payment-service
- **Type**: rest-template
- **Endpoint**: http://payment-service:8084/api/payments/analytics
- **Source**: analytics-service/src/main/java/com/example/analyticsservice/service/BusinessAnalyticsService.java

### .. → inventory-service
- **Type**: rest-template
- **Endpoint**: http://inventory-service:8085/api/inventory/analytics
- **Source**: analytics-service/src/main/java/com/example/analyticsservice/service/BusinessAnalyticsService.java

### .. → user-service
- **Type**: rest-template
- **Endpoint**: http://user-service/api/users/count
- **Source**: reporting-service/src/main/java/com/example/reportingservice/service/ReportService.java

### .. → user-service
- **Type**: rest-template
- **Endpoint**: http://user-service/api/users/count
- **Source**: reporting-service/src/main/java/com/example/reportingservice/service/ReportService.java

### .. → logging-service
- **Type**: rest-template
- **Endpoint**: http://logging-service:8088/api/logs/user-activity
- **Source**: user-service/src/main/java/com/example/userservice/service/UserActivityService.java

### .. → email-service
- **Type**: rest-template
- **Endpoint**: http://email-service:8087/api/email/send
- **Source**: user-service/src/main/java/com/example/userservice/service/UserActivityService.java

### order-service → email-service
- **Type**: rest-template
- **Endpoint**: http://email-service:8087/api/email/send
- **Source**: src/main/java/com/example/orderservice/service/OrderNotificationService.java

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

### reporting-service → user-service
- **Type**: rest-template
- **Endpoint**: http://user-service/api/users/count
- **Source**: src/main/java/com/example/reportingservice/service/ReportService.java

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

### gateway-service → ..
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

