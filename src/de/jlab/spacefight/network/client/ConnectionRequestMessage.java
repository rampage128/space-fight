/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.client;

import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.network.NetworkPlayer;
import de.jlab.spacefight.network.NetworkServer;
import de.jlab.spacefight.network.server.ChatBroadcastMessage;
import de.jlab.spacefight.network.server.ConnectionResponseMessage;
import de.jlab.spacefight.network.server.MapChangeMessage;
import de.jlab.spacefight.player.Player;

/**
 *
 * @author rampage
 */
@Serializable
public class ConnectionRequestMessage extends ClientMessage {
 
    private NetworkPlayer player;
    
    public ConnectionRequestMessage() {}
    
    public ConnectionRequestMessage(Player player) {
        this.player = new NetworkPlayer(player);
    }

    @Override
    public void messageReceived(NetworkServer server, HostedConnection source) {
        server.connectPlayer(source, player);
        server.broadcast(new ChatBroadcastMessage(null, server.getPlayer(source).getNickname() + " has joined!"));
        
        SpaceAppState space = server.getGame().getStateManager().getState(SpaceAppState.class);
        if (space != null && space.getMission() != null && space.isPlayable()) {
            MapChangeMessage message = new MapChangeMessage(space.getMission().getConfig());
            server.send(message, Filters.equalTo(source));
        } else {
            server.send(new ConnectionResponseMessage(false), Filters.equalTo(source));
        }
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
