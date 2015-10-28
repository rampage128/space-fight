/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.level.PseudoRandomField;
import de.jlab.spacefight.network.NetworkClient;

/**
 *
 * @author rampage
 */
@Serializable
public class FieldSynchronizationMessage extends ServerMessage {

    private String objectId;
    private long seed;
    
    public FieldSynchronizationMessage() {}
    
    public FieldSynchronizationMessage(String objectId, long seed) {
        this.objectId   = objectId;
        this.seed       = seed;
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
        
        PseudoRandomField field = object.getSpatial().getControl(PseudoRandomField.class);
        if (field != null) {
            field.setSeed(this.seed);
            field.createField(space);
        }
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
