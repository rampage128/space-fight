/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.client;

import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.level.PseudoRandomField;
import de.jlab.spacefight.network.NetworkServer;
import de.jlab.spacefight.network.server.FieldSynchronizationMessage;

/**
 *
 * @author rampage
 */
@Serializable
public class FieldSynchronizationRequest extends ClientMessage {

    private String objectId;
    
    public FieldSynchronizationRequest() {}
    
    public FieldSynchronizationRequest(String objectId) {
        this.objectId = objectId;
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
        
        PseudoRandomField field = object.getSpatial().getControl(PseudoRandomField.class);
        if (field != null) {
            FieldSynchronizationMessage message = new FieldSynchronizationMessage(this.objectId, field.getSeed());
            server.send(message, Filters.equalTo(source));
        }        
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
