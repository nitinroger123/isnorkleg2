package isnork.g2.Strategy;

import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import isnork.g2.SeaBoard;
import isnork.g2.EachSeaCreature;
import isnork.g2.SeaCreatureType;
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
	
	public Strategy(int p, int d, int r, Set<SeaLifePrototype> seaLifePossibilities, Random rand, int id, int numDivers) {
		myId = id;
		penalty = p;
		distance = d;
		radius = r;
		numrounds = 480;
		roundsleft = numrounds;
		numSnorkelers = numDivers;
		whereIAm = new Point2D.Double(distance, distance); //is this always true?
		boat = new Point2D.Double(distance, distance);
		board = new SeaBoard(2*d, 2*d, radius, seaLifePossibilities, distance, boat);
		random = rand;
		whatISee=new HashSet<Observation>();
		
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
			ratedCreatures.get(count).isnorkMessage = Character.toString(ALPHABET.charAt(count));
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
}
