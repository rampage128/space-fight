/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.hud;

import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.systems.SystemControl;
import de.jlab.spacefight.ui.ingame.hud.HudControl;

/**
 *
 * @author Stefan
 */
public class CockpitHudControl extends HudControl implements SystemControl {
    
    public static final String HEALTH_HUD = "healthHud";
    public static final String TARGETINDICATOR = "target";
    public static final String CROSSHAIR = "crosshair";
    public static final String VELOCITY = "velocity";
    public static final String FLIGHTDIRECTION = "flightdirection";
    public static final String RADAR = "radar";
    public static final String TASK = "task";
    //public static final String WEAPON = "weapon";
        
    public CockpitHudControl(SpaceAppState space) {
        super(space);
    }
                     
    public Control cloneForSpatial(Spatial spatial) {
        CockpitHudControl hman = new CockpitHudControl(space);
        hman.setSpatial(spatial);
        return hman;
    }

    public void resetSystem() {
        this.cleanupHud();
        this.initHud();
    }
    
    /* OWN METHODS */
    @Override
    protected void initHud() {
        HealthHud healthHud = new HealthHud(space, null);
        healthHud.initItem(getSpatial());
        addItem(CockpitHudControl.HEALTH_HUD, healthHud);
        
        FlightDirectionItem fdItem = new FlightDirectionItem(space);
        fdItem.initItem(getSpatial());
        addItem(FLIGHTDIRECTION, fdItem);
        
        TargetIndicatorItem tItem = new TargetIndicatorItem(space);
        tItem.initItem(getSpatial());
        addItem(TARGETINDICATOR, tItem);
        
        TaskIndicatorItem tsItem = new TaskIndicatorItem(space);
        tsItem.initItem(getSpatial());
        addItem(TASK, tsItem);
        
        CrosshairItem cItem = new CrosshairItem(space);
        cItem.initItem(getSpatial());
        addItem(CROSSHAIR, cItem);
        
        VelocityItem vItem = new VelocityItem(space);
        vItem.initItem(getSpatial());
        addItem(VELOCITY, vItem);
        
        RadarItem rItem = new RadarItem(space);
        rItem.initItem(getSpatial());
        addItem(RADAR, rItem);
        
        /*
        WeaponItem wItem = new WeaponItem(space);
        wItem.initItem(getSpatial());
        addItem(WEAPON, wItem);
        */
    }
    
}
