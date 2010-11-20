package isnork.g2;

import isnork.sim.Observation;
import isnork.sim.SeaLifePrototype;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Set;

import isnork.sim.GameObject.Direction;

public class SeaBoard {
	
	public SeaBoard(int x, int y, int r, Set<SeaLifePrototype> p, int d){
		creatures = new ArrayList<SeaCreature>();
		prototypes = p;
		board = new SeaSpace[x][y];
		for(int i = 0; i < x; i++){
			for(int j = 0; j < y; j++){
				board[i][j] = new SeaSpace(new Point2D.Double(i, j));
			}
		}
				
		radius = r;
		distance = d;
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
	
	public void add(Observation o, int r) {
				
		Boolean found = false;
		for(SeaCreature c: creatures){
			if(c.getId() == o.getId()){
				board[(int) o.getLocation().getX() + distance]
				      [(int) o.getLocation().getY() + distance].addCreature(c, r);
				found = true;
			}
		}
		
		if(!found){
			for(SeaLifePrototype p: prototypes){
				if(p.getName() == o.getName()){
					creatures.add(new SeaCreature(p, o.getId(), r));
				}
			}
		}
	}
	
	/**Determines if there is a dangerous animal within the radius*/
	public boolean getDangerInRadius(Point2D me, int r){
		
		for(int i = 0;i < board.length; i++){
			for(int j = 0; j < board[0].length; j++){
				if(insideRadius(me, board[i][j])){
					if(board[i][j].hasDanger(r)){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public ArrayList<Direction> getDangerousDirections(Point2D me, int r) {
		ArrayList<Direction> d = new ArrayList<Direction>();
		
		for(int i = 0;i < board.length; i++){
			for(int j = 0; j < board[0].length; j++){
				if(insideRadius(me, board[i][j])){
					if(board[i][j].hasDanger(r)){
						d.add(board[i][j].getDirection(me));
					}
				}
			}
		}
		
		return d;
	}
	
	public boolean insideRadius(Point2D me, SeaSpace s){
		
		if(s.getCenter().distance(new Point2D.Double(me.getX() + .5, me.getY() + .5)) <= radius)
			return true;
		
		else
			return false;
	}
	
	private ArrayList<SeaCreature> creatures;
	private Set<SeaLifePrototype> prototypes;
	private SeaSpace[][] board;
	private int radius, distance;
	
}
