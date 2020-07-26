#!/bin/sh

chmod u+x Scripts/IBController.sh

tail -F /tws/**/ibgateway.log &

/usr/bin/xvfb-run --auto-servernum Scripts/IBController.sh 972 -g --tws-path=/tws --tws-settings-path=/tws --ibc-path=$PWD/target --ibc-ini=/ibc-config/IBController.ini --java-path=/opt/java/openjdk/bin/java 
