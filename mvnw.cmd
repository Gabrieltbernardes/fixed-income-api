@echo off
@REM Maven Wrapper (Windows): baixa o Maven definido em .mvn\wrapper\maven-wrapper.properties e executa.
setlocal
set "MAVEN_HOME="
for /f "usebackq delims=" %%i in (`powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0.mvn\wrapper\mvnw.ps1"`) do set "MAVEN_HOME=%%i"
if not defined MAVEN_HOME (
  echo Falha ao preparar o Maven. 1>&2
  exit /b 1
)
"%MAVEN_HOME%\bin\mvn.cmd" %*
