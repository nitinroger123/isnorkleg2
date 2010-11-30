package isnork.g2.Strategy;

import java.awt.geom.Point2D;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
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

public class GeneralStrategy extends Strategy {

	private static final int TIME_TO_GO_HOME = 55;

	private Logger log = Logger.getLogger(this.getClass());
	public boolean goingOut = false;
	public Direction outDirection = null;
	protected double boatConstant = 2;
	boolean goingHome = false;

	public GeneralStrategy(int p, int d, int r,
			Set<SeaLifePrototype> seaLifePossibilites, Random rand, int id,
			int numDivers) {

		super(p, d, r, seaLifePossibilites, rand, id, numDivers);
		outDirection = getRandomDirection();
	}

	private Direction getRandomDirection() {
		return Strategy.directions.get(random.nextInt(8));
	}

	@Override
	public Direction getMove() {
		/*
		 * temp fix.
		 */
		if (roundsleft < TIME_TO_GO_HOME || goingHome) {
			return backtrack(true);
		}
		/**
		 * ON BOAT, DANGEROUS CREATURES RIGHT BELOW US
		 */
		if (whereIAm.equals(boat) && board.getSeaSpace(boat).hasDanger()) {
			return null;
		}

		/**
		 * GET BACK ON BOAT, NO TIME LEFT!!!
		 */
		// if (((whereIAm.distance(boat) + 2) > (roundsleft / 3))
		// || this.myHappiness >= board.getMaxScore()) {
		// System.err.println("backtracking " + roundsleft);
		// return backtrack(false);
		// }
		if (whereIAm.distance(boat) > roundsleft / 2
				|| this.myHappiness >= board.getMaxScore()) {
			goingHome = true;
			return backtrack(true);
		}
		
		if (whereIAm.distance(boat) > roundsleft / 3
				|| this.myHappiness >= board.getMaxScore()) {
			return backtrack(false);
		}

		/**
		 * DANGEROUS CREATURES NEARBY, RUN LIKE HELL
		 */
		if (board.areThereDangerousCreatures(this.whatISee)) {
			ArrayList<Direction> directionsToAvoid = board.getHarmfulDirections(this.whereIAm);
			return runAwayFromDanger(directionsToAvoid);
		}

		/**
		 * NO DANGEROUS ANIMALS AROUND
		 */
		// if he's reached intermediate goal, do something else
		if (intermediateGoal != null && intermediateGoal.equals(whereIAm)) {
			intermediateGoal = null;
		}

		// if he's on route to his goal and there are no dangerous creatures
		// around, go to goal
		if (intermediateGoal != null) {
			return getDirectionToGoal(intermediateGoal);
		}

		// if he's at the boat, generate a random direction and go out
		if (!goingOut && whereIAm.getX() == distance
				&& whereIAm.getY() == distance) {
			goingOut = true;
			outDirection = getRandomDirection();
			return outDirection;
		}

		// if he's going out and reached an edge, start coming back to the boat
		if (goingOut
				&& !(whereIAm.getX() < radius - 1
						|| whereIAm.getX() > board.board.length - radius + 1
						|| whereIAm.getY() < radius - 1 || whereIAm.getY() > board.board.length
						- radius + 1)) {
			return outDirection;
		}

		// if he's going out and reached an edge, start coming back to the boat
		if (goingOut
				&& (whereIAm.getX() < radius - 1
						|| whereIAm.getX() > board.board.length - radius + 1
						|| whereIAm.getY() < radius - 1 || whereIAm.getY() > board.board.length
						- radius + 1)) {
			goingOut = false;
			outDirection = null;
			return backtrack(false);
		}

		// if he's coming in and not at the boat, keep coming in
		if (!goingOut
				&& !(whereIAm.getX() == distance && whereIAm.getY() == distance)) {
			return backtrack(false);
		}

		if (!goingOut) {
			return backtrack(false);
		}

		if (outDirection == null) {
			outDirection = randomMove();
			return outDirection;
		}

		return outDirection;
	}

	public Direction runAwayFromDanger(ArrayList<Direction> harmfulDirections) {
		// If you are on the boat, you dont need to run
		if (whereIAm.getX() == boat.getX() && whereIAm.getY() == boat.getY()) {
			// you are safe
			return null;
		}
		// System.err.println("run away from danger");
		ArrayList<Direction> safeMoves = getOpposites(harmfulDirections);
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
		else {
			return randomMove();
		}
		// We could not find a valid safe move
		return randomMove();
	}

