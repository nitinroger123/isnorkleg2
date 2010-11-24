package isnork.g2.Strategy;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import isnork.g2.SeaBoard;
import isnork.sim.SeaLifePrototype;
import isnork.sim.GameObject.Direction;

public class GeneralStrategy extends Strategy {

	private Logger log = Logger.getLogger(this.getClass());
	public int myId = 1;

	public GeneralStrategy(int p, int d, int r,
			Set<SeaLifePrototype> seaLifePossibilites, Random rand, int id) {
		super(p, d, r, seaLifePossibilites, rand);
		myId = id;
		// TODO Auto-generated constructor stub
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
		if (roundsleft < 45)
			return backtrack();

		/*
		 * If condition to determine when to start heading back. Boat constant
		 * gives you a few extra rounds to head back, and dividing by three
		 * accounts for the fact that you can only make diagonal moves once
		 * every three rounds
		 */
		if (whereIAm.distance(boat) > (boatConstant * roundsleft) / 3) {
			System.err.println(myId + ": backtrack() called");
			return backtrack();
		}

		if (board.getDangerInRadius(whereIAm, numrounds - roundsleft)) {
			System.err.println(myId + ": avoid danger is called");
			return avoidHarm();
		}

		// No dangerous animals around
		System.err.println(myId + " called random move");
		return randomMove();

	}

	/** Move to avoid harm */
	public Direction avoidHarm() {
		log.trace("Move Avoiding Harm");
		ArrayList<Direction> pos = Direction.allBut(null);

		// determine the dangerous directions based on where you currently are
		ArrayList<Direction> danger = board.getDangerousDirections(whereIAm);
		log.trace("Danger length: " + danger.size());

		for (int i = 0; i < danger.size(); i++) {
			log.trace(danger.get(i));
			// if the direction is dangerous OR the direction is an invalid
			// move, remove the direction
			if (pos.contains(danger.get(i))
					|| board.isValidMove((int) whereIAm.getX(), (int) whereIAm
							.getY(), danger.get(i))) {
				log.trace("removing from pos");
				pos.remove(danger.get(i));
			}
		}

		Collections.shuffle(pos); // Randomize safe directions
		int index = 0;
		log.trace("We have " + pos.size() + " safe moves");
		Direction d = Direction.N; // Initialize
		if (pos.size() == 0) {// Need to make this better to find the best bad
			// move
			d = randomMove();
		} 
		else
		{
			d = pos.get(index);
		}

		Point2D p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
				+ d.dy);
		while (p.getX() < 0 || p.getX() > distance * 2 - 1 || p.getY() < 0
				|| p.getY() > distance * 2 - 1) {
			index++;
			if (index < pos.size())
				d = pos.get(index);
			else
				d = randomMove();
			p = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY()
					+ d.dy);
		}
		return d;
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

	/** Move to get the player back to the boat */
	public Direction backtrack() {
		double currX = whereIAm.getX();
		double currY = whereIAm.getY();
		log.trace("Current X: " + currX + " Current Y: " + currY);
		if (currX == boat.getX() && currY == boat.getY()) {
			// stay put, we have reached the boat
			return null;
		}
		if (currX > boat.getX() && currY > boat.getY()) {
			return Direction.NW;
		}
		if (currX < boat.getX() && currY < boat.getY()) {
			return Direction.SE;
		}
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
}
