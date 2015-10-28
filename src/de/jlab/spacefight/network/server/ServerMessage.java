/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import de.jlab.spacefight.network.NetworkClient;
import de.jlab.spacefight.network.NetworkMessage;


/**
 *
 * @author rampage
 */
public abstract class ServerMessage extends NetworkMessage {
    
    public abstract void messageReceived(NetworkClient client);
    public abstract boolean mustBeReliable();
    
}
