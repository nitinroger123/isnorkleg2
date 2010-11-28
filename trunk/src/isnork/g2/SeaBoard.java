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
	
	private ArrayList<SeaCreature> creatures;
	private Set<SeaLifePrototype> prototypes;
	public SeaSpace[][] board;
	private int radius, distance, maxscore;
	private ArrayList<Point2D> positionOfDangerousCreatures;
	private Point2D boat;
	
	public SeaBoard(int x, int y, int r, Set<SeaLifePrototype> p, int d, Point2D b){
		creatures = new ArrayList<SeaCreature>();
		for(SeaLifePrototype c: p){
			
			if(c.getMaxCount() >= 3)
				maxscore += 1.75 * c.getHappiness();
			
			if(c.getMaxCount() == 2)
				maxscore += 1.5*c.getHappiness();
				
			if(c.getMaxCount() == 1)
				maxscore += c.getHappiness();
		}
		prototypes = p;
		board = new SeaSpace[x + 1][y + 1];
		for(int i = 0; i < x + 1; i++){
			for(int j = 0; j < y + 1; j++){
				board[i][j] = new SeaSpace(new Point2D.Double(i, j));
			}
		}
				
		radius = r;
		distance = d;
		boat = b;
	}
	
	public ArrayList<Point2D> getDangerousPositions(){
		return positionOfDangerousCreatures;
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
	
	/*
	 * Method which checks of there are dangerous creatures around, and adds 
	 * their locations to a list
	 * */
	public boolean areThereDangerousCreatures(Set<Observation> whatISee){
		boolean isThereDanger=false;
		positionOfDangerousCreatures=new ArrayList<Point2D>();
		positionOfDangerousCreatures.clear();
		for(Observation creature: whatISee){
			if(creature.isDangerous()){
				positionOfDangerousCreatures.add(creature.getLocation());
				isThereDanger= true;
			}
		}
		return isThereDanger;
	}
	
	public ArrayList<Direction> getHarmfulDirections(Point2D myLocation){
		double myX=myLocation.getX();
		double myY=myLocation.getY();
		ArrayList<Direction> harmfulDirections=new ArrayList<Direction>();
		for(Point2D p: positionOfDangerousCreatures){
			double dangerX=p.getX()+boat.getX();
			double dangerY=p.getY()+boat.getY();
			if (myX == dangerX && myY > dangerY) {
				harmfulDirections.add(Direction.N);
				harmfulDirections.add(Direction.NE);
				harmfulDirections.add(Direction.NW);
			}

			if (myX == dangerX && myY < dangerY) {
				harmfulDirections.add(Direction.S);
				harmfulDirections.add(Direction.SE);
				harmfulDirections.add(Direction.SW);
			}

			if (myX > dangerX && myY == dangerY) {
				harmfulDirections.add(Direction.W);
				harmfulDirections.add(Direction.NW);
				harmfulDirections.add(Direction.SW);
			}
			if (myX < dangerX && myY == dangerY) {
				harmfulDirections.add(Direction.E);
				harmfulDirections.add(Direction.NE);
				harmfulDirections.add(Direction.SE);
			}
			if (myX < dangerX && myY > dangerY) {
				harmfulDirections.add(Direction.NE);
				harmfulDirections.add(Direction.N);
				harmfulDirections.add(Direction.E);
			}
			if (myX < dangerX && myY < dangerY) {
				harmfulDirections.add(Direction.SE);
				harmfulDirections.add(Direction.S);
				harmfulDirections.add(Direction.E);
			}
			if (myX > dangerX && myY > dangerY) {
				harmfulDirections.add(Direction.NW);
				harmfulDirections.add(Direction.N);
				harmfulDirections.add(Direction.W);
			}
			if (myX > dangerX && myY < dangerY) {
				harmfulDirections.add(Direction.SW);
				harmfulDirections.add(Direction.S);
				harmfulDirections.add(Direction.W);
			}
			
		}
		return harmfulDirections;
	}

	 public boolean isValidMove(int x, int y, Direction d)
     {
             Point2D p = new Point2D.Double(x + d.dx, y + d.dy);
             
             //check if the point is out of bounds
             if(p.getX() < 0 || p.getX() > distance*2-1 || p.getY() < 0 || p.getY() > distance*2-1)
                     return false;
             
             return true;
     }       

	 public SeaCreature getHighScoringCreatureInRadius(){
		 SeaCreature high = null;
		 
		 for(int i = 0;i < board.length; i++){
			 for(int j = 0; j < board[0].length; j++){
				 for( SeaCreature o: board[i][j].getOccupiedby()){
					 
					 if(high == null) //first creature
						 high = o;
					 
					 if(o.returnCreature().getHappiness() > high.returnCreature().getHappiness())
						 high = o;
				 }
			 }
		 }
		 
		 return high;
	 }

	public int getMaxScore() {
		return maxscore;
	}
	
	public SeaSpace getSeaSpace(Point2D p){
		
		return board[(int) p.getX()][(int) p.getY()];
	}

	public Boolean toBoat(Point2D whereIAm, Direction d) {
		
		Point2D going = new Point2D.Double(whereIAm.getX() + d.getDx(), whereIAm.getY() + d.getDy());
		if(whereIAm.distance(boat) > going.distance(boat))
			return true;
		return false;
		
	}
	 
		
}
