/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Flight;
import de.jlab.spacefight.network.NetworkAppState;
import de.jlab.spacefight.ui.UIAppState;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.List;

/**
 *
 * @author rampage
 */
@Deprecated
public class FactionSelectController implements ScreenController {
    
    public static final int COMMAND_SELECT   = 1;
    public static final int COMMAND_SPECTATE = 2;
    
    private SpaceAppState space;
    
    private UIAppState ui;
    private Nifty nifty;
    private Screen screen;
    
    private int command = -1;
    
    private Faction selectedFaction = null;
    private Flight selectedFlight = null;
    private ObjectInfoControl selectedClient = null;
    
    public FactionSelectController(UIAppState ui, SpaceAppState space) {
        this.ui = ui;
        this.space = space;
    }
    
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void onStartScreen() {
        loadFactionList();
    }

    public void onEndScreen() {
        switch ( command ) {
            case COMMAND_SELECT:
                if ( this.selectedClient != null ) {
                    if (!NetworkAppState.shipSelected(this.space.getGame(), this.selectedClient, this.space.getPlayer())) {
                        //this.space.getPlayer().setFaction(this.selectedFaction);
                        //this.space.getPlayer().setClient(this.selectedClient);
                        this.space.getPlayer().setClient(this.selectedClient);
                        Game.get().getCameraManager().setAutomatic(false);
                    }
                    /*
                    if (space.getGame().getNetworkClient() != null) {
                        ShipSelectionMessage message = new ShipSelectionMessage(this.selectedClient);
                        space.getGame().getNetworkClient().send(message);
                    } else {
                        if (space.getGame().getNetworkServer() != null) {
                            ShipAssignmentMessage shipAssignment = new ShipAssignmentMessage(this.selectedClient, -1);
                            space.getGame().getNetworkServer().broadcast(shipAssignment);
                        }
                        this.space.getPlayer().setFaction(this.selectedFaction);
                        this.space.getPlayer().setClient(this.selectedClient);
                        this.space.getCameraManager().setAutomatic(false);
                    }
                    */
                }
                break;
            case COMMAND_SPECTATE:
                ObjectInfoControl object = this.space.getPlayer().getClient();
                if (object != null) {
                    if (!NetworkAppState.shipSelected(this.space.getGame(), null, this.space.getPlayer())) {
                        object.setPlayer(null);
                        Game.get().getCameraManager().setAutomatic(true);
                    }
                }
                //this.space.getPlayer().setFaction(null);
                //this.space.getPlayer().setClient(null);
                break;
        }
    }
    
    /* UI FUNCTIONS */
    public void back() {
        this.nifty.gotoScreen("menu");
    }
    
    public void spectate() {
        this.command = COMMAND_SPECTATE;
        this.nifty.gotoScreen("ingame");
    }
    
    public void selectFlight() {
        this.command = COMMAND_SELECT;
        this.nifty.gotoScreen("ingame");
    }
    
  /**
   * Fill the listbox with items. In this case with Strings.
   */
  public void loadFactionList() {
    ListBox<Faction> listBox = screen.findNiftyControl("factionlist", ListBox.class);
    listBox.clear();
    loadFlightList(null);
    Faction[] factions = this.space.getMission().getFactions();
    if ( factions != null ) {
        for ( int i = 0; i < factions.length; i++ ) {
            listBox.addItem(factions[i]);
        }
    }
  }
  
  public void loadFlightList(Faction faction) {
    ListBox<Flight> listBox = screen.findNiftyControl("flightlist", ListBox.class);
    listBox.clear();
    loadClientList(null);
    if ( faction == null )
        return;
    Flight[] flights = this.space.getMission().getFlights(faction, "client");
    if ( flights != null ) {
        for ( Flight flight : flights ) {
            listBox.addItem(flight);
        }
    }
  }
  
  public void loadClientList(Flight flight) {
    ListBox<ObjectInfoControl> listBox = screen.findNiftyControl("clientlist", ListBox.class);
    listBox.clear();
    if ( flight == null )
        return;
    ObjectInfoControl[] clients = flight.getObjects();
    if ( clients != null ) {
        for ( ObjectInfoControl client : clients ) {
            listBox.addItem(client);
        }
    }
  }

  /**
   * When the selection of the ListBox changes this method is called.
   */
  @NiftyEventSubscriber(id="factionlist")
  public void onFactionListSelectionChanged(final String id, final ListBoxSelectionChangedEvent<Faction> event) {
    List<Faction> selection = event.getSelection();
    for (Faction selectedItem : selection) {
      System.out.println("listbox selection [" + selectedItem + "]");
      this.selectedFaction = selectedItem;
      this.loadFlightList(selectedItem);
    }
  }
  
  @NiftyEventSubscriber(id="flightlist")
  public void onFlightListSelectionChanged(final String id, final ListBoxSelectionChangedEvent<Flight> event) {
    List<Flight> selection = event.getSelection();
    for (Flight selectedItem : selection) {
      System.out.println("listbox selection [" + selectedItem + "]");
      this.selectedFlight = selectedItem;
      this.loadClientList(selectedItem);
    }
  }
  
  @NiftyEventSubscriber(id="clientlist")
  public void onClientListSelectionChanged(final String id, final ListBoxSelectionChangedEvent<ObjectInfoControl> event) {
    List<ObjectInfoControl> selection = event.getSelection();
    for (ObjectInfoControl selectedItem : selection) {
      System.out.println("listbox selection [" + selectedItem + "]");
      this.selectedClient = selectedItem;
    }
  }
    
}
