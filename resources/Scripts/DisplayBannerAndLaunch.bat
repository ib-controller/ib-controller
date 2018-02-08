@echo off
setlocal enableextensions enabledelayedexpansion

:: get the IBController version

for /f "tokens=1 delims=" %%i in (%IBC_PATH%\version) do set IBC_VRSN=%%i

:: write to banner window
color 0A
echo +==============================================================================
echo +
echo + IBController version %IBC_VRSN%
echo +
echo + Running %APP% %TWS_MAJOR_VRSN% at %DATE% %TIME%
echo +

:: determine the logfile path and name (if any)
if defined LOG_PATH (
	if not exist "%LOG_PATH%" (
		set PHASE=Creating logfile folder
		mkdir "%LOG_PATH%"
		if errorlevel 1 goto :err
	)
	
	set README=%LOG_PATH%\README.txt
	if not exist "!README!" (
		set PHASE=Creating README file
		(echo You can delete the files in this folder at any time
		echo.
		echo Windows will inform you if a file is currently in use
		echo when you try to delete it.) > "!README!" || set REDIRECTERROR=1
	
		if "!REDIRECTERROR!" == "1" goto :err
	)

	call "%IBC_PATH%\Scripts\getDayOfWeek.bat"
	set LOG_FILE=%LOG_PATH%\ibc-%IBC_VRSN%_%APP%-%TWS_MAJOR_VRSN%_!DAYOFWEEK!.txt
	if exist "!LOG_FILE!" (
		for %%? in (!LOG_FILE!) do (
			set LOGFILETIME=%%~t?
		)
		set s=%DATE%!LOGFILETIME:*%DATE%=!
		if not "!s!" == "!LOGFILETIME!" del "!LOG_FILE!"
	)
) else (
	set LOG_FILE=NUL
)

:: if defined LOG_PATH (
	echo + Diagnostic information is logged in:
	echo +
	echo + %LOG_FILE%
	echo +

	:: check that the logfile is accessible

	set PHASE=Checking logfile accessiblity
	(echo.
	echo ================================================================================
	echo ================================================================================
	echo.
	echo This log file is located at:
	echo.
	echo     %LOG_FILE%
	echo.) >> "%LOG_FILE%" || set REDIRECTERROR=1
	
	if "%REDIRECTERROR%" == "1" goto :err
:: )

echo +
echo + ** Caution: closing this window will close %APP% %TWS_MAJOR_VRSN% **
echo + (window will close automatically when you exit from %APP% %TWS_MAJOR_VRSN%)
echo +

::   now launch IBController

set GW_FLAG=
if /I "%APP%" == "GATEWAY" set GW_FLAG=/G

set PHASE=Running IBController.bat
set ERROR_MESSAGE=
call "%IBC_PATH%\Scripts\IBController.bat" "%TWS_MAJOR_VRSN%" %GW_FLAG% ^
     "/TwsPath:%TWS_PATH%" "/IbcPath:%IBC_PATH%" "/IbcIni:%IBC_INI%" ^
     "/User:%TWSUSERID%" "/PW:%TWSPASSWORD%" "/FIXUser:%FIXUSERID%" "/FIXPW:%FIXPASSWORD%" ^
     "/JavaPath:%JAVA_PATH%" "/Mode:%TRADING_MODE%" ^
     >> "%LOG_FILE%" 2>&1

:: note that killing the Java process sets ERRORLEVEL to 1, but we don't want to trap
:: this as an error. Note that all errorlevels set by IBController are greater than 1000
:: so there shouldn't be any other reason for ERRORLEVEL to be 1
if errorlevel 2 goto :err

if defined LOG_PATH (
	(echo IBController running %APP% %TWS_MAJOR_VRSN% has finished at %DATE% %TIME%
	echo.) >> "%LOG_FILE%"
)

color
exit

:err
set ERRORCODE=%ERRORLEVEL%
color 0C
echo +==============================================================================
echo +
echo +                       **** An error has occurred ****
echo +
echo + Error while: %PHASE%
echo +
echo + ERRORLEVEL = %ERRORCODE%
if defined LOG_FILE (
	if "%REDIRECTERROR%" == "1" (
		echo +
		echo + The diagnostics file mentioned above could not be accessed.
	) else (
		if not "%ERROR_MESSAGE%"=="" (
			echo +
			echo + Error: %ERROR_MESSAGE% 
			if not "%ERROR_MESSAGE1%"=="" (
				echo +        %ERROR_MESSAGE1%
			)
			if not "%ERROR_MESSAGE2%"=="" (
				echo +        %ERROR_MESSAGE2%
			)
		) 
		echo +
		echo + Please look in the diagnostics file mentioned above for further information
	)
)
echo +
echo +==============================================================================
echo +
echo + Press any key to close this window
pause > NUL
echo +
exit
