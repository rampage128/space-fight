/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.network.NetworkAppState;
import de.jlab.spacefight.ui.UIAppState;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author rampage
 */
public class Loading implements ScreenController {

    private Nifty nifty;
    
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
    }

    public void onStartScreen() {
        SpaceAppState space = Game.get().getStateManager().getState(SpaceAppState.class);
        if (space != null && space.isPlayable()) {
            if (space.getMission() != null && space.getMission().isInitialized()) {
                UIAppState.gotoScreen("spawnscreen", Game.get());
            }
        } else {
            if (NetworkAppState.isActive(Game.get())) {
                UIAppState.gotoScreen("multiplayerlobby", Game.get());
            } else {
                UIAppState.gotoScreen("mainmenu", Game.get());
            }
        }
    }

    public void onEndScreen() {
        
    }
    
}
