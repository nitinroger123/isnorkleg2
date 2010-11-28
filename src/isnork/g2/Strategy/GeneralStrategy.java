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
	public Direction outDirection = null;
	protected double boatConstant = 2;

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

		/*
		 * If we are on the boat and there is danger on the boat we should not
		 * move
		 */
		if (whereIAm.equals(boat) && board.getSeaSpace(boat).hasDanger()){
			log.trace("returning null");
			return null;}

		/*
		 * Desperate measure. Get back on the boat. This is to avoid the
		 * situation where the divers come near the boat and then move away. I
		 * found this happening with our current condition
		 */
		if(this.myHappiness >= board.getMaxScore())
			log.trace("backtrack because we are happy, max: " + board.getMaxScore());
		if ((boatConstant * (whereIAm.distance(boat) + 2) > (roundsleft) / 3)
				|| this.myHappiness >= board.getMaxScore())
			return backtrack();

		// If there are dangerous creatures nearby, run like hell.
		if (board.areThereDangerousCreatures(this.whatISee)) {
			return runAwayFromDanger();
		}

		/**
		 * NO DANGEROUS ANIMALS AROUND
		 */
		return randomMove();
	}

	public Direction runAwayFromDanger() {

		log.trace("Running away from danger");

		ArrayList<Direction> safeMoves = getOpposites(board
				.getHarmfulDirections(this.whereIAm));
		if (!safeMoves.isEmpty()) {
			Collections.shuffle(safeMoves);
			for (Direction safe : safeMoves) {
				if (board.isValidMove((int) whereIAm.getX(), (int) whereIAm
						.getY(), safe)) {
					return safe;
				}
			}
		}
		/* We are surrounded */
		return null;
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
		log.trace("random move on round " + (480 - this.roundsleft));
		Direction d = getRandomDirection();
		while (!board.isValidMove((int) whereIAm.getX(), (int) whereIAm.getY(),
				d)) {
			d = getRandomDirection();
		}
		return d;
	}

	/** Move to get the player back to the boat */
	public Direction backtrack() {

		log.trace("Backtracking on round " + (480 -  this.roundsleft) + " from:" + whereIAm + " and boat is: " + boat);
		
		if (whereIAm.equals(boat)) {
			log.trace("we are staying put, we reached the boat");
			return null;
		}
		
		ArrayList<Direction> temp = getOpposites(board
				.getHarmfulDirections(this.whereIAm));
		
		ArrayList<BackTrackMove> backMoves = new ArrayList<BackTrackMove>();
		for(Direction d: this.directions){
			if(temp.contains(d))
				backMoves.add(new BackTrackMove(d, board.toBoat(whereIAm, d), false, roundsleft, whereIAm, boat));
			else
				backMoves.add(new BackTrackMove(d, board.toBoat(whereIAm, d), true, roundsleft, whereIAm, boat));

		}
		if (!backMoves.isEmpty()) {
			Collections.sort(backMoves);
			log.trace("First move is: " + backMoves.get(0));
			for (BackTrackMove safe : backMoves) {
				log.trace(safe);
			}

			for (BackTrackMove safe : backMoves) {
				if (board.isValidMove((int) whereIAm.getX(), (int) whereIAm
						.getY(), safe.d)) {
					return safe.d;
				}
			}
		}
		
		//wont get here
		return null;
	}

	/**
	 * Rate the creatures using several heuristics. These are to determine the
	 * ranking at which creatures will be placed on the lettering of the iSnork.
	 * Creatures are ranked by their happiness, their frequency, and the amount
	 * of the board you can see.
	 */
	public void rateCreatures(Set<SeaLifePrototype> seaLifePossibilities) {
		
		// calculate each of the sea creature's ranking value
		for (SeaCreature sc : creatureRating) {
			// happiness x avg frequency
			double ranking = sc.returnCreature().getHappiness()
					* (sc.returnCreature().getMinCount() + sc.returnCreature()
							.getMaxCount()) / 2.0;
			sc.ranking = ranking;
		}

		Collections.sort(creatureRating);
		Collections.reverse(creatureRating);
	}

	@Override
	public String toIsnork() {

		if (board.getHighScoringCreatureInRadius() != null) {
			return Character.toString(
					board.getHighScoringCreatureInRadius().returnCreature()
							.getName().toCharArray()[0]).toLowerCase();
		} else
			return null;
	}
}
