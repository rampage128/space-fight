/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 * UI-controller for the main menu.
 * 
 * @author rampage
 */
public class MainMenu implements ScreenController {

    public static final int COMMAND_SINGLEPLAYER = 1;
    public static final int COMMAND_EDIT = 2;
    public static final int COMMAND_QUIT = 3;
    
    private Nifty _nifty;
        
    private int _command = 0;
    
    public MainMenu() {}
    
    public void onStartScreen() {
        Game.get().getInputManager().setCursorVisible(true);
    }
    
    public void onEndScreen() {
        switch(_command) {
            case COMMAND_SINGLEPLAYER:
                //_space.setEnabled(false);
                //_game.getStateManager().detach(_space);
                
                break;
            case COMMAND_EDIT:
                //_space.setEnabled(false);
                //_game.getStateManager().detach(_space);
                // NOTHING TO DO HERE YET
                break;
            case COMMAND_QUIT:
                //_space.setEnabled(false);
                //_game.getStateManager().detach(_space);
                Game.get().quit();
                break;
        }
    }
    
    public void bind(Nifty nifty, Screen screen) {
        _nifty = nifty;
        
        Label label = screen.findNiftyControl("version", Label.class);
        label.setText(Game.VERSION.toString());
    }
    
    /* INTERACTION */
    public void singlePlayer() {
        _command = COMMAND_SINGLEPLAYER;
        _nifty.gotoScreen("singleplayer");
    }
    
    public void multiPlayer() {
        _nifty.gotoScreen("serverbrowser");
    }
    
    public void quitGame() {
        _command = COMMAND_QUIT;
        _nifty.gotoScreen("end");
    }
        
    public void startEditor() {
        _command = COMMAND_EDIT;
        _nifty.gotoScreen("editor");
    }
    
    public void options() {
        _nifty.gotoScreen("options");
        //SpaceAppState.startDemo(Game.get());
    }
    
}
