package isnork.g2;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;

import isnork.sim.GameConfig;
import isnork.sim.GameController;
import isnork.sim.Observation;
import isnork.sim.Player;
import isnork.sim.SeaLife;
import isnork.sim.SeaLifePrototype;
import isnork.sim.iSnorkMessage;
import isnork.sim.GameObject.Direction;

/**Player*/
public class JustKeepSwimming extends Player {

	private SeaBoard board;
	private Point2D whereIAm = null;
	private Point2D boat;
	private int radius, distance, penalty, numrounds, roundsleft;
	private double boatConstant = .9;
	private Logger log;
	
	

	private Direction getNewDirection() {
		Direction direction = null;
		int r = random.nextInt(100);
		if (r < 10 || direction == null) {
			ArrayList<Direction> directions = Direction.allBut(direction);
			direction = directions.get(random.nextInt(directions.size()));
		}
		return direction;
	}

	@Override
	public String getName() {
		return "JustKeepSwimming";
	}

	@Override
	public String tick(Point2D myPosition, Set<Observation> whatYouSee,
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
		
		//return message to isnorq
		return null;
	}

	@Override
	public Direction getMove() {
		
		/*Desperate measure. Get back on the boat. This is to avoid the situation where the divers come 
		  near the boat and then move away. I found this happening
		   with our current condition*/ 
		if(roundsleft<45)
			return backtrack();
		

		/*If condition to determine when to start heading back.  Boat constant gives you a few extra 
		 rounds to head back, and dividing by three accounts for the fact that you can only make 
		 diagonal moves once every three rounds*/
		if(whereIAm.distance(boat) > (boatConstant * roundsleft)/3 )
			return backtrack();
		
		if(board.getDangerInRadius(whereIAm, numrounds - roundsleft))
			return avoidHarm();
		
		//No dangerous animals around
		else return randomMove();
	}

	/**Move to avoid harm*/
	public Direction avoidHarm() {
		log.trace("Move Avoiding Harm");
		ArrayList<Direction> pos = Direction.allBut(null);
		ArrayList<Direction> danger = board.getDangerousDirections(whereIAm, numrounds - roundsleft);
		log.trace("Danger length: " + danger.size());
		
		for(int i = 0; i < danger.size(); i++){
			log.trace(danger.get(i));
			if(pos.contains(danger.get(i))){
				log.trace("removing from pos");
				pos.remove(danger.get(i));
			}
		}
		
		Collections.shuffle(pos); //Randomize safe directions
		int index = 0;
		log.trace("We have " + pos.size() + " safe moves");
		Direction d = Direction.N; //Initialize
		if(pos.size() == 0){//Need to make this better to find the best bad move
			d = getNewDirection();
		}
		else
			d = pos.get(index);

		Point2D p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
				+ d.dy);
		while (!(p.getX() >= 0 || p.getX() < 2*distance
				|| p.getY() >= 0 || p.getY() < 2*distance)) {
			index++;
			if(index < pos.size())
				d = pos.get(index);
			else
				d = getNewDirection();	
			p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
					+ d.dy);
		}
		return d;
	}
	
	/**Dumb move included with dumb player*/
	public Direction randomMove() {
		log.trace("random move");
		Direction d = getNewDirection();

		Point2D p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
				+ d.dy);
		while (!(p.getX() >= 0 || p.getX() < 2*distance
				|| p.getY() >= 0 || p.getY() < 2*distance)) {
			d = getNewDirection();
			p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
					+ d.dy);
		}
		return d;
	}
	
	/**Move to get the player back to the boat*/
	public Direction backtrack() {
		double currX = whereIAm.getX();
		double currY = whereIAm.getY();
		log.trace("Current X: "+currX+" Current Y: "+currY);
		if(currX==boat.getX()&&currY==boat.getY()){
			//stay put, we have reached the boat
			return null;
		}
		if(currX>boat.getX()&&currY>boat.getY()){
			return Direction.NW;
		}
		if(currX<boat.getX()&&currY<boat.getY()){
			return Direction.SE;
		}
		if(currX<boat.getX()&&currY>boat.getY()||currX<boat.getX()&&currY==boat.getY()){
			return Direction.E;
		}
		if(currX>boat.getX()&&currY<boat.getY()||currX==boat.getX()&&currY<boat.getY()){
			return Direction.S;
		}
		
		if(currX==boat.getX()&&currY>boat.getY()){
			return Direction.N;
		}
		if(currX>boat.getX()&&currY==boat.getY()){
			return Direction.W;
		}
		return null;
	}


	/** Initialize our variables when a new game is created */
	@Override
	public void newGame(Set<SeaLifePrototype> seaLifePossibilites, int p,
			int d, int r, int n) {
		
		//Initialize game variables
		log = Logger.getLogger(this.getClass());

		penalty = p;
		distance = d;
		radius = r;
		numrounds = 480;
		roundsleft = numrounds;
		whereIAm = new Point2D.Double(distance, distance); //is this always true?
		boat = new Point2D.Double(distance, distance);
		board = new SeaBoard(2*d, 2*d, radius, seaLifePossibilites, distance);

	}

}