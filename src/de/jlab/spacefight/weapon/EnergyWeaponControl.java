/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.weapon;

import com.jme3.audio.AudioNode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.SpaceAppState;
import java.util.List;

/**
 * Implementation of energyweapons.
 * Does nothing particular and may be ommited in the future!
 * 
 * @author rampage
 */
@Deprecated
public class EnergyWeaponControl {
    
    public static final float MAX_RANGE = 1200f;
    public static final float SPEED = 500f;
    public static final float DAMAGE = 100f;
            
    private AudioNode objectHit;
    
    public EnergyWeaponControl(SpaceAppState space) {
        //super(space);
    }
    
    /*
    public Control cloneForSpatial(Spatial spatial) {
        EnergyWeaponControl control = new EnergyWeaponControl(space);
        control.setCollisionShape(getCollisionShape());
        control.setCollisionGroup(getCollisionGroup());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setOrigin(getOrigin());
        control.speed = speed;
        control.range = range;
        control.damage = damage;
        control.name = name;
        
        control.blastimpulse = this.blastimpulse;
        control.muzzleEffect = muzzleEffect;
        control.muzzleSize = muzzleSize;
        //control.hitSound = hitSound;
        control.explosionEffect = explosionEffect;
        control.explosionSize = explosionSize;
        control.setSpatial(spatial);
        return control;
    }
    */

    public void updateWeapon(float tpf) {
        // WE DON'T NEED TO UPDATE THE WEAPON, BECAUSE IT FLIES STRAIGHT
    }

    public void onDestroy() {
        
    }
    
    public void setColor(ColorRGBA color) {
        //setColorOnSpatial(getSpatial(), color);
    }
    
    private void setColorOnSpatial(Spatial spatial, ColorRGBA color) {
        if (spatial instanceof Node) {
            Node node = (Node)spatial;
            List<Spatial> childList = node.getChildren();
            for (Spatial child : childList) {
                setColorOnSpatial(child, color);
            }
        } else if (spatial instanceof Geometry) {
            Geometry gem = (Geometry)spatial;
            gem.getMaterial().setColor("Color", color);
            gem.getMaterial().setColor("GlowColor", color);
            if (gem.getMaterial().getAdditionalRenderState().isWireframe()) {
                gem.getMesh().setLineWidth(2);
            }
        }
    }

    //@Override
    public String getType() {
        return "energy";
    }
    
}
