package RflRobot;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import NeuralNetWork.NeurualNetWork;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 *
 * @author kl
 */
//import robocode.Robot;
import robocode.*;
import robocode.AdvancedRobot;

public class RFLROBOCODE extends AdvancedRobot {
	public static final double PI = Math.PI;

    // Learning constants
    private static final int NO_LEARNING_RANDOM = 0; // No learning, completely random, baselines behaviour
    private static final int NO_LEARNING_GREEDY = 1; // No learning, will pick greediest move if LUT is available
    private static final int SARSA = 2; // On-policy SARSA
    private static final int Q_LEARNING = 3; // Off-policy Q-learning

    private static final int STATE_DIMENSIONALITY = 4;
    //NeuralNetwork parameters
    private static final int NUM_INPUTS = 4; //number of NN inputs
    private static final int NUM_HIDDENS = 100;
    private static final int NUM_OUTPUTS = 8;
    private static final double MIN_VAL = -0.5;
    private static final double MAX_VAL = 0.5;
    private static final double MOMENTUM = 0.1;
    private static final double LEARNING_RATE = 0.0005;

    // Reinforcement learning parameters
    private static final double ALPHA = 0.7;    // Fraction of difference used
    private static final double GAMMA = 0.95;    // Discount factor
    private static final double EPSILON = 0.1;  // Probability of exploration

    private int mCurrentLearningPolicy = SARSA;
    //private int mCurrentLearningPolicy = NO_LEARNING_RANDOM;
    //private int mCurrentLearningPolicy = NO_LEARNING_GREEDY;
    //private int mCurrentLearningPolicy = Q_LEARNING;
    private boolean mIntermediateRewards = true;
    private boolean mTerminalRewards = true;
    private static final int REWARD_SCALER = 150; // How much to scale the rewards by for the neural network calculation

    private static final int ACTION_DIMENSIONALITY = 8;

    private static final int ACTION_MODE_MAX_Q = 0;
    private static final int ACTION_MODE_EPSILON_GREEDY = 1;

    private int mCurrentAction;
    private int mPreviousAction;
    private double [] mCurrentStateSnapshot = new double[STATE_DIMENSIONALITY];
    private double [] mPreviousStateSnapshot = new double[STATE_DIMENSIONALITY];

    // Winrate tracking for every 100 rounds
    private static final int NUM_ROUNDS = 10000;
    private static final int NUM_ROUNDS_DIV_100 = NUM_ROUNDS / 100;
    private static int [] mNumWinArray = new int[NUM_ROUNDS_DIV_100];
    private static double [] mAverageDeltaQ = new double[NUM_ROUNDS];
    private static double [] mHighestDeltaQ = new double[NUM_ROUNDS];
    private static double [] mLowestDeltaQ = new double[NUM_ROUNDS];
    private static double [][] mAverageBackpropErrors = new double[NUM_ROUNDS][NUM_OUTPUTS];
    private static double mRoundTotalDeltaQ;
    private static double mRoundHighestDeltaQ = -500.0;
    private static double mRoundLowestDeltaQ = 500.0;
    private static int mRoundDeltaQNum = 1;

    private double mPreviousEnergyDifference;
    private double mCurrentEnergyDifference;
    private double mCurrentReward;

    private static NeurualNetWork mNeuralNet;

	private File mWRFile;
	private File mLUTFile;
	private File mWeightFile;

	private TargetInfo target;
	private RLearning table;
	//TODO: replace reinforcement with mCurrentReward
	private double reinforcement = 0.0;
	private double firePower;
	private int isAiming = 0;

	public RFLROBOCODE() {
		table = new RLearning();

	}

