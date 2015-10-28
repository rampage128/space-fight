/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.ui.TabController;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author rampage
 */
public class QuickMatchConfig implements ScreenController {

    public static final int COMMAND_START = 1;
    
    private Nifty nifty;
    private Screen screen;
    
    //private TabsControl tabs;
    private TabController tabs;
           
    private int command = 0;
    
    //private AbstractMission mission = null;
    
    private SimpleConfig config = new SimpleConfig();
    
    public QuickMatchConfig() {}
    
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
        
        DropDown deathmatchLevel = screen.findNiftyControl("deathmatch_level", DropDown.class);
        DropDown teamdeathmatchLevel = screen.findNiftyControl("teamdeathmatch_level", DropDown.class);
        
        //this.tabs = screen.findNiftyControl("quickmatch_tabs", TabsControl.class);
        this.tabs = new TabController(screen);
        this.tabs.addTab("button_deathmatch", "tab_deathmatch");
        this.tabs.addTab("button_teamdeathmatch", "tab_teamdeathmatch");
        this.tabs.addTab("button_elemination", "tab_elemination");
        
        String[] levels = Game.get().getGamedataManager().listLevels();
        for ( String level : levels ) {
            deathmatchLevel.addItem(level);
            teamdeathmatchLevel.addItem(level);
        }
    }

    public void onStartScreen() {
        Game.get().getInputManager().setCursorVisible(true);
    }

    public void onEndScreen() {
        switch(this.command) {
            case COMMAND_START:
                SpaceAppState.startGame(this.config, Game.get());
                //this.ui.startMission(this.config);
                break;
        }
    }
    
      //////////////////////////////////////////////////////////////////////////
     // TAB BUTTON CALLBACKS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void showDeathmatch() {
        this.tabs.switchTab("tab_deathmatch");
    }
    
    public void showTeamDeathmatch() {
        this.tabs.switchTab("tab_teamdeathmatch");
    }
    
    public void showElemination() {
        this.tabs.switchTab("tab_elemination");
    }
    
      //////////////////////////////////////////////////////////////////////////
     // MAIN BUTTON CONTROLS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void startMatch() {
        //this.config = null;
        String type = this.tabs.getCurrentTab();
        if ( "tab_deathmatch".equalsIgnoreCase(type) ) {
            DropDown deathmatchLevel    = screen.findNiftyControl("deathmatch_level", DropDown.class);
            TextField deathmatchPlayers = screen.findNiftyControl("deathmatch_players", TextField.class);
            TextField deathmatchLimit   = screen.findNiftyControl("deathmatch_limit", TextField.class);
            
            String level = (String)deathmatchLevel.getSelection();
            String playerString = deathmatchPlayers.getText();
            int players = 0;
            try {
                players = Integer.parseInt(playerString);
            } catch ( Exception e ) {}
            String limitString = deathmatchLimit.getText();
            int limit = 0;
            try {
                limit = Integer.parseInt(limitString);
            } catch ( Exception e ) {}
            if ( players > 1 && limit > 0 && level != null) {
                config.setValue("type", "deathmatch");
                config.setValue("level", level);
                config.setValue("maxclients", Integer.toString(players));
                config.setValue("teams", Integer.toString(players));
                config.setValue("limit", Integer.toString(limit));
            }
        } else if ( "tab_teamdeathmatch".equalsIgnoreCase(type) ) {
            DropDown deathmatchLevel    = screen.findNiftyControl("teamdeathmatch_level", DropDown.class);
            TextField deathmatchPlayers = screen.findNiftyControl("teamdeathmatch_players", TextField.class);
            TextField deathmatchTeams   = screen.findNiftyControl("teamdeathmatch_teams", TextField.class);
            TextField deathmatchLimit   = screen.findNiftyControl("teamdeathmatch_limit", TextField.class);
            
            String level = (String)deathmatchLevel.getSelection();
            String playerString = deathmatchPlayers.getText();
            int players = 0;
            try {
                players = Integer.parseInt(playerString);
            } catch ( Exception e ) {}
            String teamString = deathmatchTeams.getText();
            int teams = 0;
            try {
                teams = Integer.parseInt(teamString);
            } catch ( Exception e ) {}
            String limitString = deathmatchLimit.getText();
            int limit = 0;
            try {
                limit = Integer.parseInt(limitString);
            } catch ( Exception e ) {}
            if ( teams > 1 && players > 1 && limit > 0 && level != null) {
                config.setValue("type", "deathmatch");
                config.setValue("level", level);
                config.setValue("maxclients", Integer.toString(players));
                config.setValue("teams", Integer.toString(teams));
                config.setValue("limit", Integer.toString(limit));
            }
        }
        if (config.getValue("type", null) != null) {
            this.command = COMMAND_START;
            this.nifty.gotoScreen("loading");
        }
    }
    
    public void back() {
        this.nifty.gotoScreen("singleplayer");
    }
    
}
