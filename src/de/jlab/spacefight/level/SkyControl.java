/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.level;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author rampage
 */
public class SkyControl extends AbstractControl {
    
    private Camera camera;
    
    public SkyControl(Camera camera) {
        this.camera = camera;
    }

    @Override
    protected void controlUpdate(float tpf) {
        getSpatial().setLocalTranslation(camera.getLocation());
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        SkyControl control = new SkyControl(this.camera);
        control.setSpatial(spatial);
        return control;
    }
    
    
    
}
