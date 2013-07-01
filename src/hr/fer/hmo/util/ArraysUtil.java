package hr.fer.hmo.util;

import java.util.Random;

/**
 * Pomoćni razred s metodama za rad nad poljima.
 * 
 * @author marcupic
 */
public class ArraysUtil {

	/**
	 * Metoda koja popunjava polje integera počev od 0 na dalje.
	 * Primjerice, ako je polje duljine 3, sadržaj će postati:
	 * 0, 1, 2.
	 * 
	 * @param array polje koje treba popuniti
	 */
	public static void linearFillArray(int[] array) {
		for(int i = 0; i < array.length; i++) {
			array[i] = i;
		}
	}
	
	/**
	 * Metoda koja permutira redosljed elemenata u predanom
	 * polju posredstvom slučajnog mehanizma.
	 * 
	 * @param array polje koje treba permutirati
	 * @param rand generator slučajnih brojeva
	 */
	public static void shuffleArray(int[] array, Random rand) {
		for(int i = array.length-1; i>1; i--) {
			int b = rand.nextInt(i)+1;
			if(b!=i-1) {
				int e = array[i-1];
				array[i-1] = array[b];
				array[b] = e;
			}
		}
	}

}
