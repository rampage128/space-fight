/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.scripting;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>ScriptWrapper</code> is an abstract class which provides a secure
 * environment for executing scripts. It may be extended by specialized types
 * to provide access to Java objects.
 * 
 * 
 * @author rampage
 */
public abstract class ScriptWrapper {
    
    private boolean initialized = false;
    
    protected AbstractScript script;
    private HashMap<String, Object> referenceMap = new HashMap<String, Object>();
        
    public void init(AbstractScript script) {
        this.referenceMap.put("script", this);
        this.script = script;
        this.script.compile();
        this.initialized = true;
    }
    
    protected void invoke(String functionName) {
        invoke(functionName, (Object[])null);
    }
    
    protected void invoke(String functionName, Object... args) {
        if (checkState()) {
            this.script.handleEvent(functionName, referenceMap, true, args);
        }
    }
        
    protected void addReference(String name, Object object) {
        this.referenceMap.put(name, object);
    }
    
    private boolean checkState() {
        if ( !this.initialized ) {
            Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, "Script " + this.script.getName() + " was not initialized!");
        }
        return this.initialized;
    }
    
    public void cleanup() {
        this.script = null;
        this.referenceMap.clear();
        this.initialized = false;
    }
    
    public void log(String message) {
        System.out.println(message);
        //Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, message);
    }
    
    //public abstract ScriptWrapper clone();
    
}
