package isnork.g2;

import java.util.ArrayList;

public class Guidebook {
	
	public Guidebook(){
		creatures = new ArrayList<SeaCreature>();
	}
	
	public void add(SeaCreature s){
		creatures.add(s);
	}
	
	private ArrayList<SeaCreature> creatures;

}
