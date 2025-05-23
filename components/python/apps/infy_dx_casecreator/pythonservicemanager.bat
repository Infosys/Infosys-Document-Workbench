:: =============================================================================================================== *
:: Copyright 2022 Infosys Ltd.                                                                                   *
:: Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at   *
:: http://www.apache.org/licenses/ 																				   *
:: =============================================================================================================== *

@ECHO OFF

::##### SYSTEM SPECIFIC ######
::##### SYSTEM SPECIFIC ######

::##### PROJECT SPECIFIC ######
::DON'T enclose value in quotes
SET PYTHON_LOCAL_ENV=.venv
SET PYTHON_APPLICATION=app_scheduler.py
SET COMMAND_LINE_ARG=%1
::##### PROJECT SPECIFIC ######

GOTO :MAIN

:: >>>>>>>>>>>>>>>>> FUNCTIONS >>>>>>>>>>>>>>>>>
:fnPrintGreen
			SET TEXT=%~1
			echo [92m%TEXT%[0m 
GOTO :EOF

:fnRun
			SET PIP_PATH=C:/ProgramFiles/Python310/Scripts
			SET PYTHON_PATH=C:/ProgramFiles/Python310
			SET PATH=%PIP_PATH%;%PYTHON_PATH%;%PATH%
			CD src
			python -m pipenv run python %PYTHON_APPLICATION%
			Exit
			:: CD ..
GOTO :EOF

:: <<<<<<<<<<<<<<<<< FUNCTIONS <<<<<<<<<<<<<<<<<

:: >>>>>>>>>>>>>>>>> MAIN PROGRAM >>>>>>>>>>>>>>>>>
:MAIN

IF %COMMAND_LINE_ARG%==execute (
	ECHO.
	CALL :fnPrintGreen "Launching/running application..."
	CALL :fnRun
	ECHO.
	CALL :fnPrintGreen "Launch/Run command completed. Please verify manually for any errors."
)

:End