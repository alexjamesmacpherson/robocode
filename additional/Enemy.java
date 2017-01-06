package u1409675.additional;
import java.awt.geom.*;
import java.util.*;

/**
 * Enemy.java: an object in which relevant information on enemies is stored for targeting and movement.
 * 
 * Additional File for Wilde: robot by Alex Macpherson (u1409675).
 * 
 * References:
 * 	-> Robowiki Tutorials:
 * 		>> GuessFactor Targeting Tutorial (http://robowiki.net/wiki/GuessFactor_Targeting_Tutorial)*
 * 	-> Robowiki Strategy Guides:
 * 		>> Melee Strategy (http://robowiki.net/wiki/Melee_Strategy)*
 *	-> Existing Robots:
 *		>> Coriantumr - Kawigi (http://robowiki.net/wiki/Coriantumr)
 *		>> Girl - Kawigi (http://old.robowiki.net/robowiki?Girl)*
 * References marked with * influenced the final design most heavily.
 * Other discarded design choices are discussed in the report.
 **/

public class Enemy {
	public String name;
	public Point2D.Double loc;
	public double energy, bearing, heading;
	public Vector<BulletWave> waves;	// BulletWaves are stored per enemy, for ease of access
	
	public Enemy(String name, Point2D.Double loc, double energy, double bearing, double heading, Vector<BulletWave> waves) {
		this.name = name;
		this.loc = loc;
		this.energy = energy;
		this.bearing = bearing;
		this.heading = heading;
		this.waves = waves;
	}
}