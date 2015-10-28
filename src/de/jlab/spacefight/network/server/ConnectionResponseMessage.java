/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.network.NetworkClient;
import de.jlab.spacefight.ui.UIAppState;

/**
 *
 * @author rampage
 */
@Serializable
public class ConnectionResponseMessage extends ServerMessage {

    private boolean mapChange = false;
    
    public ConnectionResponseMessage() {}
    
    public ConnectionResponseMessage(boolean mapChange) {
        this.mapChange = mapChange;
    }
    
    @Override
    public void messageReceived(NetworkClient client) {
        if (!mapChange) {
            UIAppState.gotoScreen("multiplayerlobby", Game.get());
        }
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
