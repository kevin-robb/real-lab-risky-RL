import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Receives data about each agent for each generation, 
 * including final learning parameter value and
 * list of every choice made and reward received.
 * Runs simulation based on setup parameters.
 * 
 * Writes to tab-separated .txt file in desktop folder CodeOutput.
 * If running on different machine, change user in outputPathName
 * 
 * @author Kevin Robb
 * @version 6/20/2018
 * Referenced code from Steven Roberts.
 */
public class AnalysisDriver {

    /**keeps track of the current generation being evaluated. */
    static int currentGeneration = 0;

    public static void main(String[] args)
    {
        //File for writing is runModifier + "output_" + identifier + ".txt""
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmssSSS");
        String outputPathName = "";
        String outputPathIdentifier = sdf.format(now.getTime());
        outputPathName= "C:/Users/kevin/Desktop/CodeOutput/output_" + outputPathIdentifier + ".txt";

        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new FileWriter(new File(outputPathName)));
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        try
        {
            Setup.setConfig(args);
        }
        /*
         * setConfig() throws an IOException when the config file is not found.
         * This is really not that important to the successful execution of the
         * program, but is important to the meaning of the output. Should probably
         * tell someone when this happens.
         */
        catch (IOException e1)
        {
            if (e1.getMessage().equals("config.txt"))
            {
                System.out.println("The configuration file was not found."
                        + " Using default parameters.");
            }
            else System.out.println(e1.getMessage());
        }
        //parseStates() throws an Exception (different from the IOException caught above) when
        //the states for choices are not the proper format. This is unrecoverable, so the program quits.
        catch (Exception e1)
        {
            try
            {
                out.write("\n" + e1.getMessage());
            }
            catch (IOException e2)
            {
                //At this point, the program is about to quit,
                //so I don't care about the IOException.
            }
            System.out.println("\n" + e1.getMessage());
            System.exit(0);
        }

