@echo off
setlocal
cd /d "%~dp0"

set "PATH=C:\msys64\ucrt64\bin;%PATH%"

echo ==============================================================
echo  SMART AMUSEMENT PARK - DSA TASK 1: ARRAY OPERATIONS
echo ==============================================================

gcc -Wall -Wextra -std=c11 -O2 task1_array.c ../common/dsa_common.c -o task1_array.exe
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
task1_array.exe %*
