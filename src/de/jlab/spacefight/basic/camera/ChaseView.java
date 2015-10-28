/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic.camera;

import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import de.jlab.spacefight.basic.ObjectInfoControl;

/**
 *
 * @author rampage
 */
public class ChaseView extends AbstractAttachedView {

    protected float height = 10f;
    protected float chase = -25f;
    protected float forward = 50f;
    
    @Override
    protected boolean initAttachedView(CameraNode cameraNode, ObjectInfoControl target) {        
        this.height = target.getSize(); // 10
        this.chase = target.getSize() * -3; // -25
        this.forward = target.getSize() * 10; // 50
        
        //setLinearInertia(0.1f);
        
        //cameraNode.setLocalTranslation(new Vector3f(0, this.height, this.chase));
        //cameraNode.lookAt(new Vector3f(0, 0, this.forward), Vector3f.UNIT_Y);
        setCameraOffset(new Vector3f(0, this.height, this.chase));
        setCameraLookAt(new Vector3f(0, 0, this.forward));
        return true;
    }

    @Override
    protected void updateAttachedView(CameraNode cameraNode, ObjectInfoControl target, float tpf) {
    }

    @Override
    protected void cleanupAttachedView(CameraNode cameraNode) {
    }

    public boolean displayHud() {
        return true;
    }

    @Override
    protected boolean isCockpitView() {
        return false;
    }
    
}
