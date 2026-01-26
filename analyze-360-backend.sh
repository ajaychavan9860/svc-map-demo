#!/bin/bash
# Analyze your 360/backend project dependencies with ASCII-only output

echo "========================================"
echo "Dependency Analysis for 360/backend"
echo "========================================"
echo ""

# Check if path is provided
if [ -z "$1" ]; then
    echo "Usage: $0 <path-to-360-backend-project>"
    echo "Example: $0 /path/to/360/backend"
    exit 1
fi

PROJECT_PATH="$1"

if [ ! -d "$PROJECT_PATH" ]; then
    echo "ERROR: Project path does not exist: $PROJECT_PATH"
    exit 1
fi

echo "Project: $PROJECT_PATH"
echo ""

# Build the analyzer
echo "[1/3] Building analyzer..."
cd /Users/ajay/svc-map-demo/dependency-analyzer-enhanced
mvn clean package -DskipTests -q

if [ $? -ne 0 ]; then
    echo "ERROR: Build failed"
    exit 1
fi

echo "OK - Build successful"
echo ""

# Run analysis with LC_ALL=C to force ASCII output
echo "[2/3] Running analysis (ASCII mode)..."
export LC_ALL=C
export LANG=C

java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar "$PROJECT_PATH" 2>&1 | \
    grep -E "Found Feign|Found Maven|ccg|consumer|Resolved|match" | \
    sed 's/[^[:print:]]/?/g'  # Replace non-printable chars with ?

if [ $? -ne 0 ]; then
    echo "ERROR: Analysis failed"
    exit 1
fi

echo ""
echo "[3/3] Analyzing results..."
echo ""

# Check if JSON output exists
JSON_FILE="$PROJECT_PATH/dependency-analysis/analysis-result.json"

if [ ! -f "$JSON_FILE" ]; then
    echo "ERROR: No analysis results found at $JSON_FILE"
    exit 1
fi

echo "Dependencies found:"
cat "$JSON_FILE" | jq -r '.dependencies | length'
echo ""

echo "Service pairs with BOTH directions (bidirectional):"
cat "$JSON_FILE" | jq -r '.dependencies[] | "\(.fromService) -> \(.toService)"' | \
    while IFS= read -r line; do
        from=$(echo "$line" | awk '{print $1}')
        to=$(echo "$line" | awk '{print $3}')
        
        # Check if reverse exists
        reverse=$(cat "$JSON_FILE" | jq -r ".dependencies[] | select(.fromService == \"$to\" and .toService == \"$from\") | \"\(.fromService) -> \(.toService)\"" 2>/dev/null)
        
        if [ ! -z "$reverse" ]; then
            echo "  $from <-> $to"
        fi
    done | sort -u

echo ""
echo "Specific CCG services communication:"
cat "$JSON_FILE" | jq -r '.dependencies[] | select(.fromService | contains("ccg") or .toService | contains("ccg")) | "  \(.fromService) -> \(.toService) [\(.dependencyType)]"' | sort

echo ""
echo "========================================"
echo "Analysis complete!"
echo "========================================"
echo "SVG diagram: $PROJECT_PATH/dependency-analysis/dependency-graph.svg"
echo "JSON report: $JSON_FILE"
