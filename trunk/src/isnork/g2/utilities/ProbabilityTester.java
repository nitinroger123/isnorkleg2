package isnork.g2.utilities;

import isnork.sim.SeaLifePrototype;
import isnork.sim.GameObject.Direction;
import isnork.sim.SeaLifePrototype;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**Testing class to test the probability model*/
public class ProbabilityTester {
	
	public static boolean verbose = false;

	public static void main(String[] args) {

		EachSeaCreature ourCreature = new EachSeaCreature(new SeaLifePrototype());
		ourCreature.direction = Direction.N;
		int currRound = 10;
		ArrayList<Direction> allDirections = Direction.allBut(null);

		int distance = 30;
		ProbabilityCell[][] cells = new ProbabilityCell[2 * distance][2 * distance];

		// initialize board
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[0].length; j++) {
				cells[i][j] = new ProbabilityCell(new Point2D.Double(i, j), 0,
						null, 2*distance);
			}
		}
		cells[distance][distance] = new ProbabilityCell(new Point2D.Double(
				distance, distance), 1, Direction.N, 2*distance);
		System.out.println(cells[distance][distance]);
		System.out.println("initial board");
		printProbs(cells);

		for (int k = ourCreature.getLastseen(); k < currRound; k++) {
			System.out.println("round: " + k);
			// Calculate new probabilities
			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[0].length; j++) {

					if(verbose){
					System.out.println("space: " + i + ", " + j);
					System.out.println("probs: " + cells[i][j].probs);}

					for (Direction d : allDirections) {

						if (!(i + d.dx < 0 || i + d.dx > cells.length - 1
								|| j + d.dy < 0 || j + d.dy > cells.length - 1)) {
							
							/*if(i + d.dx == 2 && j +d.dy == 1)
								verbose = true;
							else
								verbose = false;*/

							if(verbose)
							System.out.println("we can safely move to: "
									+ (i + d.dx) + ", " + (j + d.dy)
									+ " in direction: " + d + " from: " + i + ", " + j);

							for (Probability p : cells[i][j].probs) {

								if (p.dir.equals(d)) {
									// System.out.println("Same direction: " +
									// p);
									cells[(i + d.dx)][(j + d.dy)]
											.ammendsameprob(cells[i][j]
													.getProb(p.dir), d, cells[i][j].same);
								} else
									// System.out.println("Diff direction: " +
									// p);
									cells[(i + d.dx)][(j + d.dy)].ammenddif(
											cells[i][j].getProb(p.dir), d, cells[i][j].diff);
							}
						}
					}
				}
			}

			// Update
			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[0].length; j++) {
					cells[i][j].update();
				}
			}

			printProbs(cells);

		}
	}

	public static void printProbs(ProbabilityCell[][] cells) {

		ArrayList<Double> probs = new ArrayList<Double>();

		System.out.println("---------");
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[0].length; j++) {
				cells[i][j].finish();
			}
		}

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[0].length; j++) {
				if (cells[i][j].probability > 0) {
					System.out.println(cells[i][j]);
					probs.add(cells[i][j].probability);
				}
			}
		}
		
		double total = 0;
		for(Double i: probs)
			total += i;
		System.out.println("total: " + total);
		System.out.println("---------");

	}

}
