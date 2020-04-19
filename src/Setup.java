import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
/**
 * Extracts default values for variables from config file.
 * 
 * @author Kevin Robb
 * @version 6/20/2018
 * Referenced code from Steven Roberts.
 */
public class Setup {

	/**
	 * the weight that an individual gives to recent experiences
	 * compared to its cumulative experience. Ranges from 0 to 1.
	 * 0 = complete risk neutrality, where risk and reward are 
	 * completely ignored.
	 * 1 = complete risk aversion, where only the most recent trial
	 * is considered, and there is no deviation from a safe option.
	 */
	static double learningParameter = .5;
	/**the standard deviation of the mutation value used in
	 * new generation formation */
	static double mutationStdDev = 0.1;
	/**the number of individuals in each generation*/
	static int numberOfAgents = 30;
	/**the number of trials per lifetime*/
	static int numberOfTrials = 100;
	/**
	 * the number of trials which are the safe exploration period
	 * not included in final fitness. starts from trial 1.
	 * value of 0 means no nurturing.
	 */
	static int nurturingTrials = 75;
	/**the number of generations to run*/
	static int numberOfGens = 300;
	
	/**
	 * the number of agents to be picked for each tournament selection.
	 * a higher number decreases variability by increasing selection
	 * chance for higher-fitness agents. 
	 */
	static int tournamentSize = 5;
	
	/**denotes possible reward values of each choice. Each 50/50 chance.
	 * First set is choice A, second is choice B, third is choice C.
	 */
	static String stateInfo = "100-100,0-220,0-180";
	
	/**stores min and max val for each choice in form
	 * min A, max A, min B, max B, min C, max C */
	static int[] stateVals;
	
	/**weight used in boltzmann choice selection algorithm */
	static double temperature = 20;
	/**any information about the trial to be included with output name. 
	 * here, usually "nurturing" or "non-nurturing". */
	static String runInfo = "";
	
	// the rest of the variables are just for setting which type of run
	// and what data to output for graphing
	/**specifies whether to print specific agent data. */
	static boolean printAgentData;
	/**specifies whether to print only L data. false will print regular summary. */
	static boolean printSummary;
	/**specifies whether to print fitness vs L for each agent. */
	static boolean printFitness;
	/**specifies whether to print data for graphing propA vs expected value of B. */
	static boolean printPropAvsExpB;
	/**specifies whether to data dump into separate file from Generation class during simulation. */
	static boolean printAgentTrialData;
	
