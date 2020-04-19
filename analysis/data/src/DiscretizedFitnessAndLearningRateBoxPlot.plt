# Gnuplot command file
# Designed for Windows O/S; to use with other O/S, modify to adjust terminal type as necessary.
# If you invoke this script by passing in a single parameter with the value "pdf" (no quotes), pdf files will be created instead.
set xrange [0:10]
set yrange [80:120]

set title
unset key

if (ARG1 eq "pdf") {
	set terminal pdfcairo size 10,6 font ",12"# for making pdf files of plots
} else {
	window_counter=0 # for display on Windows O/S
}

base_name = "all_discrete_wrapped_final_"
do for [treatment_case in "nurt nonnurt"] {

	if (ARG1 eq "pdf") {
		set output "DiscreteFitnessVersusLearningRateBoxPlot_".treatment_case.".pdf"
	} else {
		window_counter = window_counter+1
		set terminal win window_counter #for display on Windows O/S
	}
			
	if (treatment_case eq "nurt") {
		base_title = "Nurturing"
	} else {
		base_title = "Non-nurturing"
	}		
	set title base_title
			
	set style data boxplot
	set style boxplot sorted
	unset boxwidth
	
	plot base_name.treatment_case.".dat" using (0.5):1:(0):2
}
set output  # closes last open file, if any