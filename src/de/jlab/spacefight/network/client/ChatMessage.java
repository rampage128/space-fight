/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.client;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.network.ChatListener;
import de.jlab.spacefight.network.NetworkServer;
import de.jlab.spacefight.player.Player;
import de.jlab.spacefight.ui.UIAppState;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author rampage
 */
@Serializable
public class ChatMessage extends ClientMessage {

    private String message = "";
    
    public ChatMessage() {}
    
    public ChatMessage(String message) {
        this.message = message;
        displayMessage(Game.get().getPlayer());
    }
    
    @Override
    public void messageReceived(NetworkServer server, HostedConnection source) {
        displayMessage(server.getPlayer(source));
    }

    private void displayMessage(Player player) {
        Screen screen = UIAppState.currentScreen();
        if (screen == null) {
            return;
        }
        
        ScreenController controller = screen.getScreenController();
        if (controller == null || !(controller instanceof ChatListener)) {
            return;
        }
        
        ((ChatListener)controller).onChatMessage(player, this.message);
    }
    
    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
