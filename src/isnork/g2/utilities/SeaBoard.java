package isnork.g2.utilities;

import isnork.sim.Config;
import isnork.sim.Observation;
import isnork.sim.SeaLifePrototype;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;

import isnork.sim.GameObject.Direction;

/** Represents board */
public class SeaBoard {

	private ArrayList<EachSeaCreature> creatures;
	private Set<SeaLifePrototype> prototypes;
	public SeaSpace[][] board;
	private int radius, distance, maxscore;
	private ArrayList<Point2D> positionOfDangerousCreatures;
	private ArrayList<Direction> lastKnownDirectionOfDangerousCreatures;
	private Point2D boat;
	private boolean areDangerousCreaturesMobile;
	private ArrayList<Observation> dangerousCreatures;
	public SeaBoard(int x, int y, int r, Set<SeaLifePrototype> p, int d,
			Point2D b) {
		creatures = new ArrayList<EachSeaCreature>();
		for (SeaLifePrototype c : p) {

			if (c.getMaxCount() >= 3)
				maxscore += 1.75 * c.getHappiness();

			if (c.getMaxCount() == 2)
				maxscore += 1.5 * c.getHappiness();

			if (c.getMaxCount() == 1)
				maxscore += c.getHappiness();
		}
		prototypes = p;
		board = new SeaSpace[x + 1][y + 1];
		for (int i = 0; i < x + 1; i++) {
			for (int j = 0; j < y + 1; j++) {
				board[i][j] = new SeaSpace(new Point2D.Double(i, j));
			}
		}

		radius = r;
		distance = d;
		boat = b;
	}

	public ArrayList<Point2D> getDangerousPositions() {
		return positionOfDangerousCreatures;
	}

