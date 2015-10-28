/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.controls;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rampage
 */
public class DropDownValue {
    
    private String label;
    private Object value;
    
    public DropDownValue(Object value) {
        this(value.toString(), value);
    }
    
    public DropDownValue(String label, Object value) {
        this.label = label;
        this.value = value;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public Object getValue() {
        return this.value;
    }
    
    public String getStringValue() {
        if (this.value == null) {
            return null;
        }
        return this.value.toString();
    }
    
    public int getIntValue() {
        if (this.value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number)this.value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            } catch (NumberFormatException ex) {
                Logger.getLogger(DropDownValue.class.getName()).log(Level.SEVERE, "Cannot get int from DropDownValue (" + this.label + " = " + this.value + ")", ex);
                return 0;
            }
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return this.label;
    }
    
}
