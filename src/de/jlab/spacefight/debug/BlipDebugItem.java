/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.debug;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 *
 * @author rampage
 */
public final class BlipDebugItem extends AbstractDebugItem {
    
    public BlipDebugItem(Vector3f position, float radius, ColorRGBA color) {
        setValue(position, radius, color);
    }
    
    public void setValue(Vector3f position, float radius, ColorRGBA color) {
        Geometry arrowNode = (Geometry)super.getValue();
        if ( arrowNode == null ) {
            Box box = new Box(0.5f, 0.5f, 0.5f);
            box.setLineWidth(4);
            arrowNode = SpaceDebugger.getInstance().createDebugShape(box, color, true);
            arrowNode.setQueueBucket(Bucket.Translucent);
            super.setValue(arrowNode);
        }
        arrowNode.setLocalTranslation(position);
        arrowNode.setLocalScale(radius);
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
