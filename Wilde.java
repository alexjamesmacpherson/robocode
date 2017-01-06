package u1409675;
import u1409675.additional.*;
import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;

/**
 * Wilde: robot by Alex Macpherson, u1409675 (because it's just wild.)
 * 
 * Additional Files:
 * 	~ Enemy.java: an object in which relevant information on enemies is stored for targeting and movement.
 *	~ BulletWave.java: an object representing a single wave of virtual bullets, utilised to build more accurate firing angle guesses over time based on the enemy's location, heading, energy, etc.
 * 
 * Strategies:
 * 	<> Targeting: Guess Factor-based, implementing a wave-based virtual bullet system to quickly learn possible bullet hit angles based on enemy movement patterns. Prioritises closest known enemy as target to fire at. Versatile across both melee & 1v1; segmented to allow more accurate guesses in 1v1 combat, where scans are more frequent.
 * 	<> Movement: Minimum Risk for melee, Psuedo-Random for 1v1 (attempting to remain perpendicular where possible for maximum escape angle from incoming bullets).
 * 	<> Radar: Weak Target Lock, attempting to remain focused on target but scanning entire battlefield when target is lost (often) to either find existing or a better (closer) target.
 * 
 * References:
 * 	-> Robowiki Tutorials:
 * 		>> GuessFactor Targeting Tutorial (http://robowiki.net/wiki/GuessFactor_Targeting_Tutorial)*
 * 		>> Anti-Gravity Movement Tutorial (http://robowiki.net/wiki/Anti-Gravity_Tutorial)*
 * 	-> Robowiki Strategy Guides:
 * 		>> Robocode FAQ (http://robowiki.net/wiki/Robocode/FAQ)
 * 		>> Melee Strategy (http://robowiki.net/wiki/Melee_Strategy)*
 * 		>> Understanding Coriantumr (http://robowiki.net/wiki/Melee_Strategy/Understanding_Coriantumr)
 * 		>> Minimum Risk Movement (http://robowiki.net/wiki/Minimum_Risk_Movement)*
 *	-> Existing Robots:
 *		>> HawkOnFire - Rezu (http://robowiki.net/wiki/HawkOnFire)
 *		>> Coriantumr - Kawigi (http://robowiki.net/wiki/Coriantumr)
 *		>> Girl - Kawigi (http://old.robowiki.net/robowiki?Girl)*
 *		>> Walls, TrackFire & other Sample bots
 * References marked with * influenced the final design most heavily.
 * Other discarded design choices are discussed in the report.
 **/

public class Wilde extends robocode.Robot {
	Point2D.Double myLoc, prevLoc, nextLoc;
	HashMap<String,Enemy> enemies;		// HashMap stores information on enemies when scanned
	Enemy target;
	static HashMap<String,int[][][][]> statStore = new HashMap<String,int[][][][]>();	// HashMap stores segmented guessfactor information for enemies, static to persist through rounds.
	int direction = 1;		// Direction used in guessfactor. Global variable to store between shots.
	double perpendicularDirection = 1;
	int hits;
	
