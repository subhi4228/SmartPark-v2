@echo off
setlocal
cd /d "%~dp0"

set "PATH=C:\msys64\ucrt64\bin;%PATH%"

echo ==============================================================
echo  SMART AMUSEMENT PARK - DSA TASK 3: STACK AND QUEUE
echo ==============================================================

gcc -Wall -Wextra -std=c11 -O2 task3_stack_queue.c ../common/dsa_common.c -o task3_stack_queue.exe
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
task3_stack_queue.exe %*
