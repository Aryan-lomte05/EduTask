@echo off
setlocal enabledelayedexpansion

echo.
echo ===================================
echo  EduTask Windows Installer Builder
echo ===================================
echo.

REM Set paths
set JAR_PATH=out\artifacts\EduTask_jar\EduTask.jar
set ICON_PATH=icons\app-icon.ico
set APP_NAME=EduTask
set APP_VERSION=1.0.0
set "VENDOR_NAME=Aryan and Manthan"

REM Verify JAR exists
if not exist "%JAR_PATH%" (
    echo ERROR: JAR file not found at %JAR_PATH%
    pause
    exit /b 1
)

echo [OK] Found JAR: %JAR_PATH%

REM Verify icon exists
if not exist "%ICON_PATH%" (
    echo ERROR: Icon not found at %ICON_PATH%
    pause
    exit /b 1
)

echo [OK] Found Icon: %ICON_PATH%

echo.
echo Checking Java installation...

REM Check if jpackage is available
where jpackage >nul 2>nul
if errorlevel 1 (
    echo WARNING: jpackage not found in PATH
    echo Attempting to find Java home...
    
    REM Try common Java locations
    if exist "C:\Program Files\Java\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
        echo Found: !JAVA_HOME!
    ) else if exist "C:\Program Files\Java\jdk17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk17"
        echo Found: !JAVA_HOME!
    ) else (
        echo.
        echo ERROR: Could not find Java installation
        echo.
        echo Please install Java 17 from:
        echo https://adoptium.net/temurin/releases/
        echo.
        pause
        exit /b 1
    )
) else (
    echo [OK] jpackage is available
)

echo.
echo Building Windows installer...
echo.

REM Build the installer - with full path to jpackage if needed
if defined JAVA_HOME (
    "!JAVA_HOME!\bin\jpackage" ^
      --input out\artifacts\EduTask_jar ^
      --name %APP_NAME% ^
      --main-jar EduTask.jar ^
      --main-class com.edutask.Main ^
      --type exe ^
      --icon %ICON_PATH% ^
      --app-version %APP_VERSION% ^
      --vendor "%VENDOR_NAME%" ^
      --description "Smart Task Manager for Students" ^
      --win-menu ^
      --win-shortcut ^
      --win-dir-chooser
) else (
    jpackage ^
      --input out\artifacts\EduTask_jar ^
      --name %APP_NAME% ^
      --main-jar EduTask.jar ^
      --main-class com.edutask.Main ^
      --type exe ^
      --icon %ICON_PATH% ^
      --app-version %APP_VERSION% ^
      --vendor "%VENDOR_NAME%" ^
      --description "Smart Task Manager for Students" ^
      --win-menu ^
      --win-shortcut ^
      --win-dir-chooser
)

if errorlevel 1 (
    echo.
    echo ERROR: jpackage failed!
    echo.
    pause
    exit /b 1
)

echo.
echo ===================================
echo SUCCESS! Installer created!
echo ===================================
echo.

dir /b EduTask-*.exe

echo.
echo Run the .exe file to test!
echo.
pause
