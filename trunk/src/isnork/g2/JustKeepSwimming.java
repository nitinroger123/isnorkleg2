package isnork.g2;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Set;
import org.apache.log4j.Logger;

import isnork.g2.Strategy.DangerDanger;
import isnork.g2.Strategy.GeneralStrategy;
import isnork.g2.Strategy.Strategy;
import isnork.g2.utilities.SeaBoard;
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

	private Strategy strategy;

	@Override
	public String getName() {
		return "JustKeepSwimming";
	}
	
	@Override
	public String tick(Point2D myPosition, Set<Observation> whatYouSee,
			Set<iSnorkMessage> incomingMessages,
			Set<Observation> playerLocations) {
				
		strategy.update(myPosition, whatYouSee, incomingMessages, playerLocations);
		
		//return message to isnork
		String mes = strategy.getTick();
		return (String)mes;
	}

	@Override
	public Direction getMove() {
				
		return strategy.getMove();
	}

	/** Initialize our variables when a new game is created */
	@Override
	public void newGame(Set<SeaLifePrototype> seaLifePossibilites, int p,
			int d, int r, int n) {
				
		//initialize strategy
		SeaBoard board = new SeaBoard(
				2*d, 2*d, r, seaLifePossibilites, d, new Point2D.Double(d, d), this.getId());
		
		this.strategy = new GeneralStrategy(p, d, r, seaLifePossibilites, random, this.getId(), n, board);
		if(board.dangerdanger())
			this.strategy = new DangerDanger(p, d, r, seaLifePossibilites, random, this.getId(), n, board);
		
	}

}
