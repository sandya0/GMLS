@echo off
echo Setting up Gradle...

REM Set variables
set GRADLE_USER_HOME=%CD%\gradle-user-home
set GRADLE_VERSION=gradle-8.0-bin
set GRADLE_URL=https://services.gradle.org/distributions/%GRADLE_VERSION%.zip
set GRADLE_DIST_DIR=%GRADLE_USER_HOME%\wrapper\dists\%GRADLE_VERSION%
set GRADLE_HASH_DIR=%GRADLE_DIST_DIR%\3cvrm9ukiwy6aaiwqaujqb6pa
set GRADLE_ZIP=%GRADLE_HASH_DIR%\%GRADLE_VERSION%.zip

echo Creating directories...
if not exist "%GRADLE_USER_HOME%" mkdir "%GRADLE_USER_HOME%"
if not exist "%GRADLE_USER_HOME%\wrapper\dists" mkdir "%GRADLE_USER_HOME%\wrapper\dists"
if not exist "%GRADLE_DIST_DIR%" mkdir "%GRADLE_DIST_DIR%"
if not exist "%GRADLE_HASH_DIR%" mkdir "%GRADLE_HASH_DIR%"

echo Creating local.properties file...
echo sdk.dir=C:\\Users\\sandya\\AppData\\Local\\Android\\Sdk > local.properties
echo MAPS_API_KEY=AIzaSyAGr1DVfb6owy9BX2A4VBBb0hnbAq3m3GE >> local.properties

echo Creating gradle-local.properties file...
echo org.gradle.user.home=%GRADLE_USER_HOME% > gradle-local.properties

echo Copying Gradle wrapper files...
copy /Y gradle\wrapper\gradle-wrapper.properties gradle\wrapper\gradle-wrapper.properties.bak
echo distributionBase=GRADLE_USER_HOME > gradle\wrapper\gradle-wrapper.properties
echo distributionPath=wrapper/dists >> gradle\wrapper\gradle-wrapper.properties
echo distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip >> gradle\wrapper\gradle-wrapper.properties
echo zipStoreBase=GRADLE_USER_HOME >> gradle\wrapper\gradle-wrapper.properties
echo zipStorePath=wrapper/dists >> gradle\wrapper\gradle-wrapper.properties

echo Downloading Gradle...
powershell -Command "& {Invoke-WebRequest -Uri '%GRADLE_URL%' -OutFile '%GRADLE_ZIP%'}"

echo Creating marker file...
echo > "%GRADLE_HASH_DIR%\%GRADLE_VERSION%.zip.ok"

echo Gradle setup complete!
echo.
echo IMPORTANT: In Android Studio, go to:
echo File -^> Settings -^> Build, Execution, Deployment -^> Build Tools -^> Gradle
echo And set "Gradle user home" to: %GRADLE_USER_HOME%
echo.
echo Press any key to continue...
pause 