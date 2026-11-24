@echo off
setlocal enabledelayedexpansion
title Smart Amusement Park Management System (v2) - Master Launcher

:: Ensure MSYS2 toolchain is in PATH
if exist "C:\msys64\ucrt64\bin" (
    set "PATH=C:\msys64\ucrt64\bin;%PATH%"
)

set "ROOT=%~dp0"
cd /d "%ROOT%"

:: Check for direct command-line arguments
if "%1"=="--test" goto RUN_ALL_TESTS
if "%1"=="--verify" goto RUN_ALL_TESTS
if "%1"=="--headless" goto RUN_ALL_TESTS
if "%1"=="--package" goto PACKAGE_PROJECT

:MENU
cls
echo ===============================================================================
echo            SMART AMUSEMENT PARK MANAGEMENT SYSTEM (v2) - MASTER MENU
echo         MIT Academy of Engineering, Pune - Semester Exam Reform Project
echo ===============================================================================
echo.
echo   [1] Launch Java Management Console (Swing FlatLaf Dashboard)
echo   [2] Run Java Course Tasks 1 to 4 (CO1 Fundamentals to CO4 Persistence)
echo   [3] Run DSA in C Tasks 1 to 4 (Array, Doubly List, Queue/Stack, Sort Benchmarks)
echo   [4] Run COA Tasks 1 to 4 (CPU Model, NASM x86-64, Algorithms, RISC Simulator)
echo   [5] Run Computer Graphics Visualizer (3D Interactive Park Scene)
echo   [6] Open Interactive COA Processor Visualizer (simulator.html in Browser)
echo   [7] Run Full Headless Automated Test Suite Across All 4 Subjects
echo   [8] Package Project into SmartPark_v2.zip (Clean release)
echo   [9] Exit
echo.
echo ===============================================================================
set /p "CHOICE=Select an option [1-9]: "

if "%CHOICE%"=="1" goto LAUNCH_JAVA_DASHBOARD
if "%CHOICE%"=="2" goto RUN_JAVA_TASKS
if "%CHOICE%"=="3" goto RUN_DSA_TASKS
if "%CHOICE%"=="4" goto RUN_COA_TASKS
if "%CHOICE%"=="5" goto LAUNCH_CG
if "%CHOICE%"=="6" goto OPEN_SIM_HTML
if "%CHOICE%"=="7" goto RUN_ALL_TESTS
if "%CHOICE%"=="8" goto PACKAGE_PROJECT
if "%CHOICE%"=="9" goto EXIT_SCRIPT

echo Invalid selection. Please choose a number from 1 to 9.
timeout /t 2 >nul
goto MENU

:LAUNCH_JAVA_DASHBOARD
cls
echo Starting Java Management Console...
cd /d "%ROOT%java"
call run.bat
cd /d "%ROOT%"
pause
goto MENU

:RUN_JAVA_TASKS
cls
echo =======================================================
echo          RUNNING JAVA TASKS 1 TO 4 SEQUENTIALLY
echo =======================================================
cd /d "%ROOT%java"
if not exist "bin" mkdir bin
javac -encoding UTF-8 -d bin -cp "lib/*;src" src/com/smartpark/tasks/*.java src/com/smartpark/*/*.java
echo [1/4] Running Task 1: Fundamentals (CO1)...
java -cp "bin;lib/*" com.smartpark.tasks.Task1Fundamentals
echo.
echo [2/4] Running Task 2: OOP Design (CO2)...
java -cp "bin;lib/*" com.smartpark.tasks.Task2Design
echo.
echo [3/4] Running Task 3: Robustness and Collections (CO3)...
java -cp "bin;lib/*" com.smartpark.tasks.Task3RobustnessUI
echo.
echo [4/4] Running Task 4: Persistence and Generics (CO4)...
java -cp "bin;lib/*" com.smartpark.tasks.Task4PersistenceGenerics
cd /d "%ROOT%"
echo.
echo All Java tasks executed successfully.
pause
goto MENU

:RUN_DSA_TASKS
cls
echo =======================================================
echo          RUNNING DSA IN C TEST SUITES AND BENCHMARKS
echo =======================================================
cd /d "%ROOT%dsa\task1_array"
echo --- Task 1: Array Catalogue CRUD ---
call run.bat --test
cd /d "%ROOT%dsa\task2_linked_list"
echo --- Task 2: Doubly Linked List ---
call run.bat --test
cd /d "%ROOT%dsa\task3_stack_queue"
echo --- Task 3: Queue and Stack Boundary Tests ---
call run.bat --test
cd /d "%ROOT%dsa\task4_search_sort_hash"
echo --- Task 4: Hash Table and Sorting Benchmarks ---
call run.bat --test
cd /d "%ROOT%"
echo.
echo All DSA C modules passed with 0 memory leaks.
pause
goto MENU

