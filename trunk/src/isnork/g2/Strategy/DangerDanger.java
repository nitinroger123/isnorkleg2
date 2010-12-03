package isnork.g2.Strategy;

import isnork.g2.utilities.SeaBoard;
import isnork.sim.Observation;
import isnork.sim.SeaLifePrototype;
import isnork.sim.iSnorkMessage;
import isnork.sim.GameObject.Direction;

import java.awt.geom.Point2D;
import java.util.Random;
import java.util.Set;

public class DangerDanger extends Strategy{
	
	public DangerDanger(int p, int d, int r,
			Set<SeaLifePrototype> seaLifePossibilities, Random rand, int id,
			int numDivers, SeaBoard b) {
		super(p, d, r, seaLifePossibilities, rand, id, numDivers, b);
	}

	@Override
	public void checkFoundGoal(Set<iSnorkMessage> incomingMessages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Direction getMove() {
		

		if (whereIAm.equals(boat) && board.getSeaSpace(boat).hasDanger()) {
			return null;
		}
		
		if (whereIAm.equals(boat) && board.areThereDangerousCreaturesInRadius(whatISee, whereIAm, smallradius)) {
			
			return null;
		}
		
		if(this.myHappiness == board.getMaxScore() && whereIAm.equals(boat))
			return null;
		
		else //we have not reached maximum happiness and it is safe to move
		{			
			if(!whereIAm.equals(boat))
				return getBackOnBoat();
			
			return randomMove();
		}
		
	}

	private Direction getBackOnBoat() {
		
		Point2D goal = boat;
		
		double currX = whereIAm.getX();
		double currY = whereIAm.getY();
		if (currX == goal.getX() && currY == goal.getY()) {
			// stay put, we have reached the goal
			return null;
		}

		// in a quadrant
		if (currX > goal.getX() && currY > goal.getY()) {
			return Direction.NW;
		}
		if (currX < goal.getX() && currY < goal.getY()) {
			return Direction.SE;
		}
		if (currX < goal.getX() && currY > goal.getY()) {
			return Direction.NE;
		}
		if (currX > goal.getX() && currY < goal.getY()) {
			return Direction.SW;
		}
		
		// on a line
		if (currX < goal.getX() && currY > goal.getY() || currX < goal.getX()
				&& currY == goal.getY()) {
			return Direction.E;
		}
		if (currX > goal.getX() && currY < goal.getY() || currX == goal.getX()
				&& currY < goal.getY()) {
			return Direction.S;
		}

		if (currX == goal.getX() && currY > goal.getY()) {
			return Direction.N;
		}
		if (currX > goal.getX() && currY == goal.getY()) {
			return Direction.W;
		}
		
		return null;
	}

	@Override
	public String getTick(Set<Observation> whatYouSee) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rateCreatures(Set<SeaLifePrototype> seaLifePossibilities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateIncomingMessages(Set<iSnorkMessage> incomingMessages) {
		// TODO Auto-generated method stub
		
	}

}
