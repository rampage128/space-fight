/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.structures;

import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.network.NetworkAppState;

/**
 *
 * @author rampage
 */
public class Respawn {
    
    private Flight flight;
    private ObjectInfoControl object;
    private boolean initial = false;
    
    private float respawnTime = 5f;
    
    public Respawn(ObjectInfoControl object, AbstractMission mission, float respawnTime, boolean initial) {
        this.flight = object.getFlight();
        this.object = object;
        this.respawnTime = respawnTime;
        this.initial = initial;
    }
    
    public boolean checkRespawn(float tpf, AbstractMission mission) {
        this.respawnTime -= tpf;
        if ( this.respawnTime <= 0 && (this.object.getFaction() == null || this.object.getFaction().getAllowRespawn()) ) { // flight != null && (flight.getFaction() == null || flight.getFaction().getAllowRespawn()
            return this.object.getSpawn().canSpawn(mission);
        }
        return false;
    }
    
    public boolean spawn(AbstractMission mission) {        
        if (this.object != null && (NetworkAppState.spawnObject(mission.getSpace().getGame(), this.object, this.initial) || this.initial)) {
            return this.object.spawnObject(mission, this.initial);
        }
        return false;
    }
    
    public ObjectInfoControl getObject() {
        return this.object;
    }
    
}
