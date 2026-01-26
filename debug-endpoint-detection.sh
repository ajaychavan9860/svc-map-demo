#!/bin/bash

echo "=== STEP 1: Find ccg-core-service controllers ==="
find . -path "./360-backend/ccg-core-service" -name "*.java" -o -name "*.Java" | grep -i controller

echo ""
echo "=== STEP 2: Extract endpoints from CcgController ==="
grep -n "@.*Mapping" 360-backend/ccg-core-service/src/main/java/*/controller/CcgController.java || echo "File not found - adjust path"

echo ""
echo "=== STEP 3: Search for endpoint in kafka consumer ==="
echo "Searching for '/v1/rawMessage' in ccg-kafka-consumer-service:"
grep -r '"/v1/rawMessage"' 360-backend/ccg-kafka-consumer-service/src/ || echo "NOT FOUND"

echo ""
echo "=== STEP 4: Check what the analyzer detected ==="
if [ -f "360-backend/dependency-analysis/analysis-result.json" ]; then
    echo "ccg-core-service exposes_endpoints:"
    grep -A 10 '"service_name": "ccg-core-service"' 360-backend/dependency-analysis/analysis-result.json | grep -A 5 '"exposes_endpoints"'
    
    echo ""
    echo "ccg-kafka-consumer-service dependencies:"
    grep -A 20 '"service_name": "ccg-kafka-consumer-service"' 360-backend/dependency-analysis/analysis-result.json | grep -A 15 '"dependencies"'
else
    echo "analysis-result.json not found - run analyzer first"
fi
