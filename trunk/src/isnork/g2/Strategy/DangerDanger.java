package isnork.g2.Strategy;

import isnork.g2.utilities.SeaBoard;
import isnork.sim.Observation;
import isnork.sim.SeaLifePrototype;
import isnork.sim.iSnorkMessage;
import isnork.sim.GameObject.Direction;

import java.util.Random;
import java.util.Set;

public class DangerDanger extends Strategy{

	public DangerDanger(int p, int d, int r,
			Set<SeaLifePrototype> seaLifePossibilities, Random rand, int id,
			int numDivers, SeaBoard b) {
		super(p, d, r, seaLifePossibilities, rand, id, numDivers, b);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void checkFoundGoal(Set<iSnorkMessage> incomingMessages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Direction getMove() {
		// TODO Auto-generated method stub
		return null;
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
