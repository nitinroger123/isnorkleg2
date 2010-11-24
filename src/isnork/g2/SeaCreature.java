package isnork.g2;

import isnork.sim.SeaLife;
import isnork.sim.SeaLifePrototype;

/**Represents a sea creature!*/
public class SeaCreature {
	
	private int numTimesSeen= 0;
	private SeaLifePrototype seaCreature;
	private int id = 1000;
	private int lastseen = 0;
	
	public SeaCreature(SeaLifePrototype s){
		seaCreature = s;
	} 
	
	public SeaCreature(SeaLifePrototype p, int id2) {
		seaCreature = p;
		this.id = id2;
	}

	public SeaCreature(SeaLifePrototype p, int id2, int r) {
		seaCreature = p;
		this.id = id2;
		lastseen = r;
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
	
	public int getLastseen(){
		return lastseen;
	}
	
	public void setLastSeen(int r) {
		lastseen = r;
	}
	


}
