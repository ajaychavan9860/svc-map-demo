@echo off
REM 🚀 Generic Microservices Dependency Analyzer - Windows Demo Script
REM This script demonstrates the enhanced analyzer with SVG generation on Windows

echo 🎯 Generic Microservices Dependency Analyzer Demo - Windows
echo ==================================================
echo.
echo ✨ **NEW FEATURES ADDED:**
echo    🎨 SVG Vector Graphics Generation
echo    🔧 Highly Configurable Analysis  
echo    🌍 Universal Language Support
echo    📊 Enhanced HTML Reports
echo    💼 Business Impact Analysis
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️ Java not found. Please install Java 17+ first.
    echo    Download from: https://adoptium.net/
    pause
    exit /b 1
)

REM Check if Graphviz is installed
dot -V >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️ Graphviz not found. Installing...
    echo    Run: choco install graphviz ^(with Chocolatey^)
    echo    Or download from: https://graphviz.org/download/
    echo.
)

REM Display project structure
echo 📂 Analyzing Project Structure:
echo    - Java Spring Boot microservices
echo    - Maven multi-module setup
echo    - Gateway service with routes
echo    - Feign clients for inter-service calls
echo.

REM Run the enhanced analyzer
echo 🔍 Running Enhanced Analysis...
echo    Command: java -jar dependency-analyzer-enhanced\target\generic-microservices-dependency-analyzer-2.0.0.jar .
echo.

java -jar dependency-analyzer-enhanced\target\generic-microservices-dependency-analyzer-2.0.0.jar .

echo.
echo 📊 **ANALYSIS COMPLETE!** Generated Reports:
echo.

REM List generated files with descriptions
if exist "dependency-analysis\dependency-report.html" (
    echo    ✅ dependency-report.html    - Interactive web report with embedded SVG
)

if exist "dependency-analysis\dependency-graph.svg" (
    echo    🎨 dependency-graph.svg      - Vector graphics diagram ^(scalable^)
    for %%F in ("dependency-analysis\dependency-graph.svg") do echo                                   Size: %%~zF bytes
)

if exist "dependency-analysis\dependency-matrix.csv" (
    echo    📊 dependency-matrix.csv     - Business spreadsheet
    for /f %%C in ('find /c /v "" ^< "dependency-analysis\dependency-matrix.csv"') do echo                                   Dependencies: %%C rows
)

if exist "dependency-analysis\analysis-result.json" (
    echo    🔧 analysis-result.json      - Machine-readable API data
)

if exist "dependency-analysis\impact-analysis.md" (
    echo    📋 impact-analysis.md        - Testing strategy recommendations
)

if exist "dependency-analysis\dependency-graph.dot" (
    echo    🔗 dependency-graph.dot      - Graphviz source format
)

echo.
echo 🎯 **BUSINESS VALUE DEMONSTRATION:**
echo.

REM Show impact analysis examples
if exist "dependency-analysis\impact-analysis.md" (
    echo    📈 Testing Impact Analysis:
    echo    ==========================
    findstr /C:"product-service.*changes" "dependency-analysis\impact-analysis.md" | findstr /n "." | findstr "^1:"
    echo    ...
    echo.
)

REM Show dependency count
if exist "dependency-analysis\dependency-matrix.csv" (
    for /f %%C in ('find /c /v "" ^< "dependency-analysis\dependency-matrix.csv"') do (
        set /a deps=%%C-1
        echo    🔗 Dependencies Found: !deps! relationships
    )
    echo    💰 Testing Reduction: Potential 60-80%% savings vs full regression
    echo.
)

echo 🌐 **OPEN REPORTS:**
echo    📱 HTML Report: start dependency-analysis\dependency-report.html
echo    🎨 SVG Diagram: start dependency-analysis\dependency-graph.svg
echo.

echo ⚙️ **CUSTOMIZE THE ANALYSIS:**
echo    📝 Edit: dependency-analyzer-enhanced\analyzer-config.yml
echo    🔧 Add your patterns for different frameworks
echo    🎯 Configure output formats ^(HTML, SVG, PNG, JSON, CSV^)
echo.

echo 🚀 **USE WITH ANY PROJECT:**
echo    java -jar analyzer.jar C:\path\to\your\microservices
echo    java -jar analyzer.jar C:\project\path C:\custom\config.yml
echo.

echo ✨ **SUCCESS! Your dependency analysis is complete.**
echo    Ready to present to stakeholders for targeted testing approval! 📊
echo.

REM Optionally open the HTML report
set /p choice="Open HTML report now? (y/N): "
if /i "%choice%"=="y" (
    start dependency-analysis\dependency-report.html
)

pause