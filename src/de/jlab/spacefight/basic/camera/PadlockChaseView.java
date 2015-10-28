/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic.camera;

import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;

/**
 *
 * @author rampage
 */
public class PadlockChaseView extends ChaseView {
    
    private boolean hasTarget = false;
    
    @Override
    protected void updateAttachedView(CameraNode cameraNode, ObjectInfoControl target, float tpf) {
        FlightControl flightControl = target.getObjectControl(FlightControl.class);
        WeaponSystemControl weapons = target.getObjectControl(WeaponSystemControl.class);
        if (weapons != null && flightControl != null) {
            TargetInformation otherTarget = weapons.getTarget();
            if (otherTarget != null) {
                this.hasTarget = true;
                //Vector3f targetVector = new Vector3f(); //ship.getWorldRotation().inverse().mult(target.getObject().getPosition().subtract(cameraNode.getWorldTranslation()));
                                
                Vector3f targetVector = target.getRotation().inverse().mult(otherTarget.getObject().getPosition().subtract(cameraNode.getWorldTranslation()));
                
                //cameraNode.getParent().worldToLocal(otherTarget.getObject().getPosition(), targetVector);
                //Vector3f camPosition = targetVector.negate().normalizeLocal().multLocal(-this.chase);               
                setCameraOffset(targetVector.negate().normalizeLocal().multLocal(-this.chase).addLocal(new Vector3f(0, this.height, 0)));
                
                setCameraLookAt(targetVector);
                
            } else {
                if ( this.hasTarget ) {
                    Camera cam = cameraNode.getCamera();
                    super.cleanup();
                    super.init(cam, target);
                    this.hasTarget = false;
                }
                //cameraNode.lookAt(new Vector3f(0, 0, 50), Vector3f.UNIT_Z);
                //cameraNode.setLocalTranslation(new Vector3f(0, 10, -25));
            }
        }
    }
    
}
