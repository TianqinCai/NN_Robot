package RflRobot;

import java.util.Random;

public class RLearning {

	private static double mLearningRate;
	private static double mDiscountRate;
	private static double mExplorationRate;
	public static int lastState;
	public static int lastAction;

	public RLearning(double learningRate, double discountRate, double exploitationRate){
		this.mLearningRate = learningRate;
		this.mDiscountRate = discountRate;
		this.mExplorationRate = exploitationRate;
	}


	public double getMaxQValue(int state) {
		double maxQValue = Double.MIN_VALUE;
		for (int i = 0; i < LUT.ACTION_DIMENSIONALITY; i++) {
			if (LUT.LUTTable[state][i] > maxQValue)
				maxQValue = LUT.LUTTable[state][i];
		}
		return maxQValue;
	}

	public int getBestAction(int state) {
		double maxQValue = Double.MIN_VALUE;
		int bestAction = 0;
		double qValue;
		for (int i = 0; i < LUT.ACTION_DIMENSIONALITY; i++) {
			qValue = LUT.LUTTable[state][i];

			if (qValue > maxQValue) {
				maxQValue = qValue;
				bestAction = i;
			}
		}
		return bestAction;
	}

	public double getQValue(int state, int action) {
		return LUT.LUTTable[state][action];
	}

	public void setQValue(int state, int action, double value) {
		LUT.LUTTable[state][action] = value;
	}

	//TODO: Q-Learning and SARSA
	public void QLearn(int state, int action, double reinforcement) {

		double oldQValue = getQValue(lastState, lastAction);
		double newQValue = oldQValue + mLearningRate * (reinforcement + mDiscountRate * getQValue(state,action) - oldQValue);
		setQValue(lastState, lastAction, newQValue);

		lastState = state;
		lastAction = action;
	}

	public void SARSA(int state, int action, double reinforcement){

	}

	public int selectAction(int state) {

		Random random = new Random();

		if ((1 - random.nextDouble()) <= mExplorationRate) {
			return random.nextInt(LUT.ACTION_DIMENSIONALITY);
		} else
			return getBestAction(state);

	}

}