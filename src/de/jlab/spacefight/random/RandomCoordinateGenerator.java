/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.random;

import com.jme3.math.Vector3f;
import java.util.Random;

/**
 *
 * @author rampage
 */
public class RandomCoordinateGenerator {
    
    private Random random;
    private Vector3f size;
    
    public RandomCoordinateGenerator(Vector3f size) {
        this(-1, size);
    }
    
    public RandomCoordinateGenerator(long seed, Vector3f size) {
        if (seed == -1) {
            this.random = new Random();
        } else {
            this.random = new Random(seed);
        }
        this.size = size;
    }
    
    public Vector3f getVector() {
        //float x = (float)this.random.nextInt(Math.round(this.size.x + 1)) - (this.size.x / 2);
        float x = (this.random.nextFloat() * this.size.x) - (this.size.x / 2);
        float y = (this.random.nextFloat() * this.size.y) - (this.size.y / 2);
        float z = (this.random.nextFloat() * this.size.z) - (this.size.z / 2);
        
        return new Vector3f(x, y, z);
    }
       
}
