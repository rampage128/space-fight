/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.client;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.network.NetworkServer;
import de.jlab.spacefight.network.server.WeaponChangeBroadcastMessage;
import de.jlab.spacefight.player.Player;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;

/**
 *
 * @author rampage
 */
@Serializable
public class WeaponChangeMessage extends ClientMessage {
    
    private String objectId;
    private int slotNumber;
    private String weaponName;
    
    public WeaponChangeMessage() {}
    
    public WeaponChangeMessage(ObjectInfoControl object, int slotNumber, String weaponName) {
        this.objectId   = object.getId();
        this.slotNumber = slotNumber;
        this.weaponName = weaponName;
    }

    @Override
    public void messageReceived(NetworkServer server, HostedConnection source) {
        SpaceAppState space = server.getGame().getStateManager().getState(SpaceAppState.class);
        if (space == null) {
            return;
        }
        
        if (space.getMission() == null) {
            return;
        }
        
        ObjectInfoControl object = space.getMission().getObject(this.objectId);
        if (object == null) {
            return;
        }
        
        Player player = server.getPlayer(source);
        if (player == null) {
            return;
        }
        
        ObjectInfoControl playerClient = player.getClient();
        if (playerClient == null) {
            return;
        }
        
        if (!playerClient.getId().equalsIgnoreCase(object.getId())) {
            return;
        }
        
        WeaponSystemControl weapons = object.getObjectControl(WeaponSystemControl.class);
        weapons.changeWeapon(this.slotNumber, this.weaponName);
        server.broadcast(new WeaponChangeBroadcastMessage(object, this.slotNumber, this.weaponName));
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
