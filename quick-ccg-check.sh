#!/bin/bash
# Quick diagnostic - check what was actually detected

echo "========================================"
echo "CCG Dependency Detection Check"
echo "========================================"
echo ""

# Check if this is being run from the right location
if [ ! -f "dependency-analysis/analysis-result.json" ]; then
    echo "ERROR: Run this from your 360/backend project directory"
    echo "Usage: cd /path/to/360/backend && bash /path/to/this/script.sh"
    exit 1
fi

echo "1. Checking if CCG services were discovered:"
cat dependency-analysis/analysis-result.json | jq -r '.services[] | select(.name | contains("ccg")) | "  ✓ \(.name)"'
echo ""

echo "2. Checking ALL dependencies involving CCG services:"
cat dependency-analysis/analysis-result.json | jq -r '.dependencies[] | select(.from_service | contains("ccg") or .target_service | contains("ccg")) | "  \(.from_service) -> \(.target_service) [\(.dependency_type)]"' | sort -u
echo ""

echo "3. Specific check: ccg-kafka-consumer-service -> ccg-core-service"
FOUND=$(cat dependency-analysis/analysis-result.json | jq -r '.dependencies[] | select(.from_service == "ccg-kafka-consumer-service" and .target_service == "ccg-core-service") | "FOUND"')
if [ "$FOUND" = "FOUND" ]; then
    echo "  ✓ DETECTED in JSON"
    cat dependency-analysis/analysis-result.json | jq '.dependencies[] | select(.from_service == "ccg-kafka-consumer-service" and .target_service == "ccg-core-service")'
else
    echo "  ✗ NOT FOUND in JSON"
    echo ""
    echo "Let's check what ccg-kafka-consumer-service calls:"
    cat dependency-analysis/analysis-result.json | jq -r '.dependencies[] | select(.from_service == "ccg-kafka-consumer-service") | "    -> \(.target_service) [\(.dependency_type)]"'
fi
echo ""

echo "4. Did property resolution work for ccg services?"
echo "Run with full logs to see property resolution:"
echo "  java -jar /path/to/analyzer.jar . 2>&1 | grep -i 'ccg\\|consumer\\|Resolved.*feign' > ccg-debug.log"
echo ""
echo "Check ccg-debug.log for details"
