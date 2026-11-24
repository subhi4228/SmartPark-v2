@echo off
setlocal
cd /d "%~dp0"

echo [SmartPark v2] Setting up Java libraries...
if not exist "lib" mkdir lib

if not exist "lib\flatlaf-3.5.4.jar" (
    echo Downloading FlatLaf 3.5.4...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5.4/flatlaf-3.5.4.jar' -OutFile 'lib\flatlaf-3.5.4.jar' -UseBasicParsing"
)

if not exist "lib\gson-2.11.0.jar" (
    echo Downloading Google Gson 2.11.0...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar' -OutFile 'lib\gson-2.11.0.jar' -UseBasicParsing"
)

echo [OK] Libraries ready in java\lib\.
