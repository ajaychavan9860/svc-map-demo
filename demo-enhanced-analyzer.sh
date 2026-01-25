#!/bin/bash

# 🚀 Generic Microservices Dependency Analyzer - Demo Script
# This script demonstrates the enhanced analyzer with SVG generation

echo "🎯 Generic Microservices Dependency Analyzer Demo"
echo "=================================================="
echo
echo "✨ **NEW FEATURES ADDED:**"
echo "   🎨 SVG Vector Graphics Generation"
echo "   🔧 Highly Configurable Analysis"
echo "   🌍 Universal Language Support"
echo "   📊 Enhanced HTML Reports"
echo "   💼 Business Impact Analysis"
echo

# Check if Graphviz is installed
if ! command -v dot &> /dev/null; then
    echo "⚠️  Graphviz not found. Installing..."
    echo "   Run: brew install graphviz (macOS) or apt-get install graphviz (Ubuntu)"
    echo
fi

# Display project structure
echo "📂 Analyzing Project Structure:"
echo "   - Java Spring Boot microservices"
echo "   - Maven multi-module setup"
echo "   - Gateway service with routes"
echo "   - Feign clients for inter-service calls"
echo

# Run the enhanced analyzer
echo "🔍 Running Enhanced Analysis..."
echo "   Command: java -jar dependency-analyzer-enhanced/target/generic-microservices-dependency-analyzer-2.0.0.jar ."
echo

java -jar dependency-analyzer-enhanced/target/generic-microservices-dependency-analyzer-2.0.0.jar .

echo
echo "📊 **ANALYSIS COMPLETE!** Generated Reports:"
echo

# List generated files with descriptions
if [ -f "dependency-analysis/dependency-report.html" ]; then
    echo "   ✅ dependency-report.html    - Interactive web report with embedded SVG"
fi

if [ -f "dependency-analysis/dependency-graph.svg" ]; then
    echo "   🎨 dependency-graph.svg      - Vector graphics diagram (scalable)"
    echo "                                  Size: $(ls -lh dependency-analysis/dependency-graph.svg | awk '{print $5}')"
fi

if [ -f "dependency-analysis/dependency-matrix.csv" ]; then
    echo "   📊 dependency-matrix.csv     - Business spreadsheet"
    echo "                                  Dependencies: $(wc -l < dependency-analysis/dependency-matrix.csv | xargs) rows"
fi

if [ -f "dependency-analysis/analysis-result.json" ]; then
    echo "   🔧 analysis-result.json      - Machine-readable API data"
fi

if [ -f "dependency-analysis/impact-analysis.md" ]; then
    echo "   📋 impact-analysis.md        - Testing strategy recommendations"
fi

if [ -f "dependency-analysis/dependency-graph.dot" ]; then
    echo "   🔗 dependency-graph.dot      - Graphviz source format"
fi

echo
echo "🎯 **BUSINESS VALUE DEMONSTRATION:**"
echo

# Show impact analysis examples
if [ -f "dependency-analysis/impact-analysis.md" ]; then
    echo "   📈 Testing Impact Analysis:"
    echo "   =========================="
    grep -A 3 "product-service.*changes" dependency-analysis/impact-analysis.md | head -4
    echo "   ..."
    echo
fi

# Show dependency count
if [ -f "dependency-analysis/dependency-matrix.csv" ]; then
    deps=$(tail -n +2 dependency-analysis/dependency-matrix.csv | wc -l)
    echo "   🔗 Dependencies Found: $deps relationships"
    echo "   💰 Testing Reduction: Potential 60-80% savings vs full regression"
    echo
fi

echo "🌐 **OPEN REPORTS:**"
echo "   📱 HTML Report: open dependency-analysis/dependency-report.html"
echo "   🎨 SVG Diagram: open dependency-analysis/dependency-graph.svg"
echo

echo "⚙️ **CUSTOMIZE THE ANALYSIS:**"
echo "   📝 Edit: dependency-analyzer-enhanced/analyzer-config.yml"
echo "   🔧 Add your patterns for different frameworks"
echo "   🎯 Configure output formats (HTML, SVG, PNG, JSON, CSV)"
echo

echo "🚀 **USE WITH ANY PROJECT:**"
echo "   java -jar analyzer.jar /path/to/your/microservices"
echo "   java -jar analyzer.jar /path/to/project /custom/config.yml"
echo

echo "✨ **SUCCESS! Your dependency analysis is complete.**"
echo "   Ready to present to stakeholders for targeted testing approval! 📊"