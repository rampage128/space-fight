/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.network.NetworkAppState;
import de.jlab.spacefight.ui.UIAppState;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author rampage
 */
public class GameSelect implements ScreenController {

    private Nifty _nifty;
    
    private Game _game;
    //private UIAppState _ui;
    
    private int _command = 0;
        
    public GameSelect() {}
    
    public void bind(Nifty nifty, Screen screen) {
        _nifty = nifty;
    }

    public void onStartScreen() {
        Game.get().getInputManager().setCursorVisible(true);
    }

    public void onEndScreen() {
        
    }
    
    /* UI FUNCTIONS */
    public void quickMatch() {
        _nifty.gotoScreen("quickmatch");
    }
    
    public void missionSelect() {
        _nifty.gotoScreen("missionselect");
    }
    
    public void back() {
        if (NetworkAppState.isActive(Game.get())) {
            UIAppState.gotoScreen("multiplayerlobby", Game.get());
        } else {
            UIAppState.gotoScreen("mainmenu", Game.get());
        }
    }
    
}
