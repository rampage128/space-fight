/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Flight;
import de.jlab.spacefight.network.NetworkAppState;
import de.jlab.spacefight.player.PlayerControl;
import de.jlab.spacefight.systems.perks.Perk;
import de.jlab.spacefight.systems.perks.PerkControl;
import de.jlab.spacefight.systems.weapons.WeaponSlot;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.ui.TabController;

import de.jlab.spacefight.ui.controls.DropDownValue;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.List;

/**
 *
 * @author rampage
 */
public class SpawnScreen implements ScreenController {
    
    public static final int COMMAND_DEPLOY   = 1;
    public static final int COMMAND_SPECTATE = 2;
    public static final int COMMAND_LEAVE    = 3;
    
    private SpaceAppState space;
    //private UIAppState ui;
    private Screen screen;
    private Nifty nifty;
    
    private ObjectInfoControl selectedClient = null;
    private ObjectInfoControl currentClient = null;
    
    private int command = -1;
    
    // GENERAL
    private TabController tabs;
    private Element deployButton;
    
    // FLIGHT
    private Element selectClientButton;
    
    //private TabsControl tabs;
    //private TabControl tabSpawn;
    //private TabControl tabLoadout;
    //private TabControl tabFlight;
       
    // LOADOUT
    private ListBox<WeaponSlot> slotlist;
    private ListBox<String> itemlist;
    private DropDown<DropDownValue> perkSelect;
    private Element selectitemButton;
    
    public SpawnScreen() {}
    
