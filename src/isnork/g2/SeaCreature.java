package isnork.g2;

import isnork.sim.SeaLifePrototype;

/**Represents a sea creature*/
public class SeaCreature {
	
	public SeaCreature(SeaLifePrototype s){
		seaCreature = s;
	} 
	
	public int getNumTimesSeen() {
		return numTimesSeen;
	}

	public void setNumTimesSeen(int numTimesSeen) {
		this.numTimesSeen = numTimesSeen;
	}
	
	private int numTimesSeen= 0;
	private SeaLifePrototype seaCreature;

}
