package isnork.g2.Strategy;

import java.awt.geom.Point2D;

import isnork.sim.GameObject.Direction;

public class BackTrackMove implements Comparable<BackTrackMove>{
	
	public BackTrackMove(Direction d, Boolean b, Boolean s, int r, Point2D i, Point2D bt){
		this.d = d;
		toBoat = b;
		safe = s;
		roundsleft = r;
		whereIAm = i;
		boat = bt;
	}

	public int compareTo(BackTrackMove d) {
		
		if((whereIAm.distance(boat) + 1) > (roundsleft) / 3)
			return seriousBackTrack(d);
			
		else
			return safeBackTrack(d);
	}
	
	private int seriousBackTrack(BackTrackMove d){
		//If it is in the direction of the boat return 1
		if((safe && toBoat))
			return -1;
		
		if(d.toBoat && d.toBoat)
			return 1;
		
		if(toBoat && !d.toBoat)
			return -1;
		
		if(d.toBoat && !toBoat)
			return 1;
		
		if(safe && !d.safe)
			return -1;
		
		if(d.safe && !!safe)
			return 1;
		
		return 0;
	}
	
	private int safeBackTrack(BackTrackMove d){
				
		//If it is in the direction of the boat return 1
		if((safe && toBoat))
			return -1;
		
		if(d.toBoat && d.toBoat)
			return 1;
		
		if(safe && !d.safe)
			return -1;
		
		if(d.safe && !!safe)
			return 1;
		
		if(toBoat && !d.toBoat)
			return -1;
		
		if(d.toBoat && !toBoat)
			return 1;
		
		return 0;
	}
	
	public String toString(){
		return "move in " + d + " toboat: " + toBoat + ", safe: " + safe; 
	}
	public Direction d = null;
	public Boolean toBoat = false;
	public Boolean safe = false;
	private int roundsleft = 0;
	private Point2D whereIAm, boat;
	

}
