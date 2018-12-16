package RflRobot;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;

import NeuralNetWork.NeurualNetWork;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import robocode.*;
import robocode.AdvancedRobot;

public class RFLROBOCODE extends AdvancedRobot {
	public static final double PI = Math.PI;

    // Learning constants
    private static final int NO_LEARNING_RANDOM = 0; // No learning, completely random, baselines behaviour
    private static final int NO_LEARNING_GREEDY = 1; // No learning, will pick greediest move if LUT is available
    private static final int SARSA = 2; // On-policy SARSA
    private static final int Q_LEARNING = 3; // Off-policy Q-learning

    private int mCurrentLearningPolicy = SARSA;

	private static final boolean NON_TERMINAL_STATE = false;
	private static final boolean TERMINAL_STATE = true;

	//state and action number
    private static final int STATE_DIMENSIONALITY = 6;
    private static final int ACTION_DIMENSIONALITY = 8;

    //NeuralNetwork parameters
    private static boolean NN_ENABLED = true;
    private static final int NUM_INPUTS = 6; //number of NN inputs
    private static final int NUM_HIDDENS = 30;
    private static final int NUM_OUTPUTS = 8;
    private static final double MIN_VAL = -1;
    private static final double MAX_VAL = 1;
    private static final double MOMENTUM = 0.8;
    private static final double LEARNING_RATE = 0.0005;

    // Reinforcement learning parameters
    private static final double ALPHA = 0.2;    // Fraction of difference used
    private static final double GAMMA = 0.9;    // Discount factor
    private static final double EPSILON = 0.3;  // Probability of exploration

    private boolean mIntermediateRewards = true;
    private boolean mTerminalRewards = true;
	private static final int REWARD_SCALER = 50; // How much to scale the rewards by for the neural network calculation

    private static final int ACTION_MODE_MAX_Q = 0;
    private static final int ACTION_MODE_EPSILON_GREEDY = 1;

    private int mCurrentAction;
    private int mPreviousAction;
    private double [] mCurrentStateSnapshot = new double[STATE_DIMENSIONALITY];
    private double [] mPreviousStateSnapshot = new double[STATE_DIMENSIONALITY];

    // Winrate tracking for every 100 rounds
    private static final int NUM_ROUNDS = 20000;
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

    private static NeurualNetWork mNeuralNet;

	private File mWRFile;
	private File mLUTFile;
	private File mWeightFile;
	private File mErrorFile;
    private static int mStateActionPairOccurence = 0;
    private static final int MAX_OUTPUT_SIZE = 10000;

    //error logging to view the convergence tendency
    private static double errorSum[] = new double[MAX_OUTPUT_SIZE];
    private static double cumError[] = new double[MAX_OUTPUT_SIZE];
    private static final int errorLoggingState[] = {-1,-1,-1,-1,-1,-1};
    private static final int errorLoggingAction = 6;

	private TargetInfo target;
	private RLearning rLearning;
	private double reinforcement = 0.0;
	private double firePower;
	private int isAiming = 0;


	public RFLROBOCODE() {
		rLearning = new RLearning(ALPHA,GAMMA, EPSILON);
	}

	public void run() {
	    //robot's main funcion for every battle

		mWRFile = getDataFile("win_ratio.txt");
		mLUTFile =getDataFile("Action1.csv");
		mErrorFile = getDataFile("Error1.txt");
		mWeightFile = getDataFile("weight.data");

        mNeuralNet = new NeurualNetWork(NUM_INPUTS, NUM_OUTPUTS, NUM_HIDDENS, LEARNING_RATE, MOMENTUM, MIN_VAL, MAX_VAL);

        //determine if already have weight for neuralnet or intialize the weights for the first time
		if(mWeightFile.length()==0) {
			mNeuralNet.initializeWeights();
		} else {
			mNeuralNet.loadWeight(mWeightFile);
		}

		//get enemy info
        target = new TargetInfo();
		target.distance = 100000;

        //set robot properties
		setColors(Color.green, Color.white, Color.green);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

        while (true) {
            if(NN_ENABLED){
                mCurrentStateSnapshot = getNNState();
                int action = getAction(ACTION_MODE_EPSILON_GREEDY, mCurrentStateSnapshot);
                mCurrentAction = action;
                takeAction(action);
                NNLearn();
            } else {
                //use LUT with Q-learning
                int state = getLUTState();
                int action = rLearning.selectAction(state);
                takeAction(action);
                rLearning.offPolicyLearn(state, action, reinforcement);
                //rLearning.onPolicyLearn(state,action,reinforcement);
            }
            reinforcement = 0.0;
        }
	}

