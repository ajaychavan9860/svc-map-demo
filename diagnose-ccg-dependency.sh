#!/bin/bash
# Detailed diagnostic for CCG service dependency detection

if [ -z "$1" ]; then
    echo "Usage: $0 <path-to-360-backend-project>"
    echo "Example: $0 /path/to/360/backend"
    exit 1
fi

PROJECT_PATH="$1"
echo "========================================="
echo "CCG Services Dependency Diagnostic"
echo "========================================="
echo "Project: $PROJECT_PATH"
echo ""

# Check if services exist
echo "[1] Checking if CCG services exist..."
if [ -d "$PROJECT_PATH/ccg-kafka-consumer-service" ]; then
    echo "  [OK] ccg-kafka-consumer-service found"
else
    echo "  [MISSING] ccg-kafka-consumer-service not found"
fi

if [ -d "$PROJECT_PATH/ccg-core-service" ]; then
    echo "  [OK] ccg-core-service found"
else
    echo "  [MISSING] ccg-core-service not found"
fi
echo ""

# Search for Feign clients in kafka-consumer pointing to core
echo "[2] Searching for Feign clients in ccg-kafka-consumer-service..."
if [ -d "$PROJECT_PATH/ccg-kafka-consumer-service" ]; then
    echo "  Feign client annotations:"
    find "$PROJECT_PATH/ccg-kafka-consumer-service" -name "*.java" -type f -exec grep -l "@FeignClient" {} \; | while read file; do
        echo "    File: $file"
        grep -A 2 "@FeignClient" "$file" | head -10
        echo ""
    done
    
    echo "  Property references (feign.*):"
    find "$PROJECT_PATH/ccg-kafka-consumer-service" -name "*.java" -type f -exec grep -h '\${feign\.' {} \; | sort -u
    echo ""
fi

# Search for YAML configurations
echo "[3] Searching for YAML configurations..."
echo "  ccg-kafka-consumer-service YAML properties:"
find "$PROJECT_PATH/ccg-kafka-consumer-service" -name "application*.yml" -o -name "application*.yaml" | while read yamlfile; do
    echo "    File: $yamlfile"
    grep -E "feign\.|consumer\.|ccg" "$yamlfile" | grep -v "^#"
    echo ""
done

echo "  ccg-core-service YAML properties:"
find "$PROJECT_PATH/ccg-core-service" -name "application*.yml" -o -name "application*.yaml" 2>/dev/null | while read yamlfile; do
    echo "    File: $yamlfile"
    grep -E "feign\.|consumer\.|ccg" "$yamlfile" | grep -v "^#"
    echo ""
done
echo ""

# Search for REST endpoints in ccg-core-service
echo "[4] Searching for REST endpoints in ccg-core-service..."
if [ -d "$PROJECT_PATH/ccg-core-service" ]; then
    echo "  Controller mappings:"
    find "$PROJECT_PATH/ccg-core-service" -name "*.java" -type f -exec grep -l "@RestController\|@Controller" {} \; | while read file; do
        echo "    File: $file"
        grep -E "@RequestMapping|@GetMapping|@PostMapping|@PutMapping|@DeleteMapping" "$file" | head -5
        echo ""
    done
fi
echo ""

# Now run the analyzer with detailed logging
echo "[5] Running analyzer with detailed logging..."
echo "========================================="
cd /Users/ajay/svc-map-demo/dependency-analyzer-enhanced

# Rebuild to ensure latest code
mvn clean package -DskipTests -q

# Run with detailed logging, filter for CCG-related entries
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar "$PROJECT_PATH" 2>&1 | \
    grep -i "ccg\|kafka-consumer\|Feign dependency\|Resolved.*feign\|property.*consumer\|match.*ccg" | \
    sed 's/[^[:print:]\t\n]//g'  # Remove non-printable characters

echo ""
echo "========================================="
echo "[6] Analyzing generated JSON..."
echo "========================================="

JSON_FILE="$PROJECT_PATH/dependency-analysis/analysis-result.json"
if [ -f "$JSON_FILE" ]; then
    echo "All CCG-related dependencies:"
    cat "$JSON_FILE" | jq -r '.dependencies[] | select(.fromService | contains("ccg") or .toService | contains("ccg")) | "  \(.fromService) --> \(.toService) [\(.dependencyType)]"' | sort -u
    
    echo ""
    echo "Specific check: ccg-kafka-consumer-service --> ccg-core-service"
    cat "$JSON_FILE" | jq -r '.dependencies[] | select(.fromService == "ccg-kafka-consumer-service" and .toService == "ccg-core-service") | "  FOUND: \(.fromService) --> \(.toService) [\(.dependencyType)]"'
    
    if [ $? -eq 0 ]; then
        echo "  [OK] Dependency detected"
    else
        echo "  [MISSING] Dependency NOT detected"
    fi
    
    echo ""
    echo "Reverse check: ccg-core-service --> ccg-kafka-consumer-service"
    cat "$JSON_FILE" | jq -r '.dependencies[] | select(.fromService == "ccg-core-service" and .toService == "ccg-kafka-consumer-service") | "  FOUND: \(.fromService) --> \(.toService) [\(.dependencyType)]"'
else
    echo "[ERROR] Analysis result not found at $JSON_FILE"
fi

echo ""
echo "========================================="
echo "Diagnostic complete!"
echo "========================================="
