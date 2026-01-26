#!/bin/bash

# Diagnostic script for ccg-kafka-consumer-service → ccg-core-service dependency
# This will show detailed logs about Feign client detection

echo "🔍 Diagnostic Script for Feign Client Detection"
echo "================================================="
echo ""

if [ -z "$1" ]; then
    echo "Usage: $0 /path/to/360/backend"
    echo ""
    echo "This script will:"
    echo "  1. Run analyzer with detailed logging"
    echo "  2. Show all Feign client detections"
    echo "  3. Show property resolution details"
    echo "  4. Show fuzzy matching attempts"
    exit 1
fi

PROJECT_PATH="$1"

echo "📂 Project: $PROJECT_PATH"
echo ""
echo "Running analyzer with detailed Feign client logging..."
echo "========================================================"
echo ""

java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar "$PROJECT_PATH" 2>&1 | tee feign-analysis.log

echo ""
echo "📊 Analysis Complete! Check feign-analysis.log for details"
echo ""

echo "📋 Summary of Feign Client Detections:"
echo "======================================="
grep "🔍 Analyzing Feign client" feign-analysis.log | head -20

echo ""
echo "🔑 Property Resolutions:"
echo "========================"
grep "Resolved.*=" feign-analysis.log

echo ""
echo "⚠️  Missing Properties:"
echo "======================="
grep "Property.*not found" feign-analysis.log

echo ""
echo "✓ Fuzzy Matching Results:"
echo "=========================="
grep "✓.*match:" feign-analysis.log

echo ""
echo "❌ Failed Matches:"
echo "=================="
grep "❌ Could not find matching service" feign-analysis.log

echo ""
echo "✅ Successfully Detected Dependencies:"
echo "======================================"
grep "✅ Found Feign dependency:" feign-analysis.log

echo ""
echo "📝 Specifically for ccg services:"
echo "=================================="
grep -E "ccg-core|ccg-kafka-consumer|ccg-service" feign-analysis.log | head -30

echo ""
echo "💡 Tips:"
echo "  - Check if config files are being loaded (look for '✓ Loaded properties from')"
echo "  - Check if properties are resolved (look for '✅ Resolved')"
echo "  - Check fuzzy matching (look for '✓ Partial match' or '✓ Base match')"
echo "  - Full details in: feign-analysis.log"