	public void run() {

		// loadData();
		mWRFile = getDataFile("win_ratio.txt");
		mLUTFile =getDataFile("Action1.csv");
		mWeightFile = getDataFile("weight.data");

        mNeuralNet = new NeurualNetWork(NUM_INPUTS, NUM_OUTPUTS, NUM_HIDDENS, LEARNING_RATE, MOMENTUM, MIN_VAL, MAX_VAL);
        //TODO: using getDataFile to load weight may cause problem
        mNeuralNet.loadWeight(mWeightFile);
        target = new TargetInfo();
		target.distance = 100000;

        // Set robot properties
		setColors(Color.green, Color.white, Color.green);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		turnRadarRight(360);

		//int state = getLUTState();
		double[] states = getNNState();

        while (true) {
            mCurrentStateSnapshot = getNNState();
            int action = getAction(ACTION_MODE_EPSILON_GREEDY, mCurrentStateSnapshot);
            //use LUT to select action
            //int action = table.selectAction(state);
            mCurrentAction = action;
            takeAction(action);
            // use LUT learning
            // table.learn(state, action, reinforcement);
            learn();
            reinforcement = 0.0;
        }
	}

    private void learn() {
        double qPrevNew, qPrevOld, qPrevDelta, qNext;

        double [] currentActionQs;
        double [] previousActionQs;
        double [] previousActionQsUpdated;
        double [] trainingErrors;
        int action;
        int index;

        // Take a snapshot of the current state
        mCurrentStateSnapshot = getNNState();
        // Feed forward the current state to the neural network and get state-action Q-value
        currentActionQs = mNeuralNet.outputFor(mCurrentStateSnapshot);
        // Feed forward the previous state to the neural network and get state-action Q-value
        previousActionQs = mNeuralNet.outputFor(mPreviousStateSnapshot);

        switch (mCurrentLearningPolicy)
        {
            // No learning at all (baseline)
            case NO_LEARNING_RANDOM:
                action = getRandomInt(0, ACTION_DIMENSIONALITY - 1);
                takeAction(action);
                break;
            case NO_LEARNING_GREEDY:
                //TODO: Complete Action_Mode_Max_Q in getAction function (choosing max Q action and don't learn)
                action = getAction(ACTION_MODE_MAX_Q, mCurrentStateSnapshot);
                // Take the action
                takeAction(action);
                break;
            // On-policy (SARSA)
            case SARSA:
                // Choose an on-policy action
                action = getAction(ACTION_MODE_EPSILON_GREEDY, mCurrentStateSnapshot);

                // Calculate new value for previous Q;
                qNext = currentActionQs[action];
                qPrevOld = previousActionQs[mPreviousAction];
                qPrevNew = calculateQPrevNew(qNext, qPrevOld);
                qPrevDelta = Math.abs(qPrevNew - qPrevOld);

                if (qPrevDelta > mRoundHighestDeltaQ)
                {
                    mRoundHighestDeltaQ = qPrevDelta;
                }
                if (qPrevDelta < mRoundLowestDeltaQ)
                {
                    mRoundLowestDeltaQ = qPrevDelta;
                }
                mRoundTotalDeltaQ += Math.abs(qPrevNew - qPrevOld);
                mRoundDeltaQNum++;

                // Backpropagate the action through the neural network
                // Replace the old previous Q value with the new one
                previousActionQs[mPreviousAction] = qPrevNew;
                // Train the neural network with the new dataset
                trainingErrors = mNeuralNet.train(createTrainingSet(mPreviousStateSnapshot, previousActionQs));
                for (index = 0; index < NUM_OUTPUTS; index++)
                {
                    mAverageBackpropErrors[getRoundNum()-1][index] += trainingErrors[index];
                }

                previousActionQsUpdated = mNeuralNet.outputFor(mPreviousStateSnapshot);

                // put the old q Val back in the array
                previousActionQs[mPreviousAction] = qPrevOld;

                // Reset reward until the next learn
                mCurrentReward = 0.0;

                // Take the next action
                takeAction(action);
                break;
            // Off-policy (Q-Learning)
            case Q_LEARNING:

                action = getAction(ACTION_MODE_EPSILON_GREEDY, mCurrentStateSnapshot);
                // Take the action
                takeAction(action);
                // Record our previous state snapshot
                mPreviousStateSnapshot = mCurrentStateSnapshot.clone();
                // Observe the new environment
                mCurrentStateSnapshot = getNNState();
                // Feed forward the current state to the neural network
                currentActionQs = mNeuralNet.outputFor(mCurrentStateSnapshot);
                // Get the action hash that has the maximum Q for this state
                action = getAction(ACTION_MODE_MAX_Q, mCurrentStateSnapshot);
                // Calculate new value for previous Q;
                qNext = currentActionQs[action];
                qPrevOld = previousActionQs[mPreviousAction];
                qPrevNew = calculateQPrevNew(qNext, qPrevOld);
                // Backpropagate the action through the neural network
                // Replace the old previous Q value with the new one
                previousActionQs[mPreviousAction] = qPrevNew;
                // Train the neural network with the new dataset
                mNeuralNet.train(createTrainingSet(mPreviousStateSnapshot, previousActionQs));

                // Reset reward until the next learn
                mCurrentReward = 0.0;

                break;
            default:
                break;
        }

        // Record our previous state snapshot
        mPreviousStateSnapshot = mCurrentStateSnapshot.clone();


    }

