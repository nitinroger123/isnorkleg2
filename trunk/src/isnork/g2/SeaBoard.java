package isnork.g2;

import isnork.sim.Observation;

import java.util.ArrayList;

public class SeaBoard {
	
	public SeaBoard(int x, int y){
		creatures = new ArrayList<SeaCreature>();
		board = new SeaSpace[x][y];
	}
	
	public void add(SeaCreature s){
		creatures.add(s);
	}
	
	public void remove(int id){
		
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board.length; j++){
				if(board[i][j].isoccupideby(id)){
					board[i][j].remove(id);
				}
			}
		}
	}
	
	public void add(Observation o) {
		
		SeaCreature temp;
		
		for(SeaCreature c: creatures){
			if(c.getId() == o.getId()){
				board[(int) o.getLocation().getX()][(int) o.getLocation().getY()].addCreature(c);
			}
		}
		
	}
	
	private ArrayList<SeaCreature> creatures;
	private SeaSpace[][] board;
}
