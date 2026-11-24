@echo off
setlocal enabledelayedexpansion
title Smart Amusement Park Management System (v2) - Packaging Release

set "ROOT=%~dp0"
cd /d "%ROOT%"

echo ===============================================================================
echo                PACKAGING SMART AMUSEMENT PARK (v2) ZIP ARCHIVE
echo ===============================================================================
echo.
echo Packaging project from: %ROOT%
echo Target archive: %ROOT%SmartPark_v2.zip
echo.

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference = 'Stop';" ^
    "$ProgressPreference = 'SilentlyContinue';" ^
    "$root = (Get-Item '%ROOT%').FullName;" ^
    "$zipPath = Join-Path $root 'SmartPark_v2.zip';" ^
    "$stageBase = Join-Path $env:TEMP ('SmartPark_v2_stage_' + [System.Guid]::NewGuid().ToString('N').Substring(0,8));" ^
    "$stage = Join-Path $stageBase 'SmartPark_v2';" ^
    "Write-Host 'Creating clean staging directory at:' $stage;" ^
    "New-Item -ItemType Directory -Path $stage -Force | Out-Null;" ^
    "try {" ^
    "    $excludeExtensions = @('.exe', '.obj', '.o');" ^
    "    $excludeFiles = @('supabase.properties', 'SmartPark_v2.zip');" ^
    "    Get-ChildItem -Path $root -Recurse | ForEach-Object {" ^
    "        $relPath = $_.FullName.Substring($root.Length).TrimStart('\', '/');" ^
    "        if ($relPath -match '^(bin|target|\.git|\.idea|\.vscode)($|[\\/])') { return; }" ^
    "        if ($excludeFiles -contains $_.Name) { return; }" ^
    "        if ($_.PSIsContainer) {" ^
    "            $destDir = Join-Path $stage $relPath;" ^
    "            if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null; }" ^
    "        } else {" ^
    "            if ($excludeExtensions -contains $_.Extension.ToLower()) { return; }" ^
    "            $destFile = Join-Path $stage $relPath;" ^
    "            $parentDir = Split-Path $destFile -Parent;" ^
    "            if (-not (Test-Path $parentDir)) { New-Item -ItemType Directory -Path $parentDir -Force | Out-Null; }" ^
    "            Copy-Item -Path $_.FullName -Destination $destFile -Force;" ^
    "        }" ^
    "    };" ^
    "    if (Test-Path $zipPath) { Remove-Item $zipPath -Force; }" ^
    "    Write-Host 'Compressing archive with Compress-Archive...';" ^
    "    Compress-Archive -Path $stage -DestinationPath $zipPath -CompressionLevel Optimal -Force;" ^
    "    $zipItem = Get-Item $zipPath;" ^
    "    $sizeMB = [math]::Round($zipItem.Length / 1MB, 2);" ^
    "    Write-Host '---------------------------------------------------------------';" ^
    "    Write-Host ('SUCCESS: Archive created at: ' + $zipItem.FullName);" ^
    "    Write-Host ('Archive size: ' + $sizeMB + ' MB (' + $zipItem.Length + ' bytes)');" ^
    "    Write-Host '---------------------------------------------------------------';" ^
    "} finally {" ^
    "    if (Test-Path $stageBase) { Remove-Item -Path $stageBase -Recurse -Force -ErrorAction SilentlyContinue; }" ^
    "}"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Packaging completed successfully without errors.
) else (
    echo.
    echo ERROR: Packaging script encountered an issue.
)
