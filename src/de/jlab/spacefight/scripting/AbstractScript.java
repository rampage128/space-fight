/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.scripting;

import java.util.HashMap;

/**
 *
 * @author rampage
 */
public abstract class AbstractScript {

    private AbstractScript superScript  = null;
    private StringBuilder code          = null;
    private String name                 = null;
    private boolean isCompiled          = false;
    private boolean isCritical          = false;

    public AbstractScript(String name, String code, AbstractScript superScript, boolean isCritical) {
        this.superScript 	= superScript;
        this.isCritical	 	= isCritical;
        this.name               = name;
        this.code		= new StringBuilder(code);
    }

    public AbstractScript superScript() {
        return this.superScript;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code.toString();
    }

    public void addCode(String lines) {
        this.code.append("\n");
        this.code.append(lines);
    }

    public boolean isCompiled() {
        return this.isCompiled;
    }

    public boolean isCritical() {
        return this.isCritical;
    }

    protected final void isCompiled(boolean isCompiled) {
        this.isCompiled = true;
    }

    public final void tick() throws Exception {
        this.handleEvent("tick", null, false);
    }

    public abstract void compile();
    public abstract Object handleEvent(String eventType, HashMap<String, Object> objectMap, boolean mustExist, Object... args);
    public abstract void putReference(String name, Object object);
    public abstract void include(String name);
    
}
