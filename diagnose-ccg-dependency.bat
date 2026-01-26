@echo off
REM Diagnose CCG service dependency detection on Windows

if "%1"=="" (
    echo Usage: %~n0 ^<path-to-360-backend-project^>
    echo Example: %~n0 C:\path\to\360\backend
    exit /b 1
)

set PROJECT_PATH=%1

if not exist "%PROJECT_PATH%" (
    echo ERROR: Project path does not exist: %PROJECT_PATH%
    exit /b 1
)

echo =========================================
echo CCG Services Dependency Diagnostic
echo =========================================
echo Project: %PROJECT_PATH%
echo.

echo [1] Checking if CCG services exist...
if exist "%PROJECT_PATH%\ccg-kafka-consumer-service" (
    echo   [OK] ccg-kafka-consumer-service found
) else (
    echo   [MISSING] ccg-kafka-consumer-service not found
)

if exist "%PROJECT_PATH%\ccg-core-service" (
    echo   [OK] ccg-core-service found
) else (
    echo   [MISSING] ccg-core-service not found
)
echo.

echo [2] Searching for Feign clients in ccg-kafka-consumer-service...
if exist "%PROJECT_PATH%\ccg-kafka-consumer-service" (
    echo   Feign client annotations:
    findstr /S /I /C:"@FeignClient" "%PROJECT_PATH%\ccg-kafka-consumer-service\*.java" 2>nul
    echo.
)

echo [3] Searching for YAML configurations...
echo   ccg-kafka-consumer-service YAML properties:
findstr /S /I /C:"feign" /C:"consumer" /C:"ccg" "%PROJECT_PATH%\ccg-kafka-consumer-service\*.yml" 2>nul
echo.

echo   ccg-core-service YAML properties:
findstr /S /I /C:"feign" /C:"consumer" /C:"ccg" "%PROJECT_PATH%\ccg-core-service\*.yml" 2>nul
echo.

echo [4] Searching for REST endpoints in ccg-core-service...
if exist "%PROJECT_PATH%\ccg-core-service" (
    echo   Controller mappings:
    findstr /S /I /C:"@RestController" /C:"@RequestMapping" /C:"@GetMapping" /C:"@PostMapping" "%PROJECT_PATH%\ccg-core-service\*.java" 2>nul
)
echo.

echo [5] Running analyzer with detailed logging...
echo =========================================
cd /d "%~dp0dependency-analyzer-enhanced"

REM Rebuild to ensure latest code
call mvn clean package -DskipTests -q

REM Run with detailed logging
java -jar target\generic-microservices-dependency-analyzer-2.0.0.jar "%PROJECT_PATH%" 2>&1 | findstr /I /C:"ccg" /C:"kafka-consumer" /C:"Feign dependency" /C:"Resolved" /C:"match"

echo.
echo =========================================
echo [6] Analyzing generated JSON...
echo =========================================

set JSON_FILE=%PROJECT_PATH%\dependency-analysis\analysis-result.json
if exist "%JSON_FILE%" (
    echo All CCG-related dependencies found in JSON
    echo Check: %JSON_FILE%
) else (
    echo [ERROR] Analysis result not found at %JSON_FILE%
)

echo.
echo =========================================
echo Diagnostic complete!
echo =========================================
