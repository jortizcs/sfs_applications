#!/bin/bash

while true; do 

    sleep 2400 #sleep for 40 minutes
    date
    sudo monit restart sfs
    echo "restarting sfs"
done
