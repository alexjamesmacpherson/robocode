package u1409675.additional;
import java.awt.geom.*;

/**
 * BulletWave.java: an object representing a single wave of virtual bullets, utilised to build more accurate firing angle guesses over time based on the enemy's location, heading, energy, etc.
 * 
 * Additional File for Wilde: robot by Alex Macpherson (u1409675).
 * 
 * References:
 * 	-> Robowiki Tutorials:
 * 		>> GuessFactor Targeting Tutorial (http://robowiki.net/wiki/GuessFactor_Targeting_Tutorial)*
 * 	-> Robowiki Strategy Guides:
 * 		>> Melee Strategy (http://robowiki.net/wiki/Melee_Strategy)*
 * 		>> Understanding Coriantumr (http://robowiki.net/wiki/Melee_Strategy/Understanding_Coriantumr)
 *	-> Existing Robots:
 *		>> Coriantumr - Kawigi (http://robowiki.net/wiki/Coriantumr)
 *		>> Girl - Kawigi (http://old.robowiki.net/robowiki?Girl)*
 * References marked with * influenced the final design most heavily.
 * Other discarded design choices are discussed in the report.
 **/

public class BulletWave {
	private Point2D.Double origin, lastKnown;
	private double bearing, power;
	private long fireTime;
	private int direction;
	private int[] returnSegment;
	private long lastTime;
	
	public BulletWave(Point2D.Double location, Point2D.Double enemyLoc, double bearing, double power, long fireTime, int direction, int[] segment, long time) {
		this.origin = location;
		this.lastKnown = enemyLoc;
		this.bearing = bearing;
		this.power = power;
		this.fireTime = fireTime;
		this.direction = direction;
		this.returnSegment = segment;
		lastTime = time;
	}
	
	public double getBulletSpeed() {
		return 20 - power * 3;
	}
	
	public double maxEscapeAngle() {
		return Math.asin(8 / getBulletSpeed());
	}
	
// Function calculates whether a wave hit an enemy, as well as the necessary guessfactor for a bullet of this wave to have hit:
	public boolean waveHit(Point2D.Double enemy, long time) {
		long dt = time - lastTime;
		double dx = (enemy.getX() - lastKnown.getX()) / dt;
		double dy = (enemy.getY() - lastKnown.getY()) / dt;
		
	// As scans are inconsistent, linearly interpolates known data to determine whether a wave could have hit whilst not in view of the radar:
		while(lastTime < time) {
			if(origin.distance(enemy) <= (lastTime - fireTime) * getBulletSpeed()) {
				double desiredDirection = Math.atan2(enemy.getX() - origin.getX(), enemy.getY() - origin.getY());
				double angleOffset = robocode.util.Utils.normalRelativeAngle(desiredDirection - bearing);
				double guessFactor = Math.max(-1, Math.min(1, angleOffset / maxEscapeAngle())) * direction;
				int index = (int) Math.round((returnSegment.length - 1) / 2 * (guessFactor + 1));
				returnSegment[index]++;
				return true;
			}
			lastTime++;
			lastKnown = new Point2D.Double(lastKnown.getX() + dx, lastKnown.getY() + dy);
		}
		return false;
	}
}