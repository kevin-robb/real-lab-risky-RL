import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

/**
 * Performs all trials for one lifetime.
 * Uses list of Agents from previous generation to create new
 * set of Agents for next generation.
 * Extracts learning parameter values as well as set of
 * choices and passes to AnalysisDriver.
 * 
 * @author Kevin Robb
 * @version 6/20/2018
 * Referenced code from Steven Roberts.
 */
public class Generation {
	/**current list of agents in this generation */
	Agent[] allAgents;
	
	/**stores choices with rewards for each agent in each trial for this generation.
	 * each row is one agent and each entry is a trial. */
	String[][] choices;
	
	
	/** creates list and agents for first generation*/
	public Generation()
	{
	    this.allAgents = new Agent[Setup.numberOfAgents];
	    choices = new String[Setup.numberOfAgents][Setup.numberOfTrials];
	    
		for (int i = 0; i < Setup.numberOfAgents; i++)
		{
			allAgents[i] = new Agent();
		}
	}
	
	/** returns current list of agents. */
	public Agent[] getAgentList()
	{
		return allAgents;
	}
	
	/**
	 * Iterate through all agents and perform choice for one trial.
	 * Store choice and result in choices array.
	 * Trial class will update agent's learning param, expected reward for each option,
	 * and aggregate fitness as choices are made.
	 * @param trialNum which trial in the generation this is.
	 */
	public void runTrial(int trialNum)
	{
		double L = -1;
		double currentExpectedVal = -1;
		double newFitness = 0;
		int selectedCell = -1;
		for(int agentNum = 0; agentNum < Setup.numberOfAgents; agentNum++)
		{
			//makes choice for every agent and gets reward
			choices[agentNum][trialNum] = Trial.makeChoice(allAgents[agentNum]);
			L = allAgents[agentNum].getLearningParameter();
			//gets reward value from choice
			newFitness = Integer.parseInt(
					choices[agentNum][trialNum].substring(1, choices[agentNum][trialNum].length()));
			//if A selected, sets to 0. if B selected, sets to 1. if C selected, sets to 2
			selectedCell = choices[agentNum][trialNum].charAt(0) - 65;
			//update expected rewards for option chosen
			currentExpectedVal = allAgents[agentNum].getExpectedRewards()[selectedCell];
			allAgents[agentNum].setExpectedRewards(selectedCell, (1 - L) * currentExpectedVal + L * newFitness);
			
			//updates aggregate fitness
			if (trialNum >= Setup.nurturingTrials)
			{
				//accounts for nurturing case and adds fitness to aggregate total
				allAgents[agentNum].addFitness(newFitness / (Setup.numberOfTrials - Setup.nurturingTrials));
			}
		}
	}
	
	/**
	 * Runs all trials for the current generation. 
	 */
	public void runGeneration()
	{
		//calls runTrial in a loop to run through each agent for every trial	
		for (int trialNum = 0; trialNum < Setup.numberOfTrials; trialNum++)
		{
			//runs all trials
			runTrial(trialNum);
		}
	}
	
