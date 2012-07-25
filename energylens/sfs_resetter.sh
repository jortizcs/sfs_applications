#!/bin/bash

while true; do 

    sleep 2400 #sleep for 40 minutes
    date
    sudo monit stop acmedriver
    echo "acmedriver stopped"
    sleep 20
    sudo monit restart sfs
    echo "restarting sfs"
    sleep 20
    sudo monit start acmedriver
    echo "starting acmedriver 
    "
done
