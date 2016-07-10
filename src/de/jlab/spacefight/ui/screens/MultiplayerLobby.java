/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.network.ChatListener;
import de.jlab.spacefight.network.NetworkAppState;
import de.jlab.spacefight.player.Player;
import de.jlab.spacefight.ui.TabController;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author rampage
 */
public class MultiplayerLobby implements ScreenController, ChatListener {

    private Nifty nifty;
    private Screen screen;
    
    private TabController tabs;
    private ListBox chatlist;
    
    public MultiplayerLobby() {}
    
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
        
        this.tabs = new TabController(screen);
        this.tabs.addTab("button_lobby", "tab_lobby");
        this.tabs.addTab("button_missions", "tab_missions");
        this.tabs.addTab("button_options", "tab_options");
        
        this.chatlist = screen.findNiftyControl("chatlist", ListBox.class);
        
        Element startButton = this.screen.findElementById("startbutton");
        if (NetworkAppState.isClient(Game.get())) {
            startButton.disable();
        } else {
            startButton.enable();
        }
    }

    public void onStartScreen() {
        Game.get().getInputManager().setCursorVisible(true);
    }

    public void onEndScreen() {
        
    }
        
      //////////////////////////////////////////////////////////////////////////
     // TAB BUTTON CALLBACKS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void showLobby() {
        this.tabs.switchTab("tab_lobby");
    }
    
    public void showMissions() {
        this.tabs.switchTab("tab_missions");
    }
    
    public void showOptions() {
        this.tabs.switchTab("tab_options");
    }
    
      //////////////////////////////////////////////////////////////////////////
     // MAIN BUTTON CONTROLS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void startgame() {
        this.nifty.gotoScreen("singleplayer");
    }
    
    public void leavegame() {
        NetworkAppState.leave(Game.get());
        this.nifty.gotoScreen("mainmenu");
    }

      //////////////////////////////////////////////////////////////////////////
     // CHAT LISTENER /////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void sendchat() {
        TextField field = this.screen.findNiftyControl("chatmessage", TextField.class);
        String text = field.getText();
        if (text == null || text.hashCode() == 0) {
            return;
        }
        
        NetworkAppState.sendChatMessage(text);
        field.setText("");
    }
    
    public void onChatMessage(Player player, String message) {
        StringBuilder itemBuilder = new StringBuilder(player.getNickname());
        itemBuilder.append(": ").append(message);
        this.chatlist.addItem(itemBuilder.toString());
    }
    
}
