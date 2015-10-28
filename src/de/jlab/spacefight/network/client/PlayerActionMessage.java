/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.client;

import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.network.ClientActionData;
import de.jlab.spacefight.network.NetworkServer;
import de.jlab.spacefight.network.server.PlayerActionBroadcastMessage;

/**
 *
 * @author rampage
 */
@Serializable
public class PlayerActionMessage extends ClientMessage {
    
    public ClientActionData data;
    
    public PlayerActionMessage() {}
    
    public PlayerActionMessage(ObjectInfoControl clientObject) {
        this.data = new ClientActionData(clientObject);
    }
    
    @Override
    public void messageReceived(NetworkServer server, HostedConnection source) {
        SpaceAppState space = server.getGame().getStateManager().getState(SpaceAppState.class);
        if (space == null) {
            return;
        }
        
        if (space.getMission() == null) {
            return;
        }
        
        ObjectInfoControl object = space.getMission().getObject(this.data.id);
        if (object == null) {
            return;
        }
        
        this.data.apply(object);
        
        PlayerActionBroadcastMessage actionBroadcast = new PlayerActionBroadcastMessage(this);
        server.send(actionBroadcast, Filters.notEqualTo(source));
    }

    @Override
    public boolean mustBeReliable() {
        return false;
    }
    
}
