package isnork.g2.Strategy;

import isnork.g2.utilities.SeaBoard;
import isnork.sim.Observation;
import isnork.sim.SeaLifePrototype;
import isnork.sim.iSnorkMessage;
import isnork.sim.GameObject.Direction;

import java.util.Random;
import java.util.Set;

public class DangerDanger extends Strategy{
	
	private GeneralStrategy general;

	public DangerDanger(int p, int d, int r,
			Set<SeaLifePrototype> seaLifePossibilities, Random rand, int id,
			int numDivers, SeaBoard b) {
		super(p, d, r, seaLifePossibilities, rand, id, numDivers, b);
		
		general = new GeneralStrategy(p, d, r, seaLifePossibilities, rand, id, numDivers, b);

		System.err.println("DANGER DANGER");
	}

	@Override
	public void checkFoundGoal(Set<iSnorkMessage> incomingMessages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Direction getMove() {
		/**
		 * ON BOAT, DANGEROUS CREATURES RIGHT BELOW US
		 */
		if (whereIAm.equals(boat) && board.getSeaSpace(boat).hasDanger()) {
			System.err.println("Staying put because there is danger under the boat");
			return null;
		}
		
		if (whereIAm.equals(boat) && board.areThereDangerousCreatures(whatISee)) {
			System.err.println("Staying put because there is danger near the boat");
			return null;
		}
		
		if(this.myHappiness == board.getMaxScore()){
			System.err.println("have max score of: " + this.myHappiness + ", going home");
			Direction d =  general.backtrack(true);
			System.err.println("backtracking in: " + d);
			return d;
		}
		
		else //we have not reached maximum happiness and it is safe to move
		{
			System.err.println("its safe, lets find a move!");
			return general.getMove();
			//return general.randomMove();
		}
		
	}

	@Override
	public String getTick(Set<Observation> whatYouSee) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rateCreatures(Set<SeaLifePrototype> seaLifePossibilities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateIncomingMessages(Set<iSnorkMessage> incomingMessages) {
		// TODO Auto-generated method stub
		
	}

}
