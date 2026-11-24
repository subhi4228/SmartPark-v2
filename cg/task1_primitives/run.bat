@echo off
setlocal
cd /d "%~dp0"

set "PATH=C:\msys64\ucrt64\bin;%PATH%"

echo =========================================================================
echo  SMART AMUSEMENT PARK - CG TASK 1: PRIMITIVES AND ALGORITHMS
echo =========================================================================

g++ -Wall -Wextra -std=c++11 -O2 main.cpp -lfreeglut -lopengl32 -lglu32 -o task1_cg.exe
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
task1_cg.exe %*
