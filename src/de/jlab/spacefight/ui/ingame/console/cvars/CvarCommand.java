/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console.cvars;

import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.ui.ingame.console.CommandHandler;
import de.jlab.spacefight.ui.ingame.console.ExternalConsoleCommand;
import de.jlab.spacefight.ui.ingame.console.ExternalCvar;

/**
 *
 * @author rampage
 */
public class CvarCommand extends ExternalConsoleCommand {

    private ExternalCvar cvar;
    
    public CvarCommand(ExternalCvar cvar, CommandHandler commandHandler, SpaceAppState space) {
        super(commandHandler, space);
        this.cvar = cvar;
    }
    
    @Override
    public void execute(String[] args) {
        if ( args.length > 1 ) {
            try {
                cvar.setValue(args[1]);
            } catch (IllegalArgumentException e) {
                print(e.getMessage());
            }
        }
        print(new StringBuilder(cvar.getName()).append(" = ").append(cvar.getValue()).toString());
    }

    @Override
    public String[] getVariants() {
        return new String[] { this.cvar.getName() };
    }

    @Override
    public String getDescription() {
        return this.cvar.getDescription();
    }
    
    
}
