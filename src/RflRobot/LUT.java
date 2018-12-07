package RflRobot;

import java.util.Random;

public class LUT {
	public static int WinCount;
	public static int RoundCount;
	public static double filecounterrecord[];
	public static final int GoAhead = 7;
	public static final int GoBack = 1;
	public static final int GoFWDLeft = 2;
	public static final int GoFWDRight = 3;
	public static final int GoBWDLeft = 4;
	public static final int GoBWDRight = 5;
	public static final int GoFireAtWill = 6;
	public static final int GoFindATarget = 0;
	// public static int GoFireOne = 6;
	public static final double OneStepDistance = 100.0;
	public static final double OneStepAngle = 30;
	public static final int NHowManyAction = 8;

	// public static final int NSelfEnergy = 4;
	public static final int NHeading = 8;
	public static final int NTargetDistance = 3;
	public static final int NTargetBearing = 8;
	//public static final int NWallDistance = 53;
	public static final int NIsAiming = 2;
	public static double[][] table;

	public static final int NHowManyState;

	public static final int StateTable[][][][];

	static {
		filecounterrecord = new double[301];
		WinCount = 0;
		RoundCount = 0;
		StateTable = new int[NHeading][NTargetDistance][NTargetBearing][NIsAiming];
		int count = 0;

		for (int b = 0; b < NHeading; b++) {
			for (int c = 0; c < NTargetDistance; c++) {
				for (int d = 0; d < NTargetBearing; d++) {
					
						for (int f = 0; f < NIsAiming; f++) {
							StateTable[b][c][d][f] = count;
							count++;
						}
					}
				}
			}
		

		NHowManyState = count;
		table = new double[LUT.NHowManyState][LUT.NHowManyAction];
		/////Random random = new Random();
		for (int i = 0; i < LUT.NHowManyState; i++)
			for (int j = 0; j < LUT.NHowManyAction; j++)
				table[i][j] = 0; // = random.nextDouble()-1;
	}

	public void InitializeLUT() {

	}

	public static int calculateTargetDistance(double TargetDistance) {
		int buffer = (int) (TargetDistance / 50.0);

		if (buffer == 1||buffer==0) {
			return 0;
		
		} else if (buffer >= 2 && buffer < 5) {
			return 1;
		} else if (buffer >= 5 )
		

		return 2;
		return 2;
	}

	public static int calculateHeading(double HeadingAngle) {
	

		return (int) (HeadingAngle / 45);

		// return NHeadingState;
	}

	public static int calculateTargetBearing(double TargetBearingAngle) {
		int NHeadingState;
		double angle = (TargetBearingAngle + Math.PI) / Math.PI / 2 * 360;
		// double OrgnizedAngle = TargetBearingAngle + angle / 2;
		NHeadingState = (int) (angle / 45);

		return NHeadingState;
	}

	public static int calculateSelfEnergy(double SelfEnergy) {
		int buffer = (int) (SelfEnergy / 10.0);

		if (buffer == 0) {
			return 0;
		} else if (buffer == 1) {
			return 1;
		} else if (buffer >= 2 && buffer < 5) {
			return 2;
		} else if (buffer >= 5 && buffer <= 10) {
			return 3;
		}
		return 3;
	}

	public static int getHeading(double heading) {
		// TODO Auto-generated method stub
		return (int) (heading / 45);
	}

	public static int calculateWallDistance(double x, double y, int mapwidthx, int mapheighty) {

		
		return (int) (x / 100 * 6 + y / 100);

	}
}
