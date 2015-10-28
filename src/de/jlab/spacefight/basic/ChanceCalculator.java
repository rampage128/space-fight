/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic;

import java.util.Random;

/**
 *
 * @author rampage
 */
public class ChanceCalculator {
    
    private Random random;
    
    public ChanceCalculator() {
        reset();
    }
    
    public boolean calculateChance(float probability) {
        return this.random.nextInt(100) + 1 < probability;
    }
    
    public void reset() {
        this.random = new Random();
    }
    
}
