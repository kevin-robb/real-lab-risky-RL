#!/bin/bash
for filename in final*.dat
	do
		[ -e "$filename" ] || continue # If there isn't a matching filename, skip!
		sed 's/\t/\n/g' "$filename" > "wrapped_$filename"
	done

