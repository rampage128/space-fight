/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.input;

import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.Trigger;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.gamedata.SimpleConfig;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rampage
 */
public final class InputManager {
    
    public static final String CONFIG_FILENAME = "input.cfg";
    
    private Game game;
    private SimpleConfig config;
    
    private HashMap<Class<? extends DisplayableEnum.Display>, InputListener> inputListenerMap = new HashMap<Class<? extends DisplayableEnum.Display>, InputListener>();
    
    public InputManager(Game game) {
        this.game = game;
        this.config = new SimpleConfig();
        this.loadConfig();
        this.flush();
        this.saveConfig();
    }
    
    public final void flush() {
        this.updateBindings(ViewInput.class);
        this.updateBindings(ShipInput.class);
        this.updateBindings(GameInput.class);
    }
    
    public final void loadConfig() {
        this.loadDefaults();
        this.config.loadFromFile(CONFIG_FILENAME);
    }
    
    public final void saveConfig() {
        this.config.saveToFile(CONFIG_FILENAME);
    }
    
    /* LOAD BINDINGS */
    private <T extends Enum<T> & DisplayableEnum.Display> void updateBindings(Class<T> input) {
        String[] commands = DisplayableEnum.getDisplayValues(input);
        for ( String command : commands ) {
            String inputValue = this.config.getValue(command, "");
            if ( this.game.getInputManager().hasMapping(command) ) {
                this.game.getInputManager().deleteMapping(command);
            }
            
            if (inputValue.indexOf("_") > -1) {
                try {
                    Trigger trigger = InputMapper.get(inputValue);
                    this.game.getInputManager().addMapping(command, trigger);
                } catch (Exception ex) {
                    Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, "Cannot retrieve mapping for input " + inputValue, ex);
                }
            }
        }
    }
    
    /* VIEW INPUT */
    private void loadDefaults() {
        // VIEW BINDINGS
        this.config.setValue(ViewInput.VIEW01.toString(), "KEY_F1");
        this.config.setValue(ViewInput.VIEW02.toString(), "KEY_F2");
        this.config.setValue(ViewInput.VIEW03.toString(), "KEY_F3");
        this.config.setValue(ViewInput.VIEW04.toString(), "KEY_F4");
        this.config.setValue(ViewInput.VIEW05.toString(), "KEY_F5");
        this.config.setValue(ViewInput.VIEW06.toString(), "KEY_F6");
        this.config.setValue(ViewInput.VIEW07.toString(), "KEY_F7");
        this.config.setValue(ViewInput.VIEW08.toString(), "KEY_F8");
        this.config.setValue(ViewInput.VIEW09.toString(), "KEY_F9");
        this.config.setValue(ViewInput.VIEW10.toString(), "KEY_F10");
        this.config.setValue(ViewInput.VIEW11.toString(), "KEY_F11");
        this.config.setValue(ViewInput.VIEW12.toString(), "KEY_F12");
        
        // SHIP BINDINGS
        this.config.setValue(ShipInput.RUDDERLEFT.toString(), "-MOUSE_X");
        this.config.setValue(ShipInput.RUDDERRIGHT.toString(), "+MOUSE_X");
        this.config.setValue(ShipInput.ELEVATORUP.toString(), "+MOUSE_Y");
        this.config.setValue(ShipInput.ELEVATORDOWN.toString(), "-MOUSE_Y");
        this.config.setValue(ShipInput.LIFTDOWN.toString(), "KEY_S");
        this.config.setValue(ShipInput.LIFTUP.toString(), "KEY_W");
        this.config.setValue(ShipInput.STRAFELEFT.toString(), "KEY_A");
        this.config.setValue(ShipInput.STRAFERIGHT.toString(), "KEY_D");
        this.config.setValue(ShipInput.ROLLLEFT.toString(), "KEY_Q");
        this.config.setValue(ShipInput.ROLLRIGHT.toString(), "KEY_E");
        this.config.setValue(ShipInput.FORWARD.toString(), "KEY_LSHIFT");
        this.config.setValue(ShipInput.BACKWARD.toString(), "KEY_LCONTROL");
        this.config.setValue(ShipInput.STEERING_DEBUG.toString(), "KEY_F");
        
        this.config.setValue(ShipInput.WEAPON_PRIMARY.toString(), "MOUSEBUTTON_LEFT");
        this.config.setValue(ShipInput.WEAPON_SECONDARY.toString(), "MOUSEBUTTON_RIGHT");
        this.config.setValue(ShipInput.WEAPON_NEXTSECONDARY.toString(), "KEY_X");
        this.config.setValue(ShipInput.WEAPON_PRIMARYMODE.toString(), "KEY_Y");
        
        this.config.setValue(ShipInput.PERK_USE.toString(), "MOUSEBUTTON_MIDDLE");
        
        this.config.setValue(ShipInput.THROTTLE0.toString(), "KEY_1");
        this.config.setValue(ShipInput.THROTTLE33.toString(), "KEY_2");
        this.config.setValue(ShipInput.THROTTLE66.toString(), "KEY_3");
        this.config.setValue(ShipInput.THROTTLE100.toString(), "KEY_4");
        this.config.setValue(ShipInput.CRUISE.toString(), "KEY_5");
        this.config.setValue(ShipInput.GLIDE.toString(), "KEY_LMENU");
        
        this.config.setValue(ShipInput.TARGET_NEXTENEMY.toString(), "KEY_R");
        this.config.setValue(ShipInput.TARGET_NEXT.toString(), "KEY_T");
        this.config.setValue(ShipInput.TARGET_SMART.toString(), "KEY_SPACE");
        
        this.config.setValue(ShipInput.COM_DEFENSIVE.toString(), "KEY_V");
        this.config.setValue(ShipInput.COM_OFFENSIVE.toString(), "KEY_B");
        
        // GAME BINDINGS
        this.config.setValue(GameInput.MENU.toString(), "KEY_ESCAPE");
        this.config.setValue(GameInput.CONSOLE.toString(), "KEY_APOSTROPHE");
        this.config.setValue(GameInput.SCOREBOARD.toString(), "KEY_TAB");
    }
    
    /**
     * toggles an InputListener for a special input enum.
     * @param input Class of the Enum to get commands from (needs to extend DisplayableEnum.Display)
     * @param newInputListener InputListener to register with the Enum's commands
     */
    public <T extends Enum<T> & DisplayableEnum.Display> void toggleInput(Class<T> input, InputListener newInputListener) {
        // REMOVE OLD INPUTLISTENER IF AVAILABLE
        InputListener oldListener = this.inputListenerMap.get(input);
        if ( oldListener != null ) {
            this.game.getInputManager().removeListener(oldListener);
        }
        
        if ( newInputListener != null ) {
            // ADD NEW INPUTLISTENER IF AVAILABLE
            this.game.getInputManager().addListener(
                newInputListener, DisplayableEnum.getDisplayValues(input)
            );
            this.inputListenerMap.put(input, newInputListener);
        } else {
            // REMOVE ANY INPUTLISTENER ON THAT INPUT
            this.inputListenerMap.remove(input);
        } 
    }
    
    public void cleanup() {
        for ( InputListener listener : this.inputListenerMap.values() ) {
            this.game.getInputManager().removeListener(listener);
        }
    }
    
}
