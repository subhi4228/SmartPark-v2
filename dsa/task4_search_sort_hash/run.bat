@echo off
setlocal
cd /d "%~dp0"

set "PATH=C:\msys64\ucrt64\bin;%PATH%"

echo =========================================================================
echo  SMART AMUSEMENT PARK - DSA TASK 4: SEARCH, STABLE SORT, HASH AND BENCH
echo =========================================================================

gcc -Wall -Wextra -std=c11 -O2 task4_search_sort_hash.c ../common/dsa_common.c -o task4_search_sort_hash.exe
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
task4_search_sort_hash.exe %*
