/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import com.jme3.input.controls.ActionListener;
import de.jlab.spacefight.AbstractClientControl;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.debug.DebugContext;
import de.jlab.spacefight.input.GameInput;
import de.jlab.spacefight.mission.KillListener;
import de.jlab.spacefight.mission.condition.TicketCondition;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.player.PlayerControl;
import de.jlab.spacefight.systems.perks.Perk;
import de.jlab.spacefight.systems.perks.PerkControl;
import de.jlab.spacefight.systems.weapons.WeaponSlot;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;

import de.jlab.spacefight.ui.controls.lists.killlist.KillListModel;
import de.jlab.spacefight.ui.controls.lists.scoreboard.ScoreboardModel;
import de.jlab.spacefight.ui.controls.progressbar.ProgressBarControl;
import de.jlab.spacefight.ui.ingame.console.commands.ClearCommand;
import de.jlab.spacefight.ui.ingame.console.CommandHandler;
import de.jlab.spacefight.ui.ingame.console.commands.CvarsListCommand;
import de.jlab.spacefight.ui.ingame.console.commands.HelpCommand;
import de.jlab.spacefight.ui.ingame.console.commands.KillCommand;
import de.jlab.spacefight.ui.ingame.console.commands.QuitCommand;
import de.jlab.spacefight.ui.ingame.console.commands.SpawnCommand;
import de.jlab.spacefight.ui.ingame.console.commands.ToggleSystemCommand;
import de.jlab.spacefight.ui.ingame.console.commands.VersionCommand;
import de.jlab.spacefight.ui.ingame.console.cvars.DebugContextCvar;
import de.jlab.spacefight.ui.ingame.console.cvars.DebugCvar;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Console;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyInputMapping;
import de.lessvoid.nifty.input.NiftyStandardInputEvent;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * UI-controller during gameplay.
 * 
 * @author rampage
 */
public class Ingame implements ScreenController, ActionListener, KillListener, NiftyInputMapping, KeyInputHandler {

    public static final int COMMAND_MENU = 1;
    
    private Nifty _nifty;
    private SpaceAppState space;
    
    private Screen screen;
    private CommandHandler commandHandler;
    
    private int _command = 0;
    
    private ListBox<ScoreboardModel> factionscoreboard;
    private ListBox<ScoreboardModel> pilotscoreboard;
    private ListBox<KillListModel> killlist;
    
    private ListBox<String> primaryWeaponList;
    private ListBox<String> secondaryWeaponList;
    private ProgressBarControl primaryTimer;
    private ProgressBarControl secondaryTimer;
    private Label perkName;
    private ProgressBarControl perkTimer;
    private ProgressBarControl perkCooldown;
    
    public Ingame() {

    }
        
    public void bind(Nifty nifty, Screen screen) {
        _nifty = nifty;
        this.screen = screen;
        
        this.factionscoreboard = screen.findNiftyControl("factionscoreboard", ListBox.class);
        this.pilotscoreboard = screen.findNiftyControl("pilotscoreboard", ListBox.class);
        this.killlist = screen.findNiftyControl("killlist", ListBox.class);
        
        this.primaryWeaponList = screen.findNiftyControl("primaryweaponlist", ListBox.class);
        this.secondaryWeaponList = screen.findNiftyControl("secondaryweaponlist", ListBox.class);
        this.primaryTimer = screen.findControl("primarytimer", ProgressBarControl.class);
        this.secondaryTimer = screen.findControl("secondarytimer", ProgressBarControl.class);
        this.perkName = screen.findNiftyControl("perkname", Label.class);
        this.perkTimer = screen.findControl("perktimer", ProgressBarControl.class);
        this.perkCooldown = screen.findControl("perkcooldown", ProgressBarControl.class);
    }

    public void onStartScreen() {
        this.screen.addPreKeyboardInputHandler(this, this);
        this.space = Game.get().getStateManager().getState(SpaceAppState.class);
        
        Game.get().getInputManager().setCursorVisible(false);
        space.getInputManager().toggleInput(GameInput.class, this);
        space.setEnabled(true);
        
        this.space.getMission().addKillListener(this);
        
        Console console = screen.findNiftyControl("console", Console.class);
        this.commandHandler = new CommandHandler(console, _nifty, space);
        
        this.commandHandler.registerCommand(new HelpCommand(this.commandHandler, space));
        this.commandHandler.registerCommand(new CvarsListCommand(this.commandHandler, space));
        this.commandHandler.registerCommand(new ClearCommand(this.commandHandler, space));
        this.commandHandler.registerCommand(new VersionCommand(this.commandHandler, space));
        this.commandHandler.registerCommand(new SpawnCommand(this.commandHandler, space));
        this.commandHandler.registerCommand(new KillCommand(this.commandHandler, space));
        this.commandHandler.registerCommand(new ToggleSystemCommand(this.commandHandler, space));
        //this.commandHandler.registerCommand(new DebugCommand(this.commandHandler, _space));
        this.commandHandler.registerCommand(new QuitCommand(this.commandHandler, space));
        
        this.commandHandler.registerCvar(new DebugCvar(space));
        for (DebugContext context : DebugContext.values()) {
            this.commandHandler.registerCvar(new DebugContextCvar(context, space));    
        }
        
        Element layer = this.screen.findElementById("consolelayer");
        layer.hideWithoutEffect();
        this.commandHandler.getConsole().setEnabled(false);
        
        layer = this.screen.findElementById("scoreboardlayer");
        layer.hideWithoutEffect();
    }

