package isnork.sim;

import isnork.sim.GameObject.Direction;

import java.awt.geom.Point2D;

public class Observation {
	Point2D location;
	int id;
	String name;
	Direction dir;

	public Point2D getLocation() {
		return location;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isDangerous() {
		return danger;
	}

	public int happiness() {
		return happy;
	}

	public Direction getDirection() {
		return dir;
	}

	int happy;
	boolean danger;
}