    private List<List<Double>> createTrainingSet(double [] inputVectorArray, double [] outputVectorArray)
    {
        int i;
        List<List<Double>> trainingSet = new ArrayList<>();
        List<Double> inputVector = new ArrayList<>();
        List<Double> outputVector = new ArrayList<>();

        // Convert ArrayLists into static arrays
        for(i = 0; i < NUM_INPUTS; i++)
        {
            inputVector.add(inputVectorArray[i]);
        }
        for(i = 0; i < NUM_OUTPUTS; i++)
        {
            outputVector.add(outputVectorArray[i]);
        }

        trainingSet.add(inputVector);
        trainingSet.add(outputVector);

        return trainingSet;
    }


    private void takeAction(int action) {
        switch (action) {
            case LUT.GoAhead:
                setAhead(LUT.OneStepDistance);

                break;
            case LUT.GoBack:
                setBack(LUT.OneStepDistance);
                break;
            case LUT.GoFWDLeft:
                setTurnLeft(LUT.OneStepAngle);
                // setAhead(LUT.OneStepDistance);

                // setTurnLeft(180 - (target.bearing + 90 - 30));
                break;
            case LUT.GoFWDRight:
                // setAhead(LUT.OneStepDistance);
                setTurnRight(LUT.OneStepAngle);
                // setAhead(LUT.OneStepDistance);

                // setTurnRight(target.bearing + 90 - 30);
                break;
            case LUT.GoBWDLeft:
                setTurnRight(LUT.OneStepAngle);
                // setAhead(LUT.OneStepDistance);

                // setTurnRight(target.bearing + 90 - 30);
                break;
            case LUT.GoBWDRight:
                setTurnLeft(LUT.OneStepAngle);
                // setAhead(target.bearing);

                // setTurnLeft(180 - (target.bearing + 90 - 30));
                break;
            case LUT.GoFireAtWill:
                // firePower = 400/target.distance;
                // if (firePower > 3)
                // firePower = 3;

                radarMovement();
                // gunMovement();
                gunMovement();
                if (getGunHeat() == 0) {
                    setFire(firePower);
                    reinforcement -= 1;
                    // reinforcement-=5;
                }
                break;
            case LUT.GoFindATarget:
                radarMovement();
                break;

        }
        execute();
        if (getTime() - target.ctime > 1)
            isAiming = 0;
        mPreviousAction = mCurrentAction;
    }

    private int getLUTState() {
		int heading = LUT.calculateHeading(getHeading());
		int targetDistance = LUT.calculateTargetDistance(target.distance);
		int targetBearing = LUT.calculateTargetBearing(target.bearing);
		int state = LUT.StateTable[heading][targetDistance][targetBearing][isAiming];
		return state;
	}

    private double[] getNNState() {
        int heading = LUT.calculateHeading(getHeading());
        int targetDistance = LUT.calculateTargetDistance(target.distance);
        int targetBearing = LUT.calculateTargetBearing(target.bearing);
        double[] state = new double[STATE_DIMENSIONALITY];
        state[0] = -1+ 0.125 * heading/45;
        state[1] = -1 + targetDistance;
        state[2] = -1+ 0.125 * targetBearing / 45;
        state[3] = isAiming;
        return state;
    }