    public void onEndScreen() {
        this.screen.removePreKeyboardInputHandler(this);
        this.space.getMission().removeKillListener(this);
        space.getInputManager().toggleInput(GameInput.class, null);
        switch(_command) {
            case COMMAND_MENU:
                break;
        }
    }
       
    public void gotoMenu() {
        _nifty.gotoScreen("spawnscreen");
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if ("MENU".equalsIgnoreCase(name) && isPressed) {
            gotoMenu();
        } else if ("CONSOLE".equalsIgnoreCase(name) && isPressed) {
            Element layer = this.screen.findElementByName("consolelayer");
            if ( layer.isVisible() ) {
                layer.hide();
                this.screen.processAddAndRemoveLayerElements();
                this.commandHandler.getConsole().setEnabled(false);
            } else {
                layer.show();
                this.screen.processAddAndRemoveLayerElements();
                this.commandHandler.getConsole().setEnabled(true);
                this.commandHandler.getConsole().getTextField().setText("");
                this.commandHandler.getConsole().getTextField().setFocus();
            }
        } else if ("SCOREBOARD".equalsIgnoreCase(name)) {
            toggleScoreboard(isPressed);
        } else if ("STATS".equalsIgnoreCase(name)) {
            Game.get().setDisplayStatView(isPressed);
        }
    }
    
    private void toggleScoreboard(boolean enabled) {
        Element layer = this.screen.findElementByName("scoreboardlayer");
        if (enabled == layer.isVisible())
            return;
        enablePlayerControls(!enabled);
        Game.get().getInputManager().setCursorVisible(enabled);
        if (enabled) {
            layer.show();
            this.fillScoreBoard();
        } else {
            layer.hide();
        }
    }
    
    private void enablePlayerControls(boolean enabled) {
        ObjectInfoControl currentClient = Game.get().getPlayer().getClient();
        if (currentClient != null) {
            PlayerControl playerControl = currentClient.getSpatial().getControl(PlayerControl.class);
            if (playerControl != null) {
                playerControl.setEnabled(enabled);
            }
        }
    }
        
    private void fillScoreBoard() {
        // FILL FACTION SCOREBOARD
        Faction[] factions = this.space.getMission().getFactions();        
        Arrays.sort(factions, new Comparator<Faction>() {
            public int compare(Faction f1, Faction f2) {
                return f2.getScore() - f1.getScore();
            }
        });
        this.factionscoreboard.clear();
        TicketCondition ticketCondition = this.space.getMission().getCondition(TicketCondition.class);
        for (Faction faction : factions) {
            Color color = Color.WHITE;
            if (faction.getColor() != null) {
                color = new Color(faction.getColor().getRed(), faction.getColor().getGreen(), faction.getColor().getBlue(), faction.getColor().getAlpha());
            }
            int score = faction.getScore();
            if (ticketCondition != null) {
                score = ticketCondition.getTickets(faction.getId());
            }
            this.factionscoreboard.addItem(new ScoreboardModel(faction.getName(), faction.getKillCount(), faction.getDeathCount(), score, color));
        }
        
        // FILL CLIENT SCOREBOARD
        ObjectInfoControl[] clients = this.space.getMission().getClients();
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
            if (faction.getColor() != null) {
                factionColor = new Color(faction.getColor().getRed(), faction.getColor().getGreen(), faction.getColor().getBlue(), faction.getColor().getAlpha());
            }
            this.pilotscoreboard.addItem(new ScoreboardModel(clientControl.getName(), clientControl.getKillCount(), clientControl.getDeathCount(), clientControl.getScore(), factionColor));
            //this.pilotscoreboard.addItem(factionItem.toString());
        }
        
