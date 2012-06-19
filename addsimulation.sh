#!/bin/bash
echo "Bash version ${BASH_VERSION}..."
for i in {5..12}
  do
     ./presage2-cli add -classname "uk.ac.imperial.evpool.EVPoolSimulation" -name "Testing " -P gridLoadFilename=gridLoad2.csv  -P cCount=100 -P timeStepHour=0.25 -P bC=16 -P mCPR=4 -P mCR=16  -P loadLevel=0.$i -P clusters=need_based -P seed=4 -finish 193
 done
 
 for i in {1..8}
  do
     ./presage2-cli run $i > output$i.txt
 done





