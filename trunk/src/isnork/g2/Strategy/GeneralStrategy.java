package isnork.g2.Strategy;

import java.awt.geom.Point2D;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import isnork.g2.SeaBoard;
import isnork.g2.SeaCreature;
import isnork.sim.Observation;
import isnork.sim.SeaLifePrototype;
import isnork.sim.GameObject.Direction;

public class GeneralStrategy extends Strategy {

	private static final int TIME_TO_GO_HOME = 45;

	private Logger log = Logger.getLogger(this.getClass());
	public int myId = 1;
	public boolean goingOut = false;
	public Direction outDirection = null;
	
	public GeneralStrategy(int p, int d, int r,
			Set<SeaLifePrototype> seaLifePossibilites, Random rand, int id) {

		super(p, d, r, seaLifePossibilites, rand);
		myId = id;
		outDirection = getRandomDirection();
	}

	private Direction getRandomDirection() {
		return Strategy.directions.get(random.nextInt(8));
	}

	@Override
	public Direction getMove() {
		
		//do backtrack
		//add in waitawhile patch
		
		/*If we are on the boat with our max happiness then don't do anything*/
		if(this.myHappiness >= board.getMaxScore() && whereIAm == boat)
			return null;
		
		/*
		 * Desperate measure. Get back on the boat. This is to avoid the
		 * situation where the divers come near the boat and then move away. I
		 * found this happening with our current condition
		 */
		/*
		 * If condition to determine when to start heading back. Boat constant
		 * gives you a few extra rounds to head back, and dividing by three
		 * accounts for the fact that you can only make diagonal moves once
		 * every three rounds
		 */
		//where did we get TIME_TO_GO_HOME from?
		if (roundsleft < TIME_TO_GO_HOME 
				|| (whereIAm.distance(boat) > (boatConstant * roundsleft) / 3)
				|| this.myHappiness >= board.getMaxScore())
			return backtrack();

		// If there are dangerous creatures nearby, run like hell.
		if (board.areThereDangerousCreatures(this.whatISee)) 
		{
			//What do these first two lines of code do?
			ArrayList<Point2D> positionOfDangerousCreatures = new ArrayList<Point2D>();
			positionOfDangerousCreatures = board.getDangerousPositions();
			
			ArrayList<Direction> directionsToAvoid = board.getHarmfulDirections(this.whereIAm, this.boat);
			return runAwayFromDanger(directionsToAvoid);
		}

		/**
		 * NO DANGEROUS ANIMALS AROUND
		 */
		
		//if he's at the boat, generate a random direction and go out
		if(!goingOut && whereIAm.getX()==distance && whereIAm.getY()==distance)
		{
			goingOut = true;
			outDirection = getRandomDirection();
			return outDirection;
		}
		
		//if he's going out and reached an edge, start coming back to the boat
		if(goingOut && !(whereIAm.getX() < radius || whereIAm.getX() > board.board.length-radius ||
				whereIAm.getY() < radius || whereIAm.getY() > board.board.length-radius))
		{
			return outDirection;
		}
		
		//if he's going out and reached an edge, start coming back to the boat
		if(goingOut && (whereIAm.getX() < radius || whereIAm.getX() > board.board.length-radius ||
				whereIAm.getY() < radius || whereIAm.getY() > board.board.length-radius))
		{
			goingOut = false;
			outDirection = null;
			return backtrack();
		}
		
		//if he's coming in and not at the boat, keep coming in
		if(!goingOut && !(whereIAm.getX()==distance && whereIAm.getY()==distance))
		{
			return backtrack();
		}

		if(!goingOut)
		{
			return backtrack();
		}
		
		if(outDirection == null)
		{
			outDirection = randomMove();
			return outDirection;
		}

		return outDirection;
	}

	public Direction runAwayFromDanger(ArrayList<Direction> harmfulDirections) {
		ArrayList<Direction> safeMoves = getOpposites(harmfulDirections);
		if (!safeMoves.isEmpty()) 
		{
			Collections.shuffle(safeMoves);
			for (Direction safe : safeMoves) {
				if (board.isValidMove((int) whereIAm.getX(), (int) whereIAm
						.getY(), safe)) {
					return safe;
				}
			}
		}
		/* We are surrounded */
		else {
			return randomMove();
		}
		// We could not find a valid safe move
		return randomMove();
	}

