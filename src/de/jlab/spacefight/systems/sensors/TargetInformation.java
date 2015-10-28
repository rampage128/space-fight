/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.sensors;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.basic.DamageControl;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.systems.ShieldControl;
import de.jlab.spacefight.weapon.AbstractWeaponControl;
import de.jlab.spacefight.weapon.MissileWeaponControl;

/**
 * Class for storage of target-information (information of a spatial towards another).
 * It provides sensors with different statistics about a target.
 * SensorControl uses this internally to calculate radar and targeting information.
 * 
 * @author rampage
 */
public class TargetInformation {
    
    public static final int FOF_ANY     = -1;
    public static final int FOF_NEUTRAL = 0;
    public static final int FOF_FRIEND  = 1;
    public static final int FOF_FOE     = 2;
    
    private ObjectInfoControl object = null;
    
    //private float targetDistance     = 0f;
    //private Vector3f direction = new Vector3f();
    //private Vector3f aimAt     = new Vector3f();
    //private Vector3f aimAtWorld = new Vector3f();
    //private float aimAtAngle    = 0f;
    //private float aimAtTolerance = 0f;
    //private int fof = FOF_NEUTRAL;
    
    public TargetInformation(ObjectInfoControl object) {
        this.object = object;
    }
    
    public void updateLocal() {
        
    }
    
    /**
     * Call this to update the target's statistics.
     * @param source source Spatial to compute values from
     * @param weapon source weaponsystem to compute aimahead from
     */   
    /*
    public void update(ObjectInfoControl source, AbstractWeaponControl weapon) {
        // STORE TARGET FOF
        //ObjectInfoControl sourceObjectInfo = source.getControl(ObjectInfoControl.class);


        // STORE TARGET TYPE
        //if (targetObjectInfo != null) {
            //this.type = targetObjectInfo.getObjectType();
        //}
        
        // STORE TARGET POSITION
        //this.position.set(this.target.getWorldTranslation());
        
        // CALCULATE TARGET DIRECTION USING MINIMAL OBJECT ALLOCATION
        
        
        // CALCULATE TARGET DISTANCE
        

        // CALCULATE TARGET SIZE
        //if (targetObjectInfo != null) {
            //this.size = targetObjectInfo.getSize();
        //}
        
        // CALCULATE TARGET AIMAT
        //if ( this.targetPhysics == null ) {
            // WE HAVE NOT RETRIEVED OBJECTS PHYSICS YET
            // SO WE GET IT TO COMPUTE THE NEXT FRAME VIA TARGET VELOCITY
            //this.targetPhysics = this.target.getControl(PhysicsControl.class);
            //this.aimAt.set(Vector3f.ZERO);
            //this.aimAtWorld.set(this.position);
            //this.targetFacing.set(Vector3f.UNIT_Z);
            //this.targetUpside.set(Vector3f.UNIT_Y);
            //this.targetVelocity.set(Vector3f.ZERO);
        //} else {
            // WE HAVE A PHYSICS OBJECT SO WE CAN CALCULATE AIMAT
            //this.targetFacing.set(this.targetPhysics.getFacing());
            //this.targetUpside.set(this.targetPhysics.getUpVector());
            //this.targetVelocity.set(this.targetPhysics.getLinearVelocity());

        //}

        // CALCULATE OWN ANGLE TO TARGET (ASPECT)
        //if ( this.ownFlight == null ) {
            // IF WE HAVE NO FLIGHTCONTROL YET WE RETRIEVE IT AND SET THE ANGLE TO -1
            //this.ownFlight = source.getControl(PhysicsControl.class);
            //this.aimAtAngle = -1f;
        //} else {
            // WE CALCULATE TARGETANGLE FROM FLIGHTCONTROLS FACING AND TARGETAIM
            
            // CALCULATE DISTANCE OF CURRENT FACING AND AIM-AT POSITION
            Vector3f crosshairVector = new Vector3f(this.object.getFacing());
            crosshairVector.multLocal(this.aimAtWorld.subtract(source.getPosition()).length());
            crosshairVector.addLocal(source.getPosition());
            crosshairVector.subtractLocal(this.aimAtWorld);
            this.aimAtTolerance = crosshairVector.length() - this.object.getSize();
        //}
    }
    */
            
