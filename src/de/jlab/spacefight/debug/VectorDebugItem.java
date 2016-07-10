/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.debug;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.Arrow;

/**
 *
 * @author rampage
 */
public final class VectorDebugItem extends AbstractDebugItem {
        
    public VectorDebugItem(Vector3f origin, Vector3f vector, ColorRGBA color) {
        setValue(origin, vector, color);
    }
    
    public void setValue(Vector3f origin, Vector3f vector, ColorRGBA color) {
        Geometry arrowNode = (Geometry)super.getValue();
        if ( arrowNode == null ) {
            Arrow arrow = new Arrow(Vector3f.UNIT_Z);
            arrowNode = SpaceDebugger.getInstance().createDebugShape(arrow, color, false);
            arrowNode.setQueueBucket(Bucket.Translucent);
            arrowNode.getMaterial().getAdditionalRenderState().setLineWidth(1f);
            super.setValue(arrowNode);
        }
        arrowNode.setLocalTranslation(origin);
        arrowNode.setLocalScale(vector.length());
        Quaternion rotation = new Quaternion().fromAxes(Vector3f.ZERO, Vector3f.ZERO, vector);
        rotation.lookAt(vector, new Vector3f(0, 1, 0));
        arrowNode.setLocalRotation(rotation);
    }
    
    @Override
    public Geometry getValue() {
        return (Geometry)super.getValue();
    }

    @Override
    public void cleanup() {
        Geometry value = (Geometry)super.getValue();
        if (value != null) {
            value.removeFromParent();
        }
    }
        
}
