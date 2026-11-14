@echo off
setlocal
cd /d "%~dp0\.."
where java >nul 2>&1
if errorlevel 1 (echo Install JDK 17 or newer first. & pause & exit /b 1)
if exist target\marketflow.jar goto launch
if exist dist\marketflow.jar goto launchdist
where mvn >nul 2>&1
if errorlevel 1 (echo Install Maven 3.9+ or use the built bundle from GitHub Actions. & pause & exit /b 1)
call mvn -B clean package
if errorlevel 1 (pause & exit /b 1)
:launch
java -jar target\marketflow.jar
if errorlevel 1 pause
exit /b
:launchdist
java -jar dist\marketflow.jar
if errorlevel 1 pause