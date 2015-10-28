/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems;

/**
 * Global interface for SpaceShip-Systems. It supplies methods to maintain
 * global control states (like resetting weapons on respawn).
 * 
 * @author rampage
 */
public interface SystemControl {
    
    public void resetSystem();
    public void setEnabled(boolean enabled);
    public boolean isEnabled();
    
}
