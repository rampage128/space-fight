/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.client;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.network.NetworkServer;

/**
 *
 * @author rampage
 */
@Serializable
public class MapLoadedMessage extends ClientMessage {

    public MapLoadedMessage() {}
    
    @Override
    public void messageReceived(NetworkServer server, HostedConnection source) {
        server.sendSynch(source);
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
