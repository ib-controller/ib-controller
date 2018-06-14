#!/bin/sh

apt-get install xvfb

chmod u+x Scripts/IBController.sh

/usr/sbin/xvfb-run --auto-servernum Scripts/IBController.sh 963 -g --tws-path=/tws --tws-settings-path=/tws --ibc-path=$PWD/target --ibc-ini=/ibc-config/IBController.ini