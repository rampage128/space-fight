/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.player;

import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.XMLLoader;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author rampage
 */
public class Player {
     
    //private Faction faction;
    private boolean local = true;
    
    private String nickname = "Player";
    private int networkId;
    
    private ObjectInfoControl client;
    //private PlayerControl control;
    
    private ArrayList<String> favourites = new ArrayList<String>();
    
    public Player(boolean local) {
        this.local = local;
    }
    
    /*
    public void setFaction(Faction faction) {
        this.faction = faction;
    }
    
    public Faction getFaction() {
        return this.faction;
    }
    */
    
    public int getNetworkId() {
        return this.networkId;
    }
    
    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }
    
    public String getNickname() {
        return this.nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public boolean isLocal() {
        return this.local;
    }
        
    public void setClient(ObjectInfoControl object) {
        if (this.client != null) {
            this.client.setPlayer(null);
        }
        this.client = object;
        if (object != null) {
            object.setPlayer(this);
        }
    }
    
    public ObjectInfoControl getClient() {
        return this.client;
    }
    
    public void addFavourite(String address) {
        this.favourites.add(address);
    }
    
    public void removeFavourite(int index) {
        this.favourites.remove(index);
    }
    
    public String[] getFavourites() {
        return this.favourites.toArray(new String[0]);
    }
    
    public void savePlayer() {
        Element rootElement = new Element("player");
        Element nicknameElement = new Element("nickname");
        nicknameElement.setText(nickname);
        rootElement.addContent(nicknameElement);
        
        Element multiplayerElement = new Element("multiplayer");
        Element favouritesElement = new Element("favourites");
        for (String favourite : this.favourites) {
            Element addressElement = new Element("address");
            addressElement.setText(favourite);
            Element favouriteElement = new Element("favourite");
            favouriteElement.addContent(addressElement);
            favouritesElement.addContent(favouriteElement);
        }
        multiplayerElement.addContent(favouritesElement);
        rootElement.addContent(multiplayerElement);
        
        Document document = new Document(rootElement);
            
        // save it to a file:
        try {
            XMLOutputter out = new XMLOutputter();
            out.setFormat(Format.getPrettyFormat());
            FileWriter writer = new FileWriter("player.xml");
            out.output(document, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Logger.getLogger(getClass().getCanonicalName()).log(Level.SEVERE, "Cannot save player data!", e);
        }
    }
    
    public static Player loadPlayer() {
        Player player = null;
        if (new File("player.xml").exists()) {
            player = new Player(true);
            Element element = XMLLoader.loadXML(null, "player");
            
            Element multiplayerElement = element.getChild("multiplayer");
            if (multiplayerElement != null) {
                List<Element> favourites = XMLLoader.readElementList("favourite", multiplayerElement);
                for (Element favourite : favourites) {
                    String address = XMLLoader.getStringValue(favourite, "address", null);
                    if (address != null) {
                        player.addFavourite(address);
                    }
                }
            }
            player.setNickname(XMLLoader.getStringValue(element, "nickname", "Player"));
        } else {
            player = new Player(true);
            player.savePlayer();
        }

        return player;
    }
    
    /*
    public void setClient(ObjectInfoControl newClient) {
        // MAKE OLD SHIP AN AI SHIP AGAIN!
        if ( this.client != null && this.control != null) {
            this.client.getSpatial().removeControl(this.control);
            AIShipControl aiControl = new AIShipControl(this.space);
            aiControl.copyTasks(this.control);
            this.client.getSpatial().addControl(aiControl);
        }
        
        // RETRIEVE OLD CONTROL OF NEW CLIENT
        if ( newClient != null ) {
            this.setFaction(newClient.getFaction());
            AbstractClientControl oldClientControl = newClient.getSpatial().getControl(AbstractClientControl.class);
            if ( oldClientControl != null ) {
                newClient.getSpatial().removeControl(oldClientControl);
            }
            if ( this.control == null )
                this.control = new PlayerControl(game, space);
            this.control.clearTasks();
            this.control.copyTasks(oldClientControl);
            newClient.getSpatial().addControl(this.control);
        }
                        
        // SET CLIENT
        this.client = newClient;
    }
    */
    
    /*
    public ObjectInfoControl getClient() {
        return this.client;
    }
    */
        
}
