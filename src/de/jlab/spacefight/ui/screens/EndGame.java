/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import de.jlab.spacefight.AbstractClientControl;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.basic.camera.EndGameView;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.player.PlayerControl;
import de.jlab.spacefight.ui.TabController;
import de.jlab.spacefight.ui.controls.AdvancedList;
import de.jlab.spacefight.ui.controls.lists.scoreboard.ScoreboardModel;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author rampage
 */
public class EndGame implements ScreenController {

    public static final int COMMAND_LEAVE    = 3;
    
    private Nifty nifty;
    private Screen screen;
    
    private int command = -1;
    
    private TabController tabs;
    private AdvancedList<ScoreboardModel> factionscoreboard;
    private AdvancedList<ScoreboardModel> pilotscoreboard;
    private AdvancedList<String> piloteventlog;
    
    public void bind(Nifty nifty, Screen screen) {
        this.nifty  = nifty;
        this.screen = screen;
        
        this.tabs = new TabController(this.screen);
        this.tabs.addTab("button_scoreboard", "tab_scoreboard");
        this.tabs.addTab("button_pilot", "tab_pilot");
        
        this.factionscoreboard = screen.findNiftyControl("factionscoreboard", AdvancedList.class);
        this.pilotscoreboard = screen.findNiftyControl("pilotscoreboard", AdvancedList.class);
        this.piloteventlog = screen.findNiftyControl("piloteventlog", AdvancedList.class);
    }

    public void onStartScreen() {
        Game.get().getInputManager().setCursorVisible(true);
        this.tabs.switchTab("tab_scoreboard");
    }

    public void onEndScreen() {
        switch ( command ) {
            case COMMAND_LEAVE:
                Game.get().gotoMainMenu();
                break;
        }
    }
    
      //////////////////////////////////////////////////////////////////////////
     // EFFECT CALLBACK ///////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void endgame() {
        // GET PLAYER SHIP (CLIENT)
        ObjectInfoControl client = Game.get().getPlayer().getClient();
        
        // DISABLE PLAYER CONTROLS
        if (client != null) {
            PlayerControl playerControl = client.getSpatial().getControl(PlayerControl.class);
            if (playerControl != null) {
                playerControl.setEnabled(false);
            }
        }
        
        // START ENDGAME CAMERA
        SpaceAppState space = Game.get().getStateManager().getState(SpaceAppState.class);
        Game.get().getCameraManager().setView(EndGameView.class);
        
        // SET SOUND AND MUSIC VOLUMES
        Game.get().getAudioManager().setSoundVolume(0);
        Game.get().getAudioManager().setMusicVolume(1f);
        
        // GET WINNING-TEXT LABEL & HEADLINE
        Label winningLabel  = this.screen.findNiftyControl("winningtext", Label.class);
        Label headLine      = this.screen.findNiftyControl("headLine", Label.class);
        
        // DECIDE IF IT'S WINNING OR LOOSING TIME
        if (client != null && client.getFaction() != null) {
            boolean winner = client.getFaction().isWinner();
            if (winner) {
                // MY FACTION HAS WON
                Game.get().getAudioManager().playMusic("victory.ogg", 0, true);
                winningLabel.setText("VICTORY!");
            } else {
                // MY FACTION HAS LOST
                Game.get().getAudioManager().playMusic("defeat.ogg", 0, true);
                winningLabel.setText("DEFEAT!");
            }
        } else {
            // SPECTATOR
            Game.get().getAudioManager().playMusic("victory.ogg", 0, true);
        }
        
        // SET HEADLINE TEXT
        headLine.setText(winningLabel.getText());
        
        // FILL THE SCOREBOARD
        fillScoreBoard();
        fillEventLog();
    }
    
    private void fillEventLog() {
        this.piloteventlog.clear();
        ObjectInfoControl client = Game.get().getPlayer().getClient();
        AbstractClientControl clientControl = client.getSpatial().getControl(AbstractClientControl.class);
        Kill[] events = clientControl.getEventLog();
        for (Kill event : events) {
            StringBuilder eventBuilder = new StringBuilder();
            if (event.getTarget() == client) {
                eventBuilder.append("D: ").append(event.getOrigin().getCallsign());
                AbstractClientControl otherClient = event.getOrigin().getSpatial().getControl(AbstractClientControl.class);
                if (otherClient != null) {
                    eventBuilder.append(" (").append(otherClient.getName()).append(")");
                }
            } else {
                eventBuilder.append("K: ").append(event.getTarget().getCallsign());
                AbstractClientControl otherClient = event.getTarget().getSpatial().getControl(AbstractClientControl.class);
                if (otherClient != null) {
                    eventBuilder.append(" (").append(otherClient.getName()).append(")");
                }
            }
            eventBuilder.append(" [").append(event.getDamageName()).append("]");
            this.piloteventlog.addItem(eventBuilder.toString());
        }
    }
    
    private void fillScoreBoard() {
        SpaceAppState space = Game.get().getStateManager().getState(SpaceAppState.class);
        
        // FILL FACTION SCOREBOARD
        Faction[] factions = space.getMission().getFactions();        
        Arrays.sort(factions, new Comparator<Faction>() {
            public int compare(Faction f1, Faction f2) {
                return f2.getScore() - f1.getScore();
            }
        });
        this.factionscoreboard.clear();
        for (Faction faction : factions) {
            this.factionscoreboard.addItem(new ScoreboardModel(faction.getName(), faction.getKillCount(), faction.getDeathCount(), faction.getScore(), new Color(faction.getColor().getRed(), faction.getColor().getGreen(), faction.getColor().getBlue(), faction.getColor().getAlpha())));
        }
        
        // FILL CLIENT SCOREBOARD
        ObjectInfoControl[] clients = space.getMission().getClients();
        ArrayList<AbstractClientControl> clientList = new ArrayList<AbstractClientControl>();
        for (ObjectInfoControl client : clients) {
            AbstractClientControl clientControl = client.getSpatial().getControl(AbstractClientControl.class);
            if (clientControl != null) {
                clientList.add(clientControl);
            }
        }
        Collections.sort(clientList, new Comparator<AbstractClientControl>() {
            public int compare(AbstractClientControl c1, AbstractClientControl c2) {
                return c2.getScore() - c1.getScore();
            }            
        });
        this.pilotscoreboard.clear();
        for (AbstractClientControl clientControl : clientList) {
            Faction faction = clientControl.getFlight().getFaction();
            Color factionColor = Color.WHITE;
            if (faction != null) {
                factionColor = new Color(faction.getColor().getRed(), faction.getColor().getGreen(), faction.getColor().getBlue(), faction.getColor().getAlpha());
            }
            this.pilotscoreboard.addItem(new ScoreboardModel(clientControl.getName(), clientControl.getKillCount(), clientControl.getDeathCount(), clientControl.getScore(), factionColor));
        }
    }
    
      //////////////////////////////////////////////////////////////////////////
     // TAB BUTTON CALLBACKS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void showScoreboard() {
        this.tabs.switchTab("tab_scoreboard");
    }
    
    public void showPilot() {
        this.tabs.switchTab("tab_pilot");
    }
        
      //////////////////////////////////////////////////////////////////////////
     // MAIN BUTTON CONTROLS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void leave() {
        this.command = COMMAND_LEAVE;
        this.nifty.gotoScreen("loading");
    }
    
}
