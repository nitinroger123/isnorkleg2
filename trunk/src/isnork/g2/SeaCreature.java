package isnork.g2;

import isnork.sim.SeaLife;
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
	
	public SeaLifePrototype returnCreture(){
		return seaCreature;
	}
	
	public void setId(int i){
		id = i;
	}
	
	public int getId(){
		return id;
	}
	
	private int numTimesSeen= 0;
	private SeaLifePrototype seaCreature;
	private int id = 1000;

}