    /*
    public SpawnScreenController(UIAppState ui, SpaceAppState space) {
        this.ui = ui;
        this.space = space;
    }
    */

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;        
    }

    public void onStartScreen() {
        Game.get().getInputManager().setCursorVisible(true);
        
        this.space = Game.get().getStateManager().getState(SpaceAppState.class);
        
        /*
        this.tabs = screen.findControl("spawntabs", TabsControl.class);
        this.tabSpawn = screen.findControl("tab_spawn", TabControl.class);
        this.tabLoadout = screen.findControl("tab_loadout", TabControl.class);
        this.tabFlight = screen.findControl("tab_flight", TabControl.class);
        */
        this.tabs = new TabController(this.screen);
        this.tabs.addTab("button_spawn", "tab_spawn");
        this.tabs.addTab("button_loadout", "tab_loadout");
        this.tabs.addTab("button_flight", "tab_flight");
        
        this.slotlist = screen.findNiftyControl("slotlist", ListBox.class);
        this.itemlist = screen.findNiftyControl("itemlist", ListBox.class);
        this.perkSelect = screen.findNiftyControl("perkselect", DropDown.class);
        
        this.deployButton = screen.findElementByName("deploybutton");
        this.selectClientButton = screen.findElementByName("selectclientbutton");
        this.selectitemButton = screen.findElementByName("selectitembutton");
        
        this.currentClient = this.space.getPlayer().getClient();
        
        initLoadoutScreen();
        
        initFactionScreen();      
        enablePlayerControls(false);
        checkDeployButton();
        checkSelectShipButton();
        checkTabs();
    }

    public void onEndScreen() {
        switch ( command ) {
            case COMMAND_DEPLOY:
                enablePlayerControls(true);
                // NOTHING TO DO HERE!
                break;
            case COMMAND_SPECTATE:
                ObjectInfoControl object = this.space.getPlayer().getClient();
                if (object != null) {
                    if (!NetworkAppState.shipSelected(this.space.getGame(), null, this.space.getPlayer())) {
                        object.setPlayer(null);
                        Game.get().getCameraManager().setAutomatic(true);
                    }
                }
                break;
            case COMMAND_LEAVE:
                if (NetworkAppState.isClient(this.space.getGame())) {
                    NetworkAppState.leave(this.space.getGame());
                }
                this.space.getGame().gotoMainMenu();
                break;
        }
    }
    
      //////////////////////////////////////////////////////////////////////////
     // TAB BUTTON CALLBACKS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void showSpawn() {
        this.tabs.switchTab("tab_spawn");
    }
    
    public void showLoadout() {
        this.tabs.switchTab("tab_loadout");
    }
    
    public void showFlight() {
        this.tabs.switchTab("tab_flight");
    }
    
      //////////////////////////////////////////////////////////////////////////
     // MAIN BUTTON CONTROLS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void leave() {
        this.command = COMMAND_LEAVE;
        this.nifty.gotoScreen("loading");
    }

    public void spectate() {
        this.command = COMMAND_SPECTATE;
        this.nifty.gotoScreen("ingame");
    }

    public void deploy() {
        // TODO CHECK DEPLOYMENT
        if (this.currentClient != null) {
            this.command = COMMAND_DEPLOY;
            this.nifty.gotoScreen("ingame");
        }
    }
    
    private void checkDeployButton() {
        if (this.currentClient != null) {
            this.deployButton.enable();
        } else {
            this.deployButton.disable();
        }
    }
    
    private void checkTabs() {
        if (this.currentClient != null) {
            if (this.currentClient.isAlive()) {
                this.tabs.toggleTab("tab_spawn", false);
            } else {
                this.tabs.toggleTab("tab_spawn", true);
            }
            this.tabs.toggleTab("tab_loadout", true);
            //this.tabSpawn.enable();
            //this.tabLoadout.enable();
        } else {
            this.tabs.switchTab("tab_flight");
            this.tabs.toggleTab("tab_spawn", false);
            this.tabs.toggleTab("tab_loadout", false);
            //this.tabSpawn.disable();
            //this.tabLoadout.disable();
            //this.tabs.setSelectedTab("tab_flight");
        }
    }
    
    private void enablePlayerControls(boolean enabled) {
        if (this.currentClient != null) {
            PlayerControl playerControl = this.currentClient.getSpatial().getControl(PlayerControl.class);
            if (playerControl != null) {
                playerControl.setEnabled(enabled);
            }
        }
    }

      //////////////////////////////////////////////////////////////////////////
     // SHIP & LOADOUT ////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
        
    public void selectitem() {
        List<Integer> selection = this.slotlist.getSelectedIndices();
        if (selection == null || selection.isEmpty()) {
            return;
        }
        
        List<String> items = this.itemlist.getSelection();
        if (items == null || items.isEmpty()) {
            return;
        }
        
        if (this.currentClient == null) {
            return;
        }
        
        if (NetworkAppState.changeWeapon(this.currentClient, selection.get(0), items.get(0))) {
            this.currentClient.getObjectControl(WeaponSystemControl.class).changeWeapon(selection.get(0), items.get(0));
            this.loadSlotList(this.currentClient);
        }
    }
    
    private void checkItemSelection() {
        if (this.slotlist.getSelection() == null || this.slotlist.getSelection().isEmpty() || this.itemlist.getSelection() == null || this.itemlist.getSelection().isEmpty()) {
            this.selectitemButton.disable();
        } else {
            this.selectitemButton.enable();
        }
    }
    
    private void initLoadoutScreen() {
        if (this.currentClient != null) {
            loadSlotList(this.currentClient);
            loadPerkList(this.currentClient);
        } else {
            loadSlotList(null);
            loadPerkList(null);
        }
    }
    
    public void loadPerkList(ObjectInfoControl object) {
        this.perkSelect.clear();
        this.perkSelect.addItem(new DropDownValue("None", null));
        
        if (object == null) {
            return;
        }
        String[] perks = Game.get().getGamedataManager().listPerks();
        
        PerkControl perkControl = object.getObjectControl(PerkControl.class);
        Perk perk = null;
        if (perkControl != null) {
            perk = perkControl.getPerk();
        }
        
        for (String perkName : perks) {
            DropDownValue value = new DropDownValue(perkName, perkName);
            this.perkSelect.addItem(value);
            if (perk != null && perk.getName().equalsIgnoreCase(perkName)) {
                this.perkSelect.selectItem(value);
            }
        }
        
    }
    
    public void loadSlotList(ObjectInfoControl object) {
        this.slotlist.clear();
        
        if (object != null) {        
            WeaponSlot[] primarySlots   = object.getObjectControl(WeaponSystemControl.class).getPrimarySlots();
            for (WeaponSlot slot : primarySlots) {
                this.slotlist.addItem(slot);
            }
            WeaponSlot[] secondarySlots = object.getObjectControl(WeaponSystemControl.class).getSecondarySlots();
            for (WeaponSlot slot : secondarySlots) {
                this.slotlist.addItem(slot);
            }
        }
               
        if (this.slotlist.itemCount() > 0) {
            this.slotlist.selectItemByIndex(0);
            loadItemList(this.slotlist.getItems().get(0));
        } else {
            loadItemList(null);
        }
    }
    
    public void loadItemList(WeaponSlot slot) {
        this.itemlist.clear();
        if (slot == null) {
            return;
        }
        String[] weapons = Game.get().getGamedataManager().listWeapons(slot.getWeapon().getType(), slot.getSize());
        for (String weapon : weapons) {
            this.itemlist.addItem(weapon);
        }
    }
    
    // WEAPON SELECTION EVENT HANDLERS
    @NiftyEventSubscriber(id="slotlist")
    public void onSlotListSelectionChanged(final String id, final ListBoxSelectionChangedEvent<WeaponSlot> event) {
        List<WeaponSlot> selection = event.getSelection();
        for (WeaponSlot selectedItem : selection) {
            loadItemList(selectedItem);
        }
        checkItemSelection();
    }
    
    @NiftyEventSubscriber(id="itemlist")
    public void onItemListSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
        checkItemSelection();
    }
    
    @NiftyEventSubscriber(id="perkselect")
    public void onPerkSelectionChanged(final String id, final DropDownSelectionChangedEvent<DropDownValue> event) {
        DropDownValue selection = event.getSelection();
        if (this.currentClient != null && selection != null) {
            System.out.println("perk selection [" + selection + "]");
            PerkControl perkControl = this.currentClient.getObjectControl(PerkControl.class);
            if (perkControl != null) {
                if (selection.getValue() != null) {
                    perkControl.setPerk(Game.get().getGamedataManager().loadPerk((String)selection.getValue()));
                } else {
                    perkControl.setPerk(null);
                }
            }
        }
    }
    
      //////////////////////////////////////////////////////////////////////////
     // FACTION & FLIGHT //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void selectClient() {
        if (this.selectedClient == null) {
            return;
        }
        
        if (!NetworkAppState.shipSelected(this.space.getGame(), this.selectedClient, this.space.getPlayer())) {
            this.space.getPlayer().setClient(this.selectedClient);
            Game.get().getCameraManager().setAutomatic(false);
        }
        
        this.currentClient = this.space.getPlayer().getClient();

        loadSlotList(this.currentClient);
        loadPerkList(this.currentClient);
        
        enablePlayerControls(false);
        checkTabs();
        checkDeployButton();
    }
    
    private void initFactionScreen() {
        if (this.currentClient != null) {
            loadFactionList(this.currentClient.getFaction());
            loadFlightList(this.currentClient.getFaction(), this.currentClient.getFlight());
            loadClientList(this.currentClient.getFlight(), this.currentClient);
        } else {
            loadFactionList(null);
        }
    }
    
    public void loadFactionList(Faction selectedFaction) {
        ListBox<Faction> listBox = screen.findNiftyControl("factionlist", ListBox.class);
        listBox.clear();
        loadFlightList(null, null);
        Faction[] factions = this.space.getMission().getFactions();
        if (factions != null) {
            for (Faction faction : factions) {
                listBox.addItem(faction);
            }
        }
        
        if (selectedFaction != null) {
            listBox.selectItem(selectedFaction);
        } else if (factions.length == 1) {
            listBox.selectItem(factions[0]);
        }
    }

    public void loadFlightList(Faction faction, Flight selectedFlight) {
        ListBox<Flight> listBox = screen.findNiftyControl("flightlist", ListBox.class);
        listBox.clear();
        loadClientList(null, null);
        if (faction == null)
            return;
        Flight[] flights = this.space.getMission().getFlights(faction, "client");
        if (flights != null) {
            for (Flight flight : flights) {
                listBox.addItem(flight);
            }
        }

        if (selectedFlight != null) {
            listBox.selectItem(selectedFlight);
        } else if (flights.length == 1) {
            listBox.selectItem(flights[0]);
        }
    }

    public void loadClientList(Flight flight, ObjectInfoControl selectedClient) {
        ListBox<ObjectInfoControl> listBox = screen.findNiftyControl("clientlist", ListBox.class);
        listBox.clear();
        if (flight == null)
            return;
        ObjectInfoControl[] clients = flight.getObjects();
        if (clients != null) {
            for (ObjectInfoControl client : clients) {
                listBox.addItem(client);
            }
        }
        
        if (selectedClient != null) {
            listBox.selectItem(selectedClient);
            this.selectedClient = selectedClient;
        } else if (clients.length == 1) {
            listBox.selectItem(clients[0]);
            this.selectedClient = clients[0];
        } else {
            this.selectedClient = null;
        }
        
        checkSelectShipButton();
    }
    
    // CLIENT SELECTION EVENT HANDLERS
    @NiftyEventSubscriber(id="factionlist")
    public void onFactionListSelectionChanged(final String id, final ListBoxSelectionChangedEvent<Faction> event) {
        List<Faction> selection = event.getSelection();
        for (Faction selectedItem : selection) {
            System.out.println("listbox selection [" + selectedItem + "]");
            this.loadFlightList(selectedItem, null);
        }
    }

    @NiftyEventSubscriber(id="flightlist")
    public void onFlightListSelectionChanged(final String id, final ListBoxSelectionChangedEvent<Flight> event) {
        List<Flight> selection = event.getSelection();
        for (Flight selectedItem : selection) {
            System.out.println("listbox selection [" + selectedItem + "]");
            this.loadClientList(selectedItem, null);
        }
    }

    @NiftyEventSubscriber(id="clientlist")
    public void onClientListSelectionChanged(final String id, final ListBoxSelectionChangedEvent<ObjectInfoControl> event) {
        List<ObjectInfoControl> selection = event.getSelection();
        for (ObjectInfoControl selectedItem : selection) {
            System.out.println("listbox selection [" + selectedItem + "]");
            this.selectedClient = selectedItem;
        }
        
        checkSelectShipButton();
    }
    
    // BUTTON STATE CONTROLLING
    
    private void checkSelectShipButton() {
        if (this.selectedClient != null) {
            this.selectClientButton.enable();
        } else {
            this.selectClientButton.disable();
        }
    }
    
}
