@echo off
echo ==============================================================
echo   Building Student Management System...
echo ==============================================================

if not exist "bin" mkdir bin

echo Compiling Java source files...
javac -encoding UTF-8 -d bin src\com\student\model\*.java src\com\student\util\*.java src\com\student\dao\*.java src\com\student\server\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Compilation finished successfully!
    echo Output directory: bin\
) else (
    echo.
    echo [ERROR] Compilation failed! Please ensure JDK is installed and in PATH.
)
pause
