#!/bin/bash

#usage ./run.sh outputfile radius dimension numdivers numiterations

rm $1
fname="playerClasses.txt"

echo "board || playerclass || radius || dimension || divers ||" >> $1

for (( c=1; c<=$5; c++ ))
do
for board in boards/*
do
  
  while read LINE
	do
  		echo c
  		java -cp log4j-1.2.15.jar:bin/ isnork.sim.GameEngine text "$board" "$LINE" $2 $3 $4 0 >> $1
		done <$fname
  		java -cp log4j-1.2.15.jar:bin/ isnork.sim.GameEngine text "$board" "$LINE" $2 $3 $4 0 >> $1
  		
done
done

echo "Program Finished"
cat output.txt
   



