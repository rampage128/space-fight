/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console;

import de.jlab.spacefight.SpaceAppState;
import de.lessvoid.nifty.controls.ConsoleCommands;
import de.lessvoid.nifty.controls.ConsoleCommands.ConsoleCommand;
import de.lessvoid.nifty.tools.Color;

/**
 *
 * @author rampage
 */
public abstract class ExternalConsoleCommand implements ConsoleCommand {
    
    private CommandHandler commandHandler;
    private SpaceAppState space;
    
    public ExternalConsoleCommand(CommandHandler commandHandler, SpaceAppState space) {
        this.commandHandler = commandHandler;
        this.space = space;
    }
    
    public abstract void execute(final String[] args);
    public abstract String[] getVariants();
    public abstract String getDescription();
    
    public CommandHandler getHandler() {
        return this.commandHandler;
    }
    
    public void print(String message) {
        this.commandHandler.getConsole().output(message);
    }
    
    public void print(String message, Color color) {
        this.commandHandler.getConsole().output(message, color);
    }
    
    public SpaceAppState getSpace() {
        return this.space;
    }
    
    public static void registerCommand(ExternalConsoleCommand command, ConsoleCommands commands) {
        String[] variants = command.getVariants();
        for ( String variant : variants ) {
            commands.registerCommand(variant, command);
        }
    }
    
}
