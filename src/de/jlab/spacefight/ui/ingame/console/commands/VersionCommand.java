/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console.commands;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.ui.ingame.console.CommandHandler;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.ui.ingame.console.ExternalConsoleCommand;
import de.lessvoid.nifty.tools.Color;

/**
 *
 * @author rampage
 */
public class VersionCommand extends ExternalConsoleCommand {
    
    public VersionCommand(CommandHandler commandHandler, SpaceAppState space) {
        super(commandHandler, space);
    }
    
    @Override
    public void execute(final String[] args) {
        print(Game.VERSION.toString(), Color.WHITE);
    }

    @Override
    public String[] getVariants() {
        return new String[] {
            "version"
        };
    }

    @Override
    public String getDescription() {
        return "displays your version of the game";
    }
    
}
