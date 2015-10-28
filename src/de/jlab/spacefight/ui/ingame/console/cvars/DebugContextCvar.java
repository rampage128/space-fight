/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console.cvars;

import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.debug.DebugContext;
import de.jlab.spacefight.debug.SpaceDebugger;
import de.jlab.spacefight.ui.ingame.console.ExternalCvar;

/**
 *
 * @author rampage
 */
public class DebugContextCvar extends BooleanCvar {

    private DebugContext context;
    
    public DebugContextCvar(DebugContext context, SpaceAppState space) {
        super(space);
        this.context = context;
    }

    @Override
    protected boolean getState() {
        return SpaceDebugger.getInstance().isContextEnabled(this.context);
    }

    @Override
    protected void setState(boolean state) {
        SpaceDebugger.getInstance().enableContext(this.context, state);
    }

    @Override
    public String getName() {
        return new StringBuilder("cl_debug_").append(this.context.toString().toLowerCase()).toString();
    }

    @Override
    public String getDescription() {
        return new StringBuilder("Toggles debug context for ").append(this.context).toString();
    }
    

    
}
