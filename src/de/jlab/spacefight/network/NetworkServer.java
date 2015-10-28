/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network;

import com.jme3.network.ConnectionListener;
import com.jme3.network.Filter;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.network.client.ClientMessage;
import de.jlab.spacefight.network.server.ChatBroadcastMessage;
import de.jlab.spacefight.network.server.ObjectSynchronizationMessage;
import de.jlab.spacefight.network.server.PlayerActionBroadcastMessage;
import de.jlab.spacefight.network.server.ServerMessage;
import de.jlab.spacefight.player.Player;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rampage
 */
public class NetworkServer extends AbstractNetworkInstance implements ConnectionListener, MessageListener<HostedConnection> {
    
    public float synchtime = 10f;
    public float timer = 0f;
    
    private Server server;
    
    private HashMap<HostedConnection, Player> playerMap = new HashMap<HostedConnection, Player>();
    //private HashMap<String, ObjectInfoControl> objectMap = new HashMap<String, ObjectInfoControl>();
    private HashMap<ObjectInfoControl, ObjectSynchronizationMessage> stateMap = new HashMap<ObjectInfoControl, ObjectSynchronizationMessage>();
    
    public NetworkServer(Game game) {
        super(game);
    }
    
    public void init(String host, int port) {
        try {
            this.server = Network.createServer(port);
            this.server.addConnectionListener(this);
            this.server.addMessageListener(this);
            this.server.start();
        } catch (IOException ex) {
            Logger.getLogger(NetworkServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close() {
        server.close();
        this.server.removeConnectionListener(this);
        this.server.removeMessageListener(this);
    }

    public void connectionAdded(Server server, HostedConnection conn) {
        
    }

    public void connectionRemoved(Server server, HostedConnection conn) {
        server.broadcast(new ChatBroadcastMessage(null, getPlayer(conn).getNickname() + " has left!"));
        this.playerMap.remove(conn);
    }

    public void connectPlayer(HostedConnection conn, NetworkPlayer player) {
        this.playerMap.put(conn, player.getPlayer());
    }
    
    public Player getPlayer(HostedConnection conn) {
        return this.playerMap.get(conn);
    }

    public void messageReceived(final HostedConnection source, final Message m) {
        //Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, "Received Message " + m.getClass().getSimpleName());
        if (m instanceof ClientMessage) {
            getGame().enqueue(new Callable() {
                public Object call() throws Exception {
                    ((ClientMessage)m).messageReceived(NetworkServer.this, source);
                    return null;
                }
            });
        }
    }
    
    public void send(ServerMessage message, Filter<? super HostedConnection> filter) {
        //Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, "Sending " + ((message.mustBeReliable()) ? "reliable " : "") + "message " + message.getClass().getSimpleName() + " to " + filter);
        message.setReliable(message.mustBeReliable());
        this.server.broadcast(filter, message);
    }
    
    public void broadcast(ServerMessage message) {
        //Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, "Broadcasting " + ((message.mustBeReliable()) ? "reliable " : "") + "message: " + message.getClass().getSimpleName());
        message.setReliable(message.mustBeReliable());
        this.server.broadcast(message);
    }

    @Override
    public boolean isRunning() {
        return this.server.isRunning();
    }
    
    public void sendSynch(HostedConnection connection) {
        SpaceAppState space = getGame().getStateManager().getState(SpaceAppState.class);
        if (space == null || space.getMission() == null || !space.isPlayable()) {
            return;
        }
        
        System.out.println("Sending full synch to " + connection.getAddress());
        
        ObjectInfoControl[] objects = space.getMission().getObjects();        
        for (ObjectInfoControl object : objects) {
            ObjectSynchronizationMessage message = new ObjectSynchronizationMessage(object);
            send(message, Filters.equalTo(connection));
        }
    }
    
    @Override
    public void onUpdate(float tpf) {
        SpaceAppState space = getGame().getStateManager().getState(SpaceAppState.class);
        if (space == null || space.getMission() == null || !space.isPlayable()) {
            return;
        }
        
        if (space.getPlayer().getClient() != null) {
            PlayerActionBroadcastMessage message = new PlayerActionBroadcastMessage(space.getPlayer().getClient());
            this.broadcast(message);
        }
        
        AbstractMission mission = space.getMission();
        if (mission == null) {
            return;
        }
        
        this.timer += tpf;
        if (this.timer >= this.synchtime) {
            int deltas = 0;
            ObjectInfoControl[] objects = space.getMission().getObjects();        
            for (ObjectInfoControl object : objects) {
                ObjectSynchronizationMessage message = this.stateMap.get(object);
                ObjectSynchronizationMessage deltaMessage = null;
                if (message == null) {
                    message = new ObjectSynchronizationMessage(object);
                    deltaMessage = message;
                } else {
                    deltaMessage = message.getDelta(object);
                }
                if (deltaMessage != null) {
                    broadcast(deltaMessage);
                    this.stateMap.put(object, message);
                    deltas++;
                }
            }
            //System.out.println("Sending " + deltas + " deltas!");
            this.timer = 0f;
        }
    }
    
}
