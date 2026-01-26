@echo off
REM Analyze 360/backend project with ASCII-only output

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

echo ========================================
echo Dependency Analysis for 360/backend
echo ========================================
echo.
echo Project: %PROJECT_PATH%
echo.

REM Build the analyzer
echo [1/3] Building analyzer...
cd /d "%~dp0dependency-analyzer-enhanced"
call mvn clean package -DskipTests -q

if errorlevel 1 (
    echo ERROR: Build failed
    exit /b 1
)

echo OK - Build successful
echo.

REM Run analysis
echo [2/3] Running analysis...
java -jar target\generic-microservices-dependency-analyzer-2.0.0.jar "%PROJECT_PATH%" 2>&1 | findstr /I /C:"Found Feign" /C:"Found Maven" /C:"ccg" /C:"consumer" /C:"Resolved" /C:"match"

if errorlevel 1 (
    echo ERROR: Analysis failed
    exit /b 1
)

echo.
echo [3/3] Analyzing results...
echo.

set JSON_FILE=%PROJECT_PATH%\dependency-analysis\analysis-result.json

if not exist "%JSON_FILE%" (
    echo ERROR: No analysis results found at %JSON_FILE%
    exit /b 1
)

echo Dependencies found - check: %JSON_FILE%
echo.

echo Service pairs with BOTH directions (bidirectional):
echo   NOTE: For detailed JSON analysis, use Git Bash or WSL with the .sh version
echo   Or install jq for Windows from https://stedolan.github.io/jq/download/
echo.

echo Specific CCG services communication:
findstr /C:"ccg" "%JSON_FILE%"

echo.
echo ========================================
echo Analysis complete!
echo ========================================
echo SVG diagram: %PROJECT_PATH%\dependency-analysis\dependency-diagram-graphviz-java.svg
echo JSON report: %JSON_FILE%
