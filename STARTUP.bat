@echo off
REM CompactLogix Full System Startup Script
REM This script starts all components: OpENer, Spring Boot Backend, and Angular UI

setlocal enabledelayedexpansion

echo.
echo ========================================
echo  CompactLogix System Startup
echo ========================================
echo.

REM Define color codes for output
set "INFO=[INFO]"
set "SUCCESS=[✓]"
set "ERROR=[✗]"

REM Check if Docker is running
echo %INFO% Checking Docker...
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo %ERROR% Docker is not running. Please start Docker Desktop first.
    exit /b 1
)
echo %SUCCESS% Docker is running.

REM Start OpENer Simulator
echo.
echo %INFO% Starting OpENer PLC Simulator...
cd /d C:\code\dockerConfig\opener-plc
docker compose up -d --build

REM Wait for OpENer to be healthy
echo %INFO% Waiting for OpENer to be healthy (max 30 seconds)...
setlocal enabledelayedexpansion
for /l %%i in (1,1,30) do (
    docker inspect opener-plc --format="{{.State.Health.Status}}" >nul 2>&1
    for /f "delims=" %%h in ('docker inspect opener-plc --format="{{.State.Health.Status}}" 2^>nul') do (
        if "%%h"=="healthy" (
            echo %SUCCESS% OpENer is healthy
            goto opener_ready
        )
    )
    timeout /t 1 /nobreak >nul
)
:opener_ready

REM Start Spring Boot Backend
echo.
echo %INFO% Starting Spring Boot Backend...
cd /d C:\code\compactLogix
start "CompactLogix Backend" cmd /c "mvn spring-boot:run"
timeout /t 5 /nobreak >nul
echo %SUCCESS% Spring Boot started (http://localhost:8080)

REM Start Angular UI
echo.
echo %INFO% Starting Angular UI...
cd /d C:\code\compactLogix\ui
start "CompactLogix UI" cmd /c "npm start"
timeout /t 3 /nobreak >nul
echo %SUCCESS% Angular UI starting (http://localhost:4200)

REM Final status
echo.
echo ========================================
echo  System Startup Complete
echo ========================================
echo.
echo %SUCCESS% OpENer Simulator   -> http://localhost:44818 (EtherNet/IP)
echo %SUCCESS% Spring Boot Backend -> http://localhost:8080/api
echo %SUCCESS% Angular UI         -> http://localhost:4200
echo.
echo Press any key to continue monitoring...
pause

REM Show logs
echo.
echo Showing OpENer logs:
docker logs -f opener-plc

endlocal
