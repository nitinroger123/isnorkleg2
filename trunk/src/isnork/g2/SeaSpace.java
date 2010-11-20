package isnork.g2;

import isnork.sim.GameObject.Direction;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/** Class to represent a space on the board */
public class SeaSpace {
	
	public SeaSpace(Point2D p){
		location = p;
		occupiedby = new ArrayList<SeaCreature>();
	}

	public Point2D getPoint(){
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
	
	
	/**Tells us if there is a dangerous creature on this space*/
	public Boolean hasDanger(int r){		
		for(SeaCreature o: occupiedby){
			if(o.returnCreture().isDangerous() && o.getLastseen() == r){
				System.err.println("Danger from: " + o.getId());
				return true;
			}
		}
		return false;
	}
	
	/**Returns the direction from the diver to the creature*/
	public Direction getDirection(Point2D me) {

		if(me.getX() == location.getX() && me.getY() > location.getY())
			return Direction.S;
		
		if(me.getX() == location.getX() && me.getY() < location.getY())
			return Direction.N;
		
		if(me.getX() > location.getX() && me.getY() == location.getY())
			return Direction.W;
		
		if(me.getX() < location.getX() && me.getY() == location.getY())
			return Direction.E;
		
		if(me.getX() > location.getX() && me.getY() > location.getY())
			return Direction.SE;
		
		if(me.getX() > location.getX() && me.getY() < location.getY())
			return Direction.SW;
		
		if(me.getX() > location.getX() && me.getY() < location.getY())
			return Direction.NW;
		
		if(me.getX() < location.getX() && me.getY() < location.getY())
			return Direction.NW;
		
		return null;
	}

	private ArrayList<SeaCreature> occupiedby;
	private int roundset;
	private Point2D location;
	
	
}
