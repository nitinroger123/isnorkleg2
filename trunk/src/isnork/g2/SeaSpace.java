package isnork.g2;

import isnork.sim.GameObject.Direction;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/** Class to represent a space on the board! */
public class SeaSpace {

	public SeaSpace(Point2D p) {
		location = p;
		center = new Point2D.Double(p.getX() + .5, p.getY() + .5);
		occupiedby = new ArrayList<SeaCreature>();
		log = Logger.getLogger(this.getClass());
	}

	public Point2D getPoint() {
		return location;
	}

	public void set(ArrayList<SeaCreature> occupiedby, int r) {
		roundset = r;
		this.occupiedby = occupiedby;

	}

	public ArrayList<SeaCreature> getOccupiedby() {
		return occupiedby;
	}

	public boolean isoccupideby(int id) {

		for (SeaCreature s : occupiedby) {
			if (s.getId() == id)
				return true;
		}

		return false;
	}

	public void remove(int id) {

		SeaCreature temp = null;
		for (SeaCreature s : occupiedby) {
			if (s.getId() == id)
				temp = s;
		}

		occupiedby.remove(temp);
	}

	public void addCreature(SeaCreature c, int r) {
		c.setLastSeen(r);
		occupiedby.add(c);
	}

	/** Tells us if there is a dangerous creature on this space */
	public Boolean hasDanger() {
		for (SeaCreature o : occupiedby) {
			// if(o.returnCreture().isDangerous() && o.getLastseen() == r){
			if (o.returnCreature().isDangerous()) {
				log.trace("Danger from: " + o.getId() + " on space: "
						+ this.location);
				return true;
			}
		}
		return false;
	}

	/** Returns the direction from the diver to the creature */
	public ArrayList<Direction> getDirection(Point2D me) {
		
		log.trace("i am on: " + me);
		ArrayList<Direction> temp = new ArrayList<Direction>();

		if (me.getX() == location.getX() && me.getY() > location.getY()) {
			temp.add(Direction.N);
			temp.add(Direction.NE);
			temp.add(Direction.NW);
		}

		if (me.getX() == location.getX() && me.getY() < location.getY()) {
			temp.add(Direction.S);
			temp.add(Direction.SE);
			temp.add(Direction.SW);
		}

		if (me.getX() > location.getX() && me.getY() == location.getY()) {
			temp.add(Direction.W);
			temp.add(Direction.NW);
			temp.add(Direction.SW);
		}
		if (me.getX() < location.getX() && me.getY() == location.getY()) {
			temp.add(Direction.E);
			temp.add(Direction.NE);
			temp.add(Direction.SE);
		}
		if (me.getX() < location.getX() && me.getY() > location.getY()) {
			temp.add(Direction.NE);
			temp.add(Direction.N);
			temp.add(Direction.E);
		}
		if (me.getX() < location.getX() && me.getY() < location.getY()) {
			temp.add(Direction.SE);
			temp.add(Direction.S);
			temp.add(Direction.E);
		}
		if (me.getX() > location.getX() && me.getY() > location.getY()) {
			temp.add(Direction.NW);
			temp.add(Direction.N);
			temp.add(Direction.W);
		}
		if (me.getX() > location.getX() && me.getY() < location.getY()) {
			temp.add(Direction.SW);
			temp.add(Direction.S);
			temp.add(Direction.W);
		}
		return temp;
	}

	public Point2D getCenter() {
		return center;
	}

	private ArrayList<SeaCreature> occupiedby;
	private int roundset;
	private Point2D location, center;
	private Logger log;

}
