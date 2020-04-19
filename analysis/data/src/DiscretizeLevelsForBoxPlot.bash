#!/bin/bash
for filename in wrapped_final_*.dat
	do
		[ -e "$filename" ] || continue # If there isn't a matching filename, skip!
		#floor of value from column 2 used as discrete factor variable for boxplot; placed in column 2
		#value from column 5 used as continuous value to be plotted; placed in column 1
		awk '{printf("%s [%d,%d)\n"), $5, 10*$2, 10*$2+1;}' "$filename" > "discrete_$filename"
	done