package RflRobot;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import NeuralNetWork.NeurualNetWork;

import java.awt.event.KeyEvent;
/**
 *
 * @author kl
 */
//import robocode.Robot;
import robocode.*;
import robocode.AdvancedRobot;

public class RFLROBOCODE extends AdvancedRobot {
	public static final double PI = Math.PI;
	public int filecounter = 1;

	File winFile;
	File lutFile1;
	private TargetInfo target;
	private RLearning table;
	private double reinforcement = 0.0;
	private double firePower;
	private int isAiming = 0;

	// LUT lut=new LUT();
	// File file = new File(path);
	/**
	 * @param args
	 *            the command line arguments
	 */

	// public static void main(String[] args) {
	// TODO code application logic here

	// }
	public RFLROBOCODE() {
		table = new RLearning();

	}

	public void run() {

		// loadData();
		winFile = getDataFile("win_ratio.txt");
		lutFile1=getDataFile("Action1.csv");

		
		target = new TargetInfo();
		target.distance = 100000;

		setColors(Color.green, Color.white, Color.green);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRight(360);
		int state = getState();
		///ssa int action = table.selectAction(state);
		while (true) {
			// radarMovement();
			// gunMovement();
			// robotMovement();
			// choose a from state buffer using policy
			//int action = table.selectAction(state);
	int action = table.selectAction(state);	//// (Q)

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
			// take action a

			execute();
			if (getTime() - target.ctime > 1)
				isAiming = 0;
			// observe r,s'
			state = getState();
			// renew Q(s,a)
			// if(reinforcement!=0)
		//////(sa)action = table.selectAction(state);
			table.learn(state, action, reinforcement);

			reinforcement = 0.0;
			// save s' to state buffer

		}

	}

	private int getState() {
		int heading = LUT.calculateHeading(getHeading());
		int targetDistance = LUT.calculateTargetDistance(target.distance);
		int targetBearing = LUT.calculateTargetBearing(target.bearing);
		// int selfenergy1=LUT.calculateSelfEnergy(getEnergy());
		int walldistance = LUT.calculateWallDistance(getX(), getY(), 800, 600);

		////////////////////////////// out.println("Stste(" + "heading" + heading +
		////////////////////////////// ",targetDistance: + " + targetDistance +
		////////////////////////////// ",targetBearing "
		////////////////////////////// + targetBearing + ",walldistance " + walldistance
		////////////////////////////// + ",isAiming " + isAiming + " )");
		int state = LUT.StateTable[heading][targetDistance][targetBearing][isAiming];
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
		if (target.name == e.getName()) {
			// double power = e.getBullet().getPower();
			// double change = 4 * power + 2 * (power - 1);
			// double change = e.getBullet().getPower() * 20;
			/////////////////// out.println("Bullet Hit: " + "20");
			reinforcement += 20;
		}
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
		//
	}

	public void onBulletMissed(BulletMissedEvent e) {
		// double change = -e.getBullet().getPower();
		//////////////////////////// out.println("Bullet Missed: " + "-5");
		reinforcement -= 5;
	}

	public void onHitByBullet(HitByBulletEvent e) {
		if (target.name == e.getName()) {
			// double power = e.getBullet().getPower();
			// double change = -5*(4 * power + 2 * (power - 1));
			////////////////////////// out.println("Hit By Bullet: " + "-20");
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
		////////////////// out.println("Hit Wall: " + change);
		reinforcement += change;
		// isHitWall = 1;
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		isAiming = 1;
		/*
		 
		 */
		// reinforcement+=1;
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

		if (e.getName() == target.name)
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
					RobocodeFileWriter file1 = new RobocodeFileWriter(winFile);
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

				///////////////// System.out.println("////////////////////////");
				///////////////// System.out.println(LUT.RoundCount);
				///////////////// System.out.println("////////////////////////");
				/*
				 * if (LUT.RoundCount % 100 == 0) {try { saveData(); } catch (IOException e) {
				 * // TODO Auto-generated catch block e.printStackTrace(); }
				 */
				/*
								 */

			}
			
			if (LUT.RoundCount == 3000) {
				try {
					RobocodeFileWriter fileWriter = new RobocodeFileWriter(lutFile1);
				
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

	public void onDeath(DeathEvent event) {
		LUT.RoundCount++;
		filecounter += 1;
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
					RobocodeFileWriter file1 = new RobocodeFileWriter(winFile);
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
				
				try {RobocodeFileWriter fileWriter = new RobocodeFileWriter(lutFile1);
					
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
			//2
			
			
	
		}
	}

	public void saveData() throws IOException {
		
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
