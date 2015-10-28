/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.debug;

import de.jlab.spacefight.input.DisplayableEnum;

/**
 *
 * @author rampage
 */
public enum DebugContext implements DisplayableEnum.Display {
    AUDIO,
    AI,
    PHYSICS,
    TARGET,
    WEAPONS,
    EFFECT;

    public String getDisplayValue() {
        return this.toString();
    }
    
}
