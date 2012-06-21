#!/bin/bash
 
 for i in {131..147}
  do
     ./presage2-cli run $i > ./out/output$i.txt
 done


