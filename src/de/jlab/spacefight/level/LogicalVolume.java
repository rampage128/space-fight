/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.level;

import com.jme3.scene.Node;

/**
 *
 * @author rampage
 */
public interface LogicalVolume {
    
    public void initVolume();
    public boolean intersects(Node object);
    
}
