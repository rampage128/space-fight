/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import de.jlab.spacefight.Game;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rampage
 */
public class ServerBrowser implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    
    private ListBox serverlist;
    private ListBox playerlist;
    private Element addServerPopup;
    
    public ServerBrowser() {}
    
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
        
        this.addServerPopup = nifty.createPopup("addserverpopup");
    }

    public void onStartScreen() {
        Game.get().getInputManager().setCursorVisible(true);
               
        this.serverlist   = screen.findNiftyControl("serverlist", ListBox.class);
        this.playerlist   = screen.findNiftyControl("playerlist", ListBox.class);
        
        loadServerList();
    }

    public void onEndScreen() {
        
    }

      //////////////////////////////////////////////////////////////////////////
     // ADD SERVER POPUP //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void addServer() {
        this.nifty.showPopup(this.screen, this.addServerPopup.getId(), null);
    }
    
    public void dontaddserver() {
        this.nifty.closePopup(this.addServerPopup.getId());
    }
    
    public void doaddserver() {       
        TextField ipportField = this.addServerPopup.findNiftyControl("join_ipport", TextField.class);
        String ipport = ipportField.getText();
        Game.get().getPlayer().addFavourite(ipport);
        Game.get().getPlayer().savePlayer();
        this.nifty.closePopup(this.addServerPopup.getId());
        ipportField.setText("");
        
        loadServerList();
    }
    
    public void removeserver() {
        List<Integer> selection = this.serverlist.getSelectedIndices();
        if (selection == null || selection.isEmpty()) {
            return;
        }
        
        Game.get().getPlayer().removeFavourite(selection.get(0));
        Game.get().getPlayer().savePlayer();
        loadServerList();
    }
    
    private void loadServerList() {
        this.serverlist.clear();
        String[] favourites = Game.get().getPlayer().getFavourites();
        for (String favourite : favourites) {
            this.serverlist.addItem(favourite);
        }
    }
    
      //////////////////////////////////////////////////////////////////////////
     // FILTER BUTTON CALLBACKS ///////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void showFeatured() {
        
    }
    
    public void showLAN() {
        
    }
    
    public void showFavourites() {
        
    }
    
      //////////////////////////////////////////////////////////////////////////
     // MAIN BUTTON CONTROLS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void hostgame() {       
        Game.get().createServer(4163);
        this.nifty.gotoScreen("multiplayerlobby");
    }
    
    public void joingame() {
        List<String> selection = this.serverlist.getSelection();
        if (selection == null || selection.isEmpty()) {
            return;
        }
        String ipport = selection.get(0);
        
        //TextField ipportField = screen.findNiftyControl("join_ipport", TextField.class);
        //String ipport = ipportField.getText();
        
        int port = 4163;
        String host = "localhost";

        try {
            if (ipport.indexOf(":") > -1) {
                String[] ipportArray = ipport.split(":");
                host = ipportArray[0];
                port = Integer.parseInt(ipportArray[1]);
            } else {
                host = ipport;
            }
            Game.get().joinServer(host, port);
        } catch (Exception e) {
            Logger.getLogger(ServerBrowser.class.getSimpleName()).log(Level.SEVERE, "Error parsing join-address: " + ipport, e);
        }
    }
    
    public void back() {
        this.nifty.gotoScreen("mainmenu");
    }
    
}
