@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo =========================================================================
echo  SMART AMUSEMENT PARK MANAGEMENT SYSTEM (v2) - JAVA (OOP) MODULE
echo =========================================================================

call setup_libs.bat

if not exist "build" mkdir build

echo.
echo Compiling Java source files using relative paths...
if exist sources.txt del sources.txt
for /f "delims=" %%F in ('dir /s /b src\*.java') do (
    set "FULL=%%F"
    set "REL=!FULL:%CD%\=!"
    echo !REL!>> sources.txt
)

javac -encoding UTF-8 -cp "lib/*" -d build @sources.txt
set "JAVAC_ERR=%ERRORLEVEL%"
if exist sources.txt del sources.txt

if %JAVAC_ERR% NEQ 0 (
    echo.
    echo [ERROR] Java compilation failed with exit code %JAVAC_ERR%!
    if not "%1"=="--test" pause
    exit /b %JAVAC_ERR%
)
echo [OK] Compilation successful!
echo.

if "%1"=="dashboard" goto RUN_DASHBOARD
if "%1"=="task1" goto RUN_TASK1
if "%1"=="task2" goto RUN_TASK2
if "%1"=="task3" goto RUN_TASK3
if "%1"=="task4" goto RUN_TASK4
if "%1"=="all" goto RUN_ALL
if "%1"=="--test" goto RUN_ALL_TEST
if "%1"=="test" goto RUN_ALL_TEST
if "%1"=="--headless" goto RUN_ALL_TEST

:MENU
echo Choose an option to execute:
echo  [1] Launch Integrated Management Dashboard (Swing UI)
echo  [2] Run Task 1 (CO1) Fundamentals Demo
echo  [3] Run Task 2 (CO2) OOP Design and Lambdas Demo
echo  [4] Run Task 3 (CO3) Robustness and Exception Handling Demo
echo  [5] Run Task 4 (CO4) Persistence and Generics Demo
echo  [6] Run All Console Tasks (1-4)
echo  [0] Exit
echo.
set /p opt="Enter choice [1-6, 0]: "

if "%opt%"=="1" goto RUN_DASHBOARD
if "%opt%"=="2" goto RUN_TASK1
if "%opt%"=="3" goto RUN_TASK2
if "%opt%"=="4" goto RUN_TASK3
if "%opt%"=="5" goto RUN_TASK4
if "%opt%"=="6" goto RUN_ALL
if "%opt%"=="0" exit /b 0
goto MENU

:RUN_DASHBOARD
echo.
echo =========================================================================
echo  LAUNCHING INTEGRATED MANAGEMENT DASHBOARD (Swing FlatLaf UI)
echo =========================================================================
echo Opening GUI window on your desktop...
java -cp "build;lib/*" com.smartpark.ui.ParkDashboard
if errorlevel 1 (
    echo.
    echo [ERROR] Dashboard failed to start or crashed.
    pause
)
goto MENU

:RUN_TASK1
echo.
echo --- Running Task 1 (CO1) Fundamentals ---
java -cp "build;lib/*" com.smartpark.tasks.Task1Fundamentals
echo.
pause
goto MENU

:RUN_TASK2
echo.
echo --- Running Task 2 (CO2) OOP Design ---
java -cp "build;lib/*" com.smartpark.tasks.Task2Design
echo.
pause
goto MENU

:RUN_TASK3
echo.
echo --- Running Task 3 (CO3) Robustness and Exceptions ---
java -cp "build;lib/*" com.smartpark.tasks.Task3RobustnessUI
echo.
pause
goto MENU

:RUN_TASK4
echo.
echo --- Running Task 4 (CO4) Persistence and Generics ---
java -cp "build;lib/*" com.smartpark.tasks.Task4PersistenceGenerics
echo.
pause
goto MENU

:RUN_ALL
echo.
echo ==================== RUNNING ALL TASKS (1-4) ====================
java -cp "build;lib/*" com.smartpark.tasks.Task1Fundamentals
echo.
java -cp "build;lib/*" com.smartpark.tasks.Task2Design
echo.
java -cp "build;lib/*" com.smartpark.tasks.Task3RobustnessUI
echo.
java -cp "build;lib/*" com.smartpark.tasks.Task4PersistenceGenerics
echo ================================================================
echo.
pause
goto MENU

:RUN_ALL_TEST
echo.
echo ==================== RUNNING ALL TASKS (1-4) TEST ====================
java -cp "build;lib/*" com.smartpark.tasks.Task1Fundamentals
if errorlevel 1 exit /b 1
java -cp "build;lib/*" com.smartpark.tasks.Task2Design
if errorlevel 1 exit /b 1
java -cp "build;lib/*" com.smartpark.tasks.Task3RobustnessUI
if errorlevel 1 exit /b 1
java -cp "build;lib/*" com.smartpark.tasks.Task4PersistenceGenerics
if errorlevel 1 exit /b 1
echo ================================================================
exit /b 0
