package isnork.g2.Strategy;

import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import isnork.g2.utilities.EachSeaCreature;
import isnork.g2.utilities.SeaBoard;
import isnork.g2.utilities.SeaCreatureType;
import isnork.sim.Observation;
import isnork.sim.SeaLifePrototype;
import isnork.sim.iSnorkMessage;
import isnork.sim.GameObject.Direction;

public abstract class Strategy {
	
	public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz"; 
	
	protected Set<Observation> whatISee;
	
	public static ArrayList<Direction> directions = Direction.allBut(null);
	
	private Logger log = Logger.getLogger(this.getClass());
	protected SeaBoard board;
	protected Random random;
	protected Point2D whereIAm = null;
	protected Point2D boat;
	protected int radius, distance, penalty, numrounds;
	protected int roundsleft;
	
	public ArrayList<SeaCreatureType> ratedCreatures = new ArrayList<SeaCreatureType>();
	public HashMap<String, SeaCreatureType> creatureMapping = new HashMap<String, SeaCreatureType>();
	public HashMap<String, SeaCreatureType> knownCreatures = new HashMap<String, SeaCreatureType>();
	public int minPossibleHappiness = 0;
	public int avgPossibleHappiness = 0;
	public int maxPossibleHappiness = 0;
	public int myHappiness = 0;
	public int myId = 0;
	public int numSnorkelers = 0;
	public Point2D intermediateGoal = null;
	public SeaCreatureType searchingFor = null;
	
	public Strategy(int p, int d, int r, Set<SeaLifePrototype> seaLifePossibilities, Random rand, int id, int numDivers, SeaBoard b) {
		myId = id;
		penalty = p;
		distance = d;
		radius = r;
		numrounds = 480;
		roundsleft = numrounds;
		numSnorkelers = numDivers;
		whereIAm = new Point2D.Double(distance, distance); //is this always true?
		boat = new Point2D.Double(distance, distance);
		random = rand;
		whatISee=new HashSet<Observation>();
		board = b;
		
		//create the basic arraylist of points based on the minimum amount that can be there
		for(SeaLifePrototype slp : seaLifePossibilities)
		{
			EachSeaCreature sc = new EachSeaCreature(slp);
			int h = slp.getHappiness();
			int combinedH = (h + (int)((double)h * .5) + (int)((double)h * .25));
			int minH = 0;
			int maxH = 0;
			int avgH = 0;
			
			//determine the min, max, and avg total happiness given the prototypes
			if(slp.getMinCount() > 0)
				minH += h;
			if(slp.getMinCount() > 1)
				minH += (int)((double)h * .5);
			if(slp.getMinCount() > 2)
				minH += (int)((double)h * .25);
			
			if(slp.getMaxCount() > 0)
				maxH += h;
			if(slp.getMaxCount() > 1)
				maxH += (int)((double)h * .5);
			if(slp.getMaxCount() > 2)
				maxH += (int)((double)h * .25);
			
			avgH += (minH + maxH) / 2;
			
			//add these values to the running tally
			minPossibleHappiness += minH;
			maxPossibleHappiness += maxH;
			avgPossibleHappiness += avgH;
			
			//add the prototype that maps the name to the sea life prototype (used later)
			SeaCreatureType temp = new SeaCreatureType(slp);
			temp.minPossibleHappiness = minH;
			temp.maxPossibleHappiness = maxH;
			temp.avgPossibleHappiness = avgH;
			knownCreatures.put(slp.getName(), temp);
			ratedCreatures.add(temp);
		}
		
		//given the prototypes, rate all the creatures using several heuristics
		rateCreatures(seaLifePossibilities);
		setiSnorkLetterMapping();
	}
	
	public void update(Point2D myPosition, Set<Observation> whatYouSee,
			Set<iSnorkMessage> incomingMessages,
			Set<Observation> playerLocations) {
		
		//Update variables
		this.whatISee=whatYouSee;
		whereIAm.setLocation(myPosition.getX() + distance, myPosition.getY() + distance);
		roundsleft --;
		
		log.trace("Round: " + (numrounds - roundsleft));
		log.trace("I see: " + whatYouSee.size() + " things.");
		
		for(Observation o: whatYouSee){
			
			//remove from board
			board.remove(o.getId());
			
			//add to board
			board.add(o, numrounds - roundsleft);
			if(o.isDangerous())
				log.trace("Dangerous");
			
			//determine which creatures you've seen already and decrement how many points they're worth
			updateMyPosition(myPosition, o);
		}
		
		checkFoundGoal(incomingMessages);
		updateIncomingMessages(incomingMessages);
	}

