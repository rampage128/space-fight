/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network;

import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.player.Player;

/**
 *
 * @author rampage
 */
@Serializable
public class NetworkPlayer {
    
    private String nickname = "Player";
    
    public NetworkPlayer() {}

    public NetworkPlayer(String nickname) {
        this.nickname = nickname;
    }
    
    public NetworkPlayer(Player player) {
        this.nickname = player.getNickname();
    }
        
    public Player getPlayer() {
        Player player = new Player(false);
        player.setNickname(nickname);
        return player;
    }
    
}
