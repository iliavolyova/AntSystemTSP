package hr.fer.hmo.solution;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Pomoćni razred koji sadrži metode vezane uz TSP.
 * 
*/
public class ProblemUtil {

	/**
	 * Metoda učitava listu gradova iz datoteke. Datoteka je tekstualna.
	 * U svakom retku nalazi se x i y koordinata grada razdvojene znakom
	 * tab.
	 * 
	 * @param fileName naziv datoteke
	 * @return listu gradova ili null ako dođe do pogreške
	 * @throws IOException ako se dogodi pogreška u radu s datotekom
	 */
	public static List<Vertex> loadCities(String fileName) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
			List<Vertex> cities = new ArrayList<Vertex>();
			while(true) {
				String line = br.readLine();
				if(line==null) break;
				line = line.trim();
				if(line.isEmpty()) continue;
				String[] elems = line.split("\\t");
				cities.add(new Vertex(Integer.parseInt(elems[0]),Integer.parseInt(elems[1])));
			}
			br.close();
			return cities;
		} catch(IOException ex) {
			System.out.println("Pogreška prilikom rada s datotekom "+fileName);
			if(br!=null) try { br.close(); } catch(Exception ignorable) {}
			return null;
		}
	}
	
	public static List<Vertex> loadVertices(String filename){
		List<Vertex> vrhovi = new ArrayList<Vertex>();
		
		try {
			Scanner scan = new Scanner(new File(filename));
			for (int i = 0; i <= 8; i++)
				scan.nextLine();
			
			while(scan.hasNextLine()){
				String[] strline = removeWhiteSpace(scan.nextLine()).split(" ");
				
				int[] line = new int[strline.length];
				for (int i = 0; i < strline.length; i++)
					line[i] = Integer.parseInt(strline[i]);
				
				Vertex vertex = new Vertex(line[0], line[1], line[2], line[3], line[4], line[5], line[6]);
				vrhovi.add(vertex);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return vrhovi;
	}
	
	public static int[] loadRestrictions(String filename){
		int[] res = new int[2];
		
		try {
			Scanner scan = new Scanner(new File(filename));
			for (int i = 0; i < 4; i++)
				scan.nextLine();
			
			String[] resString = removeWhiteSpace(scan.nextLine()).split(" ");
			res[0] = Integer.parseInt(resString[0]);
			res[1] = Integer.parseInt(resString[1]);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	private static String removeWhiteSpace(String line){
		StringBuilder builder = new StringBuilder("");
		char[] lineChars = line.toCharArray();
		
		for (int i = 0; i < lineChars.length; i++){
			if (lineChars[i] == ' ');
			else {
				while (i < lineChars.length && lineChars[i] != ' '){
					builder.append(lineChars[i]);
					i++;
				}
				builder.append(' ');
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * Metoda koja služi parcijalnom sortiranju predanog polja rješenja TSP-a.
	 * Zadatak metode je na početak polja staviti <code>number</code> najboljih
	 * rješenja (to su ona s najmanjom duljinom ture); poredak preostalog dijela
	 * polja nije bitan.
	 * 
	 * @param population rješenja koja treba parcijalno sortirati
	 * @param number broj najboljih rješenja koja treba staviti na početak polja
	 */
	public static void partialSort(Ant[] population, int number) {
		for(int i = 0; i < number; i++) {
			int best = i;
			for(int j = i+1; j < population.length; j++) {
				if(population[best].tourLength > population[j].tourLength) {
					best = j;
				}
			}
			if(best != i) {
				Ant tmp = population[i];
				population[i] = population[best];
				population[best] = tmp;
			}
		}
	}

	/**
	 * Metoda za predano rješenje računa njegovu duljinu temeljem predane
	 * matrice udaljenosti. U toj matrici, na mjestu [i,j] nalazi se udaljenost
	 * od grada <code>i</code> do grada <code>j</code>.
	 * 
	 * @param sol rješenje za koje treba izračunati duljinu ture
	 * @param distanceMatrix matrica udaljenosti gradova
	 */
	public static void evaluate(Ant ant, double[][] distanceMatrix) {
		double tourLength = 0.0;
		
		for (Cycle c : ant.vozila){
			for (int i = 0; i < c.vrhovi.size()-1; i++){
				tourLength += distanceMatrix[c.vrhovi.get(i).id][c.vrhovi.get(i+1).id];
			}
		}
		
		ant.tourLength = tourLength;
	}


	/**
	 * Metoda vraća novo polje gradova ne temelju izvornog polja gradova
	 * i predanog redosljeda određenog indeksima.
	 * 
	 * @param cities originalno polje gradova
	 * @param indexes željeni poredak gradova
	 * @return novo polje složeno prema indeksima
	 */
	public static Vertex[] reorderCities(Vertex[] cities, int[] indexes) {
		Vertex[] array = new Vertex[indexes.length];
		for(int i = 0; i < indexes.length; i++) {
			Vertex c = cities[indexes[i]];
			array[i] = new Vertex(c.x, c.y);
		}
		return array;
	}
	

}
