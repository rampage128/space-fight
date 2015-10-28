/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic.camera;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import de.jlab.spacefight.basic.ObjectInfoControl;

/**
 *
 * @author rampage
 */
public class FlyByView implements View {

    private Vector3f route = new Vector3f();
    
    public boolean init(Camera camera, ObjectInfoControl target) {
        if (target == null)
            return false;
        if (!target.isAlive())
            return false;
        
        Vector3f camLocation = new Vector3f(10, 10, 250);
        camLocation.set(target.getSize() / 2, target.getSize() / 2, target.getSize() * 6);
        
        camera.setLocation(target.getRotation().multLocal(camLocation).addLocal(target.getPosition()));
        camera.lookAt(target.getPosition(), Vector3f.UNIT_Y);
        return true;
    }

    public void update(Camera camera, ObjectInfoControl target, float tpf) {
        if ( target != null ) {
            camera.lookAt(target.getPosition(), Vector3f.UNIT_Y);
        }
    }
    
    public void cleanup() {
        // NOTHING TO DO HERE
    }

    public boolean displayHud() {
        return false;
    }
    
}
