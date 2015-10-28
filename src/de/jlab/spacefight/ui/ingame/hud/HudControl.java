/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.hud;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import de.jlab.spacefight.SpaceAppState;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Stefan
 */
public abstract class HudControl extends AbstractControl {
        
    protected SpaceAppState space;
    private Map<String, HudItem> hudMap;
    
    public HudControl(SpaceAppState space) {
        this.space = space;
        this.hudMap = new HashMap<String, HudItem>();
    }
            
    public void addItem(String name, HudItem item) {
        this.hudMap.put(name, item);
    }
    
    public HudItem getItem(String name) {
        return this.hudMap.get(name);
    }
       
    @Override
    public void controlUpdate(float tpf) {
        for ( HudItem item : hudMap.values() ) {
            item.updateItem(tpf, getSpatial());
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        for ( HudItem item : hudMap.values() ) {
            item.renderItem(getSpatial(), rm, vp);
        }
    }

    @Override
    public void setSpatial(Spatial spatial) {
        if ( getSpatial() != null ) {
            cleanupHud();
        }
        super.setSpatial(spatial);        
        if ( spatial != null ) {
            initHud();
        }
    }
    
    protected void initHud() {
        for ( HudItem item : hudMap.values() ) {
            item.cleanupItem(spatial);
            item.cleanup();
        }
    }

    public void cleanupHud() {
        for ( HudItem item : hudMap.values() ) {
            item.cleanupItem(spatial);
            item.cleanup();
        }
    }
    
}
