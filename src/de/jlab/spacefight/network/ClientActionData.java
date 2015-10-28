/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;

/**
 *
 * @author rampage
 */
@Serializable
public class ClientActionData {
    
    public String id = null;
    
    public float throttle = 0f;
    public float aileron = 0f;
    public float rudder = 0f;
    public float elevator = 0f;
    public float strafe = 0f;
    public float lift = 0f;
    public boolean cruise = false;
    public boolean glide = false;
    
    public int primaryMode = WeaponSystemControl.MODE_SINGLE;
    public boolean primaryFire = false;
    public boolean secondaryFire = false;
    
    public Vector3f position = new Vector3f();
    public Quaternion rotation = new Quaternion();
    public Vector3f linearvelocity = new Vector3f();
    public Vector3f angularvelocity = new Vector3f();
    
    public String targetId = null;
    
    public ClientActionData() {}
    
    public ClientActionData(ObjectInfoControl clientObject) {
        FlightControl flight = clientObject.getObjectControl(FlightControl.class);
        this.id = clientObject.getId();
        this.position.set(clientObject.getPosition());
        this.rotation.set(clientObject.getRotation());
        this.linearvelocity.set(clientObject.getLinearVelocity());
        this.angularvelocity.set(clientObject.getAngularVelocity());
        if (flight != null) {
            this.throttle = flight.getThrottle();
            this.aileron = flight.getAileron();
            this.rudder = flight.getRudder();
            this.elevator = flight.getElevator();
            this.strafe = flight.getStrafe();
            this.lift = flight.getLift();
            this.cruise = flight.getCruise();
            this.glide = flight.getGlide();
        }
        WeaponSystemControl weapons = clientObject.getObjectControl(WeaponSystemControl.class);
        if (weapons != null) {
            this.primaryMode = weapons.getPrimaryMode();
            this.primaryFire = weapons.isPrimaryFiring();
            this.secondaryFire = weapons.isSecondaryFiring();
            if (weapons.getTarget() != null) {
                this.targetId = weapons.getTarget().getObject().getId();
            }
        }
    }
    
    public ClientActionData(ClientActionData originalData) {
        this.id = originalData.id;
        this.throttle = originalData.throttle;
        this.aileron = originalData.aileron;
        this.rudder = originalData.rudder;
        this.elevator = originalData.elevator;
        this.strafe = originalData.strafe;
        this.lift = originalData.lift;
        this.cruise = originalData.cruise;
        this.glide = originalData.glide;
        this.position.set(originalData.position);
        this.rotation.set(originalData.rotation);
        this.linearvelocity.set(originalData.linearvelocity);
        this.angularvelocity.set(originalData.angularvelocity);
        
        this.primaryFire = originalData.primaryFire;
        this.secondaryFire = originalData.secondaryFire;
    }
    
    public void apply(ObjectInfoControl object) {
        object.setPosition(this.position);
        object.setRotation(this.rotation);
        object.setLinearVelocity(this.linearvelocity);
        object.setAngularVelocity(this.angularvelocity);
        FlightControl flight = object.getObjectControl(FlightControl.class);
        if (flight != null) {
            flight.setThrottle(this.throttle);
            flight.setAileron(this.aileron);
            flight.setRudder(this.rudder);
            flight.setElevator(this.elevator);
            flight.setStrafe(this.strafe);
            flight.setLift(this.lift);
            flight.setCruise(this.cruise);
            flight.setGlide(this.glide);
        }
        WeaponSystemControl weapons = object.getObjectControl(WeaponSystemControl.class);
        if (weapons != null) {
            weapons.setPrimaryMode(this.primaryMode);
            if (this.primaryFire) weapons.firePrimary();
            if (this.secondaryFire) weapons.fireSecondary();
        }
        SensorControl sensors = object.getObjectControl(SensorControl.class);
        if (sensors != null) {
            sensors.target(this.targetId);
        }
    }
    
}
