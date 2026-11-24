@echo off
setlocal
cd /d "%~dp0"

set "PATH=C:\msys64\ucrt64\bin;%PATH%"

echo ==============================================================
echo  SMART AMUSEMENT PARK - COA TASK 2: NASM x86-64 ASSEMBLY
echo ==============================================================

nasm -f win64 park_calc64.asm -o park_calc64.obj
if errorlevel 1 (
    echo [ERROR] NASM assembly failed!
    pause
    exit /b 1
)

gcc -Wall -Wextra -std=c11 -O2 runner.c park_calc64.obj -o task2_asm.exe
if errorlevel 1 (
    echo [ERROR] C linking failed!
    pause
    exit /b 1
)

echo [OK] Build successful!
echo.
task2_asm.exe
