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
public class DebugCommand extends ExternalConsoleCommand {

    public DebugCommand(CommandHandler commandHandler, SpaceAppState space) {
        super(commandHandler, space);
    }
    
    @Override
    public void execute(String[] args) {
        if ( args.length == 1 ) {
            if (getSpace().getGame().isDebug()) {
                print("0");
            } else {
                print("1");
            }
            return;
        }

        if ( "0".equals(args[1]) ) {
            getSpace().getGame().setDebug(false);
        } else if ( "1".equals(args[1]) ) {
            getSpace().getGame().setDebug(true);
        }
    }

    @Override
    public String[] getVariants() {
        return new String[] {
            "s_debug",
            "s_debug 0",
            "s_debug 1"
        };
    }

    @Override
    public String getDescription() {
        return "displays or toggles debug mode";
    }
    
}
