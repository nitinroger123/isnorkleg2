package isnork.g2;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Set;
import org.apache.log4j.Logger;
import isnork.g2.Strategy.GeneralStrategy;
import isnork.g2.Strategy.Strategy;
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

	private SeaBoard board;
	private Logger log;
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
		return null;
	}

	@Override
	public Direction getMove() {
				
		return strategy.getMove();
	}

	/** Initialize our variables when a new game is created */
	@Override
	public void newGame(Set<SeaLifePrototype> seaLifePossibilites, int p,
			int d, int r, int n) {
		
		//Initialize game variables
		log = Logger.getLogger(this.getClass());
		
		//initialize strategy
		this.strategy = new GeneralStrategy(p, d, r, seaLifePossibilites, random);
		
		/*Pre processing that we should do:
		 * Is dangerous -> Strategy that basically just leaves you off the board
		 */

	}

}
