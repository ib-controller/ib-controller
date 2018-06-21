#!/bin/sh

# this is a hopefully temporary hack!
apt-get update
apt-get -y install xvfb libxrender1 libxtst6

chmod u+x Scripts/IBController.sh

/usr/bin/xvfb-run --auto-servernum Scripts/IBController.sh 963 -g --tws-path=/tws --tws-settings-path=/tws --ibc-path=$PWD/target --ibc-ini=/ibc-config/IBController.ini