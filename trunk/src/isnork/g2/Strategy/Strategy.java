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

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

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
	protected int radius, distance, penalty, numrounds, smallradius = 0;
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


	public Set<Observation> dangerousCreaturesInMyRadius;

	public int curRound = -1;
	private ArrayList<Point2D> visitedInLastFiveRounds=new ArrayList<Point2D>();
	
	
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
		
		//use a smaller radius to avoid danger
		if (r > 5)
			smallradius = 5;
		else
			smallradius = r;
		
		//create the basic arraylist of points based on the minimum amount that can be there
		for(SeaLifePrototype slp : seaLifePossibilities)
		{
			SeaCreatureType sc = new SeaCreatureType(slp);
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
			
			if(temp.returnCreature().getHappiness() != 0)
			{
				ratedCreatures.add(temp);
			}
		}
		
		//given the prototypes, rate all the creatures using several heuristics
		rateCreatures(seaLifePossibilities);
		setiSnorkLetterMapping();
	}
	
	public void update(Point2D myPosition, Set<Observation> whatYouSee,
			Set<iSnorkMessage> incomingMessages,
			Set<Observation> playerLocations) {
		
		curRound++;
		
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
			board.add(o, numrounds - roundsleft, true);
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
		if(sc != null && !sc.seen.contains(o.getName()) && !whereIAm.equals(boat))
		{
			//add this creature to the list, increment any points that might be gained
			myHappiness += sc.addSeen(o.getId());
			
			//check if the creature is dangerous and decrement points if needed
			if(o.isDangerous() && myPosition.distance(o.getLocation()) < 1.5)
			{
				//myHappiness -= (o.happiness() * 2);
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
	public abstract String getTick();
	
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
		
		if(whereIAm.equals(goal))
			return null;
		
		if (!desperateTime && board.areThereDangerousCreaturesInRadiusNew(whatISee,whereIAm)) {
			return avoidDanger(goal);
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
			boolean areTheDangerousCreaturesMoving = board.isDangerMobile(whatISee);
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
		ArrayList<Direction> safeMoves = getSafeDirections(harmfulDirections);
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
	
	protected ArrayList<Direction> getSafeDirections(
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
	/**
	 * Get the location of static dangerous creatures
	 */
	public ArrayList<Point2D> getLocationOfStaticDanger(Set<Observation> dangerAround){
		ArrayList<Point2D> locationOfStaticDanger =new ArrayList<Point2D>();
		for(Observation creature: dangerAround){
			if(creature.isDangerous()){
				locationOfStaticDanger.add(creature.getLocation());
			}
		}
		return locationOfStaticDanger;
	}
	
	public Direction runAwayFromDanger(ArrayList<Direction> harmfulDirections,
            Point2D goal) {
    // If you are on the boat, you dont need to run
    // System.err.println("run away from danger");
    
    // if(goal!=null)
    // System.err.println("Going to goal X: "+goal.getX() +" Y:
    // "+goal.getY());
    ArrayList<Direction> safeMoves = getSafeDirections(harmfulDirections);
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
                            //System.err.println("Safe Valid Moves are: "+safe.name());
                            Point2D newPos=new Point2D.Double(whereIAm.getX()+safe.getDx(),whereIAm.getY()+safe.getDy());
                            if(goal!=null)
                            {               //System.err.println("Distance to goal : "+newPos.distance(goal));
                                            if(newPos.distance(goal)<min)
                                            {
                                                    //System.err.println("new best dir found");
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
            //System.err.println("inside bestworst");
            Collections.shuffle(bestWorstMoves);
            for(Direction d: bestWorstMoves){
                    if(board.isValidMove((int)whereIAm.getX(), (int)whereIAm.getY(), d))
                    {
                            return d;
                    }
            }
            //System.err.println("Random move gets called 1");
            return randomMove();
    }
    /* We are screwed, surrounded. Pray to god you survive. figure out the last direction of all the creatures 
     * surrounding you and anticipate its next move. If there is a direction which does not put you into trouble
     * take that direction
     * */
    //System.err.println("surrounded. Choose a direction of minimal harm");
    ArrayList<Point2D> possibleLocationsOfDanger=new ArrayList<Point2D>();
    ArrayList<Direction> possibleSafeDirection=new ArrayList<Direction>();
    for(Observation creature: dangerousCreaturesInMyRadius){
    	Point2D current=new Point2D.Double(creature.getLocation().getX()+boat.getX(),
    			creature.getLocation().getY()+boat.getY());
    	Point2D next=new Point2D.Double(current.getX()+creature.getDirection().getDx(),
    			current.getY()+creature.getDirection().getDy());
    	possibleLocationsOfDanger.add(next);
    }
    for(Direction dir: Direction.allBut(null)){
    	if(!board.isValidMove((int)whereIAm.getX(),(int)whereIAm.getY(),dir)){
    		continue;
    	}
    	Point2D next = new Point2D.Double(whereIAm.getX()+dir.getDx(),
    			whereIAm.getY()+dir.getDy());
    	boolean isSafe=true;
    	for(Point2D danger: possibleLocationsOfDanger){
    		if(next.distance(danger)<1.6){
    			isSafe=false;
    			break;
    		}
    	}
    	if(isSafe){
    		possibleSafeDirection.add(dir);
    	}
    }
    if(!possibleSafeDirection.isEmpty()){
    	Collections.shuffle(possibleSafeDirection);
    	return possibleSafeDirection.get(0);
    }
    else{
    	//System.err.println("Random move gets called 2");
    	return randomMove();
    }
}
	
	
   

	/**
	 * The new avoid danger method
	 * @param goal
	 * @return
	 */
	public Direction avoidDanger(Point2D goal){
		//gets the dangerous creatures within a radius of 5
		dangerousCreaturesInMyRadius=new HashSet<Observation>();
		dangerousCreaturesInMyRadius.addAll(board.getDangerousCreaturesInRadius(whereIAm,whatISee));
		/*The fish has not made any move, but we know it is dangerous. Stay on the boat*/
		if(roundsleft>=476){
			//System.err.println("first moves "+roundsleft );
			return null;
		}
		//if the danger is moving and you are on the boat. stay put.
		if(board.isDangerMobile(dangerousCreaturesInMyRadius)&&whereIAm.equals(boat)){
			//System.err.println("Staying on the boat");
			return null;
		}
		//All the danger you see is static. Just go to your goal without getting too close to the danger
		else if(!board.isDangerMobile(dangerousCreaturesInMyRadius)){
			//System.err.println("All static dangerous creatures");
			return avoidStaticDangerAndGoToGoal(goal);
		}
		//The danger has some moving danger. We need to make sure we dont get hit.
		else{
			//System.err.println("moving dangerous creatures");
			//return avoidMovingDangerAndGoToGoal(goal);
			ArrayList<Direction> dangerousDirections=new ArrayList<Direction>();
			for(Observation o: dangerousCreaturesInMyRadius){
				dangerousDirections.addAll(board.getDirections(whereIAm, o.getLocation()));
			}
			return runAwayFromDanger(dangerousDirections,goal);
		}
		
		//Never gets here
	}
	
	/**
	 * Simple place holder class
	 * @author nitin
	 *
	 */
	class DirectionHolder implements Comparable<DirectionHolder>
	{
		public Direction direction;
		public double distance; 
		public DirectionHolder(Direction d,double dist ){
			direction=d;
			distance=dist;
		}
		public int compareTo(DirectionHolder o) {
			if(this.distance<=o.distance){
				return -1;
			}
			else{
				return 1;
			}
		}
		
	}
	
	private ArrayList<Direction> getBestDirsToGoal(Point2D goal){
		if(goal==null){
			return Direction.allBut(null);
		}
		ArrayList<DirectionHolder> tosort =new ArrayList<DirectionHolder>();
		for(Direction d: Direction.allBut(null)){
			Point2D newpos=new Point2D.Double(whereIAm.getX()+d.getDx(),whereIAm.getY()+d.getDy());
			double distance=newpos.distance(goal);
			tosort.add(new DirectionHolder(d, distance));
		}
		Collections.sort(tosort);
		ArrayList<Direction> toReturn=new ArrayList<Direction>();
		for(DirectionHolder d:tosort){
			toReturn.add(d.direction);
		}
		return toReturn;
	}
	/**
	 * look at the direction in which the fish is moving and take that into account to make sure
	 * we don't run into him.
	 * @param safeMoves
	 * @return
	 */
	private ArrayList<Direction> pruneSafeDirectionsBasedOnHowTheFishMoves(ArrayList<Direction> safeMoves){
		for(Observation creature: dangerousCreaturesInMyRadius){
			if(board.getRelativeDirection(whereIAm, creature.getLocation())==null){
				continue;
			}
			if(board.getRelativeDirection(whereIAm,creature.getLocation()).equals(Direction.N)){
				//System.err.println("Danger at my North and he is moving "+creature.getDirection());
				if(creature.getDirection().equals(Direction.N)){	
				}
				if(creature.getDirection().equals(Direction.S)){	
				}
				if(creature.getDirection().equals(Direction.E)){	
				}
				if(creature.getDirection().equals(Direction.W)){	
				}
				if(creature.getDirection().equals(Direction.NW)){	
				}
				if(creature.getDirection().equals(Direction.NE)){	
				}
				if(creature.getDirection().equals(Direction.SW)){	
				}
				if(creature.getDirection().equals(Direction.SE)){	
				}
				
			}
			if(board.getRelativeDirection(whereIAm,creature.getLocation()).equals(Direction.S)){
				//System.err.println("Danger at my South and he is moving "+creature.getDirection());
				if(creature.getDirection().equals(Direction.N)){	
				}
				if(creature.getDirection().equals(Direction.S)){	
				}
				if(creature.getDirection().equals(Direction.E)){	
				}
				if(creature.getDirection().equals(Direction.W)){	
				}
				if(creature.getDirection().equals(Direction.NW)){	
				}
				if(creature.getDirection().equals(Direction.NE)){	
				}
				if(creature.getDirection().equals(Direction.SW)){	
				}
				if(creature.getDirection().equals(Direction.SE)){	
				}
			}
			if(board.getRelativeDirection(whereIAm,creature.getLocation()).equals(Direction.E)){
				//System.err.println("Danger at my East and he is moving "+creature.getDirection());
				if(creature.getDirection().equals(Direction.N)){	
				}
				if(creature.getDirection().equals(Direction.S)){	
				}
				if(creature.getDirection().equals(Direction.E)){	
				}
				if(creature.getDirection().equals(Direction.W)){	
				}
				if(creature.getDirection().equals(Direction.NW)){	
				}
				if(creature.getDirection().equals(Direction.NE)){	
				}
				if(creature.getDirection().equals(Direction.SW)){	
				}
				if(creature.getDirection().equals(Direction.SE)){	
				}
			}
			if(board.getRelativeDirection(whereIAm,creature.getLocation()).equals(Direction.W)){
				//System.err.println("Danger at my west and he is moving "+creature.getDirection());
				if(creature.getDirection().equals(Direction.N)){	
				}
				if(creature.getDirection().equals(Direction.S)){	
				}
				if(creature.getDirection().equals(Direction.E)){	
				}
				if(creature.getDirection().equals(Direction.W)){	
				}
				if(creature.getDirection().equals(Direction.NW)){	
				}
				if(creature.getDirection().equals(Direction.NE)){	
				}
				if(creature.getDirection().equals(Direction.SW)){	
				}
				if(creature.getDirection().equals(Direction.SE)){	
				}
			}
			if(board.getRelativeDirection(whereIAm,creature.getLocation()).equals(Direction.NW)){
				//System.err.println("Danger at my Northwest and he is moving "+creature.getDirection());
				if(creature.getDirection().equals(Direction.N)){	
				}
				if(creature.getDirection().equals(Direction.S)){	
				}
				if(creature.getDirection().equals(Direction.E)){	
				}
				if(creature.getDirection().equals(Direction.W)){	
				}
				if(creature.getDirection().equals(Direction.NW)){	
				}
				if(creature.getDirection().equals(Direction.NE)){	
				}
				if(creature.getDirection().equals(Direction.SW)){	
				}
				if(creature.getDirection().equals(Direction.SE)){	
				}
			}
			if(board.getRelativeDirection(whereIAm,creature.getLocation()).equals(Direction.NE)){
				//System.err.println("Danger at my Northeast and he is moving "+creature.getDirection());
				if(creature.getDirection().equals(Direction.N)){	
				}
				if(creature.getDirection().equals(Direction.S)){	
				}
				if(creature.getDirection().equals(Direction.E)){	
				}
				if(creature.getDirection().equals(Direction.W)){	
				}
				if(creature.getDirection().equals(Direction.NW)){	
				}
				if(creature.getDirection().equals(Direction.NE)){	
				}
				if(creature.getDirection().equals(Direction.SW)){	
				}
				if(creature.getDirection().equals(Direction.SE)){	
				}
			}
			if(board.getRelativeDirection(whereIAm,creature.getLocation()).equals(Direction.SW)){
				//System.err.println("Danger at my southwest and he is moving "+creature.getDirection());
				if(creature.getDirection().equals(Direction.N)){	
				}
				if(creature.getDirection().equals(Direction.S)){	
				}
				if(creature.getDirection().equals(Direction.E)){	
				}
				if(creature.getDirection().equals(Direction.W)){	
				}
				if(creature.getDirection().equals(Direction.NW)){	
				}
				if(creature.getDirection().equals(Direction.NE)){	
				}
				if(creature.getDirection().equals(Direction.SW)){	
				}
				if(creature.getDirection().equals(Direction.SE)){	
				}
			}
			if(board.getRelativeDirection(whereIAm,creature.getLocation()).equals(Direction.SE)){
				//System.err.println("Danger at my southeast and he is moving "+creature.getDirection());
				if(creature.getDirection().equals(Direction.N)){	
				}
				if(creature.getDirection().equals(Direction.S)){	
				}
				if(creature.getDirection().equals(Direction.E)){	
				}
				if(creature.getDirection().equals(Direction.W)){	
				}
				if(creature.getDirection().equals(Direction.NW)){	
				}
				if(creature.getDirection().equals(Direction.NE)){	
				}
				if(creature.getDirection().equals(Direction.SW)){	
				}
				if(creature.getDirection().equals(Direction.SE)){	
				}
			}
			
		}
		return safeMoves;
	}
	
	/**
	 * avoids harm when it sees atleast one dangerous moving creature within its danger radius
	 * @param goal
	 * @return
	 */
	private Direction avoidMovingDangerAndGoToGoal(Point2D goal){
		ArrayList<Direction> positionOfDanger=new ArrayList<Direction>();
		ArrayList<Direction> bestDirToGoal= getBestDirsToGoal(goal);
		ArrayList<Direction> safeMoves=new ArrayList<Direction>();
		ArrayList<Direction> oppositeDirections=new ArrayList<Direction>();
		for(Observation o: dangerousCreaturesInMyRadius){
			positionOfDanger.addAll(board.getDirections(whereIAm, o.getLocation()));
		}
		/*Get the safe directions*/
//		for(Direction d: positionOfDanger){
//			System.err.println(" Danger dirs are "+d.name());
//		}
		oppositeDirections=getSafeDirections(positionOfDanger);
		for(Direction d: oppositeDirections){
			if(!positionOfDanger.contains(d)){
				safeMoves.add(d);
			}
		}
		/*if the fish is in a direction and we know it moves in 80% chance 
		 * in one direction, we need to avoid the direction that make it catch up to us
		 * Example its at your SE and moves W. You move SW*/
		safeMoves=pruneSafeDirectionsBasedOnHowTheFishMoves(safeMoves);
//		for(Direction d: safeMoves){
//			System.err.println(" safe Moves are "+d.name());
//		}
		/*Return the best direction to goal which is safe and valid*/
		for(Direction d:bestDirToGoal){
			if(!board.isValidMove((int)whereIAm.getX(), (int)whereIAm.getY(), d)){
				continue;
			}
			if(safeMoves.contains(d)){
//				if(goal!=null){
//					System.err.println("Goal is at x: "+goal.getX()+" y: "+goal.getY());
//				}
//				System.err.println("Returned a safe valid move that is the best dir to goal "+d.name());
				return d;
			}
		}
		/*If code comes here, we don't have an obvious safe valid moves.*/
		//System.err.println("We are surrounded :(");
		
		return null;
	}
    /**
     * avoids harm when all it sees as danger are static creatures
     * @param goal
     * @return
     */
	private Direction avoidStaticDangerAndGoToGoal(Point2D goal) {
		/*	Look at all the directions and choose the best direction 
		 *	 which does not take you one cell away from the creature
		*/
//		if(goal==null){
//			System.err.println("We dont have a goal ?");
//		}
//		else{
//			System.err.println("Going to our goal! "+goal.getX()+" y: "+goal.getY());
//			if(goal.equals(boat)){
//				System.err.println("Going to Boat!");
//			}
//		}
		Direction bestDirection=randomMove();
		double minDistance=1000;
		ArrayList<Direction> safeMoves=new ArrayList<Direction>();
		for(Direction direction: Strategy.directions){
			boolean isSafe=true;
			//the direction we want to go in must be a valid one.
			if(board.isValidMove((int)whereIAm.getX(), (int)whereIAm.getY(), direction)){
				Point2D newpos=new Point2D.Double(whereIAm.getX()+direction.getDx(),whereIAm.getY()+direction.getDy());
				for(Observation creature : dangerousCreaturesInMyRadius){
					/*it is not safe to go in that direction*/
					Point2D p = new Point2D.Double(creature.getLocation().getX()
							+ boat.getX(), creature.getLocation().getY()
							+ boat.getY());
					//System.err.println("Danger "+creature.getLocation().getX()+" "+creature.getLocation().getY());
					if(newpos.distance(p)<1.6){
						isSafe=false;
						break;
					}
				}
				if(isSafe){
					safeMoves.add(direction);
				}
			}
		}
		Collections.shuffle(safeMoves);
		/*Set the best direction to some safe move. This is for the times when we don't have a goal 
		 * So we make some safe move to avoid getting hurt
		 */
		if(!safeMoves.isEmpty()){
			bestDirection=safeMoves.get(0);
		}
		/*
		 * We are surrounded by mines. The only time this happens is when we are on the boat
		 * So just stay on the boat
		 */
		else{
			return null;
		}
		
		if(visitedInLastFiveRounds.size()>16){
			visitedInLastFiveRounds.clear();
		}
		/*Find the best safe direction for the goal we want to go to*/
		for(Direction direction: safeMoves){
			System.err.println("The last 10 moves are ");
			for(Point2D p : visitedInLastFiveRounds){
				System.err.println(" X :"+p.getX()+" Y: "+p.getY());
			}
			Point2D visitedCell=null;
			Point2D newpos=new Point2D.Double(whereIAm.getX()+direction.getDx(),whereIAm.getY()+direction.getDy());
			if(visitedInLastFiveRounds.contains(newpos)){
				System.err.println("Already visited this cell in the last five rounds x: "+newpos.getX()+" y: "+newpos.getY());
				continue;
			}
			/*check if it is one of the best directions to the goal*/
			if(goal!=null)
			if(newpos.distance(goal)<minDistance){
				bestDirection=direction;
				minDistance= newpos.distance(goal);
				visitedCell=newpos;
			}
			if(visitedCell!=null){
				System.err.println("The cell visited is x: "+visitedCell.getX()+" y: "+visitedCell.getY());
				visitedInLastFiveRounds.add(visitedCell);
			}
		}
		/*TODO if stuck in a loop we need to remove that direction which takes us 
		 * into a loop. Just keep track of the last 5 moves and if we go to the same
		 * spot again, that direction that takes us there should be removed.
		 */
		
		return bestDirection;
	}
	
	/**
	 * Gets the number of rounds needed to travel from start to goal.
	 */
	public int getRoundsDistance(Point2D start, Point2D goal)
	{
		int numRounds = 0;
		
		while(!start.equals(goal))
		{
			Direction move = toGoal(start, goal);
			if(move == Direction.NW || move == Direction.SE || move == Direction.NE || move == Direction.SW)
				numRounds += 3;
			else
				numRounds += 2;
			
			start = new Point2D.Double(start.getX()+move.dx, start.getY()+move.dy);
		}
		
		return numRounds;
	}
	
	/**
	 * Determines which direction to go to get from the start to the goal.
	 */
	public Direction toGoal(Point2D whereIAm, Point2D goal) {
		// in a quadrant
		if (whereIAm.getX() > goal.getX() && whereIAm.getY() > goal.getY()) {
			return Direction.NW;
		}

		if (whereIAm.getX() < goal.getX() && whereIAm.getY() < goal.getY()) {
			return Direction.SE;
		}

		if (whereIAm.getX() < goal.getX() && whereIAm.getY() > goal.getY()) {
			return Direction.NE;
		}

		if (whereIAm.getX() > goal.getX() && whereIAm.getY() < goal.getY()) {
			return Direction.SW;
		}

		// on a line
		if (whereIAm.getX() < goal.getX() && whereIAm.getY() == goal.getY()) {
			return Direction.E; 
		}
		if (whereIAm.getX() == goal.getX() && whereIAm.getY() < goal.getY()) {
			return Direction.S;
		}

		if (whereIAm.getX() == goal.getX() && whereIAm.getY() > goal.getY()) {
			return Direction.N;
		}
		if (whereIAm.getX() > goal.getX() && whereIAm.getY() == goal.getY()) {
			return Direction.W;
		}

		return null;
	}
}
