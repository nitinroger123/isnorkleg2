package isnork.g2;

import java.util.ArrayList;

/** Class to represent a space on the board */
public class SeaSpace {

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void set(ArrayList<SeaCreature> occupiedby, int r) {
		roundset = r;
		this.occupiedby = occupiedby;

	}

	public ArrayList<SeaCreature> getOccupiedby() {
		return occupiedby;
	}

	public boolean isoccupideby(int id) {

		for (SeaCreature s : occupiedby) {
			if (s.getId() == id)
				return true;
		}

		return false;
	}

	public void remove(int id) {

		for (SeaCreature s : occupiedby) {
			if (s.getId() == id)
				occupiedby.remove(s);
		}
	}

	public void addCreature(SeaCreature c) {
		occupiedby.add(c);

	}

	private ArrayList<SeaCreature> occupiedby;
	private int x, y, roundset;
}
