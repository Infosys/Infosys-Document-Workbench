:: =============================================================================================================== *
:: Copyright 2022 Infosys Ltd.                                                                                   *
:: Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at   *
:: http://www.apache.org/licenses/ 																				   *
:: =============================================================================================================== *

@echo off
setlocal

set WORKING_DIRECTORY=C:\workarea\docwbsln\services\docwbrules
REM set JAVA_EXECUTABLE="C:\Program Files\Eclipse\jdk-8.0.402.6-hotspot\bin\java.exe"
set JAVA_EXECUTABLE="java"
set JAR_FILE=docwb-rules.war
set LOG_FILE=C:\workarea\docwbsln\logs\docwbrules.log

cd %WORKING_DIRECTORY%

if not exist %JAR_FILE% (
    echo JAR file not found: %JAR_FILE% >> %LOG_FILE%
    echo JAR file not found: %JAR_FILE%
    goto end
)

echo Starting docwb-rules at %date% %time% >> %LOG_FILE%
start "" /b %JAVA_EXECUTABLE% -jar %JAR_FILE%

:end
endlocal