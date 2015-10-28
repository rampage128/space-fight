/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console.cvars;

import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.ui.ingame.console.ExternalCvar;

/**
 *
 * @author rampage
 */
public abstract class BooleanCvar extends ExternalCvar {

    public BooleanCvar(SpaceAppState space) {
        super(space);
    }
    
    protected abstract boolean getState();
    protected abstract void setState(boolean state);
    @Override
    public abstract String getName();
    @Override
    public abstract String getDescription();
    
    @Override
    public final void setValue(String value) throws IllegalArgumentException {
        setState(parseBoolean(value));
    }

    @Override
    public final String getValue() {
        if (getState()) {
            return "1";
        } else {
            return "0";
        }
    }
    
    @Override
    public final String[] getPossibleValues() {
        return new String[] {
            "0",
            "1"
        };
    }
    
}
