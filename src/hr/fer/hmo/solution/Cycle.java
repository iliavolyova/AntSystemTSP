package hr.fer.hmo.solution;

import java.util.ArrayList;

public class Cycle {
	
	public ArrayList<Vertex> vrhovi;
	public ArrayList<Integer> vertexTimes;
	
	public Cycle(Vertex nulti){
		vrhovi = new ArrayList<Vertex>();
		vertexTimes = new ArrayList<Integer>();
		vrhovi.add(nulti);
		vertexTimes.add(0);
	}
	
	public Vertex getLastVertex(){
		return vrhovi.get(vrhovi.size()-1);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder("");
		for(int i = 0; i < vrhovi.size(); i++){	
			sb.append(vrhovi.get(i).id + "(" + vertexTimes.get(i) + ")" + "->");
		}
		
		sb.replace(sb.length()-2, sb.length(), "");
		sb.append("\n");
		return sb.toString();
	}
}
