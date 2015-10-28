/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.perks;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.ui.ingame.hud.HudItem;

/**
 *
 * @author rampage
 */
public class PerkHudItem extends HudItem {

    public PerkHudItem(SpaceAppState space) {
        super("perk", space);
    }
    
    @Override
    public void initItem(Spatial spatial) {
        
    }

    @Override
    public void updateItem(float tpf, Spatial spatial) {
        
    }

    @Override
    public void renderItem(Spatial spatial, RenderManager rm, ViewPort vp) {
        
    }

    @Override
    public void cleanupItem(Spatial spatial) {
        
    }
    
}