    /* GETTERS */
    public ObjectInfoControl getObject() {
        return this.object;
    }
    
    public float getDistance(ObjectInfoControl source) {
        return source.distanceTo(this.object);
    }
    
    public Vector3f getDirection(ObjectInfoControl source) {
        return this.object.getPosition().subtract(source.getPosition());
    }
        
    public float getAimAtAngle(ObjectInfoControl source, AbstractWeaponControl weapon) {
        return this.object.getFacing().normalize().angleBetween(getAimAtWorld(source, weapon).subtract(source.getPosition()).normalize());
    }
        
    public Vector3f getAimAt(ObjectInfoControl source, AbstractWeaponControl weapon) {
        if ( weapon == null ) {
            return Vector3f.ZERO;
        }
        
        return this.object.getLinearVelocity().mult(getDistance(source) / weapon.getSpeed());
    }
    
    public Vector3f getAimAtWorld(ObjectInfoControl source, AbstractWeaponControl weapon) {
        return this.object.getPosition().add(getAimAt(source, weapon));
    }
    
    public float getAimAtTolerance(ObjectInfoControl source, AbstractWeaponControl weapon) {
        Vector3f aimAtWorld = this.getAimAtWorld(source, weapon);
        
        Vector3f crosshairVector = source.getFacing().normalize().mult(aimAtWorld.subtract(source.getPosition()).length());
        crosshairVector.addLocal(source.getPosition());
        crosshairVector.subtractLocal(aimAtWorld);
         
        
        
        /*
        Vector3f crosshairVector = new Vector3f(this.object.getFacing());
        crosshairVector.multLocal(aimAtWorld.subtract(source.getPosition()).length());
        crosshairVector.addLocal(source.getPosition());
        crosshairVector.subtractLocal(aimAtWorld);
        */
        return crosshairVector.length() - this.object.getSize();
    }
    
    public int getFOF(ObjectInfoControl source) {
        if (source == null || this.object.getFaction() == null || source.getFaction() == null) {
            return FOF_NEUTRAL;
        } else if ( this.object.getFaction().getId() == source.getFaction().getId() ) {
            return FOF_FRIEND;
        } else {
            return FOF_FOE;
        }
    }
        
    public int shotsForKill(ObjectInfoControl source, AbstractWeaponControl weapon) {
        DamageControl damageControl = this.object.getObjectControl(DamageControl.class);
        if (damageControl == null) {
            return 0;
        }
        
        float remainingHitpoints = damageControl.getHullHP() * damageControl.getHull();
        
        ShieldControl shieldControl = this.object.getObjectControl(ShieldControl.class);
        if (shieldControl != null) {
            remainingHitpoints += shieldControl.getShieldStrength() * shieldControl.getShield(getDirection(source));
        }
        
        SensorControl sensorControl = this.object.getObjectControl(SensorControl.class);
        if (sensorControl != null) {
            AbstractWeaponControl[] missiles = sensorControl.getMissiles();
            if (missiles.length > 0) {
                remainingHitpoints -= missiles[0].getDamage() * missiles.length;
            }
        }
                
        if (remainingHitpoints <= 0) {
            return 0;
        } else {
            return (int)Math.ceil(remainingHitpoints / weapon.getDamage());
        }
    }
    
    public boolean equals(TargetInformation otherTarget) {
        return otherTarget != null && otherTarget.getObject().getId().equals(this.object.getId());
    }
    
    /* EXTERNAL LOOKUPS */
    /*
    public float getAimAtAngle(Spatial source) {
        return source.getWorldRotation().mult(Vector3f.UNIT_Z).normalizeLocal().angleBetween(this.aimAtWorld.subtract(source.getWorldTranslation()).normalize());
    }
    
    public float getAimAtTolerance(Spatial source) {
        Vector3f crosshairVector = new Vector3f(source.getWorldRotation().mult(Vector3f.UNIT_Z));
        crosshairVector.multLocal(this.aimAtWorld.subtract(source.getWorldTranslation()).length());
        crosshairVector.addLocal(source.getWorldTranslation());
        crosshairVector.subtractLocal(this.aimAtWorld);
        return crosshairVector.length() - this.object.getSize();
    }
    */
    
}
