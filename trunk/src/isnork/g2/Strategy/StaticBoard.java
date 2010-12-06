package isnork.g2.Strategy;

import isnork.g2.utilities.DestinationPoints;
import isnork.g2.utilities.DirectionToSort;
import isnork.g2.utilities.EachSeaCreature;
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
	private Point2D goal;
	private ArrayList<Point2D> allgoals;
	private ArrayList<Point2D> goalsLookedFor;
	private ArrayList<Point2D> spacehistory;
	public SeaCreatureType chasing = null;
	public int numSeen = 0, historytokeep = distance;
	public ArrayList<Integer> seenBestTracker = new ArrayList<Integer>();
	public boolean seesMax = false;
	public Point2D chasingGoal = null;
	public Point2D tempChasingGoal = null;
	private double boatconstant = 2;
	private int breakawaycount = 16;
	private ArrayList<Direction> pathtogo = null;
	private boolean seenall = false;
	private boolean goingHome = false;

	public StaticBoard(int p, int d, int r,
			Set<SeaLifePrototype> seaLifePossibilities, Random rand, int id,
			int numDivers, SeaBoard b) {
		super(p, d, r, seaLifePossibilities, rand, id, numDivers, b);
		smallradius = 2;
		goingOut = true;
		allgoals = new ArrayList<Point2D>();
		allgoals.add(new Point2D.Double(0, (distance - radius)));
		allgoals.add(new Point2D.Double(2 * distance - radius,
				(distance - radius)));
		allgoals.add(new Point2D.Double(distance - radius, 0));
		allgoals.add(new Point2D.Double(0, 0));
		allgoals.add(new Point2D.Double(2 * distance - radius, 2 * distance
				- radius));
		allgoals.add(new Point2D.Double(2 * distance - radius, 0));
		allgoals.add(new Point2D.Double(distance - radius, 2 * distance
				- radius));
		allgoals.add(new Point2D.Double(0, 2 * distance - radius));
		allgoals.add(new Point2D.Double(distance/2, 0));
		allgoals.add(new Point2D.Double(3*distance/2, 0));
		allgoals.add(new Point2D.Double(0, distance/2));
		allgoals.add(new Point2D.Double(0, 3*distance/2));
		allgoals.add(new Point2D.Double(distance/2, 2*distance));
		allgoals.add(new Point2D.Double(3*distance/2, 2*distance));
		allgoals.add(new Point2D.Double(2*distance, distance/2));
		allgoals.add(new Point2D.Double(2*distance, 3*distance/2));
		


		
		goalsLookedFor = new ArrayList<Point2D>();
		spacehistory = new ArrayList<Point2D>();

		rateCreatures(seaLifePossibilities);
	}

	@Override
	public void checkFoundGoal(Set<iSnorkMessage> incomingMessages) {
		// TODO Auto-generated method stub

	}

	@Override
	public Direction getMove() {

		/*if(myId == 0)
			System.out.println("\n\n round: " + this.roundsleft);*/
		Direction move = null;

		/*
		 * if(tempboat != null && roundsleft < 10) boat = tempboat;
		 */

		// if(roundsleft < 20 && !board.seenall())
		// System.out.println(this.myId + " has not seen everything...");
		//if(roundsleft < 20 && !board.seenall()) System.out.println(this.myId + " has not seen everything...");

		// what to do if there is a static under the boat
		/*
		 * if(whereIAm.equals(boat) && board.getSeaSpace(boat).hasDanger()){
		 * for(Direction d: this.directions){ Point2D newboat = new
		 * Point2D.Double(whereIAm.getX() + d.dx, whereIAm.getY() + d.dy);
		 * if(!board.getSeaSpace(newboat).hasDanger()){ tempboat = boat; boat =
		 * newboat; } } move = webMove(); }
		 * 
		 * else
		 */

		// Urgent return to boat
		if (goingHome || (getRoundsDistance(whereIAm, boat) + 6 >= roundsleft)) {
			goingHome = true;
			move = backtrack(true);
		}

		// Start returning to the boat
		if (getRoundsDistance(whereIAm, boat) + 21 >= roundsleft
				|| this.myHappiness >= board.getMaxScore()) {
			move = backtrack(false);
		}

		else if (pathtogo != null && pathtogo.size() > 0) {
			//System.out.println(myId + " is moving on a path");
			move = pathtogo.get(0);
			pathtogo.remove(0);
		}

		else if (board.seenall()) {
			if (seenall = false) {
			//System.out.println(myId + " has seen all and is creating a path");
				seenall = true;
				spacehistory.clear();
			}
			move = lookfor();
		}

		/*
		 * else if (this.roundsleft < 200) { System.out.println(myId + " assumes
		 * we have seen everything"); move = lookfor(); }
		 */

		// Spend the first two hours going out in different directions
		else {
			// System.out.println(myId + " is creating a web move");
			move = webMove();
		}
		// Anylize map and come up with a good path

		if (spacehistory.size() >= historytokeep)
			spacehistory.remove(0);

		if (move != null)
			spacehistory.add(spacehistory.size(), new Point2D.Double(whereIAm
					.getX()
					+ move.dx, whereIAm.getY() + move.dy));
		/*if (move == null && !boat.equals(whereIAm))
			System.out.println("uhh we somehow got a null move?");*/
		return move;
	}

	private Direction lookfor() {

		ArrayList<DestinationPoints> destinations = new ArrayList<DestinationPoints>();
		for (int i = 0; i < 2 * distance; i++) {
			for (int j = 0; j < 2 * distance; j++) {
				for (EachSeaCreature c : board.getSeaSpace(
						new Point2D.Double(i, j)).getUnseenCreatures()) {
					destinations.add(new DestinationPoints(c.location,
							whereIAm, c.returnCreature().getHappinessD()));
				}
			}
		}

		if (destinations.size() > 0) {
			for (DestinationPoints d : destinations)
				d.whereIAm = whereIAm;

			Collections.sort(destinations);
			return goToGoalWithoutGettingBit(destinations.get(0).location,
					false);
		}

		else {
			//System.out.println("nothing in destination");
			return goToGoalWithoutGettingBit(boat, false);
		}
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

			if (m.getSender() != myId) {

				if (m.getMsg() != null) {

					Point2D mloc = new Point2D.Double(m.getLocation().getX()
							+ distance, m.getLocation().getY() + distance);

					if (!board.getSeaSpace(mloc).isoccupideby(
							creatureMapping.get(m.getMsg()))) {

						/*
						 * System.out.println("adding: " +
						 * creatureMapping.get(m.getMsg())
						 * .returnCreature().getName());
						 */

						board.getSeaSpace(mloc).addCreature(
								creatureMapping.get(m.getMsg()));

						/*
						 * System.out.println(myId + " just added: " +
						 * creatureMapping.get(m.getMsg()).returnCreature().getName() + "
						 * from diver: " + m.getSender());
						 */
					}
				}
			}
		}
	}

	public Direction webMove() {

		Direction direction = null;

		boolean indanger = board.areThereDangerousCreaturesInRadius(whatISee,
				whereIAm, smallradius);

		boolean found = false;
		if (goalsLookedFor.size() == this.allgoals.size())
			goalsLookedFor.clear();
		if (goal == null) {
			while (!found) {
				Collections.shuffle(allgoals);
				if (!goalsLookedFor.contains(allgoals.get(0))) {
					goalsLookedFor.add(allgoals.get(0));
					goal = allgoals.get(0);
					found = true;
				}
				// System.out.println("just set goal to:" + goal);
			}
		}

		// System.out.println("our goal is: " + goal);

		if (!goingOut && whereIAm.getX() == distance
				&& whereIAm.getY() == distance) {
			goingOut = true;
			direction = this.goToGoalWithoutGettingBit(goal, false);
		}

		if (goingOut
				&& !(whereIAm.getX() < radius - 1
						|| whereIAm.getX() > board.board.length - radius + 1
						|| whereIAm.getY() < radius - 1 || whereIAm.getY() > board.board.length
						- radius + 1)) {
			direction = this.goToGoalWithoutGettingBit(goal, false);

		}

		// if he's going out and reached an edge, start coming back to the boat
		boolean goalcantbereached = false; // if there is danger within two of
		// the goal then you can't get there
		if (whereIAm.distance(goal) < smallradius
				&& board.areThereDangerousCreaturesInRadius(whatISee, whereIAm,
						smallradius))
			goalcantbereached = true;

		// turn around and come home
		if ((goingOut && (whereIAm.getX() < radius - 1
				|| whereIAm.getX() > board.board.length - radius + 1
				|| whereIAm.getY() < radius - 1 || whereIAm.getY() > board.board.length
				- radius + 1))
				|| goalcantbereached) {

			goingOut = false;
			goal = null;
			spacehistory.clear();
			direction = backtrack(false);
		}

		// if he's coming in and not at the boat, keep coming in
		if (!goingOut
				&& !(whereIAm.getX() == distance && whereIAm.getY() == distance)) {
			direction = backtrack(false);
		}

		if (!goingOut) {
			direction = backtrack(false);
		}

		if (direction == null) {
			direction = this.goToGoalWithoutGettingBit(goal, false);
		}

		return direction;
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
			if (goal.equals(boat))
				return null;
			else
				return goToGoalWithoutGettingBit(boat, false);
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

		Point2D.Double going = new Point2D.Double(whereIAm.getX() + ret.dx,
				whereIAm.getY() + ret.dy);
		/*
		 * if(spacehistory.contains(going)) System.out.println("we are about to
		 * reject : " + going + " beacuase we have already been there");
		 */
		if (going.equals(boat))
			return ret;

		else if (desperateTime) {
			// System.out.println(myId + " Desparate time, going directly
			// back");
			return ret;
		}

		else if ((!moveputsusindanger(ret, whereIAm, whatISee) && !spacehistory
				.contains(going))) {
			// System.out.println("move does not put us in danger");
			return ret;
		}

		else {
			System.out.println(myId + " move " + ret
					+ " was rejected, desparate: " + desperateTime
					+ ", danger: "
					+ moveputsusindanger(ret, whereIAm, whatISee)
					+ ", history: " + spacehistory.contains(going));

			ArrayList<DirectionToSort> directionstosort = new ArrayList<DirectionToSort>();
			for (Direction d : this.directions) {
				going = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm
						.getY()
						+ d.dy);
				if (!moveputsusindanger(d, whereIAm, whatISee)
						&& !spacehistory.contains(going))
					directionstosort
							.add(new DirectionToSort(d, goal, whereIAm));
				/*
				 * if (spacehistory.contains(going)) System.out.println("cant
				 * go: " + d + " because we have already been there");
				 */
			}

			if (directionstosort.size() > 0) {
				Collections.sort(directionstosort);

				/*
				 * for (DirectionToSort d : directionstosort) {
				 * System.out.println(d); } System.out.println("\n\n");
				 */

				return directionstosort.get(0).dir;
			}

			if (!goal.equals(boat))
				return goToGoalWithoutGettingBit(boat, false);
			else {
				System.out.println("need to breakaway on round: "
						+ this.roundsleft);
				return breakaway();
			}
		}
	}

	private Direction breakaway() {

		Direction ret = null;
		for (Direction d : this.directions) {
			int count = 0;
			pathtogo = new ArrayList<Direction>();
			Point2D going = new Point2D.Double(whereIAm.getX() + d.dx, whereIAm
					.getY()
					+ d.dy);

			while (!moveputsusindanger(going) && count < breakawaycount + 1) {
				for (Direction d2 : this.directions) {
					if (!moveputsusindanger(going, d2)) {
						count++;
						going = new Point2D.Double(going.getX() + d2.dx, going
								.getY()
								+ d2.dy);
						pathtogo.add(d2);
					}
				}
			}

			if (count > breakawaycount) {
				return d;
			}

			pathtogo.clear();
		}

		//System.out.println("grr still null");
		goal = null;
		boolean found = false;
		if (goalsLookedFor.size() == this.allgoals.size())
			goalsLookedFor.clear();
		if (goal == null) {
			while (!found) {
				Collections.shuffle(allgoals);
				if (!goalsLookedFor.contains(allgoals.get(0))) {
					goalsLookedFor.add(allgoals.get(0));
					goal = allgoals.get(0);
					found = true;
				}
				// System.out.println("just set goal to:" + goal);
			}
		}
		
		return goToGoalWithoutGettingBit(goal, false);
	}

	private boolean isopposite(Direction d, Direction lastmove2) {

		if (d == null || lastmove2 == null)
			return false;

		if (d.getDegrees() == lastmove2.getDegrees() + 180
				|| d.getDegrees() == lastmove2.getDegrees() - 180) {
			return true;
		}

		return false;
	}

	private boolean moveputsusindanger(Direction ret, Point2D whereIAm,
			Set<Observation> whatISee) {
		Point2D iamgoing = new Point2D.Double(whereIAm.getX() + ret.dx,
				whereIAm.getY() + ret.dy);

		if (iamgoing.equals(boat))
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

	private boolean moveputsusindanger(Point2D going) {
		if (going.equals(boat))
			return false;

		for (Observation o : whatISee) {
			Point2D newolocation = new Point2D.Double(o.getLocation().getX()
					+ distance, o.getLocation().getY() + distance);

			if (o.isDangerous() && newolocation.distance(going) < smallradius) {

				return true;
			}
		}

		return false;
	}

	private boolean moveputsusindanger(Point2D me, Direction ret) {

		Point2D iamgoing = new Point2D.Double(me.getX() + ret.dx, me.getY()
				+ ret.dy);

		if (iamgoing.equals(boat))
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

	/** Move to get the player back to the boat */
	public Direction backtrack(boolean desperateTime) {

		return goToGoalWithoutGettingBit(boat, desperateTime);
	}

}