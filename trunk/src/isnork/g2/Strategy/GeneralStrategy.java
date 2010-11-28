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
import isnork.sim.iSnorkMessage;
import isnork.sim.GameObject.Direction;

public class GeneralStrategy extends Strategy {

	private static final int TIME_TO_GO_HOME = 45;

	private Logger log = Logger.getLogger(this.getClass());
	public boolean goingOut = false;
	public Direction outDirection = null;

	public GeneralStrategy(int p, int d, int r,
			Set<SeaLifePrototype> seaLifePossibilites, Random rand, int id) {

		super(p, d, r, seaLifePossibilites, rand, id);
		outDirection = getRandomDirection();
	}

	private Direction getRandomDirection() {
		return Strategy.directions.get(random.nextInt(8));
	}

	@Override
	public Direction getMove() {
		
		/*
		 * Desperate measure. Get back on the boat. This is to avoid the
		 * situation where the divers come near the boat and then move away. I
		 * found this happening with our current condition
		 */
		if (roundsleft < TIME_TO_GO_HOME)
			return backtrack();

		/*
		 * If condition to determine when to start heading back. Boat constant
		 * gives you a few extra rounds to head back, and dividing by three
		 * accounts for the fact that you can only make diagonal moves once
		 * every three rounds
		 */
		if (whereIAm.distance(boat) > (boatConstant * roundsleft) / 3)
			return backtrack();

		// If there are dangerous creatures nearby, run like hell.
		if (board.areThereDangerousCreatures(this.whatISee)) 
		{
			//these 2 lines of code are needed:
			ArrayList<Point2D> positionOfDangerousCreatures = new ArrayList<Point2D>();
			positionOfDangerousCreatures = board.getDangerousPositions();
			
			//the rest of the schtuff
			ArrayList<Direction> directionsToAvoid = board.getHarmfulDirections(this.whereIAm);
			return runAwayFromDanger(directionsToAvoid);
		}

		/**
		 * NO DANGEROUS ANIMALS AROUND
		 */
		//if he has a direction in progress, go there!
//		if(intermediateGoal!=null && intermediateGoal.equals(whereIAm))
//		{
//			intermediateGoal = null;
//		}
//		
//		//if he's on route to his goal and there are no dangerous creatures around, go to goal
//		if(intermediateGoal!=null)
//		{
//			return getDirectionToGoal(intermediateGoal);
//		}
		
		//if he's at the boat, generate a random direction and go out
		if(!goingOut && whereIAm.getX()==distance && whereIAm.getY()==distance)
		{
			goingOut = true;
			outDirection = getRandomDirection();
			return outDirection;
		}
		
		//if he's going out and reached an edge, start coming back to the boat
		if(goingOut && !(whereIAm.getX() < radius-1 || whereIAm.getX() > board.board.length-radius+1 ||
				whereIAm.getY() < radius-1 || whereIAm.getY() > board.board.length-radius+1))
		{
			return outDirection;
		}
		
		//if he's going out and reached an edge, start coming back to the boat
		if(goingOut && (whereIAm.getX() < radius-1 || whereIAm.getX() > board.board.length-radius+1 ||
				whereIAm.getY() < radius-1 || whereIAm.getY() > board.board.length-radius+1))
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
	
	private Direction getDirectionToGoal(Point2D goal)
	{
		double currX = whereIAm.getX();
		double currY = whereIAm.getY();
		Direction bestDir = null;
		double shortestDist = Double.MAX_VALUE;
		
		//check which direction will create the next shortest distance to the goal
		for(Direction d : Strategy.directions)
		{
			double newX = currX + d.dx;
			double newY = currY + d.dy;
			double tempDist = goal.distance(newX, newY);
			if(tempDist < shortestDist)
			{
				shortestDist = tempDist;
				bestDir = d;
			}
		}
		
		return bestDir;
	}

	private ArrayList<Direction> getOpposites(
			ArrayList<Direction> harmfulDirections)
	{
		ArrayList<Direction> opposites = new ArrayList<Direction>();
		for (Direction d : harmfulDirections) {

			if (d.equals(Direction.N) && !harmfulDirections.contains(Direction.S))
				opposites.add(Direction.S);

			if (d.equals(Direction.NW) && !harmfulDirections.contains(Direction.SE))
				opposites.add(Direction.SE);

			if (d.equals(Direction.NE) && !harmfulDirections.contains(Direction.SW))
				opposites.add(Direction.SW);

			if (d.equals(Direction.S) && !harmfulDirections.contains(Direction.N))
				opposites.add(Direction.N);

			if (d.equals(Direction.SE) && !harmfulDirections.contains(Direction.NW))
				opposites.add(Direction.NW);

			if (d.equals(Direction.SW) && !harmfulDirections.contains(Direction.NE))
				opposites.add(Direction.NE);

			if (d.equals(Direction.E) && !harmfulDirections.contains(Direction.W))
				opposites.add(Direction.W);
			
			if (d.equals(Direction.W) && !harmfulDirections.contains(Direction.E))
				opposites.add(Direction.E);

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
		for(SeaCreature sc : ratedCreatures)
		{
			//ranking = happiness x avg frequency
//			double ranking = sc.returnCreature().getHappiness() * 
//				(sc.returnCreature().getMinCount() + sc.returnCreature().getMaxCount()) / 2.0;
			
			double ranking = sc.avgPossibleHappiness;
			
			//if he's dangerous, super low rank him
			if(sc.returnCreature().isDangerous())
				ranking *= -1.0;
			
			sc.ranking = ranking;
		}
		
		Collections.sort(ratedCreatures);
		Collections.reverse(ratedCreatures);
		
		int count = 0;
		for(SeaCreature sc : ratedCreatures)
		{
			creatureMapping.put(Character.toString(ALPHABET.charAt(count)), sc);
			
			if(myId == 0)
				System.err.println(count + " " + ALPHABET.charAt(count) + " id: " + sc.getId() + " " + sc.returnCreature().getName());
			count++;
		}
	}

	
	public String getTick(Set<Observation> whatYouSee)
	{
		SeaCreature bestVisible = null;
		SeaCreature worstVisible = null;
		double bestRanking = Double.MIN_VALUE;
		double worstRanking = Double.MAX_VALUE;
		
		//track which is the best creature that you can currently see
		for(Observation o : whatYouSee)
		{
			SeaCreature cur = knownCreatures.get(o.getName());
			
			if(cur != null)
			{
				if(cur.ranking > bestRanking)
				{
					bestVisible = cur;
					bestRanking = cur.ranking;
				}
				
				if(cur.ranking < worstRanking)
				{
					worstVisible = cur;
					worstRanking = cur.ranking;
				}
			}
		}
		
		//send out the message that refers to the best creature
		if(bestVisible != null && bestVisible.ranking > 0)
			return bestVisible.isnorkMessage;
		
		//if you can't see any good creatures, send out the most dangerous creature
		if(worstVisible != null)
			return worstVisible.isnorkMessage;
		
		return null;
	}

	
	public void updateIncomingMessages(Set<iSnorkMessage> incomingMessages)
	{
		SeaCreature bestFind = null;
		double curDist = Double.MAX_VALUE;
		boolean changed = false;
		for(iSnorkMessage ism : incomingMessages)
		{
			SeaCreature sc = creatureMapping.get(ism.getMsg());
			
			if(ism.getMsg().equals("a") && !sc.seenOnce)
			{
				//base case
				if(bestFind == null && sc.nextHappiness > 0)
				{
					bestFind = sc;
					intermediateGoal = ism.getLocation();
					changed = true;
				}
				//equality case, it's two of the same creature
				else if(sc != null && bestFind != null && sc.nextHappiness == bestFind.nextHappiness)
				{
					double newDist = whereIAm.distance(ism.getLocation());
					if(newDist < curDist)
					{
						curDist = newDist;
						bestFind = sc;
						intermediateGoal = ism.getLocation();
						changed = true;
					}
				}
				//general case, does this new creature have a higher next happiness?
				else if(sc != null && bestFind != null && sc.nextHappiness > bestFind.nextHappiness)
				{
					curDist = whereIAm.distance(ism.getLocation());
					bestFind = sc;
					intermediateGoal = ism.getLocation();
					changed = true;
				}
			}
		}
		
		if(myId == 0 && intermediateGoal != null && changed)
			System.err.println(myId + "'s goal: " + intermediateGoal.toString());
	}
}