        //creates first generation and begin the program
        if (Setup.printSummary)
        	runSummary(out);
        else
        	runGraphing(out);
    }

    /**
     * Runs the simulation. At top of output file, displays all seed config values
     * followed by a blank line.
     * For each generation, outputs "Gen " + gen # on the first line
     * After the agent data, or on the next line if not a mod 10 generation,
     * outputs "Summary: " + avg propA, avg fitness, avg L value, min L, and max L
     * all from end of gen, separated by commas.
     * Every 10th generation, each agent has a line containing proportion of A choices, 
     * fitness, and final L value separated by commas on one line each.
     * The end of every generation is signified by a blank line in the output.
     * @param out the writer to which output should be sent
     */
    public static void runSummary(BufferedWriter out)
    {
        //fitness of an individual at the end of its life. used to rank outputs for each gen
        double fitnessAgent = 0;
        //total of all agent fitness in generation at end. used to find average fitness
        double fitnessTotal = 0;
        //the percent an agent's choices that were A during a time period
        //excluding safe exploration. range 0-1
        double propA = -1; 
        //total of all propA in gen. used to find average propA.
        double propATotal = 0;
        //the percent an agent's choices that were B during a time period
        //excluding safe exploration. range 0-1
        double propB = -1; 
        //total of all propB in gen. used to find average propB.
        double propBTotal = 0;
        //the percent an agent's choices that were C during a time period
        //excluding safe exploration. range 0-1
        double propC = -1; 
        //total of all propC in gen. used to find average propC.
        double propCTotal = 0;
        //the final learningParameter value of a single agent. Correlated
        //to propA. range 0-1
        double finalLAgent = -1;
        //the total of all L values in gen. used to compute average at end
        double LForGenTotal = 0;
        //the minimum learningParameter value among the entire gen, at the end
        double minLForGen = 2;
        //the maximum learningParameter value among the entire gen, at the end
        double maxLForGen = -1;
        //obtained from Generation.choices array. in form "A100", meaning
        //A was chosen and a reward of 100 was received.
        String choiceInfo="";
        //number of times an agent chose A during a lifetime. Used for propA calculation.
        int a = 0;
        //number of times an agent chose B during a lifetime. Used for propB calculation.
        int b = 0;
        //number of times an agent chose C during a lifetime. Used for propC calculation.
        int c = 0;
        //number of trials recorded. measured this way to avoid needing
        //to perform numberOfTrials - nurturingTrials
        int numTrials = 0;
        //variables to be used calculating stats about expected values for each option
        double minExpA, maxExpA, totExpA, minExpB, maxExpB, totExpB, minExpC, maxExpC, totExpC;

        try
        {
            //outputs config info and format info at top of file
            out.write(Setup.parameters() + String.format("%n") + "Format: \"Gen \"currentGeneration" 
                    + String.format("%n") + "\tpropA\tpropB\tpropC\tfitnessAgent\tfinalLAgent\t\texpectedA\texpectedB" + String.format("%n")
                    + "(previous line displays for every agent in a gen, but only every 10th gen)"
                    + String.format("%n") + "\"Summary: \"avgPropA\tavgPropB\tavgPropC\tavgFitness\tavgLForGen\tminLForGen\tmaxLForGen"
                    + String.format("%n") + String.format("%n"));
            //creates generation object. forms first gen of agents
            Generation g = new Generation();
            //displays actual values of each choice on next line
            out.write("Actual Vals:\t" + Setup.stateVals[0] + "-" + Setup.stateVals[1] 
            		+ "\t" + Setup.stateVals[2] + "-" + Setup.stateVals[3] 
                    + "\t" + Setup.stateVals[4] + "-" + Setup.stateVals[5] + String.format("%n"));
            
            while (currentGeneration < Setup.numberOfGens)
            {
            	//check if we are running a data dump sim or a standard summary sim.
            	//only data dump every 50th gen. run normal otherwise
            	if (Setup.printAgentTrialData && currentGeneration % 50 == 0) g.runGenerationPrint(currentGeneration);
            	else g.runGeneration();
            	
                //reset values for new gen
                minLForGen = Integer.MAX_VALUE;
                maxLForGen = Integer.MIN_VALUE;
                LForGenTotal = 0;
                propATotal = 0; propBTotal = 0; propCTotal = 0;
                fitnessTotal = 0;
                minExpA=1000;minExpB=1000;minExpC=1000;
                maxExpA=0;maxExpB=0;maxExpC=0;
                totExpA=0;totExpB=0;totExpC=0;
                //export data then clear it for next gen
                //calculate number of times each chosen, and final learning param of each agent

                out.write(String.format("%n") + "Gen " + currentGeneration + String.format("%n"));
                //only output specific agent data every 10th generation but need to calculate every time
                for (int agentNum = 0; agentNum < Setup.numberOfAgents; agentNum++)
                {
                    //part for finding min and max L of generation
                    if (g.allAgents[agentNum].getLearningParameter() > maxLForGen)
                        maxLForGen = g.allAgents[agentNum].getLearningParameter();
                    if (g.allAgents[agentNum].getLearningParameter() < minLForGen)
                        minLForGen = g.allAgents[agentNum].getLearningParameter();

                    //part for calculating propA, propB, propC
                    a = 0; b = 0; c = 0; numTrials = 0;
                    //if trialNum < Setup.nurturingTrials, don't include in fitness calc
                    for (int trialNum = Setup.nurturingTrials; trialNum < Setup.numberOfTrials; trialNum++)
                    {
                        choiceInfo = g.choices[agentNum][trialNum];
                        if 		(choiceInfo.charAt(0) == 'A') a++;
                        else if (choiceInfo.charAt(0) == 'B') b++;
                        else if (choiceInfo.charAt(0) == 'C') c++;
                        numTrials++;
                    }
                    propA = (double) a / numTrials; propB = (double) b / numTrials; propC = (double) c / numTrials;
                    finalLAgent = g.allAgents[agentNum].getLearningParameter();
                    fitnessAgent = g.allAgents[agentNum].getFitness();
                    propATotal += propA; propBTotal += propB; propCTotal += propC;
                    fitnessTotal += fitnessAgent;
                    LForGenTotal += finalLAgent;
                    //part for outputting specific agent data. only every 10th gen and only if config
                    if (currentGeneration % 10 == 0 && Setup.printAgentData)
                    {
                        //outputs all agent values at end of gen
                        out.write("\t" + String.format("%1.2f", propA) + "\t" 
                        		+ String.format("%1.2f", propB) + "\t" 
                        		+ String.format("%1.2f", propC) + "\t" 
                                + String.format("%.1f", fitnessAgent) + "\t" 
                                + String.format("%.5f", finalLAgent));
                        //shows expected rewards for A, B, and C
                        out.write("\t\t" + String.format("%8.5f", g.allAgents[agentNum].getExpectedRewards()[0]) 
                        + "\t" + String.format("%8.5f", g.allAgents[agentNum].getExpectedRewards()[1])
                        + "\t" + String.format("%8.5f", g.allAgents[agentNum].getExpectedRewards()[2]));
                        out.write(String.format("%n"));
                    }
                    //part for calculating summary stats for min/max/avg of all agents' expected vals in one gen
                    //find new mins
                    if (g.allAgents[agentNum].getExpectedRewards()[0] < minExpA)
                        minExpA = g.allAgents[agentNum].getExpectedRewards()[0];
                    if (g.allAgents[agentNum].getExpectedRewards()[1] < minExpB)
                        minExpB = g.allAgents[agentNum].getExpectedRewards()[1];
                    if (g.allAgents[agentNum].getExpectedRewards()[2] < minExpC)
                        minExpC = g.allAgents[agentNum].getExpectedRewards()[2];
                    //find new maxs
                    if (g.allAgents[agentNum].getExpectedRewards()[0] > maxExpA)
                        maxExpA = g.allAgents[agentNum].getExpectedRewards()[0];
                    if (g.allAgents[agentNum].getExpectedRewards()[1] > maxExpB)
                        maxExpB = g.allAgents[agentNum].getExpectedRewards()[1];
                    if (g.allAgents[agentNum].getExpectedRewards()[2] > maxExpC)
                        maxExpC = g.allAgents[agentNum].getExpectedRewards()[2];
                    //find totals for avg
                    totExpA += g.allAgents[agentNum].getExpectedRewards()[0];
                    totExpB += g.allAgents[agentNum].getExpectedRewards()[1];
                    totExpC += g.allAgents[agentNum].getExpectedRewards()[2];
                    
                    
                }
                //info written to gen summary lines, also for gens with agent data printed
                out.write("Summary: " + String.format("%.2f", propATotal/Setup.numberOfAgents) + "\t" 
                		+ String.format("%.2f", propBTotal/Setup.numberOfAgents) + "\t" 
                		+ String.format("%.2f", propCTotal/Setup.numberOfAgents) + "\t" 
                        + String.format("%.3f", fitnessTotal/Setup.numberOfAgents) + "\t"
                        + String.format("%.5f", LForGenTotal/Setup.numberOfAgents) + "\t" 
                        + String.format("%.5f", minLForGen) + "\t"
                        + String.format("%.5f", maxLForGen) 
                        //stats for expected A
                        + String.format("%n")
                        + String.format("A:\t%8.5f", minExpA) + "\t" 
                        + String.format("%8.5f", totExpA/Setup.numberOfAgents) + "\t" 
                        + String.format("%8.5f", maxExpA)
                        //stats for expected B
                        + String.format("%n")
                        + String.format("B:\t%8.5f", minExpB) + "\t" 
                        + String.format("%8.5f", totExpB/Setup.numberOfAgents) + "\t" 
                        + String.format("%8.5f", maxExpB)
                        //stats for expected C
                        + String.format("%n")
                        + String.format("C:\t%8.5f", minExpC) + "\t" 
                        + String.format("%8.5f", totExpC/Setup.numberOfAgents) + "\t" 
                        + String.format("%8.5f", maxExpC)
                        + String.format("%n"));

                //don't form new gen if currently last gen
                if (currentGeneration != Setup.numberOfGens - 1)
                    g.formNewGeneration();
                fitnessAgent = 0;
                currentGeneration++;
            }
            out.flush();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    
    /**
     * Runs the simulation. Writes data convenient for graphing.
     * If all 3 params false, prints currentGeneration and avg L value for each gen on separate lines.
     * If Setup.printAgentData = true, prints min L 1st Q L, avg L, 
     * 3rd Q, and max L on one line.
     * If Setup.printFitness = true, prints pairs of agent L with 
     * fitness for every agent in every 50th gen.
     * If Setup.printPropAvsExpB = true, prints pairs of propA with 
     * end-of-life expected value of B for each agent in every 50th gen.
     * Gen groups of agents in the previous 2 cases are separated by a blank line.
     * @param out the writer to which output should be sent
     */
    public static void runGraphing(BufferedWriter out)
    {
        //the total of all L values in gen. used to compute average at end
        double LForGenTotal = 0;
        //the minimum learningParameter value among the entire gen, at the end
        double minLForGen = 0;
        //the maximum learningParameter value among the entire gen, at the end
        double maxLForGen = 0;
        //index of first quartile
        int firstQindex = Setup.numberOfAgents / 4;
        //index of third quartile
        int thirdQindex = Setup.numberOfAgents * 3 / 4;
        //L value at first quartile
        double LfirstQ = 0;
        //L value at third quartile
        double LthirdQ = 0;
        //stores L values and will be sorted to find quartiles, min and max
        double[] Lvalues = new double[Setup.numberOfAgents];
        //the percent an agent's choices that were A during a time period
        //excluding safe exploration. range 0-1
        double propA = -1; 
        //the percent an agent's choices that were B during a time period
        //excluding safe exploration. range 0-1
        double propB = -1; 
        //the percent an agent's choices that were C during a time period
        //excluding safe exploration. range 0-1
        double propC = -1; 
        //obtained from Generation.choices array. in form "A100", meaning
        //A was chosen and a reward of 100 was received.
        String choiceInfo="";
        //number of times an agent chose A during a lifetime. Used for propA calculation.
        int a = 0;
        //number of times an agent chose B during a lifetime. Used for propB calculation.
        int b = 0;
        //number of times an agent chose C during a lifetime. Used for propC calculation.
        int c = 0;
        //number of trials recorded. measured this way to avoid needing
        //to perform numberOfTrials - nurturingTrials
        int numTrials = 0;
        try
        {
            //creates generation object. forms first gen of agents
            Generation g = new Generation();
            
            while (currentGeneration < Setup.numberOfGens)
            {
            	//check if we are running a data dump sim or a standard graphing sim.
            	//only data dump every 50th gen, runs normal otherwise
            	if (Setup.printAgentTrialData && currentGeneration % 50 == 0) g.runGenerationPrint(currentGeneration);
            	else g.runGeneration();
            	
                //reset values for new gen
                //minLForGen = Integer.MAX_VALUE;
                //maxLForGen = Integer.MIN_VALUE;
                LForGenTotal = 0;
                //calculate average L for the generation
                for (int agentNum = 0; agentNum < Setup.numberOfAgents; agentNum++)
                {
                	if (Setup.printAgentData && !Setup.printFitness && !Setup.printPropAvsExpB)
                	{ /*
                		//part for finding min, max, q1, q2 for gen
                        if (g.allAgents[agentNum].getLearningParameter() > maxLForGen)
                            maxLForGen = g.allAgents[agentNum].getLearningParameter();
                        if (g.allAgents[agentNum].getLearningParameter() < minLForGen)
                            minLForGen = g.allAgents[agentNum].getLearningParameter();
                        */
                        Lvalues[agentNum] = g.allAgents[agentNum].getLearningParameter();
                	}
                    LForGenTotal += g.allAgents[agentNum].getLearningParameter();
                    
                    if (Setup.printPropAvsExpB && !Setup.printFitness && !Setup.printAgentData && currentGeneration % 50 == 0) {
                    	//writes propA (x) and expected val of B (y)
                    	
                    	//part for calculating propA, propB, and propC
	                    a = 0; b = 0; c = 0; numTrials = 0;
	                    //if trialNum < Setup.nurturingTrials, don't include in fitness calc
	                    for (int trialNum = Setup.nurturingTrials; trialNum < Setup.numberOfTrials; trialNum++)
	                    {
	                        choiceInfo = g.choices[agentNum][trialNum];
	                        if 		(choiceInfo.charAt(0) == 'A') a++;
	                        else if (choiceInfo.charAt(0) == 'B') b++;
	                        else if (choiceInfo.charAt(0) == 'C') c++;
	                        numTrials++;
	                    }
	                    propA = (double) a / numTrials; propB = (double) b / numTrials; propC = (double) c / numTrials;
	                    //adjust comments in next write line if desired independent var is prop besides A. 
	                    out.write(propA /*+ "\t" + propB + "\t" + propC */ + "\t" 
	                    		+ String.format("%.5f", g.allAgents[agentNum].getExpectedRewards()[1]) + String.format("%n"));
	                    //output needs to have only 2 vars per line so gnuplot has 1 ind and 1 dep var to graph
                    }
                    
                    
                    if (!Setup.printPropAvsExpB && Setup.printFitness && currentGeneration % 50 == 0) { 
                    	//writes L (x) and fitness (y) of every individual, every 50 gens
                    	out.write(String.format("%.5f", g.allAgents[agentNum].getLearningParameter()) + "\t" 
                    			+ String.format("%.3f", g.allAgents[agentNum].getFitness()) + String.format("%n"));
                    }
                }
                if (Setup.printAgentData && !Setup.printPropAvsExpB && !Setup.printFitness)
                {
                    Arrays.sort(Lvalues);
                    
                    minLForGen = Lvalues[0];
                    maxLForGen = Lvalues[Setup.numberOfAgents - 1];
                    LfirstQ = Lvalues[firstQindex];
                    LthirdQ = Lvalues[thirdQindex];
                    
                	out.write(currentGeneration + "\t"+ String.format("%.5f", minLForGen) + "\t" 
                			+ String.format("%.5f", LfirstQ) + "\t" 
                			+ String.format("%.5f", LForGenTotal/Setup.numberOfAgents) + "\t" 
                			+ String.format("%.5f", LthirdQ) + "\t" 
                            + String.format("%.5f", maxLForGen) + String.format("%n"));
                } else if (!Setup.printPropAvsExpB && !Setup.printFitness) { //all 4 config vars are false
                	//use generation as x and avgLForGen as y
                	out.write(currentGeneration + "\t" + String.format("%.5f", LForGenTotal/Setup.numberOfAgents)
                		+ String.format("%n"));
                } else if ((Setup.printPropAvsExpB || Setup.printFitness) && currentGeneration % 50 == 0) { 
                	//must be printing L(x) vs fitness (y) or propA (x) vs expB (y),
                	// so insert blank line between sets of agents
                	out.write(String.format("%n"));
                }
                
                //don't form new gen if currently last gen
                if (currentGeneration != Setup.numberOfGens - 1)
                    g.formNewGeneration();
                currentGeneration++;
            }
            out.flush();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
