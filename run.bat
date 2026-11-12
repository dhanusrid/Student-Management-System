@echo off
setlocal
cd /d "%~dp0"

echo ==============================================================
echo   STARTING STUDENT MANAGEMENT SYSTEM
echo ==============================================================
echo.

:: 1. Check if compiled classes exist, if not compile them
if not exist "bin\com\student\server\StudentManagementServer.class" (
    echo Compiling Java source files...
    if not exist "bin" mkdir bin
    javac -encoding UTF-8 -d bin src\com\student\model\*.java src\com\student\util\*.java src\com\student\dao\*.java src\com\student\server\*.java
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Compilation failed. Please check Java JDK installation.
        pause
        exit /b %ERRORLEVEL%
    )
)

:: 2. Launch default web browser after 1 second delay in background
start "" cmd /c "timeout /t 2 >nul & start http://localhost:8080"

:: 3. Run the Java Application Server
echo Starting Java Web Application Server on http://localhost:8080 ...
java -cp "bin;lib/*" com.student.server.StudentManagementServer

pause
