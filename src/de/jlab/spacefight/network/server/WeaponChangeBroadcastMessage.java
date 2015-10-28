/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.math.ColorRGBA;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.network.NetworkClient;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;

/**
 *
 * @author rampage
 */
@Serializable
public class WeaponChangeBroadcastMessage extends ServerMessage {

    private String objectId;
    private int slotNumber;
    private String weaponName;
    
    public WeaponChangeBroadcastMessage() {}
    
    public WeaponChangeBroadcastMessage(ObjectInfoControl object, int slotNumber, String weaponName) {
        this.objectId   = object.getId();
        this.slotNumber = slotNumber;
        this.weaponName = weaponName;
    }
    
    @Override
    public void messageReceived(NetworkClient client) {
        SpaceAppState space = client.getGame().getStateManager().getState(SpaceAppState.class);
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
        
        object.getObjectControl(WeaponSystemControl.class).changeWeapon(this.slotNumber, this.weaponName);
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
