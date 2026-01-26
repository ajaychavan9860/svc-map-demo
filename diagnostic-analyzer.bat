@echo off
REM Diagnostic script for microservices dependency analyzer
REM Run this on your other project to see what's being detected

echo ========================================
echo 🔍 Microservices Dependency Analyzer Diagnostics
echo ========================================
echo.

if "%~1"=="" (
    echo ❌ ERROR: Please provide the path to your project
    echo Usage: %0 C:\path\to\your\project
    pause
    exit /b 1
)

set PROJECT_PATH=%~1

echo 📂 Project Path: %PROJECT_PATH%
echo.

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERROR: Java not found. Please install Java 17+
    pause
    exit /b 1
)

echo ✅ Java found
echo.

REM Check if the analyzer JAR exists
if not exist "dependency-analyzer-enhanced\target\generic-microservices-dependency-analyzer-2.0.0.jar" (
    echo ❌ ERROR: Analyzer JAR not found. Please build the project first:
    echo cd dependency-analyzer-enhanced
    echo mvn clean package -DskipTests
    pause
    exit /b 1
)

echo ✅ Analyzer JAR found
echo.

echo 🔍 Running diagnostic analysis...
echo Command: java -jar dependency-analyzer-enhanced\target\generic-microservices-dependency-analyzer-2.0.0.jar "%PROJECT_PATH%"
echo.

java -jar dependency-analyzer-enhanced\target\generic-microservices-dependency-analyzer-2.0.0.jar "%PROJECT_PATH%"

echo.
echo 📊 **DIAGNOSTIC COMPLETE**
echo.

REM Check what was generated
if exist "dependency-analysis" (
    echo 📁 Generated reports in: dependency-analysis\
    dir /b dependency-analysis\

    echo.
    echo 💡 **TIPS FOR BETTER DETECTION:**
    echo 1. Check if your services have pom.xml files
    echo 2. Look for @FeignClient annotations in Java files
    echo 3. Check for RestTemplate/WebClient usage
    echo 4. Verify service names in application.yml/properties
    echo 5. Try using custom config: java -jar analyzer.jar project custom-config.yml
) else (
    echo ❌ No dependency-analysis directory created
    echo This suggests no services were found or analysis failed
)

echo.
pause