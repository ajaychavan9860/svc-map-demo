#!/bin/bash
# Debug why ccg-kafka-consumer-service -> ccg-core-service is not detected

if [ -z "$1" ]; then
    echo "Usage: $0 <path-to-360-backend>"
    exit 1
fi

PROJECT_PATH="$1"

echo "========================================"
echo "Debug: ccg-kafka-consumer -> ccg-core"
echo "========================================"
echo ""

cd "$(dirname "$0")/dependency-analyzer-enhanced"

echo "Running analyzer with detailed logging for CCG services..."
echo ""

java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar "$PROJECT_PATH" 2>&1 | \
    grep -E "ccg-kafka-consumer-service|ccg-core-service|ccg-service|Analyzing Feign.*ccg|Extracted raw|Resolving.*ccg|Matched.*ccg|Using URL|Using name|endpoint.*ccg|park-message|rawMessage" | \
    grep -v "gateway-service" | \
    sed 's/^.*GenericDependencyScanner - /  /' | \
    sed 's/[^[:print:]\t\n]//g'

echo ""
echo "========================================"
echo "Results:"
echo "========================================"

JSON_FILE="$PROJECT_PATH/dependency-analysis/analysis-result.json"

if [ -f "$JSON_FILE" ]; then
    echo ""
    echo "1. Was ccg-kafka-consumer-service discovered?"
    cat "$JSON_FILE" | jq -r '.services[] | select(.name == "ccg-kafka-consumer-service") | "  YES: \(.name) at \(.path)"'
    
    echo ""
    echo "2. Was ccg-core-service discovered?"
    cat "$JSON_FILE" | jq -r '.services[] | select(.name == "ccg-core-service") | "  YES: \(.name) at \(.path)"'
    
    echo ""
    echo "3. Dependencies FROM ccg-kafka-consumer-service:"
    cat "$JSON_FILE" | jq -r '.dependencies[] | select(.from_service == "ccg-kafka-consumer-service") | "  -> \(.target_service) [\(.dependency_type)] in \(.source_file)"'
    
    echo ""
    echo "4. Specific check: ccg-kafka-consumer-service -> ccg-core-service"
    FOUND=$(cat "$JSON_FILE" | jq -r '.dependencies[] | select(.from_service == "ccg-kafka-consumer-service" and .target_service == "ccg-core-service") | .dependency_type')
    
    if [ -z "$FOUND" ]; then
        echo "  NOT FOUND!"
        echo ""
        echo "Possible reasons:"
        echo "  1. Property \${feign.ccg.name} not resolved"
        echo "  2. Fuzzy matching failed (ccg-service -> ccg-core-service)"
        echo "  3. URL endpoint matching failed"
        echo "  4. Feign client not detected at all"
    else
        echo "  FOUND: [$FOUND]"
    fi
else
    echo "ERROR: No JSON file at $JSON_FILE"
fi
