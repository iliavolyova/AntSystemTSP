package hr.fer.hmo.solution;

public class BestSolution {

	public double tourLength;
	public int cycles;
	public String representation;
	
	public BestSolution(double tourLength, int cycles, String representation) {
		super();
		this.tourLength = tourLength;
		this.cycles = cycles;
		this.representation = representation;
	}
	
	public String toString(){
		return representation;
	}
	
}
