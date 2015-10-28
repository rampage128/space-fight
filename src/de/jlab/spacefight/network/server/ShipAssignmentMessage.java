/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.network.NetworkClient;
import de.jlab.spacefight.network.NetworkPlayer;
import de.jlab.spacefight.player.Player;



/**
 *
 * @author rampage
 */
@Serializable
public class ShipAssignmentMessage extends ServerMessage {
    
    private String objectId = null;
    private int connectionId = -1;
    private NetworkPlayer networkPlayer;
    
    public ShipAssignmentMessage() {}
    
    public ShipAssignmentMessage(ObjectInfoControl object, int connectionId, Player player) {
        this.objectId = object.getId();
        this.connectionId = connectionId;
        this.networkPlayer = new NetworkPlayer(player);
    }
    
    @Override
    public void messageReceived(NetworkClient client) {
        SpaceAppState space = client.getGame().getStateManager().getState(SpaceAppState.class);
        if (space == null) {
            return;
        }
                
        if (space.getMission() == null) {
            return;
        }
        
        if (this.objectId == null && client.getConnectionId() == this.connectionId) {
            space.getPlayer().setClient(null);
        }
        
        ObjectInfoControl object = space.getMission().getObject(this.objectId);
        if (object == null) {
            return;
        }
        
        if (client.getConnectionId() == this.connectionId) {
            space.getPlayer().setClient(object);
        } else {
            //AIShipControl aiControl = object.getSpatial().getControl(AIShipControl.class);
            //object.getSpatial().removeControl(aiControl);
            object.setPlayer(this.networkPlayer.getPlayer());
            //object.updateClientControl(true);
        }
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
