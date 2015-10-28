/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic.camera;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.CameraControl.ControlDirection;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;

/**
 *
 * @author rampage
 */
public abstract class AbstractAttachedView implements View {

    private CameraNode cameraNode;
    
    private Spatial cockpit;
    private Spatial hull;
    
    private boolean initialized = false;

    private float linearInertia = 0.5f;
    private float angularInertia = 0.5f;
    /*
    private Vector3f previousAngles = new Vector3f();
    private Vector3f currentAngles = new Vector3f();
    private Vector3f inertiaAngles = new Vector3f();
    */
    
    private float lerp = 1;
    private Vector3f camOffset = new Vector3f(Vector3f.ZERO);
    private Vector3f camLookAt = new Vector3f(0, 0, 50f);
    
    private Vector3f camMoveTo = new Vector3f(Vector3f.ZERO);
    private Vector3f camPosition = new Vector3f(Vector3f.ZERO);
        
    private Quaternion camRotation = new Quaternion();
    private Quaternion camRotateTo = new Quaternion();
    
    public final boolean init(Camera camera, ObjectInfoControl target) {
        if ( target == null )
            return false;
        this.cameraNode = new CameraNode(getClass().getSimpleName(), camera);
        this.cameraNode.setControlDir(ControlDirection.SpatialToCamera);
        if ( this.initAttachedView(this.cameraNode, target) ) {
            //((Node)target.getSpatial()).attachChild(this.cameraNode);
            Game.get().getStateManager().getState(SpaceAppState.class).getSpace().attachChild(this.cameraNode);
            this.cockpit = ((Node)target.getSpatial()).getChild("Cockpit");
            this.hull = ((Node)target.getSpatial()).getChild("Hull");
            if (isCockpitView()) {
                showCockpit();
            }
            //this.cameraNode.getParent().getWorldRotation().toAngleAxis(this.currentAngles);
            this.camPosition.set(target.getRotation().mult(camOffset)).addLocal(target.getPosition());
            this.camRotation.lookAt(this.camLookAt, target.getRotation().mult(Vector3f.UNIT_Z));
            this.initialized = true;
            return true;
        }
        return false;
    }

    public final void update(Camera camera, ObjectInfoControl target, float tpf) {
        if ( this.initialized ) {
            updateAttachedView(this.cameraNode, target, tpf);

            // UPDATE ROTATION
            this.camRotation.set(this.cameraNode.getWorldRotation());
            
            this.camRotateTo.lookAt(target.getRotation().mult(this.camLookAt), target.getRotation().mult(Vector3f.UNIT_Y));
            
            if (this.angularInertia > 0) {
                this.lerp = tpf / (this.angularInertia / 10);
                this.cameraNode.setLocalRotation(this.camRotation.slerp(this.camRotation, this.camRotateTo, this.lerp));
            } else {
                this.cameraNode.setLocalRotation(this.camRotateTo);
            }
            
            // UPDATE CAM POSITION
            this.camPosition.set(this.cameraNode.getWorldTranslation());
            
            if (isCockpitView() && this.cockpit != null) {
                 camMoveTo.set(target.getRotation().mult(this.cockpit.getLocalTranslation().add(camOffset)).addLocal(target.getPosition()));
            } else {
                 camMoveTo.set(target.getRotation().mult(camOffset).addLocal(target.getPosition()));
            }
            
            if (this.linearInertia > 0) {
                this.lerp = tpf / (this.linearInertia / 10);
                this.cameraNode.setLocalTranslation(this.camPosition.interpolate(this.camPosition, this.camMoveTo, this.lerp));
            } else {
                this.cameraNode.setLocalTranslation(this.camMoveTo);
            }
            
            //System.out.println(this.cameraNode.getLocalTranslation().subtract(target.getPosition()).length() + " | " + camMoveTo.subtract(target.getPosition()).length() + " | " + this.cameraNode.getLocalTranslation().subtract(camMoveTo).length());                      
        }
    }

    public void cleanup() {
        if (isCockpitView()) {
            hideCockpit();
        }
        if (this.cameraNode == null)
            return;
        this.cleanupAttachedView(this.cameraNode);
        this.cameraNode.removeFromParent();
        this.cameraNode.removeControl(CameraControl.class);
    }
    
    protected void showCockpit() {
        if (this.cockpit != null) {
            this.hull.setCullHint(Spatial.CullHint.Always);
            //this.cockpit.setCullHint(Spatial.CullHint.Never);
        }
    }
    
    protected void hideCockpit() {
        if (this.cockpit != null) {
            this.hull.setCullHint(Spatial.CullHint.Inherit);
            this.cockpit.setCullHint(Spatial.CullHint.Always);
        }
    }
    
    public void setAngularInertia(float angularInertia) {
        this.angularInertia = angularInertia;
    }
    
    public void setLinearInertia(float linearInertia) {
        this.linearInertia = linearInertia;
    }
    
    protected void setCameraOffset(Vector3f offset) {
        this.camOffset.set(offset);
    }
    
    public Vector3f getCameraOffset() {
        return this.camOffset;
    }
    
    protected void setCameraLookAt(Vector3f lookAt) {
        this.camLookAt.set(lookAt);
    }
    
    protected abstract boolean initAttachedView(CameraNode cameraNode, ObjectInfoControl target);
    protected abstract void updateAttachedView(CameraNode cameraNode, ObjectInfoControl target, float tpf);
    protected abstract void cleanupAttachedView(CameraNode cameraNode);
    protected abstract boolean isCockpitView();
    
}
