#!/bin/bash
echo "Bash version ${BASH_VERSION}..."
for i in {20..30}
  do
     ./presage2-cli add -classname "uk.ac.imperial.evpool.EvSimulation" -name "Testing$i " -P gridLoadFilename=gridLoad.csv  -P agentCount=100 -P timeStepHour=0.25 -P bC=16 -P mCPR=4 -P mCR=16  -P loadLevel=0.$i -P allocM=random -P seed=4 -finish 193 -P minSOC=0.2 -P maxSOC=0.9 -P usageSteepness=0
 done

