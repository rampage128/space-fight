/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network;

import com.jme3.app.state.AbstractAppState;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.network.client.ChatMessage;
import de.jlab.spacefight.network.client.FieldSynchronizationRequest;
import de.jlab.spacefight.network.client.MapLoadedMessage;
import de.jlab.spacefight.network.client.ShipSelectionMessage;
import de.jlab.spacefight.network.client.WeaponChangeMessage;
import de.jlab.spacefight.network.server.ChatBroadcastMessage;
import de.jlab.spacefight.network.server.KillObjectMessage;
import de.jlab.spacefight.network.server.MapChangeMessage;
import de.jlab.spacefight.network.server.ShipAssignmentMessage;
import de.jlab.spacefight.network.server.SpawnObjectMessage;
import de.jlab.spacefight.network.server.WeaponChangeBroadcastMessage;
import de.jlab.spacefight.player.Player;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author rampage
 */
public class NetworkAppState extends AbstractAppState {
    
    public static final float CONNECTION_TIMEOUT = 5f;
    
    private Game game;
    private AbstractNetworkInstance network;
    
    private float idleTime = 0f;
        
    private NetworkAppState(Game game, String host, int port) {
        this.game = game;
        this.network = new NetworkClient(game);
        this.network.init(host, port);
    }
    
    private NetworkAppState(Game game, int port) {
        this.game = game;
        this.network = new NetworkServer(game);
        this.network.init(null, port);
    }
    
    @Override
    public void update(float tpf) {
        if (this.network == null || this.idleTime > CONNECTION_TIMEOUT) {
            Logger.getLogger("NETWORK").severe("Network connection timed out... leaving!");
            disconnect();
            this.game.gotoMainMenu();
        }
        if (!this.network.isRunning()) {
            this.idleTime += tpf;
        } else {
            this.network.update(tpf);
        }
    }
    
    public void disconnect() {
        if (this.network != null && this.network.isRunning()) {
            this.network.close();
        }
        this.game.getStateManager().detach(this);
    }
    
    public boolean isClient() {
        return this.network != null && this.network instanceof NetworkClient;
    }
    
    public boolean isServer() {
        return this.network != null && this.network instanceof NetworkServer;
    }
    
    public NetworkServer getServer() {
        if (!isServer()) {
            throw new RuntimeException("Network instance is no server! Use isServer() to check before calling getServer()!");
        }
        return (NetworkServer)this.network;
    }
    
    public NetworkClient getClient() {
        if (!isClient()) {
            throw new RuntimeException("Network instance is no client! Use isClient() to check before calling getClient()!");
        }
        return (NetworkClient)this.network;
    }
    
      //////////////////////////////////////////////////////////////////////////
     // STATIC ACCESS METHODS /////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public static void host(Game game, int port) {
        leave(game);
        
