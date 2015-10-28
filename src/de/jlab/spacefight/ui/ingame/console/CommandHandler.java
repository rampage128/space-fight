/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console;

import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.ui.ingame.console.cvars.CvarCommand;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Console;
import de.lessvoid.nifty.controls.ConsoleCommands;
import java.util.ArrayList;

/**
 *
 * @author rampage
 */
public class CommandHandler {
    
    private ConsoleCommands commands;
    private Console console;
    private SpaceAppState space;
    
    private ArrayList<ExternalConsoleCommand> commandList = new ArrayList<ExternalConsoleCommand>();
    private ArrayList<ExternalCvar> cvarList = new ArrayList<ExternalCvar>();
    
    public CommandHandler(Console console, Nifty nifty, SpaceAppState space) {
        this.console = console;
        this.commands = new ConsoleCommands(nifty, console);
        this.commands.enableCommandCompletion(true);
        this.space = space;
    }
    
    public void registerCommand(ExternalConsoleCommand command) {
        String[] variants = command.getVariants();
        for ( String variant : variants ) {
            this.commands.registerCommand(variant, command);
        }
        this.commandList.add(command);
    }
    
    public void registerCvar(ExternalCvar cvar) {
        this.commands.registerCommand(cvar.getName(), new CvarCommand(cvar, this, this.space));
        this.cvarList.add(cvar);
    }
    
    public ExternalConsoleCommand[] getRegisteredCommands(String queryString) {
        if ( queryString == null ) {
            return this.commandList.toArray(new ExternalConsoleCommand[0]);
        }
        
        ArrayList<ExternalConsoleCommand> queryList = new ArrayList<ExternalConsoleCommand>();
        for ( ExternalConsoleCommand command : this.commandList ) {
            if ( command.getVariants()[0].indexOf(queryString) > -1 ) {
                queryList.add(command);
            }
        }
        return queryList.toArray(new ExternalConsoleCommand[0]);
    }
    
    public ExternalCvar[] getRegisteredCvars(String queryString) {
        if ( queryString == null ) {
            return this.cvarList.toArray(new ExternalCvar[0]);
        }
        
        ArrayList<ExternalCvar> queryList = new ArrayList<ExternalCvar>();
        for ( ExternalCvar cvar : this.cvarList ) {
            if ( cvar.getName().indexOf(queryString) > -1 ) {
                queryList.add(cvar);
            }
        }
        return queryList.toArray(new ExternalCvar[0]);
    }
    
    public Console getConsole() {
        return this.console;
    }
    
}
