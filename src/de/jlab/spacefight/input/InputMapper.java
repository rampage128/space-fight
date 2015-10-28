/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.input;

import com.jme3.input.Input;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import java.lang.reflect.Field;

/**
 * Connects our inputhandling with that of JME3...
 * This class provides static methods to get a Trigger from a String.
 * 
 * NOTE: It uses REFLECTION which is BUTT-UGLY... unfortunately I see
 * no way around this (except silly static HashMaps) due to the static 
 * inputhandling of JME3 -.-
 * 
 * @author rampage
 */
public final class InputMapper {
       
    /**
     * Returns a JME3 Trigger for input-mapping from a given string
     * 
     * Examples:
     * <code>KEY_F1</code>
     * <code>MOUSEBUTTON_LEFT</code>
     * <code>MOUSE_X</code>
     * 
     * @see MouseInput, KeyInput
     * 
     * @param inputValue String which describes the Input event.
     * @return
     * @throws Exception 
     */
    public static Trigger get(String inputValue) throws Exception {
        String[] inputTokens = inputValue.split("_");
        if ("KEY".equalsIgnoreCase(inputTokens[0])) {            
            return new KeyTrigger(getConstValue(KeyInput.class, inputValue));
        } else if ("MOUSEBUTTON".equalsIgnoreCase(inputTokens[0])) {
            return new MouseButtonTrigger(getConstValue(MouseInput.class, "BUTTON_" + inputTokens[1]));
        } else if ("+MOUSE".equalsIgnoreCase(inputTokens[0])) {
            return new MouseAxisTrigger(getConstValue(MouseInput.class, "AXIS_" + inputTokens[1]), false);
        } else if ("-MOUSE".equalsIgnoreCase(inputTokens[0])) {
            return new MouseAxisTrigger(getConstValue(MouseInput.class, "AXIS_" + inputTokens[1]), true);
        }
        return null;
    }    
    
    private static int getConstValue(Class<? extends Input> input, String name) throws Exception {
        Field field = input.getField(name);
        return field.getInt(null);
    }
    
}
