#!/bin/bash
# Diagnostic script for microservices dependency analyzer
# Run this on your other project to see what's being detected

echo "========================================"
echo "🔍 Microservices Dependency Analyzer Diagnostics"
echo "========================================"
echo

if [ $# -eq 0 ]; then
    echo "❌ ERROR: Please provide the path to your project"
    echo "Usage: $0 /path/to/your/project"
    exit 1
fi

PROJECT_PATH="$1"

echo "📂 Project Path: $PROJECT_PATH"
echo

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ ERROR: Java not found. Please install Java 17+"
    exit 1
fi

echo "✅ Java found"
echo

# Check if the analyzer JAR exists
if [ ! -f "dependency-analyzer-enhanced/target/generic-microservices-dependency-analyzer-2.0.0.jar" ]; then
    echo "❌ ERROR: Analyzer JAR not found. Please build the project first:"
    echo "cd dependency-analyzer-enhanced"
    echo "mvn clean package -DskipTests"
    exit 1
fi

echo "✅ Analyzer JAR found"
echo

# First, let's check what services the analyzer would find
echo "🔍 Checking for potential services..."
echo "Maven projects (pom.xml):"
find "$PROJECT_PATH" -name "pom.xml" -type f | head -10

echo
echo "Java source files:"
find "$PROJECT_PATH" -name "*.java" -type f | wc -l | xargs echo "Total Java files:"

echo
echo "Application config files:"
find "$PROJECT_PATH" -name "application.yml" -o -name "application.yaml" -o -name "application.properties" | head -5

echo
echo "🔍 Running diagnostic analysis..."
echo "Command: java -jar dependency-analyzer-enhanced/target/generic-microservices-dependency-analyzer-2.0.0.jar \"$PROJECT_PATH\""
echo

java -jar dependency-analyzer-enhanced/target/generic-microservices-dependency-analyzer-2.0.0.jar "$PROJECT_PATH"

echo
echo "📊 **DIAGNOSTIC COMPLETE**"
echo

# Check what was generated
if [ -d "dependency-analysis" ]; then
    echo "📁 Generated reports in: dependency-analysis/"
    ls -la dependency-analysis/

    echo
    echo "💡 **TIPS FOR BETTER DETECTION:**"
    echo "1. Check if your services have pom.xml files"
    echo "2. Look for @FeignClient annotations in Java files"
    echo "3. Check for RestTemplate/WebClient usage"
    echo "4. Verify service names in application.yml/properties"
    echo "5. Try using custom config: java -jar analyzer.jar project custom-config.yml"
else
    echo "❌ No dependency-analysis directory created"
    echo "This suggests no services were found or analysis failed"
fi

echo