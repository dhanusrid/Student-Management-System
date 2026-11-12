# PowerShell Launch Script for Student Management System
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "   STUDENT MANAGEMENT SYSTEM - JAVA WEB APPLICATION           " -ForegroundColor Green
Write-Host "==============================================================" -ForegroundColor Cyan

# 1. Compile if bin directory does not exist
if (-not (Test-Path "bin\com\student\server\StudentManagementServer.class")) {
    Write-Host "Compiling Java source files..." -ForegroundColor Yellow
    if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
    
    $sources = Get-ChildItem -Path "src\com\student" -Recurse -Filter "*.java" | 
               Where-Object { $_.FullName -notmatch "servlet" } | 
               Select-Object -ExpandProperty FullName
    
    & javac -encoding UTF-8 -d "bin" $sources
    Write-Host "Compilation complete." -ForegroundColor Green
}

# 2. Launch Browser in background
Start-Process "http://localhost:8080"

# 3. Start Server
Write-Host "Starting Java Web Application Server on http://localhost:8080 ..." -ForegroundColor Cyan
& java -cp "bin;lib/*" com.student.server.StudentManagementServer
