/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import java.util.List;

/**
 *
 * @author rampage
 */
public class MissionSelect implements ScreenController {

    public static final int COMMAND_START = 1;
    
    //private UIAppState _ui;
    private Nifty nifty;
    private Screen _screen;
    
    private int command = -1;
    
    private SimpleConfig config = new SimpleConfig();
    
    public MissionSelect() {}
    
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        _screen = screen;
    }

    public void onStartScreen() {
        Game.get().getInputManager().setCursorVisible(true);
        loadMissionList();
    }

    public void onEndScreen() {
        switch ( command ) {
            case COMMAND_START:
                SpaceAppState.startGame(this.config, Game.get());
                //_ui.startMission(this.config);
                break;
        }
    }
    
    /* UI FUNCTIONS */
    public void back() {
        this.nifty.gotoScreen("singleplayer");
    }
    
    public void startMission() {
        if (config.getValue("name", null) != null) {
            this.command = COMMAND_START;
            this.nifty.gotoScreen("loading");
        }
    }
    
  /**
   * Fill the listbox with items. In this case with Strings.
   */
  public void loadMissionList() {
    ListBox listBox = _screen.findNiftyControl("missionlist", ListBox.class);
    listBox.clear();
    String[] missions = Game.get().getGamedataManager().listMissions();
    if ( missions != null ) {
        for (String mission : missions) {
            listBox.addItem(mission);
        }
    }
    listBox.setHeight(new SizeValue("5"));
  }

  /**
   * When the selection of the ListBox changes this method is called.
   */
  @NiftyEventSubscriber(id="missionlist")
  public void onMyListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
    List<String> selection = event.getSelection();
    for (String selectedItem : selection) {
      System.out.println("listbox selection [" + selectedItem + "]");
      this.config.setValue("type", "scripted");
      this.config.setValue("name", selectedItem);
    }
  }
}