	public void run() {
	// Colour bot:
		setBodyColor(Color.white);
		setGunColor(Color.pink);
		setRadarColor(Color.black);
		setBulletColor(Color.pink);
		setScanColor(Color.pink);
		
	// Initialise all vars:
		enemies = new HashMap<String,Enemy>();
		target = null;
		Rectangle2D battlefield = new Rectangle2D.Double(50, 50, getBattleFieldWidth() - 100, getBattleFieldHeight() - 100);	// Battlefield bounded by a further 50px on each side for wall avoidance, preventing collisions & ease of targeting by wall-crawlers
		nextLoc = null;
		hits = 0;
		
		while(true) {
			myLoc = new Point2D.Double(getX(), getY());
		// Scanning, targeting and firing:
			if(target == null) {		// If no target, spin radar to acquire one
				turnRadarRight(360);
			} else {					// Otherwise spin to face and fire at target
				double radarAngle = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(calcAngle(myLoc, target.loc)) - getRadarHeading());
				target = null;
				turnRadarRight(radarAngle);
				if(target == null) {	// If target has moved, acquire new target
					turnRadarRight(radarAngle < 0 ? -360 - radarAngle : 360 - radarAngle);
				}
			}
		// Movement, only moves if a target exists, keeps scanning otherwise:
			if(target != null) {
				if(getOthers() > 1) {
					if(nextLoc == null) {
						nextLoc = prevLoc = myLoc;
					}
				// Generates and assesses 100 points for their risk, selecting the point of least risk
					for(int i = 0; i < 100; i++) {
						double d = (Math.random() * 100) + 100;
						Point2D.Double p = calcPoint(myLoc, Math.toRadians(Math.random() * 360), d);
						if(battlefield.contains(p) && (calcRisk(p) < calcRisk(nextLoc))) {
							nextLoc = p;
						}
					}
				} else {
				// Attempts to remain perpendicular in 1v1:
					double d = (Math.random() * 100) + 150;
				// Changes perpendicular direction of movement to enemy if a valid point (at least absolute enemy bearing +- 60deg) does not exist in the current direction OR pseudo-randomly, hindering enemy targeting algorithms. Randomness largely based on the number of enemy hits landed, striking a balance between excellent head-on targeting avoidance and a reasonable level of true randomness to deter more advanced targeting algorithms.
					if(!battlefield.contains(calcPoint(myLoc, calcAngle(myLoc, target.loc) + Math.PI / 3 * perpendicularDirection, d)) || ((Math.random() * (hits % 5) > 0.6))) {
						perpendicularDirection = -perpendicularDirection;
					}
				// If possible, will select an angle perpendicular to enemy, else finds nearest valid angle
					double angle = calcAngle(myLoc, target.loc) + (Math.PI / 2) * perpendicularDirection;
					while(!battlefield.contains(calcPoint(myLoc, angle, d))) {
						angle -= perpendicularDirection * 0.1;
					}
					nextLoc = calcPoint(myLoc, angle, d);
				}
			// Calculate absolute distance and angle to point; update prevLoc
				double distance = myLoc.distance(nextLoc);
				double moveAngle = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(calcAngle(myLoc, nextLoc)) - getHeading());
				prevLoc = myLoc;
				
			// Calculate values for smallest turn and movement to reach point
				if(Math.abs(moveAngle) > 90) {
					moveAngle = robocode.util.Utils.normalRelativeAngleDegrees(moveAngle + 180);
					distance = -distance;
				}
				turnRight(moveAngle);
				ahead(distance);
			}
		}
	}
	