        NetworkAppState newNetwork = new NetworkAppState(game, port);
        game.getStateManager().attach(newNetwork);
    }
    
    public static void join(Game game, String host, int port) {
        leave(game);
        
        NetworkAppState newNetwork = new NetworkAppState(game, host, port);
        game.getStateManager().attach(newNetwork);
    }
    
    public static void leave(Game game) {
        NetworkAppState oldNetwork = game.getStateManager().getState(NetworkAppState.class);
        if (oldNetwork != null) {
            oldNetwork.disconnect();
        }
    }
    
    public static boolean shipSelected(Game game, ObjectInfoControl ship, Player player) {
        NetworkAppState network = game.getStateManager().getState(NetworkAppState.class);
        if (network != null) {
            if (network.isClient()) {
                network.getClient().send(new ShipSelectionMessage(ship, player));
                return true;
            } else if (network.isServer()) {
                network.getServer().broadcast(new ShipAssignmentMessage(ship, -1, player));
                return false;
            }
        }
        return false;
    }
   
    public static boolean killObject(Game game, Kill kill) {
        NetworkAppState network = game.getStateManager().getState(NetworkAppState.class);
        if (network != null) {
            if (network.isClient()) {
                // TODO: SEND KILL REQUEST MESSAGE AND EVALUATE ON SERVER IF KILL IS ALLOWED
                return false;
            } else if (network.isServer()) {
                KillObjectMessage message = new KillObjectMessage(kill);
                network.getServer().broadcast(message);
                return true;
            }
        }
        return true;
    }
    
    public static boolean spawnObject(Game game, ObjectInfoControl object, boolean initial) {
        NetworkAppState network = game.getStateManager().getState(NetworkAppState.class);
        if (network != null) {
            if (network.isClient()) {
                // TODO: SEND RESPAWN REQUEST MESSAGE AND EVALUATE ON SERVER IF RESPAWN IS ALLOWED
                return false;
            } else if (network.isServer()) {
                SpawnObjectMessage message = new SpawnObjectMessage(object.getId(), initial);
                network.getServer().broadcast(message);
                return true;
            }
        }
        return true;
    }
    
    public static void mapChanged(Game game, SimpleConfig mapConfig, boolean clientLeaves) {
        NetworkAppState network = game.getStateManager().getState(NetworkAppState.class);
        SpaceAppState space = game.getStateManager().getState(SpaceAppState.class);
        if (network != null && space != null) {
            if (network.isClient()) {
                if (clientLeaves) {
                    network.getClient().close();
                } else {
                    network.getClient().send(new MapLoadedMessage());
                }
            } else if (network.isServer() && space.isPlayable()) {
                network.getServer().broadcast(new MapChangeMessage(mapConfig));
            }
        }
    }
    
    public static boolean generateField(Game game, ObjectInfoControl object) {
        NetworkAppState network = game.getStateManager().getState(NetworkAppState.class);
        if (network != null) {
            if (network.isClient()) {
                FieldSynchronizationRequest request = new FieldSynchronizationRequest(object.getId());
                network.getClient().send(request);
                return false;
            } else if (network.isServer()) {
                return true;
            }
        }
        return true;
    }
       
    public static boolean isClient(Game game) {
        NetworkAppState network = game.getStateManager().getState(NetworkAppState.class);
        if (network != null) {
            if (network.isClient()) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isServer(Game game) {
        NetworkAppState network = game.getStateManager().getState(NetworkAppState.class);
        if (network != null) {
            if (network.isServer()) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isActive(Game game) {
        NetworkAppState network = game.getStateManager().getState(NetworkAppState.class);
        if (network != null) {
            if (network.isServer()) {
                if (network.getServer().isRunning()) {
                    return true;
                }
            } else {
                if (network.getClient().isRunning()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static void sendChatMessage(String message) {
        NetworkAppState network = Game.get().getStateManager().getState(NetworkAppState.class);
        if (network != null) {
            if (network.isServer()) {
                if (network.getServer().isRunning()) {
                    network.getServer().broadcast(new ChatBroadcastMessage(Game.get().getPlayer(), message));
                }
            } else if (network.isClient()) {
                if (network.getClient().isRunning()) {
                    network.getClient().send(new ChatMessage(message));
                }
            }
        }
    }
    
    public static boolean changeWeapon(ObjectInfoControl object, int slotNumber, String weaponName) {
        NetworkAppState network = Game.get().getStateManager().getState(NetworkAppState.class);
        if (network != null) {
            if (network.isServer()) {
                if (network.getServer().isRunning()) {
                    network.getServer().broadcast(new WeaponChangeBroadcastMessage(object, slotNumber, weaponName));
                    return true;
                }
            } else if (network.isClient()) {
                if (network.getClient().isRunning()) {
                    network.getClient().send(new WeaponChangeMessage(object, slotNumber, weaponName));
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public static List<InetAddress> discover(boolean online) {
        // TODO WRITE CODE TO DISCOVER HOSTS ONLINE OR IN A LAN
        return null;
    }
    
}