	private ArrayList<Direction> getOpposites(
			ArrayList<Direction> harmfulDirections) {
		ArrayList<Direction> opposites = new ArrayList<Direction>();
		for (Direction d : harmfulDirections) {

			if (d.equals(Direction.N)) {
				if (!harmfulDirections.contains(Direction.S)) {
					opposites.add(Direction.S);
				}
			}

			if (d.equals(Direction.NW)) {
				if (!harmfulDirections.contains(Direction.SE)) {
					opposites.add(Direction.SE);
				}
			}

			if (d.equals(Direction.NE)) {
				if (!harmfulDirections.contains(Direction.SW)) {
					opposites.add(Direction.SW);
				}
			}

			if (d.equals(Direction.S)) {
				if (!harmfulDirections.contains(Direction.N)) {
					opposites.add(Direction.N);
				}
			}

			if (d.equals(Direction.SE)) {
				if (!harmfulDirections.contains(Direction.NW)) {
					opposites.add(Direction.NW);
				}
			}

			if (d.equals(Direction.SW)) {
				if (!harmfulDirections.contains(Direction.NE)) {
					opposites.add(Direction.NE);
				}
			}

			if (d.equals(Direction.E)) {
				if (!harmfulDirections.contains(Direction.W)) {
					opposites.add(Direction.W);
				}
			}
			if (d.equals(Direction.W)) {
				if (!harmfulDirections.contains(Direction.E)) {
					opposites.add(Direction.E);
				}
			}

		}
		return opposites;
	}

	/** Dumb move included with dumb player */
	public Direction randomMove() {
		log.trace("random move");
		Direction d = getRandomDirection();
		while (!board.isValidMove((int) whereIAm.getX(), (int) whereIAm.getY(), d)) 
		{
			d = getRandomDirection();
		}
		return d;
	}

	/** Move to get the player back to the boat */
	public Direction backtrack() {
		double currX = whereIAm.getX();
		double currY = whereIAm.getY();
		log.trace("Current X: " + currX + " Current Y: " + currY);
		if (currX == boat.getX() && currY == boat.getY()) {
			// stay put, we have reached the boat
			return null;
		}
		
		//in a quadrant
		if (currX > boat.getX() && currY > boat.getY()) {
			return Direction.NW;
		}
		if (currX < boat.getX() && currY < boat.getY()) {
			return Direction.SE;
		}
		if (currX < boat.getX() && currY > boat.getY()) {
			return Direction.NE;
		}
		if (currX > boat.getX() && currY < boat.getY()) {
			return Direction.SW;
		}	
		
		//on a line
		if (currX < boat.getX() && currY > boat.getY() || currX < boat.getX()
				&& currY == boat.getY()) {
			return Direction.E;
		}
		if (currX > boat.getX() && currY < boat.getY() || currX == boat.getX()
				&& currY < boat.getY()) {
			return Direction.S;
		}

		if (currX == boat.getX() && currY > boat.getY()) {
			return Direction.N;
		}
		if (currX > boat.getX() && currY == boat.getY()) {
			return Direction.W;
		}
		return null;
	}

	/**
	 * Rate the creatures using several heuristics.
	 * These are to determine the ranking at which creatures will be placed
	 * on the lettering of the iSnork. Creatures are ranked by their happiness,
	 * their frequency, and the amount of the board you can see.
	 */
	public void rateCreatures(Set<SeaLifePrototype> seaLifePossibilities)
	{
		//calculate each of the sea creature's ranking value
		for(SeaCreature sc : creatureRating)
		{
			//happiness x avg frequency
			double ranking = sc.returnCreature().getHappiness() * 
				(sc.returnCreature().getMinCount() + sc.returnCreature().getMaxCount()) / 2.0;
			sc.ranking = ranking;
		}
		
		Collections.sort(creatureRating);
		Collections.reverse(creatureRating);
	}

	@Override
	public String toIsnork() {
		
		if(board.getHighScoringCreatureInRadius() != null){
			return Character.toString(board.getHighScoringCreatureInRadius().
					returnCreature().getName().toCharArray()[0]).toLowerCase();
			}
		else
			return null;
	}
}
