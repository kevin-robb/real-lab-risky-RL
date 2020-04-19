#!/bin/bash
for filename in *.dat
	do
		[ -e "$filename" ] || continue # If there isn't a matching filename, skip!
		tail -1 "$filename" > "final_$filename"
	done