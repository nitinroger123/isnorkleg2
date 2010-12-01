package isnork.g2.Strategy;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import org.apache.log4j.Logger;

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
			int numDivers, SeaBoard b) {

		super(p, d, r, seaLifePossibilites, rand, id, numDivers, b);
		outDirection = getRandomDirection();
		setupSpiral();
	}

	private Direction getRandomDirection() {
		return Strategy.directions.get(random.nextInt(8));
	}

	@Override
	public Direction getMove() {
		// if(myId == 0)
		// {
		// System.out.println(roundsleft + " ---------------------");
		// System.out.println("board max: " + board.getMaxScore());
		// System.out.println("intermediate goal: " + intermediateGoal);
		// System.out.println("spiral goal: " + spiralGoal);
		// }

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
		if (whereIAm.distance(boat) > roundsleft / 3)
		// || this.myHappiness >= board.getMaxScore())
		{
			goingHome = true;
			return backtrack(true);
		}

		if (whereIAm.distance(boat) > roundsleft / 4)
		// || this.myHappiness >= board.getMaxScore())
		{
			return backtrack(false);
		}

		/**
		 * DANGEROUS CREATURES NEARBY, RUN LIKE HELL
		 */
		if (board.areThereDangerousCreatures(this.whatISee)) {
			ArrayList<Direction> directionsToAvoid = board
					.getHarmfulDirections(this.whereIAm, this.whatISee);
			return runAwayFromDanger(directionsToAvoid, intermediateGoal);
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
			return getDirectionToGoal(whereIAm, intermediateGoal);
		}

		// if he's at the boat, generate a random direction and go out
		/*
		 * if (!goingOut && whereIAm.getX() == distance && whereIAm.getY() ==
		 * distance) { goingOut = true; outDirection = getRandomDirection();
		 * return outDirection; }
		 * 
		 * // if he's going out and reached an edge, start coming back to the
		 * boat if (goingOut && !(whereIAm.getX() < radius - 1 ||
		 * whereIAm.getX() > board.board.length - radius + 1 || whereIAm.getY()
		 * < radius - 1 || whereIAm.getY() > board.board.length - radius + 1)) {
		 * return outDirection; }
		 * 
		 * // if he's going out and reached an edge, start coming back to the
		 * boat if (goingOut && (whereIAm.getX() < radius - 1 || whereIAm.getX()
		 * > board.board.length - radius + 1 || whereIAm.getY() < radius - 1 ||
		 * whereIAm.getY() > board.board.length - radius + 1)) { goingOut =
		 * false; outDirection = null; return backtrack(false); }
		 * 
		 * // if he's coming in and not at the boat, keep coming in if
		 * (!goingOut && !(whereIAm.getX() == distance && whereIAm.getY() ==
		 * distance)) { return backtrack(false); }
		 * 
		 * if (!goingOut) { return backtrack(false); }
		 * 
		 * if (outDirection == null) { outDirection = randomMove(); return
		 * outDirection; }
		 * 
		 * return outDirection;
		 */

		return makeSpiralMove();
	}

	public Direction runAwayFromDanger(ArrayList<Direction> harmfulDirections,
			Point2D goal) {
		// If you are on the boat, you dont need to run
		// System.err.println("run away from danger");
		
		// if(goal!=null)
		// System.err.println("Going to goal X: "+goal.getX() +" Y:
		// "+goal.getY());
		ArrayList<Direction> safeMoves = getOpposites(harmfulDirections);
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
					System.err.println("Safe Valid Moves are: "+safe.name());
					Point2D newPos=new Point2D.Double(whereIAm.getX()+safe.getDx(),whereIAm.getY()+safe.getDy());
					if(goal!=null)
					{		System.err.println("Distance to goal : "+newPos.distance(goal));
							if(newPos.distance(goal)<min)
							{
								System.err.println("new best dir found");
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
			System.err.println("inside bestworst");
			Collections.shuffle(bestWorstMoves);
			for(Direction d: bestWorstMoves){
				if(board.isValidMove((int)whereIAm.getX(), (int)whereIAm.getY(), d))
				{
					return d;
				}
			}
			return randomMove();
		}
		/* We are screwed, surrounded. Pray to god random move helps you */
		return randomMove();
	}

	private Direction getDirectionToGoal(Point2D start, Point2D goal) {
		double currX = start.getX();
		double currY = start.getY();
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
			boolean areTheDangerousCreaturesMoving = board.isDangerMobile(
					whereIAm, whatISee);
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

	/**
	 * Go to goal when the danger is mobile. Select the best path to goal when
	 * the dangerous creatures are moving around.
	 */
	private Direction gotoGoalWhenDangerIsMobile(Point2D goal,
			ArrayList<Direction> harmfulDirections) {
		ArrayList<Direction> safeMoves = getOpposites(harmfulDirections);
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

	/**
	 * New method to go to a goal without getting hurt. Desperate time tells us
	 * if its imperative that we reach the boat or not. If true, we just go to
	 * the goal and don't avoid danger.
	 */
	public Direction goToGoalWithoutGettingBit(Point2D goal,
			boolean desperateTime) {

		if (!desperateTime && board.areThereDangerousCreatures(whatISee)) {
			return runAwayFromDanger(
					board.getHarmfulDirections(whereIAm, whatISee), goal);
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
			creatureMapping.put(
					Character.toString(ALPHABET.charAt(count % 26)), sc);

			if (myId == 0) {
				System.err.println(count + " " + ALPHABET.charAt(count % 26)
						+ " id: " + sc.getId() + " "
						+ sc.returnCreature().getName());
			}
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

		if (myId == 0 && intermediateGoal != null && changed)
			System.err.println(myId + " rcvd: " + rcvd + " ||| going to "
					+ intermediateGoal.toString());
	}

	public void checkFoundGoal(Set<iSnorkMessage> incomingMessages) {
		if (searchingFor != null && searchingFor.seenOnce) {
			intermediateGoal = null;
			searchingFor = null;
		}
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// /////////// SPIRAL MOVE CALCULATIONS ///////////////////
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	public int numWaves = 0;
	public int waveLength = 0; // distance between snorkelers in dif waves
	public int myStartWave = 0;
	public int myCurWave = 0;
	public Direction curDirection = null;
	public Direction myStartDirection = null;
	public boolean spanningOut = true;
	public boolean changingWave = false;
	public Point2D spiralGoal = null;
	public int numTurns = 0;

	public void setupSpiral() {
		int absID = Math.abs(myId);
		numWaves = (int) Math.ceil(distance / radius) + 1;
		myStartWave = (int) (absID / 4) + 1;
		myCurWave = myStartWave;
		waveLength = radius;

		if (myStartWave % 2 == 1) {
			// diagonal direction
			if (absID % 4 == 0)
				myStartDirection = Direction.NE;
			if (absID % 4 == 1)
				myStartDirection = Direction.SW;
			if (absID % 4 == 2)
				myStartDirection = Direction.NW;
			if (absID % 4 == 3)
				myStartDirection = Direction.SE;
		} else {
			// horizontal or vertical direction
			if (absID % 4 == 0)
				myStartDirection = Direction.S;
			if (absID % 4 == 1)
				myStartDirection = Direction.E;
			if (absID % 4 == 2)
				myStartDirection = Direction.N;
			if (absID % 4 == 3)
				myStartDirection = Direction.W;
		}

		curDirection = myStartDirection;
	}

	public Direction makeSpiralMove() {
		if (spanningOut) {
			spanOut();
			return myStartDirection;
		} else if (!whereIAm.equals(spiralGoal)) {
			return goToGoalWithoutGettingBit(spiralGoal, false);
		} else {
			numTurns++;
			int wallDirection = 0;

			// if snorkeler reached temporary destination (aka a corner), assign
			// the next goal
			if (curDirection == Direction.NE || curDirection == Direction.N) {
				curDirection = Direction.W;
				wallDirection = 4;
			} else if (curDirection == Direction.NW
					|| curDirection == Direction.W) {
				curDirection = Direction.S;
				wallDirection = 3;
			} else if (curDirection == Direction.SW
					|| curDirection == Direction.S) {
				curDirection = Direction.E;
				wallDirection = 2;
			} else if (curDirection == Direction.SE
					|| curDirection == Direction.E) {
				curDirection = Direction.N;
				wallDirection = 1;
			}

			while (distanceFromWall(spiralGoal, wallDirection) >= waveLength
					* myCurWave) {
				double newX = spiralGoal.getX() + curDirection.dx;
				double newY = spiralGoal.getY() + curDirection.dy;
				spiralGoal = new Point2D.Double(newX, newY);
			}

			if (spiralGoal.getX() > distance && spiralGoal.getY() > distance)
				spiralGoal = new Point2D.Double(spiralGoal.getX() - 1,
						spiralGoal.getY());
			else if (spiralGoal.getY() > distance)
				spiralGoal = new Point2D.Double(spiralGoal.getX(),
						spiralGoal.getY() - 1);

			if (numTurns % 4 == 0) {
				myCurWave++;
				Direction boatDir = getDirectionToGoal(spiralGoal, boat);

				if (myCurWave == numWaves) {
					myCurWave = 1;
					// go from inner most wave to outer
					for (int x = 0; x < waveLength; x++) {
						spiralGoal = new Point2D.Double(spiralGoal.getX()
								+ boatDir.dx * -1, spiralGoal.getY()
								+ boatDir.dy * -1);
					}
				} else {
					// go to an inner wave
					for (int x = 0; x < waveLength; x++) {
						spiralGoal = new Point2D.Double(spiralGoal.getX()
								+ boatDir.dx, spiralGoal.getY() + boatDir.dy);
					}
				}
			}

			return curDirection;
		}
	}

	/**
	 * Sets the spiral location when the player is initially spanning out from
	 * the boat
	 */
	public void spanOut() {
		spanningOut = false;
		spiralGoal = new Point2D.Double(boat.getX(), boat.getY());

		while (distanceFromClosestWall(spiralGoal) >= waveLength * myStartWave) {
			double newX = spiralGoal.getX() + myStartDirection.dx;
			double newY = spiralGoal.getY() + myStartDirection.dy;

			spiralGoal = new Point2D.Double(newX, newY);
		}
	}

	public int distanceFromWall(Point2D pt, int wall) {
		if (wall == 1) // northern wall
			return (int) pt.getY() + 1;
		if (wall == 2) // eastern wall
			return distance * 2 + 1 - (int) pt.getX() + 1;
		if (wall == 3) // southern wall
			return distance * 2 + 1 - (int) pt.getY() + 1;
		if (wall == 4) // western wall
			return (int) pt.getX() + 1;

		return 0;
	}

	/**
	 * Determines the distance from a player to a closest wall.
	 */
	public int distanceFromClosestWall(Point2D pt) {
		int shortestDistance = Integer.MAX_VALUE;
		if (pt.getX() + 1 < shortestDistance)
			shortestDistance = (int) pt.getX() + 1;
		if (distance * 2 + 1 - pt.getX() + 1 < shortestDistance)
			shortestDistance = distance * 2 + 1 - (int) pt.getX() + 1;
		if (pt.getY() + 1 < shortestDistance)
			shortestDistance = (int) pt.getY() + 1;
		if (distance * 2 + 1 - pt.getY() + 1 < shortestDistance)
			shortestDistance = distance * 2 + 1 - (int) pt.getY() + 1;

		return shortestDistance;
	}
}
