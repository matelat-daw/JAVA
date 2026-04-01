@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.

@echo off

setlocal

set ERROR_CODE=0

:init
@REM Decide how to startup depending on the version of windows we are using.
if "%OS%"=="Windows_NT" goto win9xME_args

echo.ERROR: JAVA_HOME not found in your environment. Please set the JAVA_HOME variable in your user or system environment to match the location of your Java installation
echo.
goto error

:win9xME_args
@REM Decide how to startup depending on the version of windows we are using.
if "%OS%"=="Windows_NT" (
  set "DIRNAME=%~dp0"
) else (
  set DIRNAME=.\
)

set EXEC_DIR=%DIRNAME%
set WDIR=%EXEC_DIR%

:loop
if exist "%WDIR%mvnw.cmd" goto found
cd /d "%WDIR%.."
if "%WDIR%"=="%CD%\" goto:init

set "WDIR=%CD%\"
goto loop

:found
set "MAVEN_PROJECTBASEDIR=%WDIR%"

:endDetectBaseDir

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo.ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
  echo.Please set the JAVA_HOME variable in your user or system environment to match the
  echo.location of your Java installation.
  echo.
  goto error
)

if exist "%M2_HOME%\bin\mvn.cmd" goto endDetectMvnHome

:endDetectMvnHome

"%JAVA_HOME%\bin\java.exe" %JDWP_OPTS% -classpath "%EXEC_DIR%.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %MAVEN_DEBUG_OPTS% %MAVEN_OPTS% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & exit /B %ERROR_CODE%
