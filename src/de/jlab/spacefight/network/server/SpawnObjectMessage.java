/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.network.NetworkClient;

/**
 *
 * @author rampage
 */
@Serializable
public class SpawnObjectMessage extends ServerMessage {

    private String objectId;
    private boolean initial = false;
    
    public SpawnObjectMessage() {}
    
    public SpawnObjectMessage(String objectId, boolean initial) {
        this.objectId = objectId;
        this.initial = initial;
    }
    
    @Override
    public void messageReceived(NetworkClient client) {
        SpaceAppState space = client.getGame().getStateManager().getState(SpaceAppState.class);
        if (space == null || space.getMission() == null) {
            return;
        }

        ObjectInfoControl object = space.getMission().getObject(this.objectId);
        if (object == null) {
            return;
        }
        
        object.spawnObject(space.getMission(), this.initial);
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
