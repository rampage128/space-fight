/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.structures;

import com.jme3.math.Vector3f;
import de.jlab.spacefight.basic.ObjectInfoControl;

/**
 * Information for damage
 * @author rampage
 */
public class DamageInformation {
    
    private String damageName;
    private String damageType;
    private float time = 0f;
    private long systemTime = 0l;
    private float damage = 0f;
    private ObjectInfoControl origin;
    private ObjectInfoControl target;
    private Vector3f position;
    private Vector3f direction;
    private Vector3f relativeDirection;
    
    public DamageInformation(String damageType, String damageName, float damage, ObjectInfoControl origin, ObjectInfoControl target, Vector3f direction, Vector3f worldPosition) {
        this.damageType = damageType;
        this.damageName = damageName;
        this.damage = damage;
        this.origin = origin;
        this.target = target;
        this.position = worldPosition;
        this.direction = direction;
        this.relativeDirection = target.getRotation().inverse().mult(direction);
        this.systemTime = System.currentTimeMillis();
    }
    
    public void update(float tpf) {
        this.time += tpf;
    }
    
    public ObjectInfoControl getOrigin() {
        return this.origin;
    }
    
    public ObjectInfoControl getTarget() {
        return this.target;
    }
    
    public float getDamage() {
        return this.damage;
    }
    
    public float getTime() {
        return this.time;
    }
    
    public long getSystemTime() {
        return this.systemTime;
    }
    
    public Vector3f getDirection() {
        return this.direction;
    }
    
    public Vector3f getWorldPosition() {
        return this.position;
    }
        
    public Vector3f getRelativeDirection() {
        return this.relativeDirection;
    }
    
    public String getDamageType() {
        return this.damageType;
    }
    
    public String getDamageName() {
        return this.damageName;
    }
    
}
