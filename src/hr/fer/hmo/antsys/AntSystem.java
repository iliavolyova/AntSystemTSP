package hr.fer.hmo.antsys;

import hr.fer.hmo.solution.BestSolution;
import hr.fer.hmo.solution.Vertex;
import hr.fer.hmo.solution.Ant;
import hr.fer.hmo.solution.ProblemUtil;
import hr.fer.hmo.util.ArraysUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AntSystem {

	/** Polje gradova poredano po identifikatorima*/
	private Vertex[] vertices;
	
	// Generator slučajnih brojeva
	private Random rand;

	/** Polje indeksa radova (uvijek oblika 0, 1, 2, 3, ...). */
	private int[] indexes;

	/** Feromonski tragovi - simetrična matrica */
	private double[][] trails;

	/** Udaljenosti između gradova - simetrična matrica */
	private double[][] distances;

	/** Heurističke vrijednosti */
	private double[][] heuristics;
	
	/** Populacija mrava koji rješavaju problem */
	private Ant[] ants;

	/** Pomoćno polje vjerojatnosti odabira grada */
	private double[] probabilities;

	/** Konstanta isparavanja */
	private double ro;
	
	/** Konstanta alfa - važnost feromonskog traga*/
	private double alpha;

	/** Konstanta beta - važnost heuristike vidljivosti */
	private double beta;
	
	/**Konstanta gama - važnost heuristike uštede */
	private double lambda;
	
	/**Konstanta theta - važnost heuristike čekanja */
	private double theta;
	
	/**Konstante koje koristi heuristika uštede */
	private double g,f;
	
	/**Konstanta simuliranog kaljenja */
	private double temperature;
	
	/** Konstanta brzine kaljenja */
	private double phi;
	
	private double initialPheromoneStrength;

	/** Pomoćno rješenje koje pamti najbolju pronađenu turu - ikada. */
	private BestSolution best;
	private boolean haveBest = false;
	
	
	/**kapacitet jednog vozila */
	private int maxCapacity;
	
	public AntSystem(List<Vertex> cities, int[] restrictions) {
		this.vertices = new Vertex[cities.size()];
		cities.toArray(this.vertices);
		
		rand = new Random();
		indexes = new int[this.vertices.length];
		ArraysUtil.linearFillArray(indexes);
		probabilities = new double[this.vertices.length];
		distances = new double[this.vertices.length][this.vertices.length];
		heuristics = new double[this.vertices.length][this.vertices.length];
		trails = new double[this.vertices.length][this.vertices.length];
		
		maxCapacity = restrictions[1];
		double initTrail = initialPheromoneStrength = 1.0/1000000.0;
		int m = 100;
		alpha = 2;
		beta = 1;
		lambda = 1;
		theta = .75;
		ro = 0.15;
		temperature = 100;
		phi = 0.75;
		f = g = 2;
		
		for(int i = 0; i < this.vertices.length; i++) {
			Vertex a = this.vertices[i];
			distances[i][i] = 0;
			trails[i][i] = initTrail;
			
			for(int j = i+1; j < this.vertices.length; j++) {
				Vertex b = this.vertices[j];
				double dist = Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
				distances[i][j] = dist;
				distances[j][i] = dist;
				trails[i][j] = initTrail;
				trails[j][i] = initTrail;
				
				heuristics[i][j] = 1.0/dist;
				heuristics[j][i] = heuristics[i][j];
				
			}
			
		}
		
		ants = new Ant[m];
		for(int i = 0; i < ants.length; i++) {
			ants[i] = new Ant(this.vertices.length, vertices[0]);
		}
		
		best = new BestSolution(0, 0, "dummy");
	}

	/**
	 * Glavna metoda algoritma.
	 */
	public void go() {
		int iter = 0;
		int iterLimit = 500;
		
		while(iter < iterLimit) {
			iter++;
			for(int antIndex = 0; antIndex < ants.length; antIndex++) {
				Ant ant = ants[antIndex];
				
				//hodaj
				while(!isAllVisited(ant)) {
					// nalazimo ciklus koji predstavlja turu jednog vozila
					nadjiCiklus(ant);
					
					//oznacimo zatvaranje ciklusa i resetiramo kapacitet mrava
					ant.isCurrentCycleClosed = true;
					ant.currentCapacity = 0;
					ant.currentTime = 0;
				}
				
				ProblemUtil.evaluate(ant, distances);
			}
			
			updateTrails();
			evaporateTrails();
			checkBestSolution();
			updateTemperature();
		}
		
	}

	private boolean isAllVisited(Ant ant) {
		for (int i=1; i<vertices.length; i++)
			if(ant.unvisited[i] == true)
				return false;
		return true;
	}

	public void nadjiCiklus(Ant ant) {
		
		while(true) {
			
			if (isAllVisited(ant)){
				closeCurrentCycle(ant);
				return;
			}
			
			//otvori novi ciklus ako je prošli upravo zatvoren i ima li još neposjećenih vrhova na grafu
			if (ant.isCurrentCycleClosed){
				//prvo stvarno zatvorimo stari ciklus dodavanjem zadnjeg čvora i vremena do njega
				closeCurrentCycle(ant);
				
				//otvaramo novi ciklus
				ant.newCycle(vertices[0]);
				ant.isCurrentCycleClosed = false;
				ant.currentTime = 0;
				ant.currentCapacity = 0;
			}
			
			int previousCityIndex = ant.getCycle().getLastVertex().id;
			if (ant.isCurrentCycleClosed)
				previousCityIndex = 0;
			
			ArrayList<Integer> reachable = getReachableVertices(ant, previousCityIndex);
			
			double probSum = 0.0;
			for(int candidate = 0; candidate < reachable.size(); candidate++) {
				int cityIndex = reachable.get(candidate);
				
				double gamma = heuristikaUstede(previousCityIndex, cityIndex);
				double omega = heuristikaCekanja(previousCityIndex, cityIndex, ant);
				
				probabilities[cityIndex] = 
					Math.pow(trails[previousCityIndex][cityIndex],alpha) * 
					Math.pow(heuristics[previousCityIndex][cityIndex], beta)*
					Math.pow(gamma, lambda) *
					Math.pow(omega, theta);
				
				probSum += probabilities[cityIndex];
			}
			
			// Normalizacija vjerojatnosti:
			for(int candidate = 0; candidate < reachable.size(); candidate++) {
				int cityIndex = reachable.get(candidate);
				probabilities[cityIndex] = probabilities[cityIndex] / probSum;
			}
			
			// Odluka kuda dalje?
			double number = rand.nextDouble();
			int selectedCandidate = -1;
			
			//eksploatacija ili eksploracija
			if (number < 0.85){
				double maxprob = -1;
				for (int i = 0; i < reachable.size(); i++){
					int index = reachable.get(i);
					if (probabilities[index ] > maxprob){
						maxprob = probabilities[index ];
						selectedCandidate = index;
					}
				}
			}else {
				probSum = 0.0;
				number = rand.nextDouble();
				for(int candidate = 0; candidate < reachable.size(); candidate++) {
					int cityIndex = reachable.get(candidate);
					probSum += probabilities[cityIndex];
					
					if(number <= probSum) { 
						selectedCandidate = cityIndex;
						break;
					}
				}
			}
			//nismo uspijeli prijeći u ni jedan grad od slobodnih - kapacitet ispunjen, završavamo turu
			if(selectedCandidate==-1) {
				return;
			}
			
			//ažuriraj posjetu vrha
			ant.getCycle().vrhovi.add(vertices[selectedCandidate]);
			ant.currentCapacity += vertices[selectedCandidate].capacity;
			ant.unvisited[selectedCandidate] = false;
			
			//ažuriraj vrijeme provedeno u ciklusu:
			ant.currentTime  = Math.max(vertices[selectedCandidate].startTime, 
					(int)Math.ceil(ant.currentTime + distances[previousCityIndex][selectedCandidate] + vertices[previousCityIndex].serviceTime));
			ant.getCycle().vertexTimes.add(ant.currentTime);
			
			//lokalno pravilo ažuriranja feromona
			updateLocalPheromones(previousCityIndex, selectedCandidate);
		}
	}

	private void updateLocalPheromones(int here, int there) {
		trails[here][there] = (1-ro)*trails[here][there] + ro*initialPheromoneStrength;
		trails[there][here] = trails[here][there];
	}

	public ArrayList<Integer> getReachableVertices(Ant ant, int previousCityIndex) {
		ArrayList<Integer> reachable = new ArrayList<Integer>();
		for (int i = 0; i < vertices.length; i++){
			if (ant.unvisited[i] && ant.currentCapacity + vertices[i].capacity <= maxCapacity && !isOutOfTimeBounds(ant, i)){
				reachable.add(i);
			}
		}
		return reachable;
	}

	private boolean isOutOfTimeBounds(Ant ant, int selectedCandidate) {
		int prev = ant.getCycle().getLastVertex().id;
		int possibleTime = Math.max(vertices[selectedCandidate].startTime, 
				(int)Math.ceil(ant.currentTime + distances[prev][selectedCandidate] + vertices[prev].serviceTime));
		
		return possibleTime > vertices[selectedCandidate].endTime;
	}

	public void closeCurrentCycle(Ant ant) {
		double newTime = ant.getCycle().vertexTimes.get(ant.getCycle().vertexTimes.size()-1) +
				distances[ant.getCycle().getLastVertex().id][0] + vertices[ant.getCycle().getLastVertex().id].serviceTime;
		ant.currentTime = (int)Math.ceil(newTime);
		ant.getCycle().vertexTimes.add(ant.currentTime);
		ant.getCycle().vrhovi.add(vertices[0]);
	}
	
	private double heuristikaCekanja(int previousCityIndex, int cityIndex, Ant ant) {
		
		double wOdrediste;
		double earliestTime = vertices[cityIndex].startTime;
		double actualArrivalTime = ant.currentTime + distances[previousCityIndex][cityIndex] + vertices[previousCityIndex].serviceTime;
		
		if (earliestTime - actualArrivalTime > 0)
			wOdrediste = earliestTime - actualArrivalTime;
		else
			wOdrediste = 1;
		
		return 1/wOdrediste;
	}
	
	private double heuristikaUstede(int here, int there){	
		double rez = distances[here][0] + distances[0][there] 
				- g * distances[here][there] 
				+ f * Math.abs(distances[here][0] - distances[0][there]);
		return Math.abs(rez);
	}

	/**
	 * Metoda koja obavlja ažuriranje feromonskih tragova 
	 */
	private void updateTrails() {
		
		//odabir mrava koji mogu osvježavati tragove simuliranim kaljenjem
		List<Integer> antUpdaters = new ArrayList<Integer>();
		double randnum = rand.nextDouble();
		
		for (int i = 0; i < ants.length; i++){
			double solutionQuality = ants[i].tourLength - best.tourLength;
			if (solutionQuality < 0 || Math.exp(-solutionQuality/temperature) > randnum)
				antUpdaters.add(i);
		}
		
		// Ažuriranje feromonskog traga:
		for(Integer antIndex : antUpdaters) {
			// S kojim mravom radim?
			Ant ant = ants[antIndex];
			double delta = 1.0 / ant.tourLength;
			for(int i = 0; i < ant.getCycle().vrhovi.size()-1; i++){
				int a = ant.getCycle().vrhovi.get(i).id;
				int b = ant.getCycle().vrhovi.get(i+1).id;
				trails[a][b] = (1-ro)*trails[a][b] + ro*delta;
				trails[b][a] = trails[a][b];
			}
		}
	}

	/**
	 * Metoda koja obavlja isparavanje feromonskih tragova. 
	 */
	private void evaporateTrails() {
		for(int i = 0; i < this.vertices.length; i++) {
			for(int j = i+1; j < this.vertices.length; j++) {
				trails[i][j] = trails[i][j]*(1-ro);
				trails[j][i] = trails[i][j];
			}
		}
	}
	
	private void updateTemperature(){
		temperature *= phi;
	}

	/**
	 * Metoda provjerava je li pronađeno bolje rješenje od
	 * prethodno najboljeg.
	 */
	private void checkBestSolution() {
		if(!haveBest) {
			haveBest = true;
			Ant ant = ants[0];
			best = new BestSolution(ant.tourLength, ant.vozila.size(), ant.toString());
		}
		double currentBest = best.tourLength;
		int bestIndex = -1;
		for(int antIndex = 0; antIndex < ants.length; antIndex++) {
			Ant ant = ants[antIndex];
			if(ant.vozila.size() <= best.cycles) {
				if (ant.vozila.size() == best.cycles && ant.tourLength > currentBest)
					return;
				currentBest = ant.tourLength;
				bestIndex = antIndex;
			}
		}
		if(bestIndex!=-1) {
			Ant ant = ants[bestIndex];
			best = new BestSolution(ant.tourLength, ant.vozila.size(), ant.toString());
		}
	}

	/**
	 * Ulazna točka u program.
	 * 
	 * @param args argumenti komandne linije
	 */
	public static void main(String[] args) throws IOException {
		String fileName = args.length<1 ? 
				"data/ulaz2.txt"
				: args[0];
		List<Vertex> cities = ProblemUtil.loadVertices(fileName);
		int[] restrictions = ProblemUtil.loadRestrictions(fileName);
		
		BestSolution globalbest = new BestSolution(252, 10000, "dummy");
		
		for (int i = 0; i < 2; i++) {
			AntSystem as = new AntSystem(cities, restrictions);
			as.go();
			BestSolution newBest = as.best;
			
			if (newBest.cycles <= globalbest.cycles){
				if (newBest.cycles == globalbest.cycles && newBest.tourLength > globalbest.tourLength){
					continue;
				}else {
					globalbest = newBest;
				}
			}
			
//			if(i%100 == 0) System.out.println(i);
		}
		
		System.out.print(globalbest);
		System.out.format(Locale.FRANCE, "%f", globalbest.tourLength);
	}
}