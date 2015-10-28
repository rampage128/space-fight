/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic.camera;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;

/**
 *
 * @author rampage
 */
public class PadlockCockpitView extends CockpitView {

    private boolean hasTarget = false;
    
    @Override
    protected void updateAttachedView(CameraNode cameraNode, ObjectInfoControl target, float tpf) {
        FlightControl flightControl = target.getObjectControl(FlightControl.class);
        WeaponSystemControl weapons = target.getObjectControl(WeaponSystemControl.class);
        if ( weapons != null && flightControl != null ) {
            TargetInformation otherTarget = weapons.getTarget();
            if (otherTarget != null) {
                this.hasTarget = true;
                Vector3f targetVector = target.getRotation().inverse().mult(otherTarget.getObject().getPosition().subtract(cameraNode.getWorldTranslation()));
                //targetVector.y *= -1;
                
                // TODO IF FORWARD VECTOR IS ON SCREEN WE TURN THE CAMERA FORWARD
                // THIS HELPS THE PLAYER TO NAVIGATE AND SHOOT WHEN THE TARGET IS WITHIN FRONT VISION
                
                setCameraLookAt(targetVector);
                /*
                Quaternion lookAt = new Quaternion();
                lookAt.lookAt(targetVector, Vector3f.UNIT_Y);
                cameraNode.setLocalRotation(lookAt);
                */
            } else {
                if ( this.hasTarget ) {
                    Camera cam = cameraNode.getCamera();
                    super.cleanup();
                    super.init(cam, target);
                    this.hasTarget = false;
                }
            }
        }
    }
    
}
