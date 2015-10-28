/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.network.ClientActionData;
import de.jlab.spacefight.network.NetworkClient;
import de.jlab.spacefight.network.client.PlayerActionMessage;

/**
 *
 * @author rampage
 */
@Serializable
public class PlayerActionBroadcastMessage extends ServerMessage {

    private ClientActionData data;
    
    public PlayerActionBroadcastMessage() {}
    
    public PlayerActionBroadcastMessage(PlayerActionMessage originalMessage) {
        this.data = new ClientActionData(originalMessage.data);
    }
    
    public PlayerActionBroadcastMessage(ObjectInfoControl clientObject) {
        this.data = new ClientActionData(clientObject);
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
        
        ObjectInfoControl object = mission.getObject(this.data.id);
        if (object == null) {
            return;
        }
        
        this.data.apply(object);
    }

    @Override
    public boolean mustBeReliable() {
        return false;
    }
    
}
