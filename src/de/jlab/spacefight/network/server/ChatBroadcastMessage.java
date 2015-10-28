/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.network.ChatListener;
import de.jlab.spacefight.network.NetworkClient;
import de.jlab.spacefight.network.NetworkPlayer;
import de.jlab.spacefight.player.Player;
import de.jlab.spacefight.ui.UIAppState;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author rampage
 */
@Serializable
public class ChatBroadcastMessage extends ServerMessage {

    private NetworkPlayer player = null;
    private String message = "";
    
    public ChatBroadcastMessage() {}
    
    public ChatBroadcastMessage(Player player, String message) {
        this.message = message;
        if (player != null) {
            this.player = new NetworkPlayer(player);
            if (player.isLocal()) {
                displayMessage();
            }
        } else {
            this.player = new NetworkPlayer("Server");
            displayMessage();
        }
    }
    
    @Override
    public void messageReceived(NetworkClient client) {
        displayMessage();
    }
    
    private void displayMessage() {
        Screen screen = UIAppState.currentScreen();
        if (screen == null) {
            return;
        }
        
        ScreenController controller = screen.getScreenController();
        if (controller == null || !(controller instanceof ChatListener)) {
            return;
        }
        
        ((ChatListener)controller).onChatMessage(this.player.getPlayer(), this.message);
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
