/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.test;

import java.util.Random;

/**
 *
 * @author rampage
 */
public class Pseudorandom {

    public static void main(String[] args) {
        printArray(generatePseudos(5, 10, 5, 1337));
        printArray(generatePseudos(5, 10, 5, 1337));
        
        printArray(generatePseudos(5, 100, 5, 1337));
    }
    
    private static int[] generatePseudos(int min, int max, int count, long seed) {
        Random random = new Random(seed);
        int[] ints = new int[count];
        for ( int i = 0; i < count; i++ ) {
            ints[i] = random.nextInt(max - min) + min;
        }
        return ints;
    }
    
    private static void printArray(int[] array) {
        StringBuilder builder = new StringBuilder();
        for ( int value : array ) {
            if ( builder.length() != 0 ) {
                builder.append(", ");
            }
            builder.append(value);
        }
        System.out.println(builder);
    }
    
}


