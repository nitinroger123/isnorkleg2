package isnork.g2.Strategy;

import isnork.g2.utilities.DirectionToSort;
import isnork.g2.utilities.SeaBoard;
import isnork.g2.utilities.SeaCreatureType;
import isnork.sim.GameObject;
import isnork.sim.Observation;
import isnork.sim.SeaLifePrototype;
import isnork.sim.iSnorkMessage;
import isnork.sim.GameObject.Direction;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class StaticBoard extends Strategy {

	private Boolean goingOut;
	public Direction outDirection = null;
	private Point2D goal;
	private ArrayList<Point2D> allgoals;
	private ArrayList<Direction> directionstraversed;
	private ArrayList<Point2D> lastthreespaces;
	public SeaCreatureType chasing = null;
	public int numSeen = 0;
	public ArrayList<Integer> seenBestTracker = new ArrayList<Integer>();
	public boolean seesMax = false;
	public Point2D chasingGoal = null;
	public Point2D tempChasingGoal = null;

	public StaticBoard(int p, int d, int r,
			Set<SeaLifePrototype> seaLifePossibilities, Random rand, int id,
			int numDivers, SeaBoard b) {
		super(p, d, r, seaLifePossibilities, rand, id, numDivers, b);
		smallradius = 2;
		goingOut = true;
		allgoals = new ArrayList<Point2D>();
		allgoals.add(new Point2D.Double(0, distance));
		allgoals.add(new Point2D.Double(2 * distance, distance));
		allgoals.add(new Point2D.Double(distance, 0));
		allgoals.add(new Point2D.Double(0, 0));
		allgoals.add(new Point2D.Double(2 * distance, 2 * distance));
		allgoals.add(new Point2D.Double(2 * distance, 0));
		allgoals.add(new Point2D.Double(distance, 2 * distance));
		allgoals.add(new Point2D.Double(0, 2 * distance));

		directionstraversed = new ArrayList<Direction>();
		lastthreespaces = new ArrayList<Point2D>();

		rateCreatures(seaLifePossibilities);
	}

	@Override
	public void checkFoundGoal(Set<iSnorkMessage> incomingMessages) {
		// TODO Auto-generated method stub

	}

	@Override
	public Direction getMove() {

		// what to do if there is a static under the boat

		if (this.myHappiness == board.getMaxScore())
			return backtrack(false);

		if (whereIAm.distance(boat) + 3 > this.roundsleft / 3)
			return backtrack(false);

		if (whereIAm.distance(boat) > this.roundsleft / 3)
			return backtrack(true);

		if (board.seenall()) {
			System.out.println("I know where everythign is");
			// return lookfor()
		}

		// Spend the first two hours going out in different directions
		return webMove(null);

		// Anylize map and come up with a good path
	}

	public String getTick() {
		SeaCreatureType bestVisible = null;
		SeaCreatureType worstVisible = null;
		double bestRanking = Double.MIN_VALUE;
		double worstRanking = Double.MAX_VALUE;
		boolean maxThisRound = false;

		// track which is the best creature that you can currently see
		for (Observation o : whatISee) {
			SeaCreatureType cur = knownCreatures.get(o.getName());

			if (cur != null && cur.isnorkMessage != null) {
				cur.seenOnce = true;

				if (cur.isnorkMessage.equals("a")) {
					maxThisRound = true;
					seesMax = true;
					tempChasingGoal = new Point2D.Double(o.getLocation().getX()
							+ distance, o.getLocation().getY() + distance);
				}

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

		if (!maxThisRound)
			seesMax = false;

		if (chasing != null)
			return chasing.isnorkMessage;

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

		for (iSnorkMessage m : incomingMessages) {

			if (m.getMsg() != null) {

				Point2D mloc = new Point2D.Double(m.getLocation().getX() + distance, m.getLocation().getY() + distance);
				
				if (!board.getSeaSpace(mloc).isoccupideby(
						creatureMapping.get(m.getMsg()))) {
					board.getSeaSpace(mloc).addCreature(
							creatureMapping.get(m.getMsg()));
				}
			}
		}
	}

	public Direction webMove(ArrayList<Direction> directionsOffLimits) {

		boolean indanger = board.areThereDangerousCreaturesInRadius(whatISee,
				whereIAm, smallradius);

		boolean found = false;
		if (directionstraversed.size() == this.directions.size())
			directionstraversed.clear();
		if (goal == null) {
			while (!found) {
				Collections.shuffle(allgoals);
				if (!directionstraversed.contains(allgoals.get(0))) {
					goal = allgoals.get(0);
					found = true;
				}
				//System.out.println("just set goal to:" + goal);
			}
		}

		//System.out.println("our goal is: " + goal);

		if (!goingOut && whereIAm.getX() == distance
				&& whereIAm.getY() == distance) {
			goingOut = true;
			outDirection = this.goToGoalWithoutGettingBit(goal, false);
		}

		if (goingOut
				&& !(whereIAm.getX() < radius - 1
						|| whereIAm.getX() > board.board.length - radius + 1
						|| whereIAm.getY() < radius - 1 || whereIAm.getY() > board.board.length
						- radius + 1)) {
			outDirection = this.goToGoalWithoutGettingBit(goal, false);

		}

		// if he's going out and reached an edge, start coming back to the boat
		if (goingOut
				&& (whereIAm.getX() < radius - 1
						|| whereIAm.getX() > board.board.length - radius + 1
						|| whereIAm.getY() < radius - 1 || whereIAm.getY() > board.board.length
						- radius + 1)) {

			goingOut = false;
			goal = null;
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
			outDirection = this.goToGoalWithoutGettingBit(goal, false);
		}

		if (lastthreespaces.size() >= 3)
			lastthreespaces.remove(0);
		if(outDirection != null)
			lastthreespaces.add(lastthreespaces.size(), new Point2D.Double(whereIAm
				.getX()
				+ outDirection.dx, whereIAm.getY() + outDirection.dy));
		return outDirection;
	}

	/** Gets the closest direction that is not harmful */
	private Direction closestDirection(Direction outDirection2,
			ArrayList<Direction> harmfulDirections) {

		for (Direction d : this.directions) {

			if (outDirection2.getDegrees() - 45 == d.getDegrees()
					|| outDirection2.getDegrees() + 45 == d.getDegrees()) {
				if (!harmfulDirections.contains(d))
					return d;
			}
		}

		for (Direction d : this.directions) {
			if (!harmfulDirections.contains(d))
				return d;
		}

		return null;

	}

	public Direction randomBut(ArrayList<Direction> n) {
		ArrayList<Direction> r = new ArrayList<GameObject.Direction>();
		for (Direction d : Direction.values()) {
			if (n == null || !n.contains(d))
				r.add(d);
		}

		if (r != null) {
			Collections.shuffle(r);
			return r.get(0);
		}

		return null;
	}

	/*
	 * public Direction goToGoalWithoutGettingBit(Point2D goal, boolean
	 * desperateTime) {
	 * 
	 * return null; }
	 */

	/**
	 * New method to go to a goal without getting hurt. Desperate time tells us
	 * if its imperative that we reach the boat or not. If true, we just go to
	 * the goal and don't avoid danger.
	 */
	public Direction goToGoalWithoutGettingBit(Point2D goal,
			boolean desperateTime) {

		Direction ret = null;
		double currX = whereIAm.getX();
		double currY = whereIAm.getY();
		if (currX == goal.getX() && currY == goal.getY()) {
			// stay put, we have reached the goal
			return null;
		}

		// in a quadrant
		if (currX > goal.getX() && currY > goal.getY()) {
			ret = Direction.NW;
		}
		if (currX < goal.getX() && currY < goal.getY()) {
			ret = Direction.SE;
		}
		if (currX < goal.getX() && currY > goal.getY()) {
			ret = Direction.NE;
		}
		if (currX > goal.getX() && currY < goal.getY()) {
			ret = Direction.SW;
		}

		// on a line
		if (currX < goal.getX() && currY > goal.getY() || currX < goal.getX()
				&& currY == goal.getY()) {
			ret = Direction.E;
		}
		if (currX > goal.getX() && currY < goal.getY() || currX == goal.getX()
				&& currY < goal.getY()) {
			ret = Direction.S;
		}

		if (currX == goal.getX() && currY > goal.getY()) {
			ret = Direction.N;
		}
		if (currX > goal.getX() && currY == goal.getY()) {
			ret = Direction.W;
		}

		if (!moveputsusindanger(ret, whereIAm, whatISee)) {
			//System.out.println("move does not put us in danger");
			return ret;
		}

		else {
			//System.out.println("move " + ret + " puts us in danger\n\n");

			ArrayList<DirectionToSort> directionstosort = new ArrayList<DirectionToSort>();
			for (Direction d : this.directions) {
				Point2D going = new Point2D.Double(whereIAm.getX() + d.dx,
						whereIAm.getY() + d.dy);
				if (!moveputsusindanger(d, whereIAm, whatISee)
						&& !lastthreespaces.contains(going))
					directionstosort
							.add(new DirectionToSort(d, goal, whereIAm));
				/*if (lastthreespaces.contains(going))
					System.out.println("cant go: " + d
							+ " because we have already been there");*/
			}

			if (directionstosort.size() > 0) {
				Collections.sort(directionstosort);

				/*for (DirectionToSort d : directionstosort) {
					System.out.println(d);
				}*/

				if (lastthreespaces.size() >= 3)
					lastthreespaces.remove(0);
				lastthreespaces.add(lastthreespaces.size(), new Point2D.Double(
						whereIAm.getX() + directionstosort.get(0).dir.dx,
						whereIAm.getY() + directionstosort.get(0).dir.dy));
				return directionstosort.get(0).dir;
			}

			//System.out.println("could not find a safe move");
			return null;
		}
	}

	private boolean isopposite(Direction d, Direction lastmove2) {

		if (d == null || lastmove2 == null)
			return false;

		if (d.getDegrees() == lastmove2.getDegrees() + 180
				|| d.getDegrees() == lastmove2.getDegrees() - 180) {
			System.out.println(d + " and " + lastmove2 + " are opposites");
			return true;
		}

		return false;
	}

	private boolean moveputsusindanger(Direction ret, Point2D whereIAm,
			Set<Observation> whatISee) {
		Point2D iamgoing = new Point2D.Double(whereIAm.getX() + ret.dx,
				whereIAm.getY() + ret.dy);

		if (iamgoing == boat)
			return false;

		for (Observation o : whatISee) {
			Point2D newolocation = new Point2D.Double(o.getLocation().getX()
					+ distance, o.getLocation().getY() + distance);

			if (o.isDangerous()
					&& newolocation.distance(iamgoing) < smallradius) {

				return true;
			}
		}

		return false;
	}

	/**
	 * Rate the creatures using several heuristics. These are to determine the
	 * ranking at which creatures will be placed on the lettering of the iSnork.
	 * Creatures are ranked by their happiness, their frequency, and the amount
	 * of the board you can see.
	 */
	public void rateCreatures(Set<SeaLifePrototype> seaLifePossibilities) {

		System.out.println("rating creatures");

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

		// if there are more than 26 creatures, remove the
		// ones that are of the least importance
		while (ratedCreatures.size() > 26) {
			ratedCreatures.remove(ratedCreatures.size() - 1);
		}

		int count = 0;
		for (SeaCreatureType sc : ratedCreatures) {
			creatureMapping.put(
					Character.toString(ALPHABET.charAt(count % 26)), sc);

			/*
			 * if (myId == 0) { System.err.println(count + " " +
			 * ALPHABET.charAt(count % 26) + " id: " + sc.getId() + " " +
			 * sc.returnCreature().getName()); }
			 */
			count++;
		}
	}

}