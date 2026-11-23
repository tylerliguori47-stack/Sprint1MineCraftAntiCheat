@echo off
setlocal
pushd "%~dp0" || exit /b 2
set "runner=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "runner=%JAVA_HOME%\bin\java.exe"
if exist "%CD%\runtime\bin\java.exe" set "runner=%CD%\runtime\bin\java.exe"
"%runner%" -version >nul 2>&1
if errorlevel 1 goto noJava
"%runner%" -jar "sprint3-checks.jar"
set "result=%errorlevel%"
if not "%result%"=="0" echo Check the output above. Running the Java checks requires Java 21 or newer.
goto finish

:noJava
echo No Java runtime was found. Opening the recorded demo in your browser.
echo Click Run log check to verify the saved history.
echo To repeat every Java test, see START-HERE.txt for the portable Java option.
start "" "OPEN-RESULTS.html"
set "result=2"

:finish
pause
popd
exit /b %result%
