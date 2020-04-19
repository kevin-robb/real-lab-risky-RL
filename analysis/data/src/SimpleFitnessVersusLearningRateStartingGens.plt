# Gnuplot command file
# Designed for Windows O/S; to use with other O/S, modify to adjust terminal type as necessary.
# If you invoke this script by passing in a single parameter with the value "pdf" (no quotes), pdf files will be created instead.
set xrange [0:1]
set yrange [80:120]

set title
set key horizontal bottom

if (ARG1 eq "pdf") {
	set terminal pdfcairo size 10,6 font ",12"# for making pdf files of plots
} else {
	window_counter=0 # for display on Windows O/S
}

base_name = "wrapped_final_"
do for [treatment_case in "nurt nonnurt"] {

		if (ARG1 eq "pdf") {
			set output "FitnessVersusLearningRate_".treatment_case.".pdf"
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
		
		
		# Because the syntax for gnuplot is an ugly cludge, our code is too.
		filename(n) = sprintf(base_name.treatment_case."_Gen%d.dat", n)
		# For iteration, we need to make a bunch of redundant functions with different names, including for the variables.
		fstr(n) = sprintf("f%d(x) = m%d*x + b%d", n, n, n)
		fitstr(n) = sprintf("fit f%d(x) filename(%d*5) using 2:5 via m%d,b%d", n, n, n, n) 
		
		file_counter = 10
		
		do for [i=1:file_counter] {
			eval(fstr(i))
			eval(fitstr(i))
		}
		
		stats filename(0) using 5 nooutput
		set style data points
		
		f0(x) = 0*x + STATS_mean 
		plotstr = "plot f0(x) title '', filename(0) using 2:5 linestyle 1 title 'Gen 0', "
		do for [i=1:file_counter] {
			plotstr = plotstr.sprintf("f%d(x) linestyle %d+1 title '', filename(%d*5) using 2:5 linestyle %d+1 title 'Gen %d'%s", i, i, i, i, i*5, (i == file_counter) ? "" : ", ")
		}
		
		print plotstr
		
		eval(plotstr)
}
set output  # closes last open file, if any