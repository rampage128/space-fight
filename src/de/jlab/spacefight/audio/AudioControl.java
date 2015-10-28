/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.audio;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author rampage
 */
public class AudioControl extends AbstractControl {

    private AudioManager manager;
    
    public AudioControl(AudioManager manager) {
        this.manager = manager;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        manager.update();
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
    }

    public Control cloneForSpatial(Spatial spatial) {
        AudioControl control = new AudioControl(manager);
        control.setSpatial(spatial);
        return control;
    }
    
}