	/**
	 * Creates new generation based on the previous generation.
	 * Creates tournament bracket by randomly selecting a certain number
	 * of agents at a time and copying the one of highest fitness into a new
	 * array. Individuals can be selected more than once.
	 * Next, an individual's learning parameter undergoes mutation via the addition
	 * of a value selected from a normal distribution with mean 0 and std dev 0.1. 
	 * The choices array is cleared, and allAgents is filled with the new agents.
	 */
	public void formNewGeneration()
	{
		//contains agents selected to move on to next generation. can be duplicates
		Agent[] chosenAgents = new Agent[Setup.numberOfAgents];
		//contains selected agents after undergoing mutation of L
		Agent[] newAgents = new Agent[Setup.numberOfAgents];
		//temporarily contains agents selected for each tournament
		Agent[] tournament = new Agent[Setup.tournamentSize];
		
		//select agents to move on to mutation phase and fill chosenAgents
		int rand = 0;
		double highestFitness;
		int agentWithHighestFitness;
		for (int i = 0; i < Setup.numberOfAgents; i++)
		{
			for (int t = 0; t < Setup.tournamentSize; t++)
			{
				rand = (int) Math.floor(Math.random() * Setup.numberOfAgents);
				tournament[t] = this.allAgents[rand];
			}
			highestFitness = -1;
			agentWithHighestFitness = -1;
			//probability of winning tournament based on fitness
			for (int f = 0; f < Setup.tournamentSize; f++)
			{
				//picks agent with highest fitness out of those selected for tournament
				if (tournament[f].getFitness() > highestFitness)
				{
					highestFitness = tournament[f].getFitness();
					agentWithHighestFitness = f;
				}
			}
			
			chosenAgents[i] = tournament[agentWithHighestFitness];
		}
		
		//final learning parameter of old agent
		double L = -1;
		//mutation value to be added to L for new agent. will have mean 0 and std dev specified in setup
		double M = 0;
		//created just to be able to invoke nextGaussian in a non-static context
		Random r = new Random();
		//generate next generation of agents
		for (int i = 0; i < Setup.numberOfAgents; i++)
		{
			L = chosenAgents[i].getLearningParameter();
			M = r.nextGaussian() * Setup.mutationStdDev; //selects random w/ mean 0 and std dev 1, so mult by std dev
			if (L + M < 0 || L + M > 1)
			    newAgents[i] = new Agent(L); 
			else //make sure L value doesn't go outside of range
			    newAgents[i] = new Agent(L + M);
			//if we want agents to carry over expected reward values, copy agent and 
			//set new L here instead of creating new agent
		}
		//replace old generation with new generation of agents
		this.allAgents = newAgents;
		
		//clears choices array back to default blank values. not necessary but prevents error masking
		this.choices = new String[Setup.numberOfAgents][Setup.numberOfTrials];
	}
//****************************************************************************************************************************************************************************************************
//everything below this is new 6/20/2018 for writing data from agents at each trial
	/**
	 * Creates a BufferedWriter to be used for writing specific agent/trial data to a file
	 * @return BufferedWriter output
	 */
	public BufferedWriter setupWriter(int currentGeneration)
	{
		Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmssSSS");
        String outputPathName = "";
        String outputPathIdentifier = sdf.format(now.getTime());
        outputPathName= "C:/Users/kevin/Desktop/CodeOutput/TrialOutputs/output_" + outputPathIdentifier + "_Gen" + currentGeneration + ".txt";

        BufferedWriter output = null;
        try
        {
            output = new BufferedWriter(new FileWriter(new File(outputPathName)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return output;
	}
	
	/**
	 * Runs all trials for the current generation and writes data to a new file. 
	 */
	public void runGenerationPrint(int currentGeneration)
	{
		BufferedWriter output = this.setupWriter(currentGeneration);
		try {
			output.write("each row consists of groups of data for each agent, "
				+ "in form:\tagentNum(learningParameter):choice,fitness,[expA,expB,expC]" 
				+ "where choice has letter chosen and reward received" + String.format("%n")
				+ "each column consists of every trial for the same agent." + String.format("%n")
				+ "this entire file is from Generation " + currentGeneration + String.format("%n") + String.format("%n"));
			//calls runTrial and runTrialPrint in a loop to run through each agent for every trial	
			for (int trialNum = 0; trialNum < Setup.numberOfTrials; trialNum++)
			{
				//runs all trials. only data dump every 50th gen (this method should only be called every 50th gen)
				runTrialPrint(trialNum, output);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Iterate through all agents and perform choice for one trial.
	 * Store choice and result in choices array.
	 * Trial class will update agent's learning param, expected reward for each option,
	 * and aggregate fitness as choices are made.
	 * 
	 * Same as normal runTrial, but writes data after choices have been made for all agents.
	 * @param trialNum which trial in the generation this is.
	 */
	public void runTrialPrint(int trialNum, BufferedWriter output) throws IOException
	{
		double L = -1;
		double currentExpectedVal = -1;
		double newFitness = 0;
		int selectedCell = -1;
		String dataGroup = "";
		for(int agentNum = 0; agentNum < Setup.numberOfAgents; agentNum++)
		{
			//makes choice for every agent and gets reward
			choices[agentNum][trialNum] = Trial.makeChoice(allAgents[agentNum]);
			L = allAgents[agentNum].getLearningParameter();
			//gets reward value from choice
			newFitness = Integer.parseInt(
					choices[agentNum][trialNum].substring(1, choices[agentNum][trialNum].length()));
			//if A selected, sets to 0. if B selected, sets to 1. if C selected, sets to 2
			selectedCell = choices[agentNum][trialNum].charAt(0) - 65;
			//update expected rewards for option chosen
			currentExpectedVal = allAgents[agentNum].getExpectedRewards()[selectedCell];
			allAgents[agentNum].setExpectedRewards(selectedCell, (1 - L) * currentExpectedVal + L * newFitness);
			
			//updates aggregate fitness
			if (trialNum >= Setup.nurturingTrials)
			{
				//accounts for nurturing case and adds fitness to aggregate total
				allAgents[agentNum].addFitness(newFitness / (Setup.numberOfTrials - Setup.nurturingTrials));
			}
			
			//write data to file (temporarily only write some agents to improve readability of output)
			if (agentNum % 5 == 0) {
				dataGroup = agentNum + "(" + allAgents[agentNum].getLearningParameter() + "):" + choices[agentNum][trialNum] + ","
						+ String.format("%06.2f",allAgents[agentNum].getFitness()) + ",[" 
						+ String.format("%06.2f", allAgents[agentNum].getExpectedRewards()[0]) 
						+ "," + String.format("%06.2f", allAgents[agentNum].getExpectedRewards()[1])
						+ "," + String.format("%06.2f", allAgents[agentNum].getExpectedRewards()[2]) + "]";
				
				
				output.write(dataGroup + "\t");
			}
		}
		//carriage return after each trial
		output.write(String.format("%n"));
	}

}
