::   This command file starts the IB Gateway, which provides a low-resource capability
::   for running TWS API programs without the complex TWS user interface.

::   If your TWS user id and password are not included in the IBController 
::   configuration file, set them here (do not encrypt the password):

set TWSUSERID=
set TWSPASSWORD=


::   The folder containing the IBController files:

set IBCDIR=C:\IBController


::   The location and filename of the IBController configuration file. This file should
::   be in a folder in your personal filestore, so that other users of your computer can't
::   access it. This folder and its contents should also be encrypted so that even users
::   with administrator privileges can't see the contents. Note that you can use the HOMEPATH
::   environment variable to address the root of your personal filestore (HOMEPATH is set
::   automatically by Windows):

set IBCINI="%HOMEPATH%\Documents\IBController\IBController.ini"


::   The folder where TWS is installed:

set TWSDIR=C:\Jts\


::   The classpath for the IB Gateway. The value below is correct for version
::   952 (you can verify which version of the Gateway you are using by going
::   to the Help | About IB Gateway menu in the Gateway UI).
::
::   For other versions of the IB Gateway, the information needed may be different.
::   First, look in the folder where TWS is installed (typically C:\Jts).
::   If this folder contains a file called StartIBGateway.bat, open the file with
::   a text editor and look at the line beginning with START. Select 
::   everything after "-cp " up to the first subsequent space character,
::   then press Ctrl-C to copy it to the clipboard, then paste it into the
::   "set TWSCP=" command below, replacing everything after the "=" character.
::
::   If the StartIBGateway.bat file doesn't exist, you'll need to look in  the 
::   Gateway shortcut created when you installed TWS. How to find this depends on 
::   which version of Windows you're using:
::
::   - Windows 7: right click on the start menu entry for the IB Gateway and
::     click 'Properties'. 
::
::   - Windows 8: locate the tile for the IB Gateway in the start screen (or
::     the 'all apps' screen), right click it, and select 'Open file location'
::     on the menu bar at the bottom of the screen. Then right click on the
::     highlighted entry in the File Explorer window, and click 'Properties'.
::
::   - Windows 8.1: locate the tile for the IB Gateway in the start screen (or
::     the 'all apps' screen), right click it, and select 'Open file location'
::     in the context menu. Then right click on the highlighted entry in the
::     File Explorer window, and click 'Properties'.
::
::   - Windows 10: locate the tile for the IB Gateway in the start menu (or the 
::     'all apps' list), right click it, and select 'Open file location'
::     in the context menu. Then right click on the highlighted entry in
::     the File Explorer window, and click 'Properties'.
::
::   In the field labelled 'Target', select everything after "-cp " up to the 
::   first subsequent space character, then press Ctrl-C to copy it to the 
::   clipboard, then paste it into the "set TWSCP=" command below, replacing
::   everything after the "=" character:

set TWSCP=jts.jar;total.2015b.jar


::   Other Java VM options for the IB Gateway. You can find this information in
::   the START line in StartIBGateway.bat, or in the properties for the IB Gateway
::   shortcut created when you installed TWS if StartIBGateway.bat doesn't exist.
::   Note that jclient/LoginFrame is NOT part of the Java options, nor is anything
::   that comes after it, so don't include that here):

set JAVAOPTS=-Dsun.java2d.noddraw=true -Dswing.boldMetal=false -Dsun.locale.formatasdefault=true -Xmx768M -XX:MaxPermSize=128M


pushd %TWSDIR%

:: prevent other Java tools interfering with IBController
setlocal
set JAVA_TOOL_OPTIONS=

%SystemDrive%\ProgramData\Oracle\Java\javapath\java.exe -cp  %TWSCP%;%IBCDIR%\IBController.jar %JAVAOPTS% ibcontroller.IBGatewayController %IBCINI% %TWSUSERID% %TWSPASSWORD%
popd
