#!/bin/bash

# get the IBController version
read IBC_VRSN < "${IBC_PATH}/version"

if [[ -n "$LOG_PATH" ]]; then
	if [[ ! -e  "$LOG_PATH" ]]; then
		mkdir -p "$LOG_PATH"
	fi
	
	readme=${LOG_PATH}/README.txt
	if [[ ! -e  "$readme" ]]; then
		echo You can delete the files in this folder at any time > "$readme"
		echo >> "$readme"
		echo "You'll be informed if a file is currently in use." >> "$readme"
	fi

	log_file=${LOG_PATH}/ibc-${IBC_VRSN}_${APP}-${TWS_MAJOR_VRSN}_$(date +%A).txt
	if [[ -e "$log_file" ]]; then
		if [[ $(uname) = [dD]arwin* ]]; then
			if [[ $(stat -f "%Sm" -t %D "$log_file") != $(date +%D) ]]; then rm "$log_file"; fi
		else
			if [[ $(date -r "$log_file" +%D) != $(date +%D) ]]; then rm "$log_file"; fi
		fi
	fi
else
	log_file=/dev/null
fi

#   now launch IBController

normal='\033[0m'
light_red='\033[1;31m'
light_green='\033[1;32m'
echo -e ${light_green}
echo "+=============================================================================="
echo "+"
echo -e "+ IBController version ${IBC_VRSN}"
echo "+"
echo -e "+ Running ${APP} ${TWS_MAJOR_VRSN}"
echo "+"
if [[ -n "$LOG_PATH" ]]; then
	echo "+ Diagnostic information is logged in:"
	echo "+"
	echo -e "+ ${log_file}"
	echo "+"
fi
echo "+"

if [[ "$(echo ${APP} | tr '[:lower:]' '[:upper:]')" = "GATEWAY" ]]; then 
	gw_flag=-g
fi

export IBC_VRSN
"${IBC_PATH}/Scripts/IBController.sh" "${TWS_MAJOR_VRSN}" ${gw_flag} \
     "--tws-path=${TWS_PATH}" "--tws-settings-path=${TWS_CONFIG_PATH}" \
	 "--ibc-path=${IBC_PATH}" "--ibc-ini=${IBC_INI}" \
     "--user=${TWSUSERID}" "--pw=${TWSPASSWORD}" "--fix-user=${FIXUSERID}" "--fix-pw=${FIXPASSWORD}" \
     "--java-path=${JAVA_PATH}" "--mode=${TRADING_MODE}" \
     >> "${log_file}" 2>&1

if [ "$?" != "0" ]; then
	echo -e ${light_red}
	echo "+=============================================================================="
	echo "+"
	echo -e "+                       **** An error has occurred ****"
	if [[ -n LOG_PATH ]]; then
		echo "+"
		echo "+                     Please look in the diagnostics file "
		echo "+                   mentioned above for further information"
	fi
else
	echo -e "+ ${APP} ${TWS_MAJOR_VRSN} has finished"
fi

echo "+"
echo "+=============================================================================="
echo -e ${normal}

exit