	public void remove(int id) {

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				if (board[i][j].isoccupideby(id)) {
					board[i][j].remove(id);
				}
			}
		}
	}

	public void add(Observation o, int r) {

		Boolean found = false;
		for (EachSeaCreature c : creatures) {
			if (c.getId() == o.getId()) {
				board[(int) o.getLocation().getX() + distance][(int) o
						.getLocation().getY()
						+ distance].addCreature(c, r, new Point2D.Double(
						(int) o.getLocation().getX(), (int) o.getLocation()
								.getY()));
				found = true;
			}
		}

		if (!found) {
			for (SeaLifePrototype p : prototypes) {
				if (p.getName() == o.getName()) {
					creatures.add(new EachSeaCreature(p, o.getId(), r));
				}
			}
		}
	}

	/*
	 * Method which checks if there are dangerous creatures around, and adds
	 * their locations to a list
	 */
	public boolean areThereDangerousCreatures(Set<Observation> whatISee) {
		boolean isThereDanger = false;
		areDangerousCreaturesMobile=false;
		positionOfDangerousCreatures = new ArrayList<Point2D>();
		positionOfDangerousCreatures.clear();
		lastKnownDirectionOfDangerousCreatures=new ArrayList<Direction>();
		dangerousCreatures=new ArrayList<Observation>();
		dangerousCreatures.clear();
		for (Observation creature : whatISee) {
			if (creature.isDangerous()) {
				dangerousCreatures.add(creature);
				if(creature.getDirection()!=null){
					areDangerousCreaturesMobile=true;
					lastKnownDirectionOfDangerousCreatures.add(creature.getDirection());
				}
				positionOfDangerousCreatures.add(creature.getLocation());
				isThereDanger = true;
			}
		}
		return isThereDanger;
	}
	
	public ArrayList<Observation> getDangerousCreaturesInRadius(){
		return dangerousCreatures;
	}

	public ArrayList<Direction> getHarmfulDirections(Point2D myLocation) {
		double myX = myLocation.getX();
		double myY = myLocation.getY();
		ArrayList<Direction> harmfulDirections = new ArrayList<Direction>();

		for (Point2D p : positionOfDangerousCreatures) {
			double dangerX = p.getX() + boat.getX();
			double dangerY = p.getY() + boat.getY();
			if (myX == dangerX && myY > dangerY) {
				harmfulDirections.add(Direction.N);
				harmfulDirections.add(Direction.NE);
				harmfulDirections.add(Direction.NW);
			}

			if (myX == dangerX && myY < dangerY) {
				harmfulDirections.add(Direction.S);
				harmfulDirections.add(Direction.SE);
				harmfulDirections.add(Direction.SW);
			}

			if (myX > dangerX && myY == dangerY) {
				harmfulDirections.add(Direction.W);
				harmfulDirections.add(Direction.NW);
				harmfulDirections.add(Direction.SW);
			}
			if (myX < dangerX && myY == dangerY) {
				harmfulDirections.add(Direction.E);
				harmfulDirections.add(Direction.NE);
				harmfulDirections.add(Direction.SE);
			}
			if (myX < dangerX && myY > dangerY) {
				harmfulDirections.add(Direction.NE);
				harmfulDirections.add(Direction.N);
				harmfulDirections.add(Direction.E);
			}
			if (myX < dangerX && myY < dangerY) {
				harmfulDirections.add(Direction.SE);
				harmfulDirections.add(Direction.S);
				harmfulDirections.add(Direction.E);
			}
			if (myX > dangerX && myY > dangerY) {
				harmfulDirections.add(Direction.NW);
				harmfulDirections.add(Direction.N);
				harmfulDirections.add(Direction.W);
			}
			if (myX > dangerX && myY < dangerY) {
				harmfulDirections.add(Direction.SW);
				harmfulDirections.add(Direction.S);
				harmfulDirections.add(Direction.W);
			}

		}
		return harmfulDirections;
	}

	public boolean isValidMove(int x, int y, Direction d) {
		Point2D p = new Point2D.Double(x + d.dx, y + d.dy);

		// check if the point is out of bounds
		if (p.getX() < 0 || p.getX() > distance * 2 - 1 || p.getY() < 0
				|| p.getY() > distance * 2 - 1)
			return false;

		return true;
	}

	public EachSeaCreature getHighScoringCreatureInRadius() {
		EachSeaCreature high = null;

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				for (EachSeaCreature o : board[i][j].getOccupiedby()) {

					if (high == null) // first creature
						high = o;

					if (o.returnCreature().getHappiness() > high
							.returnCreature().getHappiness())
						high = o;
				}
			}
		}

		return high;
	}

	public int getMaxScore() {
		return maxscore;
	}

	public SeaSpace getSeaSpace(Point2D p) {

		return board[(int) p.getX()][(int) p.getY()];
	}

	public Boolean toGoal(Point2D whereIAm, Direction d, Point2D goal) {
		// in a quadrant
		if (whereIAm.getX() > goal.getX() && whereIAm.getY() > goal.getY()) {
			if (d == Direction.NW)
				return true;
		}

		if (whereIAm.getX() < goal.getX() && whereIAm.getY() < goal.getY()) {
			if (d == Direction.SE)
				return true;
		}

		if (whereIAm.getX() < goal.getX() && whereIAm.getY() > goal.getY()) {
			if (d == Direction.NE)
				return true;
		}

		if (whereIAm.getX() > goal.getX() && whereIAm.getY() < goal.getY()) {
			if (d == Direction.SW)
				return true;
		}

		// on a line
		if (whereIAm.getX() < goal.getX() && whereIAm.getY() == goal.getY()) {
			if (d == Direction.E)
				return true;
		}
		if (whereIAm.getX() == goal.getX() && whereIAm.getY() < goal.getY()) {
			if (d == Direction.S)
				return true;
		}

		if (whereIAm.getX() == goal.getX() && whereIAm.getY() > goal.getY()) {
			if (d == Direction.N)
				return true;
		}
		if (whereIAm.getX() > goal.getX() && whereIAm.getY() == goal.getY()) {
			if (d == Direction.W)
				return true;
		}

		return false;
	}

	public EachSeaCreature getCreature(int id) {

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {

				if (board[i][j].isoccupideby(id))
					return board[i][j].getCreature(id);
			}
		}
		return null;
	}

	/**Get the probability that a creature with an ID is on a particular space*/
	public double getProbOnSpace(int id, int x, int y, int currRound) {

		EachSeaCreature ourCreature = getCreature(id);

		// on space and static creature
		if (board[x][y].isoccupideby(id)
				&& board[x][y].getCreature(id).returnCreature().getSpeed() == 0)
			return 1;

		// not on space and static creature
		if (!board[x][y].isoccupideby(id)
				&& board[x][y].getCreature(id).returnCreature().getSpeed() == 0)
			return 0;

		// too far away to possibly be there
		Point2D goingTo = new Point2D.Double(x, y);
		if (goingTo.distance(ourCreature.location) > (currRound - ourCreature
				.getLastseen()) / 2)
			return 0;

		// Otherwise return a probability
		return getProbOnSpace(x, y, currRound, ourCreature);
	}

	/** Internal method to calculate prob when it is not 1 or 0 */
	private double getProbOnSpace(int x, int y, int currRound,
			EachSeaCreature ourCreature) {

		ArrayList<Direction> allDirections = Direction.allBut(null);
		ProbabilityCell[][] cells = new ProbabilityCell[2 * distance][2 * distance];

		// initialize board
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[0].length; j++) {
				cells[i][j] = new ProbabilityCell(new Point2D.Double(i, j), 0,
						null, 2 * distance);
			}
		}
		cells[distance][distance] = new ProbabilityCell(new Point2D.Double(
				distance, distance), 1, Direction.N, 2 * distance);

		for (int k = ourCreature.getLastseen(); k < currRound; k++) {
			// Calculate new probabilities
			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[0].length; j++) {

					for (Direction d : allDirections) {

						if (!(i + d.dx < 0 || i + d.dx > cells.length - 1
								|| j + d.dy < 0 || j + d.dy > cells.length - 1)) {

							for (Probability p : cells[i][j].probs) {

								if (p.dir.equals(d)) {

									cells[(i + d.dx)][(j + d.dy)]
											.ammendsameprob(cells[i][j]
													.getProb(p.dir), d,
													cells[i][j].same);
								} else

									cells[(i + d.dx)][(j + d.dy)].ammenddif(
											cells[i][j].getProb(p.dir), d,
											cells[i][j].diff);
							}
						}
					}
				}
			}

			// Update
			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[0].length; j++) {
					cells[i][j].update();
				}
			}
		}

		return cells[x][y].probability;
	}

	/**Determines whether or not we have seen an animal of a particular id*/
	public boolean alreadySeen(int id){
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[0].length; j++){
				if(board[i][j].isoccupideby(id))
					return true;
			}
		}
		
		return false;
	}


	public ArrayList<Direction> getLastDirectionOfHarmfulCreatures() {
		return lastKnownDirectionOfDangerousCreatures;
	}

	public boolean isDangerMobile(Point2D whereIAm) {
		return areDangerousCreaturesMobile;
	}

	
	/**Determines whether or not we have seen the creature of type on that space using a probability model*/
	public boolean seenCreatureOnSpace(SeaCreatureType c, Point2D space, int currRound){
		
		double totalprob = 0;
		for(Integer id: c.seen){
			totalprob += getProbOnSpace(id, (int) space.getX(), (int) space.getY(), currRound);
		}
		
		if(totalprob > .1) //this is a constant that we can change in testing
			return true;
		
		return false;
	}
}
