package isnork.g2;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Set;

import isnork.sim.GameConfig;
import isnork.sim.Observation;
import isnork.sim.Player;
import isnork.sim.SeaLife;
import isnork.sim.SeaLifePrototype;
import isnork.sim.iSnorkMessage;
import isnork.sim.GameObject.Direction;

public class JustKeepSwimming extends Player {

	private Direction direction;
	private Guidebook guidebook;
	private Point2D whereIAm = null;
	private int n = -1;
	private int radius, distance, penalty, numrounds;

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
		whereIAm = myPosition;
		if (n % 10 == 0)
			return "s";
		else
			return null;
	}

	@Override
	public Direction getMove() {
		return dumbMove();
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

	/** Initialize our variables when a new game is created */
	@Override
	public void newGame(Set<SeaLifePrototype> seaLifePossibilites, int p,
			int d, int r, int n) {

		guidebook = new Guidebook();
		for (SeaLifePrototype s : seaLifePossibilites) {
			guidebook.add(new SeaCreature(s));
		}

		penalty = p;
		distance = d;
		radius = r;
		numrounds = n;
	}

}
