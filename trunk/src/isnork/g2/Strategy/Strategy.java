package isnork.g2.Strategy;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import isnork.g2.SeaBoard;
import isnork.sim.Observation;
import isnork.sim.SeaLifePrototype;
import isnork.sim.iSnorkMessage;
import isnork.sim.GameObject.Direction;

public abstract class Strategy {

	public static ArrayList<Direction> directions = new ArrayList<Direction>();
	
	private Logger log = Logger.getLogger(this.getClass());
	protected SeaBoard board;
	protected Random random;
	protected Point2D whereIAm = null;
	protected Point2D boat;
	protected int radius, distance, penalty, numrounds;
	protected int roundsleft;
	protected double boatConstant = .9;
	
	static
	{
		directions.add(Direction.N);
		directions.add(Direction.NE);
		directions.add(Direction.E);
		directions.add(Direction.SE);
		directions.add(Direction.S);
		directions.add(Direction.SW);
		directions.add(Direction.W);
		directions.add(Direction.NW);
	};
	
	public Strategy(int p, int d, int r, Set<SeaLifePrototype> seaLifePossibilites, Random rand){
		penalty = p;
		distance = d;
		radius = r;
		numrounds = 480;
		roundsleft = numrounds;
		whereIAm = new Point2D.Double(distance, distance); //is this always true?
		boat = new Point2D.Double(distance, distance);
		board = new SeaBoard(2*d, 2*d, radius, seaLifePossibilites, distance);
		random = rand;
	}

	public abstract Direction getMove();
	
	public void update(Point2D myPosition, Set<Observation> whatYouSee,
			Set<iSnorkMessage> incomingMessages,
			Set<Observation> playerLocations) {
		
		//Update variables
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
		}
	}
}
