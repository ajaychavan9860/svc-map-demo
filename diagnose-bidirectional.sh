#!/bin/bash

# Diagnostic script to troubleshoot bidirectional Maven dependencies
# Usage: ./diagnose-bidirectional.sh /path/to/your/project

if [ -z "$1" ]; then
    echo "❌ Usage: $0 /path/to/your/project"
    exit 1
fi

PROJECT_PATH="$1"

echo "🔍 Diagnosing Maven dependencies in: $PROJECT_PATH"
echo ""

# Check if project exists
if [ ! -d "$PROJECT_PATH" ]; then
    echo "❌ Project directory not found: $PROJECT_PATH"
    exit 1
fi

echo "📋 Step 1: Finding all services with pom.xml..."
echo "================================================"
find "$PROJECT_PATH" -name "pom.xml" -not -path "*/target/*" | while read pomfile; do
    SERVICE_DIR=$(dirname "$pomfile")
    SERVICE_NAME=$(basename "$SERVICE_DIR")
    echo "  ✓ $SERVICE_NAME"
done
echo ""

echo "📦 Step 2: Checking for internal dependencies in pom.xml files..."
echo "==================================================================="
find "$PROJECT_PATH" -name "pom.xml" -not -path "*/target/*" | while read pomfile; do
    SERVICE_DIR=$(dirname "$pomfile")
    SERVICE_NAME=$(basename "$SERVICE_DIR")
    
    # Extract groupId from pom.xml
    GROUP_ID=$(xmllint --xpath "string(//*[local-name()='project']/*[local-name()='groupId'])" "$pomfile" 2>/dev/null || \
               xmllint --xpath "string(//*[local-name()='project']/*[local-name()='parent']/*[local-name()='groupId'])" "$pomfile" 2>/dev/null)
    
    if [ -n "$GROUP_ID" ]; then
        echo ""
        echo "🔹 Service: $SERVICE_NAME (groupId: $GROUP_ID)"
        
        # Find dependencies with the same groupId
        xmllint --xpath "//*[local-name()='dependencies']/*[local-name()='dependency']" "$pomfile" 2>/dev/null | \
        grep -A 3 "<groupId>$GROUP_ID</groupId>" | \
        grep -o "<artifactId>[^<]*</artifactId>" | \
        sed 's/<artifactId>//g; s/<\/artifactId>//g' | while read artifact; do
            echo "    → depends on: $artifact (Maven dependency)"
        done
    fi
done
echo ""

echo "📊 Step 3: Building dependency matrix..."
echo "=========================================="
echo "Source Service → Target Service | Detection Method"
echo "---------------------------------------------------"

# Create temp file for analysis
TEMP_FILE=$(mktemp)

find "$PROJECT_PATH" -name "pom.xml" -not -path "*/target/*" | while read pomfile; do
    SERVICE_DIR=$(dirname "$pomfile")
    SERVICE_NAME=$(basename "$SERVICE_DIR")
    
    GROUP_ID=$(xmllint --xpath "string(//*[local-name()='project']/*[local-name()='groupId'])" "$pomfile" 2>/dev/null || \
               xmllint --xpath "string(//*[local-name()='project']/*[local-name()='parent']/*[local-name()='groupId'])" "$pomfile" 2>/dev/null)
    
    if [ -n "$GROUP_ID" ]; then
        xmllint --xpath "//*[local-name()='dependencies']/*[local-name()='dependency']" "$pomfile" 2>/dev/null | \
        grep -A 3 "<groupId>$GROUP_ID</groupId>" | \
        grep -o "<artifactId>[^<]*</artifactId>" | \
        sed 's/<artifactId>//g; s/<\/artifactId>//g' | while read artifact; do
            echo "$SERVICE_NAME → $artifact | Maven pom.xml"
        done
    fi
done

rm -f "$TEMP_FILE"
echo ""

echo "🎯 Step 4: Checking for Feign clients..."
echo "=========================================="
find "$PROJECT_PATH" -name "*.java" -not -path "*/target/*" | while read javafile; do
    if grep -q "@FeignClient" "$javafile"; then
        SERVICE_DIR=$(echo "$javafile" | grep -oE "$PROJECT_PATH/[^/]+/" | head -1)
        SERVICE_NAME=$(basename "$SERVICE_DIR")
        TARGET=$(grep "@FeignClient" "$javafile" | grep -oE 'name *= *"[^"]+"|value *= *"[^"]+"' | head -1 | sed 's/.*"\([^"]*\)".*/\1/')
        if [ -n "$TARGET" ]; then
            echo "$SERVICE_NAME → $TARGET | Feign Client"
        fi
    fi
done
echo ""

echo "✅ Diagnostic complete!"
echo ""
echo "💡 Tips for troubleshooting:"
echo "  1. Make sure services use the same groupId in their pom.xml"
echo "  2. Check if artifactId matches the service folder name"
echo "  3. Verify dependencies are not in <dependencyManagement> only"
echo "  4. Run the analyzer with: java -jar dependency-analyzer.jar /path/to/project"
echo "  5. Check the logs for 'Found Maven dependency' messages"
