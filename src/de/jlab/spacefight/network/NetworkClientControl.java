/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.AbstractClientControl;
import de.jlab.spacefight.mission.AbstractMission;

/**
 *
 * @author rampage
 */
@Deprecated
public class NetworkClientControl extends AbstractClientControl {
    
    public NetworkClientControl(AbstractMission mission) {
        super(mission);
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        // NOTHIN TO DO YET
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        NetworkClientControl control = new NetworkClientControl(getMission());
        control.setSpatial(spatial);
        return control;
    }

    @Override
    public String getName() {
        return "XXX";
    }
    
}
