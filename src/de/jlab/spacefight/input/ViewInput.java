/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.input;

/**
 *
 * @author rampage
 */
public enum ViewInput implements DisplayableEnum.Display {
    VIEW01,
    VIEW02,
    VIEW03,
    VIEW04,
    VIEW05,
    VIEW06,
    VIEW07,
    VIEW08,
    VIEW09,
    VIEW10,
    VIEW11,
    VIEW12;

    public String getDisplayValue() {
        return this.toString();
    }
    
}
