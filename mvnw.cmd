@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script for Windows
@REM Apache Maven Wrapper 3.3.2
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0") ELSE SET "BASE_DIR=%__MVNW_ARG0_NAME__%"

@SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
@IF NOT "%MAVEN_PROJECTBASEDIR%"=="" GOTO endDetectBaseDir

@SET EXEC_DIR=%CD%
@SET WDIR=%EXEC_DIR%
:findBaseDir
@IF EXIST "%WDIR%"\.mvn GOTO baseDirFound
@cd ..
@IF "%WDIR%"=="%CD%" GOTO baseDirNotFound
@SET WDIR=%CD%
@GOTO findBaseDir

:baseDirFound
@SET MAVEN_PROJECTBASEDIR=%WDIR%
@cd "%EXEC_DIR%"
@GOTO endDetectBaseDir

:baseDirNotFound
@SET MAVEN_PROJECTBASEDIR=%EXEC_DIR%
@cd "%EXEC_DIR%"

:endDetectBaseDir

@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
@SET DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

@IF NOT EXIST %WRAPPER_JAR% (
    @IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" (MD "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper")
    @ECHO Downloading Maven Wrapper...
    @powershell -Command ^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;" ^
        "(New-Object System.Net.WebClient).DownloadFile('%DOWNLOAD_URL%', %WRAPPER_JAR%)"
)

@SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
@IF NOT EXIST "%JAVA_EXE%" SET JAVA_EXE=java

%JAVA_EXE% %MAVEN_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %*

@IF NOT "%ERRORLEVEL%"=="0" @PAUSE
