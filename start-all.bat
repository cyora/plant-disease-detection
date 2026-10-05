@echo off
rem ============================================================
rem  LeafScan: starts the three services, each in its own window
rem  MySQL is not started here: it runs as a Windows service.
rem ============================================================
title LeafScan launcher
set "ROOT=%~dp0"

rem --- Find Anaconda (edit CONDA_ROOT if it is installed elsewhere) ---
set "CONDA_ROOT=%LOCALAPPDATA%\anaconda3"
if not exist "%CONDA_ROOT%\Scripts\activate.bat" set "CONDA_ROOT=%USERPROFILE%\anaconda3"
if not exist "%CONDA_ROOT%\Scripts\activate.bat" (
    echo Anaconda was not found. Edit CONDA_ROOT at the top of start-all.bat.
    pause
    exit /b 1
)

rem --- Check that the model is present ---
if not exist "%ROOT%models\efficientnetb0.keras" (
    echo The model file models\efficientnetb0.keras is missing. See README.md, section "Get the model".
    pause
    exit /b 1
)

echo [1/3] Starting the ML service (FastAPI, port 8000)...
start "LeafScan - ML service" /D "%ROOT%ml-api" cmd /k "call "%CONDA_ROOT%\Scripts\activate.bat" plantdisease && uvicorn app.main:app --port 8000"

echo [2/3] Starting the backend (Spring Boot, port 8080)...
start "LeafScan - Backend" /D "%ROOT%backend" cmd /k "mvnw.cmd spring-boot:run"

echo [3/3] Starting the interface (Angular, port 4200)...
start "LeafScan - Interface" /D "%ROOT%frontend" cmd /k "npm start"

echo.
echo Waiting 45 seconds for the services to start...
timeout /t 45 /nobreak >nul
start "" http://localhost:4200

echo.
echo LeafScan is running at http://localhost:4200
echo To stop it, close the three service windows (or press Ctrl+C in each).
echo.
pause
