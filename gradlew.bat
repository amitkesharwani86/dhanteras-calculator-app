\
@echo off
REM Newly.app cloud build environment provides Gradle directly.
gradle %*
exit /b %ERRORLEVEL%
