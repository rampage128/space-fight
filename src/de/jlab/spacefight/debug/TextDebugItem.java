/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.debug;

/**
 *
 * @author rampage
 */
public final class TextDebugItem extends AbstractDebugItem {
    
    private String name;
    
    public TextDebugItem(String name, String text) {
        this.name = name;
        setValue(text);
    }
    
    public void setValue(String name, String value) {
        this.name = name;
        super.setValue(value);
    }
    
    @Override
    public String getValue() {
        StringBuilder valueBuilder = new StringBuilder(this.name).append(": ").append(getValue());
        return valueBuilder.toString();
    }

    @Override
    public void cleanup() {
        // NOTHING TO DO HERE
    }
    
}
