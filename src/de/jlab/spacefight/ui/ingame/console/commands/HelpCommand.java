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
public class HelpCommand extends ExternalConsoleCommand {

    private CommandHandler commandHandler;
    
    public HelpCommand(CommandHandler commandHandler, SpaceAppState space) {
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
        ExternalConsoleCommand[] commands = this.commandHandler.getRegisteredCommands(queryCommand);
        if ( commands.length > 0 ) {
            for ( ExternalConsoleCommand command : commands ) {
                printCommand(command);
            }
        } else {
            StringBuilder builder = new StringBuilder("No commands found");
            if ( queryCommand != null ) {
                builder.append(" for \"").append(queryCommand).append("\"!");
            } else {
                builder.append("!");
            }
            print(builder.toString());
        }
        print("--------------------------");
    }

    private void printCommand(ExternalConsoleCommand command) {
        StringBuilder builder = new StringBuilder(command.getVariants()[0]).append(" - ").append(command.getDescription());
        print(builder.toString());
    }
    
    @Override
    public String[] getVariants() {
        return new String[] {
            "help"
        };
    }

    @Override
    public String getDescription() {
        return "describes console commands (use searchword as parameter to query commands)";
    }
    
}
