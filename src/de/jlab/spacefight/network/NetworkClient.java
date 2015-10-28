/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network;

import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.network.client.ClientMessage;
import de.jlab.spacefight.network.client.ConnectionRequestMessage;
import de.jlab.spacefight.network.client.PlayerActionMessage;
import de.jlab.spacefight.network.server.ServerMessage;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rampage
 */
public class NetworkClient extends AbstractNetworkInstance implements ClientStateListener, MessageListener<Client> {
    
   private Client client;
   
   public NetworkClient(Game game) {
       super(game);
   }
   
   public void init(String host, int port) {
        try {
            this.client = Network.connectToServer(host, port);
            this.client.addClientStateListener(this);
            this.client.addMessageListener(this);
            this.client.start();
        } catch (IOException ex) {
            NetworkAppState.leave(Game.get());
            Logger.getLogger(NetworkClient.class.getName()).log(Level.SEVERE, null, ex);
        }
   }

    @Override
    public void close() {
        this.client.close();
        this.client.removeClientStateListener(this);
        this.client.removeMessageListener(this);
    }

    public void clientConnected(Client c) {
        this.send(new ConnectionRequestMessage(getGame().getPlayer()));
        System.out.println("Connected!");
    }

    public void clientDisconnected(Client c, DisconnectInfo info) {
        String message = "disconnect";
        if (info != null) {
            message = info.reason;
        }
        System.out.println("Disconnected: " + message);
    }

    public void messageReceived(Client source, final Message m) {
        if (m instanceof ServerMessage) {
            getGame().enqueue(new Callable() {
                public Object call() throws Exception {
                    ((ServerMessage)m).messageReceived(NetworkClient.this);
                    return null;
                }
            });
        }
    }


    public void send(ClientMessage message) {
        //Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, "Sending " + ((message.mustBeReliable()) ? "reliable " : "") + "message: " + message.getClass().getSimpleName());
        message.setReliable(message.mustBeReliable());
        this.client.send(message);
    }

    @Override
    public boolean isRunning() {
        return this.client != null && this.client.isConnected();
    }
    
    public int getConnectionId() {
        return this.client.getId();
    }

    @Override
    public void onUpdate(float tpf) {
        SpaceAppState space = getGame().getStateManager().getState(SpaceAppState.class);
        if (space == null || space.getMission() == null || !space.isPlayable()) {
            return;
        }
        
        if (space.getPlayer().getClient() != null) {
            PlayerActionMessage message = new PlayerActionMessage(space.getPlayer().getClient());
            this.send(message);
        }
    }

}
