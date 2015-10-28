/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.input;

/**
 *
 * @author rampage
 */
public enum GameInput implements DisplayableEnum.Display {
    CONSOLE,
    MENU,
    SCOREBOARD;
    
    public String getDisplayValue() {
        return this.toString();
    }
    
}
