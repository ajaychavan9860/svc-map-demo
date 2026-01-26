#!/bin/bash
# Test the exact CCG scenario locally

echo "Creating test project matching CCG structure..."
TEST_DIR="/tmp/ccg-test-exact"
rm -rf "$TEST_DIR"
mkdir -p "$TEST_DIR"

# Create ccg-kafka-consumer-service
mkdir -p "$TEST_DIR/ccg-kafka-consumer-service/src/main/"{java/com/ccg,resources}

cat > "$TEST_DIR/ccg-kafka-consumer-service/src/main/resources/application.yml" << 'EOF'
feign:
  ccg:
    name: ccg-service
    url: http://localhost:9095/ccgcore
EOF

cat > "$TEST_DIR/ccg-kafka-consumer-service/src/main/java/com/ccg/CcgCoreServiceProxy.java" << 'EOF'
package com.ccg;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "${feign.ccg.name}", url = "${feign.ccg.url}")
public interface CcgCoreServiceProxy {
    @PostMapping("/v1/rawMessage")
    String postRawMessage();
}
EOF

cat > "$TEST_DIR/ccg-kafka-consumer-service/pom.xml" << 'EOF'
<project>
    <groupId>com.test</groupId>
    <artifactId>ccg-kafka-consumer-service</artifactId>
    <version>1.0</version>
</project>
EOF

# Create ccg-core-service
mkdir -p "$TEST_DIR/ccg-core-service/src/main/"{java/com/ccg,resources}

cat > "$TEST_DIR/ccg-core-service/src/main/java/com/ccg/CcgController.java" << 'EOF'
package com.ccg;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class CcgController {
    @PostMapping("/v1/rawMessage")
    public String rawMessage() {
        return "ok";
    }
    
    @PostMapping("/v1/handleErrorMessage")
    public String handleError() {
        return "ok";
    }
}
EOF

cat > "$TEST_DIR/ccg-core-service/pom.xml" << 'EOF'
<project>
    <groupId>com.test</groupId>
    <artifactId>ccg-core-service</artifactId>
    <version>1.0</version>
</project>
EOF

echo "✓ Test project created at $TEST_DIR"
echo ""
echo "Running analyzer..."
java -jar "$(dirname "$0")/dependency-analyzer-enhanced/target/generic-microservices-dependency-analyzer-2.0.0.jar" "$TEST_DIR" 2>&1 | \
    grep -E "ccg|Analyzing Feign|Resolved|Fuzzy matching|Direct match|Partial match|Found Feign|Available services" | \
    sed 's/^.*GenericDependencyScanner - //'

echo ""
echo "========================================"
echo "Result:"
echo "========================================"

JSON="$TEST_DIR/dependency-analysis/analysis-result.json"
if [ -f "$JSON" ]; then
    echo "Dependencies found:"
    cat "$JSON" | jq -r '.dependencies[] | "\(.from_service) -> \(.target_service) [\(.dependency_type)]"'
    
    echo ""
    echo "Specific check: ccg-kafka-consumer-service -> ccg-core-service"
    FOUND=$(cat "$JSON" | jq -r '.dependencies[] | select(.from_service == "ccg-kafka-consumer-service" and .target_service == "ccg-core-service") | "FOUND"')
    if [ "$FOUND" = "FOUND" ]; then
        echo "  ✓ SUCCESS - Dependency detected!"
    else
        echo "  ✗ FAILED - Dependency not detected"
        echo ""
        echo "What ccg-kafka-consumer-service calls:"
        cat "$JSON" | jq -r '.dependencies[] | select(.from_service == "ccg-kafka-consumer-service") | "  -> \(.target_service)"'
    fi
else
    echo "ERROR: No JSON generated"
fi
