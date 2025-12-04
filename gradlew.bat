@rem
@rem Copyright 2012 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @set DEBUG=0

@if "%OS%" == "Windows_NT" @goto winNT
@echo off
call :main
goto end

:winNT
@setlocal

call :main
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
endlocal
goto end

:main
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS.
set DEFAULT_JVM_OPTS=

set DIR=%~dp0
if "%DIR%" == "" set DIR=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIR%

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
for %%i in (%JAVA_EXE%) do if "%%~fi" NEQ "" call :setJavaExe "%%~fi"
if defined JAVA_EXE goto runWrapper

:findJavaFromJavaHome
set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
if exist %JAVA_EXE% goto runWrapper

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME environment variable in your shell to the correct location of your Java Development Kit (JDK).
echo.
goto fail

:runWrapper
@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
if %ERRORLEVEL% NEQ 0 goto fail

@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @endlocal

goto end

:fail
@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @endlocal
exit /b 1

:end