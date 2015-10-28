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
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.CameraControl.ControlDirection;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.random.RandomCoordinateGenerator;

/**
 *
 * @author rampage
 */
public class EndGameView implements View {
    
    private CameraNode cameraNode;
    
    //private Quaternion rotation = new Quaternion();
    private Vector3f speeds;
    
    public boolean init(Camera camera, ObjectInfoControl target) {
        
        Vector3f position = camera.getLocation().clone();
        Quaternion rotation = camera.getRotation().clone();
        
        this.cameraNode = new CameraNode(getClass().getSimpleName(), camera);
        this.cameraNode.setControlDir(ControlDirection.SpatialToCamera);
        
        this.cameraNode.setLocalTranslation(position);
        this.cameraNode.setLocalRotation(rotation);
               
        RandomCoordinateGenerator gen = new RandomCoordinateGenerator(System.currentTimeMillis(), new Vector3f(0.2f, 0.2f, 0.2f));
        this.speeds = gen.getVector();
        
        SpaceAppState space = Game.get().getStateManager().getState(SpaceAppState.class);
        space.getSpace().attachChild(cameraNode);
        
        return true;
    }

    public void update(Camera camera, ObjectInfoControl target, float tpf) {
        this.cameraNode.rotate(this.speeds.x * tpf, this.speeds.y * tpf, this.speeds.z * tpf);
    }
    
    public void cleanup() {
        this.cameraNode.removeFromParent();
        this.cameraNode.removeControl(CameraControl.class);
    }

    public boolean displayHud() {
        return false;
    }
    
}
