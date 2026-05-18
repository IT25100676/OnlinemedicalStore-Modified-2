@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

if not "%JAVA_HOME%"=="" set "MAVEN_JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not "%JAVA_HOME%"=="" if not exist "%MAVEN_JAVA_EXE%" goto invalidJavaHome

if "%MAVEN_JAVA_EXE%"=="" for %%i in (java.exe) do set "MAVEN_JAVA_EXE=%%~$PATH:i"
if "%MAVEN_JAVA_EXE%"=="" goto missingJava

if exist "%WRAPPER_JAR%" goto runMaven

echo Downloading Maven Wrapper...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
if errorlevel 1 goto error

:runMaven
"%MAVEN_JAVA_EXE%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*
exit /b %ERRORLEVEL%

:missingJava
echo Error: Java not found in PATH and JAVA_HOME is not set. 1>&2
exit /b 1

:invalidJavaHome
echo Error: JAVA_HOME is set to an invalid directory: "%JAVA_HOME%" 1>&2
exit /b 1

:error
exit /b 1
