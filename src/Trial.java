/**
 * Performs one trial for each agent.
 * i.e., each agent makes one choice and stores result
 * to recalculate learning parameter value.
 * 
 * @author Kevin Robb
 * @version 5/11/2018
 * Referenced code from Steven Roberts.
 */
public class Trial {
	
	/**
	 * An agent makes a choice between the two options based
	 * on a Boltzmann distribution created from the expected
	 * rewards of each option.
	 * The reward is then calculated as a random choice between 
	 * the two possible rewards.
	 * The choice and reward are returned as a string in
	 * the form "A100" to represent a choice of state A and
	 * a received reward of 100.
	 * The learning parameter and the fitness of the agent are updated.
	 * @param agent the Agent making the choice
	 * @return choice and reward in form "A100"
	 */
	public static String makeChoice(Agent agent)
	{
		//Boltzmann algorithm for making choice
		//choice will be set in algorithm. 0 = option A and 1 = option B
		int choice = -1;
		double z = 0;
		for (double reward : agent.getExpectedRewards())
		{
			z += Math.pow(Math.E, reward / Setup.temperature);
		}
		double pVal = Math.random();
		for (int i = 0; i < agent.getExpectedRewards().length; i++)
		{
			pVal -= Math.pow(Math.E, agent.expectedRewards[i] / Setup.temperature) / z;
			if (pVal < 0)
			{
				choice = i;
				break;
			}
		}
		
		//random reward selection based on choice
		int reward = 0;
		char choiceLetter = 'B';
		if (choice == 0) {choiceLetter = 'A'; }
		else if (choice != 1)
		{
		    System.exit(1);
		}
		//should never happen but if choice != 0 or 1, it will select option B
		
		int rewardPick = (int) Math.round(Math.random());
		if (choiceLetter == 'A')
		{
			reward = Setup.stateVals[rewardPick];
		} else {
			reward = Setup.stateVals[rewardPick + 2];
		}
		
		return "" + choiceLetter + reward;
	}
}
