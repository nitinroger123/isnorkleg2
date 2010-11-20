package isnork.g2;

import java.awt.geom.Point2D;
import java.util.ArrayList;
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

public class JustKeepSwimming extends Player {

	private Direction direction;
	private SeaBoard board;
	private Point2D whereIAm = null;
	private Point2D boat = new Point2D.Double(0, 0);
	private int n = -1;
	private int radius, distance, penalty, numrounds, roundsleft;
	private double boatConstant = 1.2;
	private Logger log;

	

	private Direction getNewDirection() {
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
		whereIAm = myPosition;
		roundsleft --;
		
		for(Observation o: whatYouSee){
			
			//remove from board
			board.remove(o.getId());
			
			//add to board
			board.add(o);
		}
		
		//return message to isnorq
		if (n % 10 == 0)
			return "s";
		else
			return null;
	}

	@Override
	public Direction getMove() {
		
		/*If condition to determine when to start heading back.  Boat constant gives you a few extra 
		 rounds to head back, and dividing by three accounts for the fact that you can only make 
		 diagonal moves once every three rounds*/ 
		if(whereIAm.distance(boat) > (boatConstant * roundsleft)/3 ) 
			return backtrack();
		
		else return avoidHarm();
	}

	/**Move to avoid harm*/
	public Direction avoidHarm() {
		Direction d = getNewDirection();

		Point2D p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
				+ d.dy);
		while (Math.abs(p.getX()) > GameConfig.d
				|| Math.abs(p.getY()) > GameConfig.d) {
			d = getNewDirection();
			p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
					+ d.dy);
		}
		return d;
	}
	
	/**Dumb move included with dumb player*/
	public Direction dumbMove() {
		Direction d = getNewDirection();

		Point2D p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
				+ d.dy);
		while (Math.abs(p.getX()) > GameConfig.d
				|| Math.abs(p.getY()) > GameConfig.d) {
			d = getNewDirection();
			p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
					+ d.dy);
		}
		return d;
	}
	
	/**Move to get the player back to the boat*/
	public Direction backtrack() {
		log.trace("backtracking");
		Direction d = getNewDirection();

		Point2D p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
				+ d.dy);
		while (Math.abs(p.getX()) > GameConfig.d
				|| Math.abs(p.getY()) > GameConfig.d) {
			d = getNewDirection();
			p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
					+ d.dy);
		}
		return d;
	}

	/** Initialize our variables when a new game is created */
	@Override
	public void newGame(Set<SeaLifePrototype> seaLifePossibilites, int p,
			int d, int r, int n) {
		
		//Initialize game variables
		log = Logger.getLogger(this.getClass());

		board = new SeaBoard(2*d, 2*d);
		
		for (SeaLifePrototype s : seaLifePossibilites) {
			board.add(new SeaCreature(s));
		}

		penalty = p;
		distance = d;
		radius = r;
		numrounds = n;
		roundsleft = n;
	}

}
