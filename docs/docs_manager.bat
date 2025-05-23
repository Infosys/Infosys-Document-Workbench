:: # ===============================================================================================================#
:: # (C) 2025 Infosys Limited, Bangalore, India. All Rights Reserved.                                               #
:: # Version: 2.0                                                                                                   #
:: #                                                                                                                #
:: # Except for any open source software components embedded in this Infosys proprietary software program           #
:: # ("Program"), this Program is protected by copyright laws, international treaties and other pending or          #
:: # existing intellectual property rights in India, the United States and other countries. Except as expressly     #
:: # permitted, any unauthorized reproduction, storage, transmission in any form or by any means (including         #
:: # without limitation electronic, mechanical, printing, photocopying, recording or otherwise), or any             #
:: # distribution of this Program, or any portion of it, may result in severe civil and criminal penalties, and will#
:: # be prosecuted to the maximum extent possible under the law.                                                    #
:: # ===============================================================================================================#

@echo off
chcp 65001 >nul

echo.
echo Checking for prerequisites...
call python --version 1>nul
if %ERRORLEVEL% GEQ 1 GOTO PYTHON_NOT_FOUND_ERROR
echo ✓ python 
echo.


python ..\_internal\scripts\docs_manager\docs_manager.py
GOTO END


:PYTHON_NOT_FOUND_ERROR
echo.
echo ✕ python 
echo ERROR: Python not found. Please install and then re-run this script. 
echo.
pause 
GOTO END

:END
endlocal
