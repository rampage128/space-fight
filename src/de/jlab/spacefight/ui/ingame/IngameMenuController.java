/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame;

import com.jme3.network.NetworkClient;
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
@Deprecated
public class IngameMenuController implements ScreenController {

    public static final int COMMAND_RESUME = 1;
    public static final int COMMAND_EDIT = 2;
    public static final int COMMAND_MAINMENU = 3;
    
    private Nifty _nifty;
    
    private Game _game;
    private SpaceAppState _space;
    private UIAppState _ui;
    
    private int _command = 0;
    
    public IngameMenuController(Game game, UIAppState ui, SpaceAppState space) {
        _game = game;
        _ui = ui;
        _space = space;
    }
        
    public void bind(Nifty nifty, Screen screen) {
        _nifty = nifty;
    }

    public void onStartScreen() {
        //_game.getFlyByCamera().setEnabled(false);
        //_game.getFlyByCamera().setRotationSpeed(2f);
        //_game.getFlyByCamera().setDragToRotate(true);
        _game.getInputManager().setCursorVisible(true);
        //_space.setEnabled(false);
        //if ( _game.getInputManager().hasMapping("MENU") )
            //_game.getInputManager().deleteMapping("MENU");
    }

    public void onEndScreen() {
        switch(_command) {
            case COMMAND_RESUME:
                // NOTHING TO DO HERE YET
                break;
            case COMMAND_EDIT:
                // NOTHING TO DO HERE YET
                break;
            case COMMAND_MAINMENU:
                if (NetworkAppState.isClient(this._game)) {
                    NetworkAppState.leave(this._game);
                }
                this._game.gotoMainMenu();
                break;
        }
    }
    
    public void quitGame() {
        _command = COMMAND_MAINMENU;
        _nifty.gotoScreen("end");
    }
    
    public void resumeGame() {
        _command = COMMAND_RESUME;
        _nifty.gotoScreen("ingame");
    }
    
    public void selectFlight() {
        _nifty.gotoScreen("flightselect");
    }
    
    public void gotoMenu() {
        _nifty.gotoScreen("menu");
    }
    
}
