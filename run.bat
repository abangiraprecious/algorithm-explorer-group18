@echo off
rem Compile and start Algorithm Explorer (needs JDK 11 or newer).
cd /d "%~dp0"
if exist out rmdir /s /q out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
  del sources.txt
  pause
  exit /b 1
)
del sources.txt
set PORT=%1
if "%PORT%"=="" set PORT=8080
java -cp out explorer.server.Main %PORT%
pause
