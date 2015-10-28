/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.network.NetworkClient;
import java.util.ArrayList;

/**
 *
 * @author rampage
 */
@Serializable
public final class ObjectSynchronizationMessage extends ServerMessage {

    private String id;
       
    private ArrayList<ObjectStateData> stateList = new ArrayList<ObjectStateData>();
    
    public ObjectSynchronizationMessage() {}
    
    public ObjectSynchronizationMessage(ObjectInfoControl object) {
        this.id = object.getId();
        addState(ObjectStateData.PROPERTY_ALIVE, object);
        addState(ObjectStateData.PROPERTY_POSITION, object);
        addState(ObjectStateData.PROPERTY_ROTATION, object);
        addState(ObjectStateData.PROPERTY_LINEARVELOCITY, object);
        addState(ObjectStateData.PROPERTY_ANGULARVELOCITY, object);
        addState(ObjectStateData.PROPERTY_DAMAGE, object);
        addState(ObjectStateData.PROPERTY_SHIELDFRONT, object);
        addState(ObjectStateData.PROPERTY_SHIELDREAR, object);
        
    }
    
    public ObjectSynchronizationMessage(String objectId) {
        this.id = objectId;
    }
    
    public void addState(String key, ObjectInfoControl object) {
        if (!object.getId().equals(this.id)) {
            throw new IllegalArgumentException("Cannot transfer state " + key + " from object " + this.id + " to object " + object.getId() + "!");
        }
        
        this.stateList.add(new ObjectStateData(key, object));
    }
    
    @Override
    public void messageReceived(NetworkClient client) {
        SpaceAppState space = client.getGame().getStateManager().getState(SpaceAppState.class);
        if (space == null) {
            return;
        }
        
        AbstractMission mission = space.getMission();
        if (mission == null) {
            return;
        }
        
        apply(mission);
    }

    @Override
    public boolean mustBeReliable() {
        return false;
    }
    
    public int getNumChanges() {
        return this.stateList.size();
    }
    
    private void apply(AbstractMission mission) {
        ObjectInfoControl object = mission.getObject(this.id);
        if (object == null) {
            return;
        }

        for (ObjectStateData state : this.stateList) {
            state.apply(object);
        }
    }
    
    public ObjectSynchronizationMessage getDelta(ObjectInfoControl object) {        
        if (!object.getId().equals(this.id)) {
            throw new IllegalArgumentException("Cannot get delta from message " + this.id + " and object " + object.getId() + "!");
        }
        
        ObjectSynchronizationMessage deltaMessage = new ObjectSynchronizationMessage(this.id);
        for (ObjectStateData data : this.stateList) {
            if (data.hasValueChanged(object)) {
                deltaMessage.addState(data.getKey(), object);
                data.updateValue(object);
            }
        }
        
        if (deltaMessage.getNumChanges() > 0) {
            return deltaMessage;
        } else {
            return null;
        }
    }
    
}
