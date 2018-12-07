package RflRobot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import robocode.*;

public class RLearning {

	public static double a = 0;
	// double[][] table= new double[LUT.NHowManyState][LUT.NHowManyAction];

	public RLearning() {

		// initialize();
	}

	private void initialize() {
		Random random = new Random();
		for (int i = 0; i < LUT.NHowManyState; i++)
			for (int j = 0; j < LUT.NHowManyAction; j++)
				LUT.table[i][j] = random.nextDouble() - 1;
		// table[i][j] = 0;
	}

	public double getMaxQValue(int state) {
		double maxinum = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < LUT.table[state].length; i++) {
			if (LUT.table[state][i] > maxinum)
				maxinum = LUT.table[state][i];
		}
		return maxinum;
	}

	public int getBestAction(int state) {
		double maxinum = Double.NEGATIVE_INFINITY;
		int bestAction = 0;
		for (int i = 0; i < LUT.table[state].length; i++) {
			double qValue = LUT.table[state][i]; ///////////////////////////////////////////////

			if (qValue > maxinum) {
				maxinum = qValue;
				bestAction = i;
			}
		}
		////////////// System.out.println("Action " + bestAction + ": " + maxinum);
		return bestAction;
	}

	public double getQValue(int state, int action) {
		return LUT.table[state][action];
	}

	public void setQValue(int state, int action, double value) {
		//if (LUT.table[state][action] != value) {
			//++a;//////////////////// System.out.println("******changed for"+a+"times******");
		//}
		LUT.table[state][action] = value;
	}

	public final double LearningRate = 0.2;
	public final double DiscountRate = 0.99;
	public static double ExploitationRate = 0.3;
	public static int lastState;
	public static int lastAction;
	private boolean first = true;

	public void learn(int state, int action, double reinforcement) {
		//////////////////// System.out.println("Reinforcement: " + reinforcement);
		if (first)
			first = false;
		else {
			double oldQValue = getQValue(lastState, lastAction);
			double newQValue = (1 - LearningRate) * oldQValue
					+ LearningRate * (reinforcement + DiscountRate * getQValue(state,action));
			////////////// System.out.println("Old Q-Value: " + oldQValue + ", New Q-Value:
			////////////// " + newQValue + ", Different: " + (newQValue - oldQValue));
			
			
			setQValue(lastState, lastAction, newQValue);
		}
		lastState = state;
		lastAction = action;
	}

	public int selectAction(int state) {
		
		// double[] value = new double[LUT.NHowManyAction];
		Random random = new Random();
		

		if ((1 - random.nextDouble()) <= ExploitationRate) {
			//////////////////// System.out.println("RANDOM!");
			return random.nextInt(8);
		} else
			return getBestAction(state);

	}

}