/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.client;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.network.NetworkPlayer;
import de.jlab.spacefight.network.NetworkServer;
import de.jlab.spacefight.network.server.ShipAssignmentMessage;
import de.jlab.spacefight.player.Player;
import de.jlab.spacefight.player.PlayerControl;

/**
 *
 * @author rampage
 */
@Serializable
public class ShipSelectionMessage extends ClientMessage {

    private String id = null;
    private NetworkPlayer networkPlayer;
    
    public ShipSelectionMessage() {}
    
    public ShipSelectionMessage(ObjectInfoControl object, Player player) {
        this.id = object.getId();
        this.networkPlayer = new NetworkPlayer(player);
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
        
        ObjectInfoControl object = space.getMission().getObject(id);
        if (object == null) {
            return;
        }
        if (object.getSpatial().getControl(PlayerControl.class) != null) {
            return;
        }
        
        /*
        AIShipControl aiControl = object.getSpatial().getControl(AIShipControl.class);
        object.getSpatial().removeControl(aiControl);
        
        NetworkClientControl netControl = object.getSpatial().getControl(NetworkClientControl.class);
        if (netControl == null) {
            netControl = new NetworkClientControl(space.getMission());
            object.getSpatial().addControl(netControl);
        }
        */
        
        Player player = server.getPlayer(source);
        player.setClient(object);        
        //Player player = this.networkPlayer.getPlayer();
        //object.setPlayer(player);
        
        ShipAssignmentMessage shipAssignment = new ShipAssignmentMessage(object, source.getId(), player);
        server.broadcast(shipAssignment);
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