	private void radarMovement() {
		double radarOffset;

		long time;
		long nextTime;
		Point2D.Double p;
		radarOffset = 2 * PI;

		if (isAiming == 0) {

			while (true) {
				turnRadarLeft(10);
				if (isAiming == 1)
					break;
				turnRadarRight(20);
				if (isAiming == 1)
					break;
				turnRadarLeft(30);
				if (isAiming == 1)
					break;
				turnRadarRight(40);
				if (isAiming == 1)
					break;
				turnRadarLeft(60);
				if (isAiming == 1)
					break;
				turnRadarRight(80);
				if (isAiming == 1)
					break;
				turnRadarLeft(110);
				if (isAiming == 1)
					break;
				turnRadarRight(150);
				if (isAiming == 1)
					break;
				turnRadarLeft(200);
				if (isAiming == 1)
					break;
				turnRadarRight(270);
				if (isAiming == 1)
					break;
			}

		} else {
			p = new Point2D.Double(target.x, target.y);

			for (int i = 0; i < 20; i++) {
				nextTime = (int) Math.round((getrange(getX(), getY(), p.x, p.y) / (20 - (3 * firePower))));
				time = getTime() + nextTime - 10;
				p = target.guessPosition(time);
			}
			// offsets the gun by the angle to the next shot based on linear targeting
			// provided by the enemy class
			radarOffset = getGunHeading() / 360 * 2 * PI - (Math.PI / 2 - Math.atan2(p.y - getY(), p.x - getX()));

			// turn the radar
			turnRadarLeft(radarOffset / 2 / PI * 360);

		}

	}

	private void gunMovement() {
		long time;
		long nextTime;
		Point2D.Double p;
		p = new Point2D.Double(target.x, target.y);
		for (int i = 0; i < 20; i++) {
			nextTime = (int) Math.round((getrange(getX(), getY(), p.x, p.y) / (20 - (3 * firePower))));
			time = getTime() + nextTime - 10;
			p = target.guessPosition(time);
		}
		// offsets the gun by the angle to the next shot based on linear targeting
		// provided by the enemy class
		double gunOffset = getGunHeading() / 360 * 2 * PI - (Math.PI / 2 - Math.atan2(p.y - getY(), p.x - getX()));
		turnGunLeft((NormaliseBearing(gunOffset)) / 2 / PI * 360);
	}

	// bearing is within the -pi to pi range
	double NormaliseBearing(double ang) {
		if (ang > PI)
			ang -= 2 * PI;
		if (ang < -PI)
			ang += 2 * PI;
		return ang;
	}

	// heading within the 0 to 2pi range

	// returns the distance between two x,y coordinates
	public double getrange(double x1, double y1, double x2, double y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		double h = Math.sqrt(xo * xo + yo * yo);
		return h;
	}

	// gets the absolute bearing between to x,y coordinates
	public void onStatus(StatusEvent e) {
	}

	public void onBulletHit(BulletHitEvent e) {
		if (target.name.equals(e.getName())) {
			reinforcement += 20;
		}
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
	}

	public void onBulletMissed(BulletMissedEvent e) {
		// double change = -e.getBullet().getPower();
		//////////////////////////// out.println("Bullet Missed: " + "-5");
		reinforcement -= 5;
	}

	public void onHitByBullet(HitByBulletEvent e) {
		if (target.name.equals(e.getName())) {
			reinforcement -= 20;
		}

	}

	public void onHitRobot(HitRobotEvent e) {
		if (target.name == e.getName()) {
			double change = 5.0;
			//////////////// out.println("Hit Robot: " + change);
			reinforcement += change;
		}
	}