	/**
	 * Updates your current score and seen creatures based on an observation. If you've already seen
	 * that exact creature before, you won't get points. If you've seen another of that creature before,
	 * you'll get the decremented amount of points. If it's dangerous, you might lose points.
	 * It also keeps track of the id's that you've seen of a specific creature, so you won't 
	 * get duplicate points for the same creature with the same id.
	 */
	private void updateMyPosition(Point2D myPosition, Observation o)
	{
		SeaCreatureType sc = knownCreatures.get(o.getName());
		if(sc != null && !sc.seen.contains(o.getName()))
		{
			//add this creature to the list, increment any points that might be gained
			myHappiness += sc.addSeen(o.getId());
			
			//check if the creature is dangerous and decrement points if needed
			if(o.isDangerous() && myPosition.distance(o.getLocation()) < 1.5)
			{
				myHappiness -= (o.happiness() * 2);
			}
		}
	}
	
	/**
	 * Sets the creatures to a specific lettering so the snorklers know which letters refer to which creature.
	 */
	private void setiSnorkLetterMapping()
	{
		for(int count=0; count<ratedCreatures.size(); count++)
		{
			ratedCreatures.get(count%26).isnorkMessage = Character.toString(ALPHABET.charAt(count%26));
		}
	}
	
	/**
	 * Rate the creatures using several heuristics.
	 * These are to determine the ranking at which creatures will be placed
	 * on the lettering of the iSnork. Creatures are ranked by their happiness,
	 * their frequency, and the amount of the board you can see.
	 */
	public abstract void rateCreatures(Set<SeaLifePrototype> seaLifePossibilities);
	
	/**
	 * Returns the snorklers message that will be broadcast to the other snorklers.
	 */
	public abstract String getTick(Set<Observation> whatYouSee);
	
	/**
	 * Reads in the messages from the other snorklers and determines which
	 * point on the board to swim to.
	 */
	public abstract void updateIncomingMessages(Set<iSnorkMessage> incomingMessages);
	
	/**
	 * Returns a move for the snorkler
	 */
	public abstract Direction getMove();
	
	/**
	 * Checks if we found the creature we were looking for, so we no longer have
	 * an intermediate goal.
	 */
	public abstract void checkFoundGoal(Set<iSnorkMessage> incomingMessages);
	
	/**Methods to move*/
	
	/** Move to get the player back to the boat */
	public Direction backtrack(boolean desperateTime) {
		return goToGoalWithoutGettingBit(boat, desperateTime);
	}
	
