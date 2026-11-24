@echo off
setlocal
cd /d "%~dp0"

set "PATH=C:\msys64\ucrt64\bin;%PATH%"

echo =========================================================================
echo  SMART AMUSEMENT PARK - CG TASK 4: FINAL 3D PRESENTATION SIMULATION
echo =========================================================================

g++ -Wall -Wextra -std=c++11 -O2 main.cpp -lfreeglut -lopengl32 -lglu32 -o task4_cg.exe
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
task4_cg.exe %*