	public void onHitWall(HitWallEvent e) {
		double change = -5;
		reinforcement += change;
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		isAiming = 1;
		if ((e.getDistance() < target.distance) || (target.name == e.getName())) {
			// the next line gets the absolute bearing to the point where the bot is
			double absbearing_rad = (getHeading() / 360 * 2 * PI + e.getBearingRadians()) % (2 * PI);
			// this section sets all the information about our target
			target.name = e.getName();
			double h = NormaliseBearing(e.getHeadingRadians() - target.head);
			h = h / (getTime() - target.ctime);

			target.changehead = h;
			target.x = getX() + Math.sin(absbearing_rad) * e.getDistance(); // works out the x coordinate of where the
																			// target is
			target.y = getY() + Math.cos(absbearing_rad) * e.getDistance(); // works out the y coordinate of where the
																			// target is
			target.bearing = e.getBearingRadians();
			target.head = e.getHeadingRadians();
			target.ctime = getTime(); // game time at which this scan was produced
			target.speed = e.getVelocity();
			target.distance = e.getDistance();
			target.energy = e.getEnergy();
			firePower = 400 / target.distance;
			if (firePower > 3)
				firePower = 3;
		}
	}

	public void onRobotDeath(RobotDeathEvent e) {

		if (e.getName().equals(target.name))
			target.distance = 10000;
	}

