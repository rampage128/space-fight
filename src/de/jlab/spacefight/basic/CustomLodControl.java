/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic;

import com.jme3.bounding.BoundingVolume;
import com.jme3.math.FastMath;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.AreaUtils;
import com.jme3.scene.control.Control;
import java.util.List;

/**
 *
 * @author rampage
 */
public class CustomLodControl extends AbstractControl {

    public static final float VAR_LODTOLERANCE = 5f;
    
    private float _lastDistance = 0f;
    private List<Spatial> _lodList;
    private int _lastLevel;
    
    public CustomLodControl() {
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        BoundingVolume bv = spatial.getWorldBound();

        Camera cam = vp.getCamera();
        float atanNH = FastMath.atan(cam.getFrustumNear() * cam.getFrustumTop());
        float ratio = (FastMath.PI / (8f * atanNH));
        float newDistance = bv.distanceTo(vp.getCamera().getLocation()) / ratio;
        int level;

        if (Math.abs(newDistance - _lastDistance) <= VAR_LODTOLERANCE)
            level = _lastLevel; // we haven't moved relative to the model, send the old measurement back.
        //else if (_lastDistance > newDistance && _lastLevel == 0)
            //level = _lastLevel; // we're already at the lowest setting and we just got closer to the model, no need to keep trying.
        //else if (_lastDistance < newDistance && _lastLevel == _lodList.size() - 1)
            //level = _lastLevel; // we're already at the highest setting and we just got further from the model, no need to keep trying.
        else{
            _lastDistance = newDistance;

            // estimate area of polygon via bounding volume
            float area = AreaUtils.calcScreenArea(bv, _lastDistance, cam.getWidth());
            float areap = Math.min(1, area / (cam.getWidth() * 5f));
                        
            Spatial lodToUse = null;
            for ( int i = 0; i < _lodList.size(); i++ ) {
                Spatial lod = _lodList.get(i);
                lod.setCullHint(Spatial.CullHint.Always);
                lod.setShadowMode(ShadowMode.Off);
                Object lodvalue = lod.getUserData("lod");
                float lodsize = 1f;
                if ( lodvalue != null && lodvalue instanceof Float ) {
                    lodsize = (Float)lodvalue;
                }
                if ( areap <= lodsize )
                        lodToUse = lod;
            }
            
            if ( lodToUse != null ) {
                lodToUse.setCullHint(Spatial.CullHint.Inherit);
                lodToUse.setShadowMode(ShadowMode.Inherit);
            }
        }

        //spatial.setLodLevel(level);
    }

    public Control cloneForSpatial(Spatial spatial) {
        CustomLodControl cld = new CustomLodControl();
        cld.setSpatial(spatial);
        return cld;
    }
 
    public void setSpatial(Spatial spatial) {
        if ( spatial != null ) {
            Node hull = (Node)((Node)spatial).getChild("Hull");
            if (hull == null) {
                return;
            }            
            this._lodList = hull.getChildren();
            if ( this._lodList.isEmpty() )
                return;
        }
        super.setSpatial(spatial);
    }
    
}
