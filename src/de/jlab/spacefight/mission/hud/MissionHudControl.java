/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.hud;

import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.ui.ingame.hud.HudControl;

/**
 *
 * @author rampage
 */
public class MissionHudControl extends HudControl {

    public static final String ITEM_KILLS = "kills";
    
    public MissionHudControl(SpaceAppState space) {
        super(space);
    }
    
    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
 
    @Override
    protected void initHud() {
        KillHudItem killHud = new KillHudItem(space);
        killHud.initItem(getSpatial());
        addItem(ITEM_KILLS, killHud);
    }
    
}
