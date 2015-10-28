/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console.commands;

import de.jlab.spacefight.ui.ingame.console.CommandHandler;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.ui.ingame.console.ExternalConsoleCommand;

/**
 *
 * @author rampage
 */
public class ClearCommand extends ExternalConsoleCommand {

    public ClearCommand(CommandHandler commandHandler, SpaceAppState space) {
        super(commandHandler, space);
    }
    
    @Override
    public void execute(String[] args) {
        getHandler().getConsole().clear();
    }

    @Override
    public String[] getVariants() {
        return new String[] {
            "clear"
        };
    }

    @Override
    public String getDescription() {
        return "clears the console";
    }
    
}
