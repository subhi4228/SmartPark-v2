@echo off
setlocal
cd /d "%~dp0"

set "PATH=C:\msys64\ucrt64\bin;%PATH%"

echo =========================================================================
echo  SMART AMUSEMENT PARK - COA TASK 4 INTEGRATED CPU SIMULATOR
echo =========================================================================

gcc -Wall -Wextra -std=c11 -O2 cpu_sim.c -o cpu_sim.exe
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
echo Running automated instruction encoding and functionality verification...
cpu_sim.exe --verify

echo.
echo Launching offline interactive HTML simulator presentation demo...
start simulator.html

if "%1"=="" (
    echo.
    echo Launching interactive console simulator...
    cpu_sim.exe
)
