/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network;

import de.jlab.spacefight.player.Player;

/**
 *
 * @author rampage
 */
public interface ChatListener {
    
    public void onChatMessage(Player player, String message);
    
}
