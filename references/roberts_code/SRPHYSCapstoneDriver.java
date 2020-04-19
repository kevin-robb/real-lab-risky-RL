import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SRPHYSCapstoneDriver
{
	public static void main(String[] args)
	{
		//File for writing is runModifier + "output" + identifier + ".txt"/".dat"
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmssSSS");
		String outputPathName = "";
		String outputPathIdentifier = sdf.format(now.getTime());
		if (Configuration.runType.equals("loud"))
		{
			outputPathName= "detailedoutput" + outputPathIdentifier + ".dat";
		}
		else if (Configuration.runType.equals("fancy"))
		{
			outputPathName = "fancyoutput" + outputPathIdentifier + ".dat";
		}
		else
		{
			outputPathName = "output" + outputPathIdentifier + ".txt";
		}
		
		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter(
					new FileWriter(new File(outputPathName)));
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		
		/*
		 * represents the "dimensions" of the S-A space. Length of d
		 * matches the number of states, each int in d represents the
		 * number of actions possible for that state.
		 */
		int[] d = null;

		try
		{
			Configuration.setConfig(args);
			d = Configuration.parseSASpace();
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
		//parseSASpace() throws an Exception (different from the IOException caught above) when
		//the S-A space is not the proper format. This is unrecoverable, so the program quits.
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
		
		/*
		 * create all Agents with S-A space dimensions as parameter.
		 * Also uses global parameters provided in config file or
		 * passed as arguments to the program.
		 */
		for (int i = 0; i < Configuration.numberOfAgents; i++)
		{
			new Agent(d, Configuration.selfWeight, Configuration.timeWeight,
					Configuration.resourceShare, State.getAllStates().get(0), 600);
		}
		
		if (Configuration.runType.equals("loud")) loudRun(out);
		else if (Configuration.runType.equals("loudRes")) loudResourceRun(out);
		else if (Configuration.runType.equals("fancy")) fancyRun(out);
		else normalRun(out);
	}
	
	/**
	 * The standard output format. Tracks the proportion of the remaining Agents who prefer
	 * the safe option and the amount of population remaining (from 0 to 1) at each timestep.
	 * @param out the writer to which output should be sent
	 */
	public static void normalRun(BufferedWriter out)
	{
		//Create the Environment and run the meat of the program: e.trial()
		//for number of times specified by config or as argument in args.
		Environment e = new Environment();
		try
		{
			double optA;//Track proportion of Agents who prefer safe option
			double difference;//the average expected value of A - B;
			double totalResources;//the total resource pool (shared and sum of individuals)
			int n;//the number of remaining agents
			for (int i = 0; i < Configuration.numberOfTrials; i++)
			{
				e.trial();
				optA = 0;
				difference = 0;
				totalResources = e.getResourcePool() + e.getCumulativeNegativeResources();
				for (Agent a : Agent.getAllAgents())
				{
					ArrayList<Double> eR = a.getExpectedRewards(0);
					if (eR.get(0) > eR.get(1))
					{
						optA++;
					}
					difference += eR.get(0) - eR.get(1);
					totalResources += a.getFitness();
				}
				n = Agent.getAllAgents().size();
				out.write(optA/n + "\t" + difference/n + "\t"
						+ ((double) n)/Configuration.numberOfAgents + "\t"
						+ totalResources + "\t" + totalResources/n + "\n");
			}
			out.write(Configuration.parameters().replaceAll(", ", "\t").replaceAll(
					"[\\[\\]]", ""));
			out.flush();
			out.close();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
	}
	
	/**
	 * An alternative to normalRun. Outputs the perceived value of all options
	 * for each Agent at each timestep.
	 * @param out the writer to which output should be sent
	 */
	public static void loudRun(BufferedWriter out)
	{
		//Create the Environment and run the meat of the program: e.trial()
		//for number of times specified by config or as argument in args.
		Environment e = new Environment();
		try
		{
			double eRATot = 0;//expected reward for A summed over all agents
			double eRBTot = 0;//expected reward for B summed over all agents
			for (Agent a : Agent.getAllAgents())
			{
				Double eRA = a.getExpectedRewards(0).get(0);
				Double eRB = a.getExpectedRewards(0).get(1);
				eRATot += eRA;
				eRBTot += eRB;
				out.write(eRA + "\t"
						+ eRB + "\t");
			}
			eRATot = eRATot/Agent.getAllAgents().size();
			eRBTot = eRBTot/Agent.getAllAgents().size();
			out.write(eRATot + "\t" + eRBTot + "\n");
			for (int i = 0; i < Configuration.numberOfTrials; i++)
			{
				e.trial();
				eRATot = 0;
				eRBTot = 0;
				for (Agent a : Agent.getAllAgents())
				{
					Double eRA = a.getExpectedRewards(0).get(0);
					Double eRB = a.getExpectedRewards(0).get(1);
					eRATot += eRA;
					eRBTot += eRB;
					out.write(eRA + "\t"
							+ eRB + "\t");
				}
				eRATot = eRATot/Agent.getAllAgents().size();
				eRBTot = eRBTot/Agent.getAllAgents().size();
				out.write(eRATot + "\t" + eRBTot + "\n");
			}
			out.flush();
			out.close();
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}
	}
	
	/**
	 * An alternative to normalRun. Outputs the obtained resources
	 * for each Agent at each timestep.
	 * @param out the writer to which output should be sent
	 */
	public static void loudResourceRun(BufferedWriter out)
	{
		//Create the Environment and run the meat of the program: e.trial()
		//for number of times specified by config or as argument in args.
		Environment e = new Environment();
		try
		{
			double resObtainedTot = 0;//obtained reward summed over all agents
			for (Agent a : Agent.getAllAgents())
			{
				Double resObtained = a.getNewReward();//this agent's obtained reward
				resObtainedTot += resObtained;
				out.write(resObtained + "\t");
			}
			resObtainedTot = resObtainedTot/Agent.getAllAgents().size();
			out.write(resObtainedTot + "\n");
			for (int i = 0; i < Configuration.numberOfTrials; i++)
			{
				e.trial();
				resObtainedTot = 0;
				for (Agent a : Agent.getAllAgents())
				{
					Double resObtained = a.getNewReward();
					resObtainedTot += resObtained;
					out.write(resObtained + "\t");
				}
				for (int fill = Agent.getAllAgents().size(); fill < 100; fill++)
				{
					//hard-coded things are bad, especially if they are meaningless
					out.write("-10" + "\t");
				}
				resObtainedTot = resObtainedTot/Agent.getAllAgents().size();
				out.write(resObtainedTot + "\n");
			}
			out.flush();
			out.close();
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}
	}
	
	/**
	 * The ultimate sanity check. Tracks a single Agent over the course of its
	 * life and outputs the following at each timestep: 1. tracked Agent's perceived
	 * value of option A; 2. average perceived value of option A among the rest of
	 * the group; 3. tracked Agent's recently reported reward from option A;
	 * 4. average recently reported reward from option A for the rest of the group;
	 * repeat 1-4 for all other options; total number of Agents.
	 * @param out the writer to which output should be sent
	 */
	public static void fancyRun(BufferedWriter out)
	{
		//Create the Environment and run the meat of the program: e.trial()
		//for number of times specified by config or as argument in args.
		Environment e = new Environment();
		try
		{
			Agent a = Agent.getAllAgents().get(0);
			double recentA = 100;
			double recentB = a.getExpectedRewards(0).get(1);
			double eRA = 0;
			double eRB = 0;
			for (int i = 1; i < Agent.getAllAgents().size(); i++)
			{
				eRA += Agent.getAllAgents().get(i).getExpectedRewards(0).get(0);
				eRB += Agent.getAllAgents().get(i).getExpectedRewards(0).get(1);
			}
			out.write(a.getExpectedRewards(0).get(0) + "\t"
					+ eRA/(Agent.getAllAgents().size() - 1) + "\t"
					+ a.getExpectedRewards(0).get(0) + "\t"
					+ eRA/(Agent.getAllAgents().size() - 1) + "\t"
					+ recentB + "\t" + eRB/(Agent.getAllAgents().size() - 1) + "\t"
					+ recentB + "\t" + eRB/(Agent.getAllAgents().size() - 1) 
					+ "\t" + Agent.getAllAgents().size() + "\n");
			for (int t = 0; t < Configuration.numberOfTrials; t++)
			{
				e.trial();
				if (a != Agent.getAllAgents().get(0)) break;//tracked Agent has died
				recentA = (a.getNewReward() == 100) ? a.getNewReward() : a.getExpectedRewards(0).get(0);
				recentB = (a.getNewReward() == 100) ? a.getExpectedRewards(0).get(1) : a.getNewReward();
				eRA = 0;
				eRB = 0;
				double rA = 0;
				double rB = 0;
				int numA = 0;
				for (int i = 1; i < Agent.getAllAgents().size(); i++)
				{
					eRA += Agent.getAllAgents().get(i).getExpectedRewards(0).get(0);
					eRB += Agent.getAllAgents().get(i).getExpectedRewards(0).get(1);
					if (Agent.getAllAgents().get(i).getChoice() == 0)
					{
						rA += Agent.getAllAgents().get(i).getNewReward();
						numA++;
					}
					else
					{
						rB += Agent.getAllAgents().get(i).getNewReward();
					}
				}
				int numB = Agent.getAllAgents().size() - numA - 1;
				out.write(a.getExpectedRewards(0).get(0) + "\t"
						+ eRA/(Agent.getAllAgents().size() - 1) + "\t"
						+ recentA + "\t" + rA/numA + "\t" + a.getExpectedRewards(0).get(1) + "\t"
						+ eRB/(Agent.getAllAgents().size() - 1) + "\t" + recentB + "\t"
						+ rB/numB + "\t" + Agent.getAllAgents().size() + "\n");
			}
			out.flush();
			out.close();
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}
	}
}
