@echo off
set GRADLE_USER_HOME=%CD%\gradle-home
echo Using Gradle home: %GRADLE_USER_HOME%
.\gradlew.bat %* 