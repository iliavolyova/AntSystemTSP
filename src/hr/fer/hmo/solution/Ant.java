package hr.fer.hmo.solution;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Mrav je prikaz rješenja AntColonySystem problema
 */
public class Ant {
	
	public ArrayList<Cycle> vozila;
	
	public int currentCapacity;
	public int currentTime;
	
	public boolean[] unvisited;
	
	/** Ukupna duljina ture */
	public double tourLength;
	
	public boolean isCurrentCycleClosed;
	

	public Ant(int numcities, Vertex start) {
		unvisited = new boolean[numcities];
		Arrays.fill(unvisited, true);
		vozila = new ArrayList<Cycle>();
		vozila.add(new Cycle(start));
		unvisited[0] = false;
	}
	
	public Cycle getCycle(){
		return vozila.get(vozila.size()-1);
	}
	
	public void newCycle(Vertex start){
		vozila.add(new Cycle(start));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(vozila.size()+"\n\n");
		int i = 1;
		for (Cycle c : vozila){
			sb.append(i++ + ": " + c.toString() + "\n");
		}
		return sb.toString();
	}
}
