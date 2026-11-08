@echo off
setlocal EnableExtensions
set "ANDROID_HOME=E:\ChitramAndroidSdk"
set "ANDROID_SDK_ROOT=E:\ChitramAndroidSdk"
set "ANDROID_AVD_HOME=E:\ChitramAVD"
set "EMULATOR=%ANDROID_HOME%\emulator\emulator.exe"
set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
set "APK=%~dp0..\app\build\outputs\apk\debug\app-debug.apk"

if not exist "%EMULATOR%" goto missing
if not exist "%ADB%" goto missing
if not exist "%APK%" goto missing

"%EMULATOR%" -accel-check >nul
if errorlevel 1 goto acceleration

echo Starting the Google TV emulator...
start "" "%EMULATOR%" -avd Chitram_TV -no-snapshot-load
set /a tries=0

:poll
set "boot="
for /f "delims=" %%A in ('%ADB% -e shell getprop sys.boot_completed 2^>nul') do set "boot=%%A"
if "%boot%"=="1" goto ready
set /a tries+=1
if %tries% GEQ 100 goto timeout
timeout /t 3 >nul
goto poll

:ready
echo Installing Chitram...
"%ADB%" -e install -r "%APK%"
if errorlevel 1 goto failed
"%ADB%" -e shell am start -n com.chitram.tv/.MainActivity
if errorlevel 1 goto failed
echo Chitram is open in the Google TV emulator.
pause
exit /b 0

:acceleration
echo CPU virtualization and Windows Hypervisor Platform are needed before this emulator can start.
echo Enable them, restart Windows, then run this file again.
pause
exit /b 1

:missing
echo Missing emulator tools or debug APK. Check the README build instructions.
pause
exit /b 1

:timeout
echo The emulator did not finish booting within five minutes.
pause
exit /b 1

:failed
echo Chitram could not be installed or opened. Check the messages above.
pause
exit /b 1
