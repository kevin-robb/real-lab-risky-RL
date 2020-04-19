# Gnuplot command file
# Designed for Windows O/S; to use with other O/S, modify to adjust terminal type as necessary.
# If you invoke this script by passing in a single parameter with the value "pdf" (no quotes), pdf files will be created instead.
set yrange [0:220] #220 is max fitness possible when B has max reward of 220. raise accordingly
set xrange [0:200] #default is 200, represents number of trials

set title
if (ARG1 eq "pdf") {
	set terminal pdfcairo font ",6"# for making pdf files of plots
} else {
	window_counter=0 # for display on Windows O/S
}

do for [base_name in "nurt_Gen nonnurt_Gen"] {
	do for [i=0:450:50] { # should be 250
		input_file=base_name.i.'.dat'
		do for [j=0:9] { # should be 9
			learning_rate_col=j*8+2
			fitness_col=j*8+5
			estA_col=j*8+6
			estB_col=j*8+7
			estC_col=j*8+8
		
			# This is kind of a cludge to get the learning rate and final fitness into variables.
			# It requires UNIX-like utilities "head," "tail," and "awk" in your path. 
			learning_rate=system("head -1 ".input_file." | awk '{print $".learning_rate_col."}'")
			fitness=system("tail -1 ".input_file." | awk '{print $".fitness_col."}'")
					
			if (ARG1 eq "pdf") {
				set output base_name.i."Agent".(j*5).".pdf"
			} else {
				window_counter=window_counter+1
				set terminal win window_counter #for display on Windows O/S
			}

			if (base_name eq "nurt_Gen") {
				base_title="Nurturing Generation: "
			} else {
				base_title="Non-nurturing Generation: "
			}		
			
			set title base_title.i."     Agent: ".(j*5)."     Learning Rate: ".learning_rate."     Fitness: ".fitness
			plot input_file using fitness_col with lines title "fitness", \
			'' using estA_col with lines title "estimate A", \
			'' using estB_col with lines title "estimate B", \
			'' using estC_col with lines title "estimate C"
		}
	}
}
set output  # closes last open file, if any