	/**
	 * New method to go to a goal without getting hurt. Desperate time tells us
	 * if its imperative that we reach the boat or not. If true, we just go to
	 * the goal and don't avoid danger.
	 */
	public Direction goToGoalWithoutGettingBit(Point2D goal,
			boolean desperateTime) {

		if (!desperateTime && board.areThereDangerousCreatures(whatISee)) {
			return runAwayFromDanger(
					board.getHarmfulDirections(whereIAm, whatISee), goal);
		}
		double currX = whereIAm.getX();
		double currY = whereIAm.getY();
		log.trace("Current X: " + currX + " Current Y: " + currY);
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
	
	/**
	 * Another method. We need to clean up the code and remove all dead code.
	 * run away from danger if any and make the best possible move to go to the
	 * goal. The priority is avoiding danger though.
	 */
	public Direction runAwayFromDangerAndGotoGoal(Point2D goal,
			boolean desperateTime) {
		/**
		 * need to go to the boat and its late
		 */
		if (desperateTime) {
			return goToGoalWithoutGettingBit(goal, desperateTime);
		}

		if (board.areThereDangerousCreatures(whatISee)) {
			ArrayList<Direction> harmfulDirections = board
					.getHarmfulDirections(whereIAm, whatISee);
			boolean areTheDangerousCreaturesMoving = board.isDangerMobile(
					whereIAm, whatISee);
			// go to goal by selecting the best possible direction to goal
			// without getting bit.
			if (areTheDangerousCreaturesMoving) {
				return gotoGoalWhenDangerIsMobile(goal, harmfulDirections);

			} else {
				// the danger is static, just avoid it and make sure you are
				// never on a cell right next to it
				return gotoGoalWhenDangerIsStatic(goal, harmfulDirections);
			}

		}
		return null;
	}
	
	/**
	 * Go to goal when the danger is mobile. Select the best path to goal when
	 * the dangerous creatures are moving around.
	 */
	private Direction gotoGoalWhenDangerIsMobile(Point2D goal,
			ArrayList<Direction> harmfulDirections) {
		ArrayList<Direction> safeMoves = getOpposites(harmfulDirections);
		double min = 100;
		Direction bestDirectionToGoal = randomMove();
		for (Direction safe : safeMoves) {
			Point2D newPosition = new Point2D.Double(whereIAm.getX()
					+ safe.getDx(), whereIAm.getY() + safe.getDy());
			if (newPosition.distance(goal) < min) {
				min = newPosition.distance(goal);
				bestDirectionToGoal = safe;
			}
		}
		return bestDirectionToGoal;
	}
	
	private ArrayList<Direction> getOpposites(
			ArrayList<Direction> harmfulDirections) {
		ArrayList<Direction> opposites = new ArrayList<Direction>();
		for (Direction d : harmfulDirections) {

			if (d.equals(Direction.N)
					&& !harmfulDirections.contains(Direction.S))
				opposites.add(Direction.S);

			if (d.equals(Direction.NW)
					&& !harmfulDirections.contains(Direction.SE))
				opposites.add(Direction.SE);

			if (d.equals(Direction.NE)
					&& !harmfulDirections.contains(Direction.SW))
				opposites.add(Direction.SW);

			if (d.equals(Direction.S)
					&& !harmfulDirections.contains(Direction.N))
				opposites.add(Direction.N);

			if (d.equals(Direction.SE)
					&& !harmfulDirections.contains(Direction.NW))
				opposites.add(Direction.NW);

			if (d.equals(Direction.SW)
					&& !harmfulDirections.contains(Direction.NE))
				opposites.add(Direction.NE);

			if (d.equals(Direction.E)
					&& !harmfulDirections.contains(Direction.W))
				opposites.add(Direction.W);

			if (d.equals(Direction.W)
					&& !harmfulDirections.contains(Direction.E))
				opposites.add(Direction.E);

		}
		return opposites;
	}
	
	private Direction gotoGoalWhenDangerIsStatic(Point2D goal,
			ArrayList<Direction> harmfulDirections) {
		ArrayList<Direction> allDirections = Direction.allBut(null);
		double min = 100;
		Direction bestDirectionToGoal = randomMove();
		for (Direction safe : allDirections) {
			if (harmfulDirections.contains(safe)) {
				/*
				 * to do ..remove the direction only when it takes us to close
				 * to the static object
				 */
				continue;
			}
			Point2D newPosition = new Point2D.Double(whereIAm.getX()
					+ safe.getDx(), whereIAm.getY() + safe.getDy());
			if (newPosition.distance(goal) < min) {
				min = newPosition.distance(goal);
				bestDirectionToGoal = safe;
			}
		}
		return bestDirectionToGoal;
	}
	
	/** Dumb move included with dumb player */
	public Direction randomMove() {
		log.trace("random move");
		Direction d = getRandomDirection();
		while (!board.isValidMove((int) whereIAm.getX(), (int) whereIAm.getY(),
				d)) {
			d = getRandomDirection();
		}
		return d;
	}
	
	protected Direction getRandomDirection() {
		return Strategy.directions.get(random.nextInt(8));
	}
	
	public Direction runAwayFromDanger(ArrayList<Direction> harmfulDirections,
			Point2D goal) {
		// If you are on the boat, you dont need to run
		// System.err.println("run away from danger");
		
		// if(goal!=null)
		// System.err.println("Going to goal X: "+goal.getX() +" Y:
		// "+goal.getY());
		ArrayList<Direction> safeMoves = getOpposites(harmfulDirections);
		Direction bestDirection=randomMove();
		
		ArrayList<Direction> directionTheDangerousCreaturesMove = 
			board.getLastDirectionOfHarmfulCreatures(whatISee);
		
		ArrayList<Direction> bestWorstMoves =new ArrayList<Direction>(); 
		if(!safeMoves.isEmpty()){
			/*Remove the direction the dangerous fish moves in. its not safe if he chases you*/
			if(safeMoves.size()>directionTheDangerousCreaturesMove.size()){
				for(Direction d:directionTheDangerousCreaturesMove){
					if(safeMoves.contains(d)){
						safeMoves.remove(d);
						bestWorstMoves.add(d);
					}
				}
			}
			Collections.shuffle(safeMoves);
		}
		bestDirection=randomMove();
		double min = 10000;
		if (!safeMoves.isEmpty()) {
			for (Direction safe : safeMoves) {
				// System.err.println("Safe Moves are: "+safe.name());
				if (board.isValidMove((int) whereIAm.getX(), (int) whereIAm
						.getY(), safe)) {
					System.err.println("Safe Valid Moves are: "+safe.name());
					Point2D newPos=new Point2D.Double(whereIAm.getX()+safe.getDx(),whereIAm.getY()+safe.getDy());
					if(goal!=null)
					{		System.err.println("Distance to goal : "+newPos.distance(goal));
							if(newPos.distance(goal)<min)
							{
								System.err.println("new best dir found");
								min=newPos.distance(goal);
								bestDirection=safe;
							}
					}
					else{
						bestDirection=safe;
					}
			// System.err.println("Best Direction :"+bestDirection.name());
			return bestDirection;
		}
			}
		}
		/*
		 * We don't have a good safe move but we have a move that just runs in
		 * the same direction as the dangerous creature
		 */
		else if (!bestWorstMoves.isEmpty()) 
		{
			System.err.println("inside bestworst");
			Collections.shuffle(bestWorstMoves);
			for(Direction d: bestWorstMoves){
				if(board.isValidMove((int)whereIAm.getX(), (int)whereIAm.getY(), d))
				{
					return d;
				}
			}
			return randomMove();
		}
		/* We are screwed, surrounded. Pray to god random move helps you */
		return randomMove();
	}

}