	/**
	 * sets environment parameters and variables from config file.
	 * Any variables not provided will take on default states.
	 * @param args the full list of arguments passed to main
	 * @throws IOException when fails to find "config.txt"
	 */
	public static void setConfig(String[] args) throws IOException
	{
		List<String> lines = null;
		Path config = Paths.get("config.txt");
		lines = Files.readAllLines(config);
		
		for (String s : lines)
		{
			//split varName=varValue
			String[] split = s.split("=");
			String varName = split[0];
			if (split.length > 1)//if proper format was provided
			{
				String varValue = split[1];
				//come here to add more "else if"s if more parameters are added
				//set value of parameters based on varName
				if (varName.equals("learningParameter")) learningParameter = Double.parseDouble(varValue);
				else if (varName.equals("mutationStdDev")) mutationStdDev = Double.parseDouble(varValue);
				else if (varName.equals("numberOfAgents")) numberOfAgents = Integer.parseInt(varValue);
				else if (varName.equals("numberOfTrials")) numberOfTrials = Integer.parseInt(varValue);
				else if (varName.equals("nurturingTrials")) nurturingTrials = Integer.parseInt(varValue);
				else if (varName.equals("numberOfGens")) numberOfGens = Integer.parseInt(varValue);
				else if (varName.equals("tournamentSize")) tournamentSize = Integer.parseInt(varValue);
				else if (varName.equals("stateInfo")) stateInfo = varValue;
				else if (varName.equals("printAgentData")) printAgentData = Boolean.parseBoolean(varValue);
				else if (varName.equals("printSummary")) printSummary = Boolean.parseBoolean(varValue);
				else if (varName.equals("printFitness")) printFitness = Boolean.parseBoolean(varValue);
				else if (varName.equals("printPropAvsExpB")) printPropAvsExpB = Boolean.parseBoolean(varValue);
				else if (varName.equals("printAgentTrialData")) printAgentTrialData = Boolean.parseBoolean(varValue);
				else System.out.println("Parameter " + varName + " is not valid.");
			}
		}
		//Same procedure as above, but uses arguments passed to program instead of the lines
		//read from config. This order ensures that command-line arguments have the final say
		//in a parameter's value.
		for (String s : args)
		{
			String[] split = s.split("=");
			String varName = split[0];
			if (split.length > 1)
			{
				String varValue = split[1];
				if (varName.equals("learningParamter")) learningParameter = Double.parseDouble(varValue);
				else if (varName.equals("mutationStdDev")) mutationStdDev = Double.parseDouble(varValue);
				else if (varName.equals("numberOfAgents")) numberOfAgents = Integer.parseInt(varValue);
				else if (varName.equals("numberOfTrials")) numberOfTrials = Integer.parseInt(varValue);
				else if (varName.equals("nurturingTrials")) nurturingTrials = Integer.parseInt(varValue);
				else if (varName.equals("numberOfGens")) numberOfGens = Integer.parseInt(varValue);
				else if (varName.equals("tournamentSize")) tournamentSize = Integer.parseInt(varValue);
				else if (varName.equals("stateInfo")) stateInfo = varValue;
                else if (varName.equals("printAgentData")) printAgentData = Boolean.parseBoolean(varValue);
                else if (varName.equals("printSummary")) printSummary = Boolean.parseBoolean(varValue);
				else if (varName.equals("printFitness")) printFitness = Boolean.parseBoolean(varValue);
				else if (varName.equals("printAgentTrialData")) printAgentTrialData = Boolean.parseBoolean(varValue);
				else System.out.println("Parameter " + varName + " is not valid.");
			}
		}
		Setup.parseStates();
		if (nurturingTrials == 0) runInfo = "non-nurturing";
		else runInfo = "nurturing";
	}
	
	public static void parseStates()
	{
		//stateInfo in form "100-100,0-200" with first set for A and second for B
		String stateA = stateInfo.substring(0, stateInfo.indexOf(","));
		String stateB = stateInfo.substring(stateInfo.indexOf(",") + 1, stateInfo.lastIndexOf(","));
		String stateC = stateInfo.substring(stateInfo.lastIndexOf(",") + 1, stateInfo.length());
		//initialize stateVals array
		stateVals = new int[6];
		//stores min and max for all choices
		Setup.stateVals[0] = Integer.parseInt(stateA.substring(0, stateA.indexOf("-"))); //A min
		Setup.stateVals[1] = Integer.parseInt(stateA.substring(stateA.indexOf("-") + 1, stateA.length())); //A max
		Setup.stateVals[2] = Integer.parseInt(stateB.substring(0, stateB.indexOf("-"))); //B min
		Setup.stateVals[3] = Integer.parseInt(stateB.substring(stateB.indexOf("-") + 1, stateB.length())); //B max
		Setup.stateVals[4] = Integer.parseInt(stateC.substring(0, stateC.indexOf("-"))); //C min
		Setup.stateVals[5] = Integer.parseInt(stateC.substring(stateC.indexOf("-") + 1, stateC.length())); //C max
	}
	
	/**
	 * Lists the current parameters of the program as "varName=varValue" separated by
	 * commas and enclosed in [].
	 * @return this list of parameters as a String
	 */
	public static String parameters()
	{
		return "[" + runInfo + ", learningParameter=" + learningParameter + 
		        ", mutationStdDev=" + mutationStdDev + ", numberOfAgents=" + numberOfAgents + 
		        ", numberOfTrials="	+ numberOfTrials + ", nurturingTrials=" + nurturingTrials + 
		        ", numberOfGens=" + numberOfGens + ", tournamentSize=" + tournamentSize + 
		        ", stateInfo=" + stateInfo + "]";
	}
}
