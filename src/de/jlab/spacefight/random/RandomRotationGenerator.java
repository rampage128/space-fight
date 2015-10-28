/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.random;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import java.util.Random;

/**
 *
 * @author rampage
 */
public class RandomRotationGenerator {
    
    private Random random;
    
    public RandomRotationGenerator(long seed) {
        this.random = new Random(seed);
    }
    
    public Quaternion getRotation() {
        float round = 360 * FastMath.DEG_TO_RAD;
        float x = (this.random.nextFloat() * (round * 2)) - (round);
        float y = (this.random.nextFloat() * (round * 2)) - (round);
        float z = (this.random.nextFloat() * (round * 2)) - (round);
        
        return new Quaternion().fromAngles(x, y, z);
    }
    
}
