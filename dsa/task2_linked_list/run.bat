@echo off
setlocal
cd /d "%~dp0"

set "PATH=C:\msys64\ucrt64\bin;%PATH%"

echo ==============================================================
echo  SMART AMUSEMENT PARK - DSA TASK 2: DOUBLY LINKED LIST
echo ==============================================================

gcc -Wall -Wextra -std=c11 -O2 task2_linked_list.c ../common/dsa_common.c -o task2_linked_list.exe
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
task2_linked_list.exe %*
