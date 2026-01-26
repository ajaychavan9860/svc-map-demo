@echo off
REM Diagnostic script for Feign client detection debugging

echo ========================================
echo Feign Client Detection Diagnostics
echo ========================================
echo.

if "%~1"=="" (
    echo ERROR: Please provide the project path
    echo Usage: debug-feign-detection.bat "C:\path\to\project"
    exit /b 1
)

set PROJECT_PATH=%~1
echo Project Path: %PROJECT_PATH%
echo.

echo Step 1: Checking for services (pom.xml files)
echo ================================================
dir /s /b "%PROJECT_PATH%\pom.xml" 2>nul | find /c "pom.xml"
echo.

echo Step 2: Checking for @FeignClient annotations
echo ================================================
findstr /s /i /m "@FeignClient" "%PROJECT_PATH%\*.java" 2>nul
echo.

echo Step 3: Sample FeignClient patterns found
echo ================================================
findstr /s /i /n "@FeignClient" "%PROJECT_PATH%\*.java" 2>nul | findstr /i "name value" | more
echo.

echo Step 4: Checking application.yml/properties
echo ================================================
dir /s /b "%PROJECT_PATH%\application.yml" "%PROJECT_PATH%\application.yaml" "%PROJECT_PATH%\application.properties" 2>nul
echo.

echo Step 5: Running analyzer with verbose output
echo ================================================
java -jar dependency-analyzer-enhanced\target\generic-microservices-dependency-analyzer-2.0.0.jar "%PROJECT_PATH%" 2>&1 | findstr /i "found feign service dependency"
echo.

echo ========================================
echo Diagnostic Complete
echo ========================================
echo.
echo Please share this output to help debug the issue.