	public void onWin(WinEvent event) {
		LUT.RoundCount++;
		LUT.WinCount++;
		reinforcement += 20;
		if (LUT.RoundCount % 100 == 0) {
			DecimalFormat    df   = new DecimalFormat("#0.00");   
			double winRatio;
			if (LUT.RoundCount % 100 == 0) {
				winRatio = LUT.WinCount;
				System.out.println(String.valueOf(LUT.RoundCount));
				System.out.println(String.valueOf(LUT.WinCount));
				LUT.filecounterrecord[LUT.RoundCount / 100 - 1] = winRatio;
				LUT.WinCount = 0;
			}

			if (LUT.RoundCount == 3000) {
				try {
					RobocodeFileWriter file1 = new RobocodeFileWriter(mWRFile);
					for (int i = 0; i <= 200; ++i) {
						file1.write(String.valueOf(LUT.filecounterrecord[i]) + " ");
						file1.write("\n");
						/// file1.write("aaa");
					}
					file1.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (LUT.RoundCount == 3000) {
				try {
					RobocodeFileWriter fileWriter = new RobocodeFileWriter(mLUTFile);
				
					for (int i = 0; i < LUT.NHowManyState; i++)
						for (int j = 0; j < LUT.NHowManyAction; j++)
						{
				
							fileWriter.write(String.valueOf(LUT.table[i][j]));
							fileWriter.write("\n");
						}
				
			
					fileWriter.close();
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				
			
		}
		

	}

	public void onDeath(DeathEvent event) {
		LUT.RoundCount++;
		reinforcement -= 20;
		System.out.println(RLearning.ExploitationRate);
		DecimalFormat    df   = new DecimalFormat("#0.00"); 
		if (LUT.RoundCount % 100 == 0) {
			double winRatio = 0;
			
			if (LUT.RoundCount % 100 == 0) {
				winRatio = LUT.WinCount;
				LUT.filecounterrecord[LUT.RoundCount / 100 - 1] = winRatio;
				System.out.println(String.valueOf(LUT.RoundCount));
				System.out.println(String.valueOf(LUT.WinCount));
				LUT.WinCount = 0;
			}

			if (LUT.RoundCount == 3000) {
				try {
					RobocodeFileWriter file1 = new RobocodeFileWriter(mWRFile);
					for (int i = 0; i <= 200; ++i) {
						file1.write(String.valueOf(LUT.filecounterrecord[i]) + " ");
						file1.write("\n");
						// file1.write("a");
					}
					file1.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			//1
			if (LUT.RoundCount == 3000) {
												
				//FileWriter fileWriter;
				
				try {RobocodeFileWriter fileWriter = new RobocodeFileWriter(mLUTFile);
					
					for (int i = 0; i < LUT.NHowManyState; i++)
						for (int j = 0; j < LUT.NHowManyAction; j++)
					 {
					
							fileWriter.write(String.valueOf(LUT.table[i][j]));
							fileWriter.write("\n");
					// file1.write("a");
				}
					
				
					fileWriter.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

    private int getAction(int mode, double [] currentStateSnapshot)
    {
        int index, selectedAction;
        ArrayList<Integer> qMaxActions = new ArrayList<>();
        double [] actionQs;
        double qVal, randomDouble;
        double qMax = Double.MIN_VALUE;

        // Feed forward current state snapshot into neural network and obtain a set of action Q values
        actionQs = mNeuralNet.outputFor(currentStateSnapshot);

        // Get the maximum action
        for (index = 0; index < ACTION_DIMENSIONALITY; index++)
        {
            qVal = actionQs[index];

            // Update current max
            if (qVal > qMax)
            {
                // New max, clear array
                // We can have a maximum of the number of possible actions as the number of possible actions
                qMaxActions = new ArrayList<>();
                qMaxActions.add(index);
                qMax = qVal;
            }
            else if (qVal == qMax)
            {
                // We found a q value equal to the max, add it to the possible actions
                qMaxActions.add(index);
            }
        }
        if (qMaxActions.size() == 1)
        {
            selectedAction = qMaxActions.get(0);
        }
        else
        {
            selectedAction = getRandomInt(0, qMaxActions.size());
        }


        switch (mode)
        {
            // If we're choosing epsilon greedy, then we must choose between max Q or exploratory, so do that here
            case ACTION_MODE_EPSILON_GREEDY:
                Random random = new Random();
                randomDouble = random.nextDouble();
                if (randomDouble < EPSILON)
                {
                    // Take random action
                    selectedAction = getRandomInt(0, ACTION_DIMENSIONALITY - 1);
                }
                else
                {
                    // Take greedy action
                    // so do nothing here
                }
                break;
            // We should already have max Q from above, so choose that
            default:
                // We should never be here
                break;
        }

        return selectedAction;
    }

    private int getRandomInt(int min, int max)
    {
        int result;
        Random random;

        random = new Random();
        result = random.nextInt(max - min + 1) + min;

        return result;
    }

    private double calculateQPrevNew(double qNext, double qPrevOld)
    {
        double qPrevNew;

        qPrevNew = qPrevOld + (ALPHA * (reinforcement + (GAMMA * qNext) - qPrevOld));

        return qPrevNew;
    }

    public void onKeyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_0:
			setColors(Color.red, Color.red, Color.red); // save to file

			RLearning.ExploitationRate = 0.0;
			break;
		case KeyEvent.VK_1:
			setColors(Color.red, Color.red, Color.red); // save to file

			RLearning.ExploitationRate = 0.1;
			break;
		case KeyEvent.VK_2:
			setColors(Color.white, Color.white, Color.white);// save to file
			RLearning.ExploitationRate = 0.2;
			break;
		case KeyEvent.VK_3:
			setColors(Color.red, Color.red, Color.red); // save to file

			RLearning.ExploitationRate = 0.3;
			break;
		case KeyEvent.VK_4:
			setColors(Color.white, Color.white, Color.white);// save to file

			RLearning.ExploitationRate = 0.4;
			break;
		case KeyEvent.VK_5:
			setColors(Color.white, Color.white, Color.white);// save to file

			RLearning.ExploitationRate = 0.5;
			break;
		case KeyEvent.VK_6:
			setColors(Color.white, Color.white, Color.white);// save to file

			RLearning.ExploitationRate = 0.6;
			break;
		case KeyEvent.VK_7:
			setColors(Color.white, Color.white, Color.white);// save to file

			RLearning.ExploitationRate = 0.7;
			break;
		case KeyEvent.VK_8:
			setColors(Color.white, Color.white, Color.white);// save to file

			RLearning.ExploitationRate = 0.8;
			break;
		case KeyEvent.VK_9:
			setColors(Color.pink, Color.pink, Color.pink);// save to file

			RLearning.ExploitationRate = 1;
			break;

		default: // What to do if any other key is pressed.

		}
	}

}
