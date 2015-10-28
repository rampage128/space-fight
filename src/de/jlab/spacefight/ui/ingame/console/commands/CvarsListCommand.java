/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console.commands;

import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.ui.ingame.console.CommandHandler;
import de.jlab.spacefight.ui.ingame.console.ExternalConsoleCommand;
import de.jlab.spacefight.ui.ingame.console.ExternalCvar;

/**
 *
 * @author rampage
 */
public class CvarsListCommand extends ExternalConsoleCommand {

    private CommandHandler commandHandler;
    
    public CvarsListCommand(CommandHandler commandHandler, SpaceAppState space) {
        super(commandHandler, space);
        this.commandHandler = commandHandler;
    }
    
    @Override
    public void execute(String[] args) {
        String queryCommand = null;
        if ( args.length > 1 ) {
            queryCommand = args[1];
        }

        print("--------------------------");
        ExternalCvar[] cvars = this.commandHandler.getRegisteredCvars(queryCommand);
        if ( cvars.length > 0 ) {
            for ( ExternalCvar cvar : cvars ) {
                printCvar(cvar);
            }
        } else {
            StringBuilder builder = new StringBuilder("No cvars found");
            if ( queryCommand != null ) {
                builder.append(" for \"").append(queryCommand).append("\"!");
            } else {
                builder.append("!");
            }
            print(builder.toString());
        }
        print("--------------------------");
    }

    private void printCvar(ExternalCvar cvar) {
        StringBuilder builder = new StringBuilder(cvar.getName());
        String[] possibleValues = cvar.getPossibleValues();
        if ( possibleValues != null && possibleValues.length > 0 ) {
            builder.append(" [");
            for (int i = 0; i < possibleValues.length; i++) {
                if ( i > 0 ) {
                    builder.append("|");
                }
                builder.append(possibleValues[i]);
            }
            builder.append("]");
        }
        builder.append(" - ").append(cvar.getDescription());
        print(builder.toString());
    }
    
    @Override
    public String[] getVariants() {
        return new String[] {
            "cvars"
        };
    }

    @Override
    public String getDescription() {
        return "describes console cvars (use searchword as parameter to query cvars)";
    }
    
}
