@echo off
REM Check what dependencies were actually detected

set PROJECT_PATH=%~1
if "%PROJECT_PATH%"=="" (
    echo Usage: check-dependencies.bat "C:\path\to\project"
    exit /b 1
)

echo Checking dependency-analysis folder...
echo.

if exist "dependency-analysis\impact-analysis.md" (
    echo === First 50 dependencies detected ===
    type dependency-analysis\impact-analysis.md | findstr /i "###" | more
    echo.
    echo === Dependency types found ===
    type dependency-analysis\impact-analysis.md | findstr /i "Type:" | findstr /v "Total" | more
    echo.
    echo === Service names in dependencies ===
    type dependency-analysis\impact-analysis.md | findstr /i "→" | more
) else (
    echo ERROR: dependency-analysis\impact-analysis.md not found!
    echo Please run the analyzer first.
)
