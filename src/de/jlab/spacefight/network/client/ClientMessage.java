/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.client;

import com.jme3.network.HostedConnection;
import de.jlab.spacefight.network.NetworkMessage;
import de.jlab.spacefight.network.NetworkServer;

/**
 *
 * @author rampage
 */
public abstract class ClientMessage extends NetworkMessage {

    public abstract void messageReceived(NetworkServer server, HostedConnection source);
    public abstract boolean mustBeReliable();
    
}
