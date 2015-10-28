/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.random;

import java.util.Random;

/**
 *
 * @author rampage
 */
public class RandomScaleGenerator {
 
    private Random random;
    private float maxscale;
    private float minscale;
    
    public RandomScaleGenerator(long seed, float minscale, float maxscale) {
        this.random = new Random(seed);
        this.minscale = minscale;
        this.maxscale = maxscale;
    }
    
    public float getScale() {
        float range = this.maxscale - this.minscale;       
        float random = this.random.nextFloat() * range;
        return this.minscale + random;
    }
    
}
