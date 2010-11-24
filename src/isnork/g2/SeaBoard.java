package isnork.g2;

import isnork.sim.Observation;
import isnork.sim.SeaLifePrototype;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;

import isnork.sim.GameObject.Direction;

/**Represents board*/
public class SeaBoard {
	
	private Logger log = Logger.getLogger(this.getClass());
	private ArrayList<SeaCreature> creatures;
	private Set<SeaLifePrototype> prototypes;
	private SeaSpace[][] board;
	private int radius, distance;
	
	public SeaBoard(int x, int y, int r, Set<SeaLifePrototype> p, int d){
		creatures = new ArrayList<SeaCreature>();
		prototypes = p;
		board = new SeaSpace[x + 1][y + 1];
		for(int i = 0; i < x + 1; i++){
			for(int j = 0; j < y + 1; j++){
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
				board[(int) o.getLocation().getX() + distance ]
				      [(int) o.getLocation().getY() + distance ].addCreature(c, r);
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
	
	public ArrayList<Direction> getDangerousDirections(Point2D me) {
		ArrayList<Direction> d = new ArrayList<Direction>();
		
		for(int i = 0;i < board.length; i++){
			for(int j = 0; j < board[0].length; j++){
				if(insideRadius(me, board[i][j])){
					if(board[i][j].hasDanger(radius)){
						d.addAll(board[i][j].getDirection(me));
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
	
	public boolean isValidMove(int x, int y, Direction d)
	{
		Point2D p = new Point2D.Double(x + d.dx, y + d.dy);
		
		//check if the point is out of bounds
		if(p.getX() < 0 || p.getX() > distance*2-1 || p.getY() < 0 || p.getY() > distance*2-1)
			return false;
		
		return true;
	}	
}
