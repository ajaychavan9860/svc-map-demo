#!/bin/bash

echo "=== Testing Inter-Service Communication ==="
echo ""

# Test service health
echo "1. Checking Service Health:"
echo "--------------------------"
for port in 8080 8081 8082 8083 8084 8085 8086; do
    health=$(curl -s http://localhost:$port/actuator/health 2>/dev/null | grep -o '"status":"UP"' || echo "DOWN")
    case $port in
        8080) service="Gateway Service" ;;
        8081) service="User Service" ;;
        8082) service="Product Service" ;;
        8083) service="Order Service" ;;
        8084) service="Payment Service" ;;
        8085) service="Inventory Service" ;;
        8086) service="Notification Service" ;;
    esac
    echo "Port $port ($service): $health"
done

echo ""
echo "2. Testing Gateway Service Routes:"
echo "-------------------------------"

# Test User Service through Gateway
echo "Creating users through Gateway Service..."
user1=$(curl -s -X POST -H "Content-Type: application/json" \
    -d '{"name":"Alice Johnson","email":"alice@example.com","address":"789 Pine St","phone":"555-9999"}' \
    http://localhost:8080/api/users)
echo "Created User: $user1"

user2=$(curl -s -X POST -H "Content-Type: application/json" \
    -d '{"name":"Bob Williams","email":"bob@example.com","address":"321 Elm St","phone":"555-8888"}' \
    http://localhost:8080/api/users)
echo "Created User: $user2"

# Test Product Service through Gateway
echo ""
echo "Creating products through Gateway Service..."
product1=$(curl -s -X POST -H "Content-Type: application/json" \
    -d '{"name":"MacBook Pro","description":"Professional laptop","price":2499.99,"category":"Electronics"}' \
    http://localhost:8080/api/products)
echo "Created Product: $product1"

product2=$(curl -s -X POST -H "Content-Type: application/json" \
    -d '{"name":"Wireless Mouse","description":"Ergonomic wireless mouse","price":29.99,"category":"Accessories"}' \
    http://localhost:8080/api/products)
echo "Created Product: $product2"

echo ""
echo "3. Testing Direct Service Access:"
echo "-------------------------------"

# Get all users
echo "All Users:"
curl -s http://localhost:8081/api/users | jq '.' 2>/dev/null || curl -s http://localhost:8081/api/users

echo ""
echo "All Products:"
curl -s http://localhost:8082/api/products | jq '.' 2>/dev/null || curl -s http://localhost:8082/api/products

echo ""
echo "4. Testing Service Discovery:"
echo "---------------------------"
echo "Checking Eureka Registry:"
eureka_apps=$(curl -s http://localhost:8761/eureka/apps 2>/dev/null | grep -o '<name>[^<]*</name>' | sort | uniq || echo "Unable to fetch from Eureka")
echo "$eureka_apps"

echo ""
echo "5. Testing Load Balancing (through Gateway):"
echo "-------------------------------------------"
echo "Testing multiple requests through Gateway to User Service:"
for i in {1..3}; do
    echo "Request $i:"
    curl -s http://localhost:8080/api/users | wc -l
done

echo ""
echo "6. Testing Available Endpoints:"
echo "-----------------------------"
echo "Gateway actuator info:"
curl -s http://localhost:8080/actuator/info 2>/dev/null || echo "No info endpoint"

echo ""
echo "User Service actuator info:"
curl -s http://localhost:8081/actuator/info 2>/dev/null || echo "No info endpoint"

echo ""
echo "=== Inter-Service Communication Test Complete ==="