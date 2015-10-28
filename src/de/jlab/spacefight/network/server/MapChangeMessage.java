/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.network.NetworkClient;
import de.jlab.spacefight.ui.UIAppState;
import java.util.HashMap;



/**
 *
 * @author rampage
 */
@Serializable()
public class MapChangeMessage extends ServerMessage {

    private HashMap config;
    
    public MapChangeMessage() {}
    
    public MapChangeMessage(SimpleConfig config) {
        this.config = new HashMap<String, String>();
        config.copy(this.config);
    }

    @Override
    public void messageReceived(NetworkClient client) {
        SimpleConfig config = new SimpleConfig();
        config.putAll(this.config);
        UIAppState.gotoScreen("loading", Game.get());
        SpaceAppState.startGame(config, client.getGame());
        //client.getGame().getUI().startSpace(config);
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
