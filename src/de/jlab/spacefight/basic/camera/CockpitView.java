/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic.camera;

import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import de.jlab.spacefight.basic.ObjectInfoControl;

/**
 *
 * @author rampage
 */
public class CockpitView extends AbstractAttachedView {
    
    @Override
    protected boolean initAttachedView(CameraNode cameraNode, ObjectInfoControl target) {
        this.setCameraLookAt(getCameraOffset().add(new Vector3f(0, 0, 50)));
        this.setLinearInertia(0.0f);
        this.setAngularInertia(0.5f);
        /*
        Node cockpit = (Node)((Node)target.getSpatial()).getChild("Cockpit");
        if (cockpit != null) {
            cameraNode.setLocalTranslation(cockpit.getLocalTranslation());
        } else {
            cameraNode.setLocalTranslation(Vector3f.ZERO);
        }
        cameraNode.lookAt(new Vector3f(0, 0, 50), Vector3f.UNIT_Y);
         */
        return true;
    }

    @Override
    protected void updateAttachedView(CameraNode cameraNode, ObjectInfoControl target, float tpf) {
        // NOTHING TO DO HERE (YET)
    }

    @Override
    protected void cleanupAttachedView(CameraNode cameraNode) {
        if ( cameraNode.getParent() != null ) {
            Node hull = (Node)cameraNode.getParent().getChild("Hull");
            if ( hull != null ) {
                hull.setCullHint(CullHint.Inherit);
            }
            Node cockpit = (Node)cameraNode.getParent().getChild("Cockpit");
            if ( cockpit != null ) {
                cockpit.setCullHint(CullHint.Always);
            }
        }
    }

    public boolean displayHud() {
        return true;
    }

    @Override
    protected boolean isCockpitView() {
        return true;
    }
    
}
