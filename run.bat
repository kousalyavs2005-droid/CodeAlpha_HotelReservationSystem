@echo off
cd /d "%~dp0"

if not exist out mkdir out

echo Compiling Java files...

for /r src %%f in (*.java) do (
    javac -cp out -d out "%%f"
    if errorlevel 1 (
        echo.
        echo Build failed while compiling:
        echo %%f
        pause
        exit /b 1
    )
)

xcopy /E /I /Y "src\resources" "out\resources" >nul

echo.
echo Build successful!
echo Starting Hotel Reservation System...
echo.

java -cp out Main

pause