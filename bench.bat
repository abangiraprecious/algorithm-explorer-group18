@echo off
rem Compile and run the Module D timing experiment; writes results\module_d.csv.
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
set RUNS=%1
if "%RUNS%"=="" set RUNS=3
java -cp out explorer.Benchmark %RUNS%
pause