	private Direction getDirectionToGoal(Point2D goal) {
		double currX = whereIAm.getX();
		double currY = whereIAm.getY();
		Direction bestDir = null;
		double shortestDist = Double.MAX_VALUE;

		// check which direction will create the next shortest distance to the
		// goal
		for (Direction d : Strategy.directions) {
			double newX = currX + d.dx;
			double newY = currY + d.dy;
			double tempDist = goal.distance(newX, newY);
			if (tempDist < shortestDist) {
				shortestDist = tempDist;
				bestDir = d;
			}
		}

		return bestDir;
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

	/*
	 * New method to go to a goal without getting hurt. desperate time tells us
	 * if its imperative that we reach the boat or not. If true, we just go to
	 * the boat and don't avoid danger.
	 * 
	 */
	public Direction goToGoalWithoutGettingBit(Point2D goal,
			boolean desperateTime) {

		if (!desperateTime && board.areThereDangerousCreatures(whatISee)) {
			return runAwayFromDanger(board.getHarmfulDirections(whereIAm));
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

	/** Move to get the player back to the boat */
	public Direction backtrack(boolean desperateTime) {
		return goToGoalWithoutGettingBit(boat, desperateTime);
	}

	private Direction goToGoal(Point2D goal) {
		// System.err.println("Backtracking on round " + (480 - this.roundsleft)
		// + " from:" + whereIAm + " and boat is: " + boat);

		if (whereIAm.equals(goal)) {
			log.trace("we are staying put, we reached the goal");
			return null;
		}

		ArrayList<Direction> temp = new ArrayList<Direction>();
		if (board.areThereDangerousCreatures(whatISee))
			temp = board.getHarmfulDirections(this.whereIAm);

		ArrayList<BackTrackMove> backMoves = new ArrayList<BackTrackMove>();
		for (Direction d : Strategy.directions) {

			if (temp.contains(d))
				backMoves.add(new BackTrackMove(d, board.toGoal(whereIAm, d,
						goal), false, roundsleft, whereIAm, goal));
			else
				backMoves.add(new BackTrackMove(d, board.toGoal(whereIAm, d,
						goal), true, roundsleft, whereIAm, goal));

		}
		Collections.sort(backMoves);
		for (BackTrackMove safe : backMoves) {
			log.trace(safe);
		}

		for (BackTrackMove safe : backMoves) {
			if (board.isValidMove((int) whereIAm.getX(), (int) whereIAm.getY(),
					safe.d)) {
				return safe.d;
			}
		}

		// won't get here
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
		for (SeaCreatureType sc : ratedCreatures) {
			// ranking = happiness x avg frequency
			// double ranking = sc.returnCreature().getHappiness() *
			// (sc.returnCreature().getMinCount() +
			// sc.returnCreature().getMaxCount()) / 2.0;

			double ranking = sc.avgPossibleHappiness;

			// if he's dangerous, super low rank him
			// if (sc.returnCreature().isDangerous())
			// ranking *= -1.0;

			sc.ranking = ranking;
		}

		Collections.sort(ratedCreatures);
		Collections.reverse(ratedCreatures);

		int count = 0;
		for (SeaCreatureType sc : ratedCreatures) {
			creatureMapping.put(Character.toString(ALPHABET.charAt(count)), sc);

			// if (myId == 0)
			// System.err.println(count + " " + ALPHABET.charAt(count)
			// + " id: " + sc.getId() + " "
			// + sc.returnCreature().getName());
			count++;
		}
	}

	public String getTick(Set<Observation> whatYouSee) {
		SeaCreatureType bestVisible = null;
		SeaCreatureType worstVisible = null;
		double bestRanking = Double.MIN_VALUE;
		double worstRanking = Double.MAX_VALUE;

		// track which is the best creature that you can currently see
		for (Observation o : whatYouSee) {
			SeaCreatureType cur = knownCreatures.get(o.getName());

			if (cur != null) {
				if (cur.ranking > bestRanking) {
					bestVisible = cur;
					bestRanking = cur.ranking;
				}

				if (cur.ranking < worstRanking) {
					worstVisible = cur;
					worstRanking = cur.ranking;
				}
			}
		}

		// send out the message that refers to the best creature
		if (bestVisible != null && bestVisible.ranking > 0)
			return bestVisible.isnorkMessage;

		// if you can't see any good creatures, send out the most dangerous
		// creature
		if (worstVisible != null)
			return worstVisible.isnorkMessage;

		return null;
	}

	public void updateIncomingMessages(Set<iSnorkMessage> incomingMessages) {
		SeaCreatureType bestFind = null;
		double curDist = Double.MAX_VALUE;
		boolean changed = false;
		String rcvd = null;

		for (iSnorkMessage ism : incomingMessages) {
			Point2D newLoc = new Point2D.Double(ism.getLocation().getX()
					+ distance, ism.getLocation().getY() + distance);
			SeaCreatureType sc = creatureMapping.get(ism.getMsg());
			rcvd = ism.getMsg();

			if ((ism.getMsg().equals("a") || ism.getMsg().equals("b"))
					&& !sc.seenOnce) {
				// base case
				if (bestFind == null && sc.nextHappiness > 0) {
					if (ism.getMsg().equals("a") && !sc.seenOnce) {
						// base case
						if (bestFind == null && sc.nextHappiness > 0) {
							bestFind = sc;
							intermediateGoal = newLoc;
							searchingFor = sc;
							changed = true;
						}
						// equality case, it's two of the same creature, get the
						// closest one
						else if (sc != null && bestFind != null
								&& sc.nextHappiness == bestFind.nextHappiness) {
							double newDist = whereIAm.distance(newLoc);
							if (newDist < curDist) {
								curDist = newDist;
								bestFind = sc;
								intermediateGoal = newLoc;
								searchingFor = sc;
								changed = true;
							}
						}
						// general case, does this new creature have a higher
						// next
						// happiness?
						else if (sc != null && bestFind != null
								&& sc.nextHappiness > bestFind.nextHappiness) {
							curDist = whereIAm.distance(newLoc);
							bestFind = sc;
							intermediateGoal = newLoc;
							searchingFor = sc;
							changed = true;
						}
					}
				}
			}
		}

		// if (myId == 0 && intermediateGoal != null && changed)
		// System.err.println(myId + " rcvd: " + rcvd + " ||| going to "
		// + intermediateGoal.toString());
	}

	public void checkFoundGoal(Set<iSnorkMessage> incomingMessages) {
		if (searchingFor != null && searchingFor.seenOnce) {
			intermediateGoal = null;
			searchingFor = null;
		}
	}

	
	public Direction makeSpiralMove()
	{
		
		return null;
	}
}