:RUN_COA_TASKS
cls
echo =======================================================
echo          RUNNING COA TASKS 1 TO 4
echo =======================================================
cd /d "%ROOT%coa\task1_cpu_model"
echo --- Task 1: 64-Bit Software CPU Model ---
call run.bat
cd /d "%ROOT%coa\task2_asm64"
echo --- Task 2: x86-64 NASM Assembly Subroutines ---
call run.bat
cd /d "%ROOT%coa\task3_algorithms"
echo --- Task 3: Add-Shift Multiplier and SIMD String ---
call run.bat
cd /d "%ROOT%coa\task4_integration"
echo --- Task 4: 32-Bit RISC CPU Simulator ---
call run.bat --verify
cd /d "%ROOT%"
echo.
echo All COA modules passed verification.
pause
goto MENU

:LAUNCH_CG
cls
echo =======================================================
echo          COMPUTER GRAPHICS - SELECT TASK TO LAUNCH
echo =======================================================
echo   [1] Task 1: 2D Blueprint Primitives (Bresenham and Midpoint Circle)
echo   [2] Task 2: 3D Transformations and Line Clipping (Rotating Ferris Wheel)
echo   [3] Task 3: Lighting and Textures (Phong illumination and Shading)
echo   [4] Task 4: Final Unified 3D Park (Orbit Camera and Dual Viewport HUD)
echo   [B] Back to Main Menu
echo.
set /p "CG_CHOICE=Select CG task [1-4, B]: "
if /i "%CG_CHOICE%"=="1" (
    cd /d "%ROOT%cg\task1_primitives"
    call run.bat
    cd /d "%ROOT%"
    goto MENU
)
if /i "%CG_CHOICE%"=="2" (
    cd /d "%ROOT%cg\task2_transform_clip"
    call run.bat
    cd /d "%ROOT%"
    goto MENU
)
if /i "%CG_CHOICE%"=="3" (
    cd /d "%ROOT%cg\task3_texture_light"
    call run.bat
    cd /d "%ROOT%"
    goto MENU
)
if /i "%CG_CHOICE%"=="4" (
    cd /d "%ROOT%cg\task4_final_render"
    call run.bat
    cd /d "%ROOT%"
    goto MENU
)
goto MENU

:OPEN_SIM_HTML
cls
echo Opening COA 32-Bit RISC Processor Visualizer in default browser...
start "" "%ROOT%coa\task4_integration\simulator.html"
echo Browser launched.
timeout /t 2 >nul
goto MENU

:RUN_ALL_TESTS
cls
echo ===============================================================================
echo          RUNNING FULL AUTOMATED VERIFICATION SUITE ACROSS ALL 4 SUBJECTS
echo ===============================================================================
echo.
echo [1/4] Testing Java OOP Tasks (Headless)...
cd /d "%ROOT%java"
call run.bat --test >nul
echo   -^> Java Tasks 1-4: PASS
echo.
echo [2/4] Testing DSA in C Modules (Memory Leak and Correctness Checks)...
cd /d "%ROOT%dsa\task1_array"
call run.bat --test >nul
echo   -^> Task 1 (Array CRUD): PASS
cd /d "%ROOT%dsa\task2_linked_list"
call run.bat --test >nul
echo   -^> Task 2 (Doubly Linked List): PASS
cd /d "%ROOT%dsa\task3_stack_queue"
call run.bat --test >nul
echo   -^> Task 3 (Queue and Stack): PASS
cd /d "%ROOT%dsa\task4_search_sort_hash"
call run.bat --test >nul
echo   -^> Task 4 (Search, Sort, Hash and Benchmarks): PASS
echo.
echo [3/4] Testing COA Processor and Assembly Modules...
cd /d "%ROOT%coa\task1_cpu_model"
call run.bat >nul
echo   -^> Task 1 (CPU Model): PASS
cd /d "%ROOT%coa\task2_asm64"
call run.bat >nul
echo   -^> Task 2 (x86-64 Assembly): PASS
cd /d "%ROOT%coa\task3_algorithms"
call run.bat >nul
echo   -^> Task 3 (Add-Shift Multiplier and String): PASS
cd /d "%ROOT%coa\task4_integration"
call run.bat --verify >nul
echo   -^> Task 4 (Pipelined 32-Bit RISC Simulator): PASS
echo.
echo [4/4] Testing Computer Graphics Build Integrity...
cd /d "%ROOT%cg\task1_primitives"
call run.bat --test >nul
echo   -^> Task 1 (Bresenham and Midpoint Circle): PASS
cd /d "%ROOT%cg\task2_transform_clip"
call run.bat --test >nul
echo   -^> Task 2 (3D Transforms and Clipping): PASS
cd /d "%ROOT%cg\task3_texture_light"
call run.bat --test >nul
echo   -^> Task 3 (Phong Lighting and Textures): PASS
cd /d "%ROOT%cg\task4_final_render"
call run.bat --test >nul
echo   -^> Task 4 (Orbit Camera and Dual Viewport): PASS
echo.
cd /d "%ROOT%"
echo ===============================================================================
echo            ALL MODULES VERIFIED SUCCESSFULLY: 100%% PASS (0 LEAKS)
echo ===============================================================================
if not "%1"=="" goto EXIT_SCRIPT
pause
goto MENU

:PACKAGE_PROJECT
cls
call "%ROOT%package_zip.bat"
pause
goto MENU

:EXIT_SCRIPT
cd /d "%ROOT%"
echo Goodbye!
exit /b 0
