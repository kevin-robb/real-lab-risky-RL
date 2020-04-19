/**
 * Stores information about one Agent.
 * 
 * @author Kevin Robb
 * @version 6/9/2018
 * Referenced code from Steven Roberts.
 */
public class Agent {
	/**current learning parameter for this specific agent. initially default*/
	private double learningParameter;
	/**the fitness an individual has. fitness is only used at the end of life
	 * in generational selection and not used by agent itself for choices. */
	private double fitness;
	/**stores the expected reward value of options A and B */
	double[] expectedRewards;
	
	/**
	 * Default constructor. Used to create agents for first generation.
	 */
	public Agent()
	{
		//sets to default starting L value
		this.learningParameter = Setup.learningParameter;
		this.expectedRewards = new double[3];
		this.fitness = 0;
		//sets expected rewards to simple average reward for each option
		expectedRewards[0] = (Setup.stateVals[0] + Setup.stateVals[1]) / 2;
		expectedRewards[1] = (Setup.stateVals[2] + Setup.stateVals[3]) / 2;
		expectedRewards[2] = (Setup.stateVals[4] + Setup.stateVals[5]) / 2;
		
		//Sets all exp values to 100 rather than actual. remove next 3 lines if actual desired
        expectedRewards[0] = (Setup.stateVals[0] + Setup.stateVals[1]) / 2;
        expectedRewards[1] = (Setup.stateVals[0] + Setup.stateVals[1]) / 2;
        expectedRewards[2] = (Setup.stateVals[0] + Setup.stateVals[1]) / 2;
	}
	/**
	 * Constructor to be used when creating agents for new generations.
	 * @param learningParameter
	 */
	public Agent(double learningParameter)
	{ 
		this.learningParameter = learningParameter;
		this.expectedRewards = new double[3];
		this.fitness = 0;
        //sets expected rewards to simple average reward for each option
		expectedRewards[0] = (Setup.stateVals[0] + Setup.stateVals[1]) / 2;
		expectedRewards[1] = (Setup.stateVals[2] + Setup.stateVals[3]) / 2;
		expectedRewards[2] = (Setup.stateVals[4] + Setup.stateVals[5]) / 2;
		
		//Sets all exp values to 100 rather than actual. remove next 3 lines if actual desired
        expectedRewards[0] = (Setup.stateVals[0] + Setup.stateVals[1]) / 2;
        expectedRewards[1] = (Setup.stateVals[0] + Setup.stateVals[1]) / 2;
        expectedRewards[2] = (Setup.stateVals[0] + Setup.stateVals[1]) / 2;
		
		//if we want, agents could carry over expected values between generations
		//this would work by copying agents and modifying only L (in Generation.formNewGeneration)
		//instead of creating new agents and assigning L, as we do here
	}
	/**
	 * changes current value of learning parameter to passed value.
	 * @param double new learning parameter 
	 */
	public void setLearningParameter(double newLearningParameter)
	{
		this.learningParameter = newLearningParameter;
	}
	/** @return current value of learning parameter. */
	public double getLearningParameter()
	{
		return this.learningParameter;
	}
	/** @return current amount of fitness this individual has. */
	public double getFitness()
	{
		return fitness;
	}
	/**
	 * adds fitness for each trial. parameter already accounts for nurturing trials
	 * @param newFitness the fitness to add
	 */
	public void addFitness(double newFitness){
		this.fitness += newFitness;
	}
	/** @return double array with two cells containing expected reward for A and B. */
	public double[] getExpectedRewards()
	{
		return expectedRewards;
	}
	/** @param cell defines which option's expected value is being updated
	 * @param newValue the new expected reward to be set */
	public void setExpectedRewards(int cell, double newValue)
	{
		 this.expectedRewards[cell] = newValue;
	}
}
