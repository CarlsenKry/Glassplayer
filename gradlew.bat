@echo off
setlocal EnableExtensions
set "GRADLE_VERSION=9.3.1"
where gradle >NUL 2>NUL
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
set "GRADLE_HOME_DIR=%USERPROFILE%\.gradle\distributions\gradle-%GRADLE_VERSION%"
set "GRADLE_BIN=%GRADLE_HOME_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat"
if not exist "%GRADLE_BIN%" (
  echo Gradle %GRADLE_VERSION% is not installed; downloading the pinned distribution...
  if not exist "%GRADLE_HOME_DIR%" mkdir "%GRADLE_HOME_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%GRADLE_HOME_DIR%\gradle.zip'; Expand-Archive -LiteralPath '%GRADLE_HOME_DIR%\gradle.zip' -DestinationPath '%GRADLE_HOME_DIR%' -Force; Remove-Item '%GRADLE_HOME_DIR%\gradle.zip' -Force"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_BIN%" %*
exit /b %ERRORLEVEL%
