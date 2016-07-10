/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.scripting;

import de.jlab.spacefight.gamedata.GamedataManager;
import java.util.HashMap;
import java.util.Iterator;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author rampage
 */
public class JScript extends AbstractScript {

        private GamedataManager gamedataManager;
	private ScriptEngine scriptHost = null;
	
	public JScript(String name, String code, AbstractScript superScript, boolean isCritical, GamedataManager gamedataManager) {
		super(name, code, superScript, isCritical);
		
                this.gamedataManager = gamedataManager;
		ScriptEngineManager mgr = new ScriptEngineManager();
		this.scriptHost = mgr.getEngineByName("JavaScript");
	}
	        
	@Override
	public void compile() {	
		try {
                        //putReference("script", this);
			this.scriptHost.eval(this.getCode());
                        this.isCompiled(true);
		} catch ( ScriptException e ) {
			throw new RuntimeException(e.getMessage() + " in Script " + this.getName(), e);
		}
	}

	@Override
	public Object handleEvent(String eventName, HashMap<String, Object> objectMap, boolean mustExist, Object... args) {
		Object result = null;
		
		if ( objectMap != null )
			this.putAll(objectMap);
		try {
			result = ((Invocable)this.scriptHost).invokeFunction(eventName, args);
		} catch (ScriptException e) {
                    throw new RuntimeException(e.getMessage() + " in Script " + this.getName(), e);
			//Engine.get().handleError(new RuntimeException(e.getMessage() + " in Script " + this.getName(), e), this.isCritical());
		} catch (NoSuchMethodException e) {
			if ( this.superScript() != null )
				this.superScript().handleEvent(eventName, objectMap, mustExist, args);
			else
				if ( mustExist )
					throw new RuntimeException(e.getMessage() + " in Script " + this.getName(), e);
		}
		if ( objectMap != null )
			this.removeAll(objectMap);
		
		return result;
	}
		
	private void putAll(HashMap<String, Object> objectMap) {
		for ( Iterator<String> kit = objectMap.keySet().iterator(); kit.hasNext(); ) {
			String key = kit.next();
			Object object = objectMap.get(key);
			this.scriptHost.put(key, object);
		}
	}
	
	private void removeAll(HashMap<String, Object> objectMap) {
		for ( Iterator<String> kit = objectMap.keySet().iterator(); kit.hasNext(); ) {
			String key = kit.next();
			this.scriptHost.put(key, null);
		}
	}

	@Override
	public void putReference(String name, Object object) {
		this.scriptHost.put(name, object);
	}

    @Override
    public void include(String name) {
        //StringBuilder scriptPath = new StringBuilder(DataStore.SCRIPT_BASE_PATH).append(name.replaceAll("\\.", "/"));
        
        int entityClassPosition = name.lastIndexOf(".");
        if ( entityClassPosition > -1 ) {
            name = name.substring(entityClassPosition + 1);
        }
        AbstractScript script = this.gamedataManager.loadScript(null, name);
        try {
            scriptHost.eval(script.getCode());
        } catch (ScriptException ex) {
            throw new RuntimeException("Cannot include Script " + name, ex);
        }
    }

}
