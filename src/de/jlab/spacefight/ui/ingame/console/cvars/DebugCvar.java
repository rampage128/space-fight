/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console.cvars;

import de.jlab.spacefight.SpaceAppState;

/**
 *
 * @author rampage
 */
public class DebugCvar extends BooleanCvar {

    public DebugCvar(SpaceAppState space) {
        super(space);
    }
    
    @Override
    public String getName() {
        return "cl_debug";
    }

    @Override
    public String getDescription() {
        return "Toggles debug mode";
    }

    @Override
    protected boolean getState() {
        return getSpace().getGame().isDebug();
    }

    @Override
    protected void setState(boolean state) {
        getSpace().getGame().setDebug(state);
    }


    
}