    private void NNLearn() {
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
                //TODO: Complete Action_Mode_Max_Q in getAction function (choosing max Q action and don't offPolicyLearn)
                action = getAction(ACTION_MODE_MAX_Q, mCurrentStateSnapshot);
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


                logErrorForStateActionPair(errorLoggingState, errorLoggingAction, action);

                previousActionQsUpdated = mNeuralNet.outputFor(mPreviousStateSnapshot);

                // put the old q Val back in the array
                previousActionQs[mPreviousAction] = qPrevOld;

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

                break;
            default:
                break;
        }

        // Record our previous state snapshot
        mPreviousStateSnapshot = mCurrentStateSnapshot.clone();


    }

    private void logErrorForStateActionPair(int[] targetState, int targetAction, int currentAction) {
        boolean flag = true;
        for(int i = 0; i < STATE_DIMENSIONALITY; i++) {
            if(mCurrentStateSnapshot[i] != targetState[i])
                return;
        }
        if(currentAction!=targetAction)
            return;
        if(mStateActionPairOccurence < MAX_OUTPUT_SIZE) {
            errorSum[mStateActionPairOccurence +1]=mNeuralNet.outputFor(mCurrentStateSnapshot)[targetAction];
            cumError[mStateActionPairOccurence] = errorSum[mStateActionPairOccurence +1] - errorSum[mStateActionPairOccurence];
            mStateActionPairOccurence++;
        }

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
	    mCurrentAction = action;
        switch (action) {
            case LUT.GoAhead:
                setAhead(LUT.OneStepDistance);
                break;
            case LUT.GoBack:
                setBack(LUT.OneStepDistance);
                break;
            case LUT.GoFWDLeft:
                setTurnLeft(LUT.OneStepAngle);
                setAhead(LUT.OneStepDistance);
                break;
            case LUT.GoFWDRight:
                setTurnRight(LUT.OneStepAngle);
                setAhead(LUT.OneStepDistance);
                break;
            case LUT.GoBWDLeft:
                setTurnRight(LUT.OneStepAngle);
                setAhead(LUT.OneStepDistance);
                break;
            case LUT.GoBWDRight:
                setTurnLeft(LUT.OneStepAngle);
                setAhead(target.bearing);
                break;
            case LUT.GoFireAtWill:
                firePower = 400/target.distance;
                if (firePower > 3)
                    firePower = 3;
                radarMovement();
                gunMovement();
                if(getGunHeat()!=0) {
                    reinforcement-=1;
                }
                else {
                    setFire(firePower);
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
		return LUT.StateTable[heading][targetDistance][targetBearing][isAiming];
	}

    private double[] getNNState() {
        int heading = LUT.calculateHeading(getHeading());
        int targetDistance = LUT.calculateTargetDistance(target.distance);
        int targetBearing = LUT.calculateTargetBearing(target.bearing);
        int wallDistance = LUT.calculateWallDistance(getX(), getY(), 800, 600);
        double[] state = new double[STATE_DIMENSIONALITY];
        //normalize inputs
        state[0] = -1 + 0.25 * heading;
        state[1] = -1 + targetDistance;
        state[2] = -1+ 0.25 * targetBearing;
        state[3] = isAiming==0?-1:1;
        state[4] = wallDistance-1;
        state[5] = (LUT.calculateSelfEnergy(getEnergy())-2)/2;
        return state;
    }

	private void radarMovement() {
		double radarOffset;

		long time;
		long nextTime;
		Point2D.Double p;

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
				nextTime = (int) Math.round((getAbsoluteDistance(getX(), getY(), p.x, p.y) / (20 - (3 * firePower))));
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
			nextTime = (int) Math.round((getAbsoluteDistance(getX(), getY(), p.x, p.y) / (20 - (3 * firePower))));
			time = getTime() + nextTime - 10;
			p = target.guessPosition(time);
		}
		// offsets the gun by the angle to the next shot based on linear targeting provided by the enemy class
		double gunOffset = getGunHeading() / 360 * 2 * PI - (Math.PI / 2 - Math.atan2(p.y - getY(), p.x - getX()));
		turnGunLeft((NormaliseBearing(gunOffset)) / 2 / PI * 360);
	}

	// bearing is within the -pi to pi range
    private double NormaliseBearing(double ang) {
		if (ang > PI)
			ang -= 2 * PI;
		if (ang < -PI)
			ang += 2 * PI;
		return ang;
	}

	// returns the distance between two x,y coordinates
	private double getAbsoluteDistance(double x1, double y1, double x2, double y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		return Math.sqrt(xo * xo + yo * yo);
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
		reinforcement -= 2;
	}

	public void onHitByBullet(HitByBulletEvent e) {
		if (target.name.equals(e.getName())) {
			reinforcement -= 20;
		}

	}

	public void onHitRobot(HitRobotEvent e) {
		if (target.name.equals(e.getName())) {
			reinforcement += 2;
		}
	}

	public void onHitWall(HitWallEvent e) {
		reinforcement -= 2;
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		isAiming = 1;
		//It's effective to give reinforcement for scanning Robot
		reinforcement += 1;

		if ((e.getDistance() < target.distance) || (target.name.equals(e.getName()))) {

			// get the absolute bearing to the point where the bot is
			double absbearing_rad = (getHeading() / 360 * 2 * PI + e.getBearingRadians()) % (2 * PI);

			//set the information about our target
			target.name = e.getName();
			double h = NormaliseBearing(e.getHeadingRadians() - target.head);
			h = h / (getTime() - target.ctime);

			//get target info
			target.changehead = h;
			target.x = getX() + Math.sin(absbearing_rad) * e.getDistance();
			target.y = getY() + Math.cos(absbearing_rad) * e.getDistance();
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
		    //for every 100 rounds, log win rate
            logWinRate();
		}
	}

    public void onDeath(DeathEvent event) {
		LUT.RoundCount++;
		reinforcement -= 20;
		if (LUT.RoundCount % 100 == 0) {
            //for every 100 rounds, log win rate
            logWinRate();
		}
	}

    public void onBattleEnded(BattleEndedEvent event)
    {
        LUT.outputWinRate(mWRFile);
        //for NN learning, save the weight
        if(NN_ENABLED)
            mNeuralNet.saveWeight();
        //for LUT learning, save the LUT table
        else
            LUT.outputLUTTable(mLUTFile);
    }

    private void logWinRate() {
        double winRatio;
        winRatio = LUT.WinCount;
        LUT.winRateLog[LUT.RoundCount / 100 - 1] = winRatio;
        LUT.WinCount = 0;
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
                    // Take exploration move
                    selectedAction = getRandomInt(0, ACTION_DIMENSIONALITY - 1);
                }
                break;
            case ACTION_MODE_MAX_Q:
                // We should already have max Q from above, so choose that
                break;
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

        qPrevNew = qPrevOld + (ALPHA * (reinforcement/REWARD_SCALER + (GAMMA * qNext) - qPrevOld));

        return qPrevNew;
    }

    public void onKeyPressed(KeyEvent e) {
	}

}
