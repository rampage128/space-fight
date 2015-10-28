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
public class QuitCommand extends ExternalConsoleCommand {

    public QuitCommand(CommandHandler commandHandler, SpaceAppState space) {
        super(commandHandler, space);
    }
    
    @Override
    public void execute(String[] args) {
        getSpace().getGame().gotoMainMenu();
        if ( args.length < 2 || !"match".equalsIgnoreCase(args[1]) ) {
            getSpace().getGame().quit();
        }
    }

    @Override
    public String[] getVariants() {
        return new String[] {
            "quit",
            "quit match"
        };
    }

    @Override
    public String getDescription() {
        return "quits a match or the game (you don't really want that, do you?!)";
    }
    
}
