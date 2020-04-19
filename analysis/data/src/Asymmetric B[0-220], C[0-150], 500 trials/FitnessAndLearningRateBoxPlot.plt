# Gnuplot command file
# Designed for Windows O/S; to use with other O/S, modify to adjust terminal type as necessary.
# If you invoke this script by passing in a single parameter with the value "pdf" (no quotes), pdf files will be created instead.
set xrange [0:1]
set yrange [80:120]

set title
set key bottom

if (ARG1 eq "pdf") {
	set terminal pdfcairo size 10,6 font ",12"# for making pdf files of plots
} else {
	window_counter=0 # for display on Windows O/S
}

base_name = "wrapped_final_"
do for [treatment_case in "nurt nonnurt"] {

		if (ARG1 eq "pdf") {
			set output "FitnessVersusLearningRateBoxPlot_".treatment_case.".pdf"
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
		prefix(n) = sprintf("F%d", n)
		#x_mean(n) = sprintf("F%d_mean_x", n)
		# For iteration, we need to make a bunch of redundant functions with different names, including for the variables.
		#fstr(n) = sprintf("f%d(x) = m%d*x + b%d", n, n, n)
		#fitstr(n) = sprintf("fit f%d(x) filename(%d*50) using 2:5 via m%d,b%d", n, n, n, n) 
		
		num_files = 10
		
		array x_mean[num_files]
		do for [i=0:num_files-1] {
		#	eval(fstr(i))
		#	eval(fitstr(i))
			stats filename(i*50) using 2:5 #name prefix(i) nooutput
			x_mean[i+1]=STATS_mean_x
		}
				
		set style data boxplot
		set boxwidth 0.01
		
		#f0(x) = 0*x + STATS_mean 
		plotstr = "plot "
		do for [i=0:num_files-1] {
			plotstr = plotstr.sprintf("filename(%d*50) using (x_mean[%d+1]):5 linestyle 3*%d+1 title 'Gen %d'%s", i, i, i, i*50, (i == num_files) ? "" : ", ")
		}
		
		print plotstr
		
		eval(plotstr)
}
set output  # closes last open file, if any