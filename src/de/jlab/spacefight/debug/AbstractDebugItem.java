/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.debug;

/**
 *
 * @author rampage
 */
public abstract class AbstractDebugItem {
    
    private Object value;
    
    protected void setValue(Object value) {
        this.value = value;
    }
    
    protected Object getValue() {
        return this.value;
    }
    
    abstract public void cleanup();
    
}