// Scan robot, add to records
	public void onScannedRobot(ScannedRobotEvent e) {
	// Add/update enemy record:
		String name = e.getName();
		Enemy enemy;
		if(enemies.get(name) == null) {
			enemy = new Enemy(name, calcPoint(myLoc, Math.toRadians(getHeading() + e.getBearing()), e.getDistance()), e.getEnergy(), e.getBearing(), e.getHeading(), new Vector<BulletWave>());
		} else {
			enemy = new Enemy(name, calcPoint(myLoc, Math.toRadians(getHeading() + e.getBearing()), e.getDistance()), e.getEnergy(), e.getBearing(), e.getHeading(), enemies.get(name).waves);
		}
		enemies.put(name, enemy);
		
	// If necessary, update target:
		if((target == null) || (target.name.equals(enemy.name)) || (e.getDistance() < target.loc.distance(myLoc))) {
			target = enemy;
		}
		
	// Get/create enemy stat records:
		int[][][][] stats = statStore.get(name.split(" ")[0]);	// Robots of the same type are named "XXX (#)"; by combining data of bots with the same AI, guessfactors can be learned far more quickly if facing multiple bots of the same type.
		if(stats == null) {
			stats = new int[2][9][13][31];		// Segments guessfactor stats according to melee/1v1, lateral direction (towards/away/parallel movement OR not moving), enemy distance (max scan distance = 1200px, one segment per 100px), with 31 possible unique 'guessfactors'
			statStore.put(name.split(" ")[0], stats);
		}
		
	// Simple distance-based bullet power function. Far away enemy => lower bullet power => higher bullet speed => higher accuracy (hopefully). Always: 1 <= Power <= 3
		double power = getOthers() > 1 ? 3 : Math.min(3, Math.max(600 / e.getDistance(), 1));
		double absoluteBearing = Math.toRadians(getHeading() + enemy.bearing);
		
	// Only update direction if moving: it is likely movement will resume in same direction after standstill to scan/fire.
		if(e.getVelocity() != 0) {
			if(Math.sin(Math.toRadians(enemy.heading) - absoluteBearing) * e.getVelocity() < 0) {
				direction = -1;
			} else {
				direction = 1;
			}
		}
		
	// Fetch existing guessfactor stats according to enemy status:
		int[] currentStats = stats[getOthers() > 1 ? 0 : 1][(int) (e.getVelocity() == 0 ? 8 : Math.abs(Math.sin(Math.toRadians(enemy.heading) - absoluteBearing) * e.getVelocity() / 3))][(int) (e.getDistance() / 100)];
		
	// Create and add new wave; update all existing waves:
		BulletWave newWave = new BulletWave(myLoc, enemy.loc, absoluteBearing, power, getTime(), direction, currentStats, getTime() - 1);
		enemy.waves.add(newWave);
		for(int i = 0; i < enemy.waves.size(); i++) {
			BulletWave currentWave = enemy.waves.get(i);
			if(currentWave.waveHit(enemy.loc, getTime())) {
				enemy.waves.remove(currentWave);
				i--;
			}
		}
		
	// Only fires at target; only fires if it will not disable (can't outgun, but might outlive enemies)
		if((enemy == target) && (power < getEnergy())) {
			int bestindex = 15;
			for(int i = 0; i < 31; i++) {
				if(currentStats[bestindex] < currentStats[i]) {
					bestindex = i;
				}
			}
			
			double guessFactor = (double)(bestindex - (currentStats.length - 1) / 2) / ((currentStats.length - 1) / 2);
			double angleOffset = direction * guessFactor * newWave.maxEscapeAngle();
			double gunAdjust = Math.toDegrees(robocode.util.Utils.normalRelativeAngle(absoluteBearing - Math.toRadians(getGunHeading()) + angleOffset));
		// Desired improvement: only fire if projected target destination is within battlefield
			turnGunRight(gunAdjust);
			fire(power);
		}
	}
	
// Used in 1v1 to allow change of direction if enemy has hit in the current direction, helping to lower targeting accuracy:
	public void onHitByBullet(HitByBulletEvent e) {
		if(getOthers() == 1) {
			hits++;
		}
	}
	
// Remove robot from records on death
	public void onRobotDeath(RobotDeathEvent e) {
		enemies.remove(e.getName());
		if((target != null) && (target.name.equals(e.getName()))) {
			target = null;
		}
	}
	
// "Victory Dance"
	public void onWin(WinEvent e) {
		turnRight(15);
		while(true) {
			turnLeft(30);
			turnRight(30);
		}
	}
	
// Function calculates the risk incurred by moving to a given point:
	public double calcRisk(Point2D point) {
		double risk = 0;
		Iterator<Enemy> it = enemies.values().iterator();
	// Uses antigravity to repel from enemies; also accounts for high-energy enemies being a greater risk
		while(it.hasNext()) {
			Enemy enemy = it.next();
			risk += (enemy.energy + 50) / point.distanceSq(enemy.loc);
		}
	// Repels from last and current locations to prevent staying too close to a single spot
		risk += 0.1 / point.distanceSq(prevLoc);
		risk += 0.1 / point.distanceSq(myLoc);
		
		return risk;
	}
	
// Function calculates a point at a given angle to and distance from an origin point:
	public Point2D.Double calcPoint(Point2D origin, double angle, double distance) {
		return new Point2D.Double(origin.getX() + distance * Math.sin(angle), origin.getY() + distance * Math.cos(angle));
	}
	
// Function calculates the angle between 2 points:
	public double calcAngle(Point2D p, Point2D q) {
		return Math.atan2(q.getX() - p.getX(), q.getY() - p.getY());
	}
}