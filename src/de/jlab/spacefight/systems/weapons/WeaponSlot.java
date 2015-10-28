/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.weapons;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import de.jlab.spacefight.weapon.AbstractWeaponControl;
import de.lessvoid.nifty.tools.Color;

/**
 *
 * @author rampage
 */
public class WeaponSlot {
 
    private Spatial slot;
    private AbstractWeaponControl weapon;
    private float size = 1f;
    
    private String parentId;
    private Node objectNode;
    private String type = "energy";
    
    private int usageCount = 0;
    
    private ColorRGBA color = ColorRGBA.White;
    
    public WeaponSlot(Spatial slot, String parentId, float size) {
        this.slot = slot;
        this.parentId = parentId;
        this.size = size;
    }
    
    public AbstractWeaponControl getWeapon() {
        return this.weapon;
    }
    
    public float getSize() {
        return this.size;
    }
    
    public void setSize(float size) {
        this.size = size;
    }
    
    public void setWeapon(AbstractWeaponControl weapon) {
        if (weapon.getSize() > this.size) {
            throw new IllegalArgumentException("Cannot set weapon " + weapon.getName() + " into slot. Weapon is too large!");
        }
        this.weapon = weapon;
        weapon.setColor(this.color);
    }
    
    public void attach(Node objectNode) {
        Node parent = objectNode;
        if (this.parentId != null) {
            parent = (Node)parent.getChild(this.parentId);
        }
        
        parent.attachChild(this.slot);
        this.objectNode = objectNode;
    }
    
    public int weaponCount() {
        if (this.weapon.getSize() == 0) {
            return 0;
        }
        
        return (int)Math.floor(this.size / this.weapon.getSize()) - this.usageCount;       
    }
    
    public int getUsageCount() {
        return this.usageCount;
    }
    
    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
    
    public void fireWeapon(TargetInformation target, SpaceAppState space) {
        if (this.objectNode == null) {
            throw new RuntimeException("WeaponSlot has not been attached ot an object yet.");
        }
        //if (this.weapon instanceof MissileWeaponControl) {
            if (target != null) {
                this.weapon.setTarget(target);
            } else {
                this.weapon.setTarget(null);
            }
        //}
        this.weapon.getSpatial().setLocalTranslation(this.slot.getWorldTranslation());
        this.weapon.getSpatial().setLocalRotation(this.slot.getWorldRotation());
        space.addWeapon(this.weapon.getInstance(null), this.objectNode.getControl(ObjectInfoControl.class));
        this.usageCount++;
    }
    
    public void setWeaponColor(ColorRGBA color) {       
        if (color != null) { // && this.weapon.getType() == AbstractWeaponControl.TYPE_ENERGY
            this.color = color;
            this.weapon.setColor(color);
        } else {
            this.color = ColorRGBA.White;
        }
    }
    
    public void reset() {
        this.usageCount = 0;
    }
    
    @Override
    public String toString() {
        return this.weapon.getName();
    }
    
}
