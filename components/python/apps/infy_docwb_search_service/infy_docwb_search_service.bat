:: =============================================================================================================== *
:: Copyright 2022 Infosys Ltd.                                                                                   *
:: Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at   *
:: http://www.apache.org/licenses/ 																				   *
:: =============================================================================================================== *

@echo off
REM Change to the working directory
cd /d c:/workarea/docwbsln/services/infy_docwb_search_service/src

REM Activate the virtual environment
call c:/workarea/docwbsln/services/infy_docwb_search_service/.venv/Scripts/activate.bat

REM Export environment variables from .env file
for /f "tokens=*" %%i in (..\..env) do set %%i

REM Start the Python script
python main.py

REM Restart the script if it exits
:restart
timeout /t 9
python main.py
goto restart