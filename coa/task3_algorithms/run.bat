@echo off
setlocal
cd /d "%~dp0"

set "PATH=C:\msys64\ucrt64\bin;%PATH%"

echo =========================================================================
echo  SMART AMUSEMENT PARK - COA TASK 3: ADD-AND-SHIFT MULT AND STRING OPS
echo =========================================================================

nasm -f win64 algo_string_proc64.asm -o algo_string_proc64.obj
if errorlevel 1 (
    echo [ERROR] NASM assembly failed!
    pause
    exit /b 1
)

gcc -Wall -Wextra -std=c11 -O2 runner.c algo_string_proc64.obj -o task3_algo.exe
if errorlevel 1 (
    echo [ERROR] C linking failed!
    pause
    exit /b 1
)

echo [OK] Build successful!
echo.
task3_algo.exe
