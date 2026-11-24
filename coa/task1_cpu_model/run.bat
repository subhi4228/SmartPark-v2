@echo off
setlocal
cd /d "%~dp0"

set "PATH=C:\msys64\ucrt64\bin;%PATH%"

echo ==============================================================
echo  SMART AMUSEMENT PARK - COA TASK 1: 64-BIT CPU MODEL
echo ==============================================================

gcc -Wall -Wextra -std=c11 -O2 cpu_model.c -o cpu_model.exe
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
cpu_model.exe