        /*
        Faction[] factions = _space.getMission().getFactions();
        int i = 0;
        if (factions != null && factions.length > 0) {
            Element loopContainer = container;
            for (Faction faction : factions) {
                if (factions.length > 1 && (i == 0 || i % 2 == 0)) {
                    String id = "scoreboardrow_" + (int)Math.floor(i / 2f);
                    PanelBuilder pb = new PanelBuilder(id);
                    pb.childLayoutHorizontal();
                    pb.width("100%");
                    pb.build(_nifty, screen, container);
                    loopContainer = screen.findElementByName(id);
                }
                
                ControlBuilder builder = new ControlBuilder("faction_" + faction.getId(), "scoreboard" );
                builder.width("50%");
                builder.backgroundColor("#f00f");
                builder.build(_nifty, screen, loopContainer);

                loopContainer.layoutElements();
                container.layoutElements();
                panel.setUp(0, 5, 0, 50, ScrollPanel.AutoScroll.OFF);
                i++;
            }
        }
        */
    }

    public void onKill(Kill kill) {
        List<KillListModel> items = this.killlist.getItems();
        int removeCounter = items.size() - this.killlist.getDisplayItemCount();
        if (removeCounter > 0) {
            for (int i = 0; i < removeCounter; i++) {
                this.killlist.removeItemByIndex(0);
            }
        }
        this.killlist.addItem(new KillListModel(kill));
    }

    public void update(float tpf) {
        List<KillListModel> items = this.killlist.getItems();
        for (int i = 0; i < items.size(); i++) {
            KillListModel item = items.get(i);
            if (item.isTimedOut(3)) {
                this.killlist.removeItem(item);
            }
        }
        
        updateClientData();
    }
    
    public void updateClientData() {
        ObjectInfoControl client = Game.get().getPlayer().getClient();
        if (client == null) {
            return;
        }
        
        this.primaryWeaponList.clear();
        this.secondaryWeaponList.clear();
        WeaponSystemControl weapons = client.getObjectControl(WeaponSystemControl.class);
        if (weapons != null) {
            int i = 0;
            WeaponSlot[] slots = weapons.getPrimarySlots();
            int primarySlot = 0;
            for (WeaponSlot slot : slots) {
                this.primaryWeaponList.addItem((i+1) + " " + slot.getWeapon().getName());
                if (slot == weapons.getPrimarySlot()) {
                    primarySlot = i;
                }
                i++;
            }
            if (weapons.getPrimaryMode() == WeaponSystemControl.MODE_HALF) {
                for (int j = 0; j < weapons.getPrimarySlotCount() / 2; j++) {
                    if (primarySlot+j < this.primaryWeaponList.itemCount()) {
                        this.primaryWeaponList.selectItemByIndex(primarySlot+j);
                    } else {
                        this.primaryWeaponList.selectItemByIndex(primarySlot+j - this.primaryWeaponList.itemCount());                                
                    }
                }
            } else if (weapons.getPrimaryMode() == WeaponSystemControl.MODE_FULL) {
                for (int j = 0; j < this.primaryWeaponList.itemCount(); j++) {
                    this.primaryWeaponList.selectItemByIndex(j);
                }
            } else {
                this.primaryWeaponList.selectItemByIndex(primarySlot);
            }
            
            
            i = 0;
            slots = weapons.getSecondarySlots();
            for (WeaponSlot slot : slots) {
                this.secondaryWeaponList.addItem((i+1) + " " + slot.getWeapon().getName() + " (" + slot.weaponCount() + ")");
                if (slot == weapons.getSecondarySlot()) {
                    this.secondaryWeaponList.selectItemByIndex(i);
                }
                i++;
            }
            
            this.primaryTimer.setProgress(weapons.getPrimaryEnergy());
            this.secondaryTimer.setProgress((WeaponSystemControl.VAR_SECONDARY_COOLDOWN - weapons.getSecondaryCooldown()) / WeaponSystemControl.VAR_SECONDARY_COOLDOWN);
        }
        
        PerkControl perkControl = client.getObjectControl(PerkControl.class);
        if (perkControl != null) {
            Perk perk = perkControl.getPerk();
            if (perk != null) {
                this.perkName.setText(perk.getName());
                this.perkTimer.setProgress(perk.getEnergy());
                this.perkCooldown.setProgress((perk.getCooldownTime() - perk.getCooldown()) / perk.getCooldownTime());
            }
        }
    }

    public NiftyInputEvent convert(KeyboardInputEvent inputEvent) {
        if (inputEvent.getKey() == KeyboardInputEvent.KEY_TAB) { // or whatever PHYSICAL event you need ...
            if (inputEvent.isKeyDown()) {
                return NiftyStandardInputEvent.NextInputElement; // this is the LOGICAL event
            } else {
                return NiftyStandardInputEvent.PrevInputElement;
            }
        }
        return null;
    }

    public boolean keyEvent(NiftyInputEvent inputEvent) {
        if (NiftyStandardInputEvent.NextInputElement.equals(inputEvent)) {
            this.toggleScoreboard(true);
            return true;
        } else if (NiftyStandardInputEvent.PrevInputElement.equals(inputEvent)) {
            this.toggleScoreboard(false);
            return true;
        }
        return false;
    }
    
}
