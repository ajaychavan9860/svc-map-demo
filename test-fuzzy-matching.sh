#!/bin/bash

# Test fuzzy matching with your specific service names

echo "🧪 Testing Fuzzy Matching Logic"
echo "================================"
echo ""

test_cases=(
    "ccg-service:ccg-core-service"
    "ccg-kafka-consumer:ccg-kafka-consumer-service"
    "task-management:task-management-service"
    "task:task-management-service"
    "excel:excel-service"
    "wsp-service:wsp-service"
)

echo "Test Cases:"
for test in "${test_cases[@]}"; do
    IFS=':' read -r feign_name service_name <<< "$test"
    echo "  Feign: '$feign_name' → Service: '$service_name'"
done
echo ""

echo "Expected Matching Results:"
echo "  ccg-service (normalized: 'ccg') should match ccg-core-service (normalized: 'ccg core')"
echo "    ✓ Via partial match: 'ccg core' contains 'ccg'"
echo ""
echo "  ccg-kafka-consumer (normalized: 'ccg kafka consumer') should match ccg-kafka-consumer-service"
echo "    ✓ Via base match: 'ccg-kafka-consumer' == 'ccg-kafka-consumer' (after removing -service)"
echo ""
echo "  task-management (normalized: 'task management') should match task-management-service"
echo "    ✓ Via base match: 'task-management' == 'task-management' (after removing -service)"
echo ""

echo "Download latest version and run:"
echo "  git pull origin main"
echo "  cd dependency-analyzer-enhanced"
echo "  mvn clean package -DskipTests"
echo "  java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/360/backend 2>&1 | tee analysis.log"
echo ""
echo "Then check the logs:"
echo "  grep '✓.*match:' analysis.log"
echo "  grep '❌ Could not find' analysis.log"
