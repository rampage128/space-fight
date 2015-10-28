/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network;

import com.jme3.math.Quaternion;
import com.jme3.network.serializing.Serializer;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.network.client.ChatMessage;
import de.jlab.spacefight.network.client.ConnectionRequestMessage;
import de.jlab.spacefight.network.client.FieldSynchronizationRequest;
import de.jlab.spacefight.network.client.MapLoadedMessage;
import de.jlab.spacefight.network.client.PlayerActionMessage;
import de.jlab.spacefight.network.client.ShipSelectionMessage;
import de.jlab.spacefight.network.client.WeaponChangeMessage;
import de.jlab.spacefight.network.server.ChatBroadcastMessage;
import de.jlab.spacefight.network.server.ConnectionResponseMessage;
import de.jlab.spacefight.network.server.FieldSynchronizationMessage;
import de.jlab.spacefight.network.server.KillObjectMessage;
import de.jlab.spacefight.network.server.MapChangeMessage;
import de.jlab.spacefight.network.server.ObjectStateData;
import de.jlab.spacefight.network.server.ObjectSynchronizationMessage;
import de.jlab.spacefight.network.server.PlayerActionBroadcastMessage;
import de.jlab.spacefight.network.server.ShipAssignmentMessage;
import de.jlab.spacefight.network.server.SpawnObjectMessage;
import de.jlab.spacefight.network.server.WeaponChangeBroadcastMessage;

/**
 *
 * @author rampage
 */
public abstract class AbstractNetworkInstance {
    
    private Game game;
    
    private int updaterate = 4;
    private float updatetimer = 0f;
        
    public void setUpdateRate(int updaterate) {
        if (updaterate < 1 || updaterate > 100) {
            throw new IllegalArgumentException("updaterate must be between 1-100! Found: " + updaterate);
        }
        this.updaterate = updaterate;
    }
    
    public AbstractNetworkInstance(Game game) {
        this.game = game;                         
        registerMessages();
    }
    
    public Game getGame() {
        return this.game;
    }
    
    public final void update(float tpf) {
        this.updatetimer += tpf;
        if (this.updatetimer >= (1 / this.updaterate)) {
            this.onUpdate(tpf);
        }
    }
    
    public abstract void onUpdate(float tpf);
    public abstract void init(String host, int port);
    public abstract void close();
    public abstract boolean isRunning();
 
    private void registerMessages() {
        // OTHER CLASSES
        Serializer.registerClass(NetworkPlayer.class, true);
        Serializer.registerClass(SimpleConfig.class, true);
        Serializer.registerClass(ClientActionData.class, true);
        Serializer.registerClass(ObjectStateData.class, true);
        Serializer.registerClass(Quaternion.class, new QuaternionSerializer());
        
        // SERVER MESSAGES
        Serializer.registerClass(MapChangeMessage.class, true);
        Serializer.registerClass(ObjectSynchronizationMessage.class, true);
        Serializer.registerClass(ShipAssignmentMessage.class, true);
        Serializer.registerClass(PlayerActionBroadcastMessage.class, true);
        Serializer.registerClass(SpawnObjectMessage.class, true);
        Serializer.registerClass(KillObjectMessage.class, true);
        Serializer.registerClass(FieldSynchronizationMessage.class, true);
        Serializer.registerClass(ConnectionResponseMessage.class, true);
        Serializer.registerClass(ChatBroadcastMessage.class, true);
        Serializer.registerClass(WeaponChangeBroadcastMessage.class, true);
        
        // CLIENT MESSAGES
        Serializer.registerClass(ConnectionRequestMessage.class, true);
        Serializer.registerClass(MapLoadedMessage.class, true);
        Serializer.registerClass(ShipSelectionMessage.class, true);
        Serializer.registerClass(PlayerActionMessage.class, true);
        Serializer.registerClass(FieldSynchronizationRequest.class, true);        
        Serializer.registerClass(ChatMessage.class, true);
        Serializer.registerClass(WeaponChangeMessage.class, true);
    }
    
}
