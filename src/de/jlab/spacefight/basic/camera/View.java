/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic.camera;

import com.jme3.renderer.Camera;
import de.jlab.spacefight.basic.ObjectInfoControl;

/**
 *
 * @author rampage
 */
public interface View {

    public boolean init(Camera camera, ObjectInfoControl target);
    public void update(Camera camera, ObjectInfoControl target, float tpf);
    public void cleanup();
    public boolean displayHud();
    
}
