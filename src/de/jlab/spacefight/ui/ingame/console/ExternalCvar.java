/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console;

import de.jlab.spacefight.SpaceAppState;

/**
 *
 * @author rampage
 */
public abstract class ExternalCvar {     
    
    private SpaceAppState space;
    
    public ExternalCvar(SpaceAppState space) {
        this.space = space;
    }
    
    public SpaceAppState getSpace() {
        return this.space;
    }
    
    public abstract void setValue(String value) throws IllegalArgumentException;
    public abstract String getValue();
    
    public abstract String getName();
    public abstract String getDescription();    
    public abstract String[] getPossibleValues();
    
    protected boolean parseBoolean(String value) throws IllegalArgumentException {        
        if ("1".equals(value) || "true".equalsIgnoreCase(value)) {
            return true;
        } else if ("0".equals(value) || "false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("Illegal cvar value \"" + value + "\"!");
    }
    
}
