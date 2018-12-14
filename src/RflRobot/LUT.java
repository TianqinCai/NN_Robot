package RflRobot;

public class LUT {
	public static int WinCount;
	public static int RoundCount;
	//TODO: rename this variable
	public static double filecounterrecord[];
	public static final int GoAhead = 0;
	public static final int GoBack = 1;
	public static final int GoFWDLeft = 2;
	public static final int GoFWDRight = 3;
	public static final int GoBWDLeft = 4;
	public static final int GoBWDRight = 5;
	public static final int GoFindATarget = 6;
	public static final int GoFireAtWill = 7;

	public static final double OneStepDistance = 50.0;
	public static final double OneStepAngle = 30;

	//public static final int STATE_DIMENSIONALITY = 6;
	public static final int ACTION_DIMENSIONALITY = 8;

	public static final int NHeading = 8;
	public static final int NTargetDistance = 3;
	public static final int NTargetBearing = 8;
	public static final int NIsAiming = 2;
	//neglecting the following two states for space reduction
	public static final int NSelfEnergy = 5;
	public static final int NWallDistance = 3;
	public static double[][] table;

	public static final int STATE_DIMENSIONALITY;

	public static final int StateTable[][][][];

	static {
		filecounterrecord = new double[301];
		WinCount = 0;
		RoundCount = 0;
		StateTable = new int[NHeading][NTargetDistance][NTargetBearing][NIsAiming];
		int stateNumberCounter = 0;

		for (int b = 0; b < NHeading; b++) {
			for (int c = 0; c < NTargetDistance; c++) {
				for (int d = 0; d < NTargetBearing; d++) {
					for (int f = 0; f < NIsAiming; f++) {
						StateTable[b][c][d][f] = stateNumberCounter;
						stateNumberCounter++;
					}
				}
			}
		}

		STATE_DIMENSIONALITY = stateNumberCounter;
		table = new double[LUT.STATE_DIMENSIONALITY][LUT.ACTION_DIMENSIONALITY];
		for (int i = 0; i < LUT.STATE_DIMENSIONALITY; i++)
			for (int j = 0; j < LUT.ACTION_DIMENSIONALITY; j++)
				table[i][j] = 0; // = random.nextDouble()-1;
	}

	public static int calculateTargetDistance(double TargetDistance) {
		int buffer = (int) (TargetDistance / 50.0);
		if (buffer <=2) {
			return 0;

		} else if (buffer < 7) {
			return 1;
		} else
			return 2;
	}

	public static int calculateHeading(double HeadingAngle) {
		return (int) (HeadingAngle / 45);
	}

	public static int calculateTargetBearing(double TargetBearingAngle) {
		int NHeadingState;
		double angle = (TargetBearingAngle + Math.PI) / Math.PI / 2 * 360;
		NHeadingState = (int) (angle / 45);

		return NHeadingState;
	}

	public static int calculateSelfEnergy(double SelfEnergy) {
		int buffer = (int) (SelfEnergy / 10.0);

		if (buffer == 0) {
			return 0;
		} else if (buffer == 1) {
			return 1;
		} else if (buffer >= 2 && buffer < 4) {
			return 2;
		} else if (buffer >= 4 && buffer <= 7) {
			return 3;
		}
		return 4;
	}

	public static int getHeading(double heading) {
		return (int) (heading / 45);
	}

	public static int calculateWallDistance(double x, double y, int mapwidthx, int mapheighty) {
		if(x<50||x>750||y<50||y>550)
			return 0;
		if(x<150||x>650||y<150||y>350)
			return 1;

		return 2;
	}
}
