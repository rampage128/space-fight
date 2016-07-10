/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console.commands;

import com.jme3.scene.control.Control;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.SystemControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.ui.ingame.console.CommandHandler;
import de.jlab.spacefight.ui.ingame.console.ExternalConsoleCommand;

/**
 *
 * @author rampage
 */
public class ToggleSystemCommand extends ExternalConsoleCommand {
    
    public ToggleSystemCommand(CommandHandler commandHandler, SpaceAppState space) {
        super(commandHandler, space);
    }

    @Override
    public void execute(String[] args) {
        ObjectInfoControl target = null;
        ObjectInfoControl client = getSpace().getPlayer().getClient(); //getSpace().getMission().getObject(getSpace().getPlayer().getObjectId());       
        if (client != null) {
            if (args.length > 2) {
                if ("target".equalsIgnoreCase(args[2])) {
                    WeaponSystemControl weapons = client.getObjectControl(WeaponSystemControl.class);
                    if (weapons != null) {
                        TargetInformation targetInfo = weapons.getTarget();
                        if (targetInfo != null) {
                            target = targetInfo.getObject();
                        }
                    }
                } else {
                    ObjectInfoControl objectInfo = getSpace().getMission().getObject(args[2]);
                    if (objectInfo != null) {
                        target = objectInfo;
                    }
                }
            } else {
                target = client;
            }
        }
        
        if (target != null) {
            String systemName = null;
            if (args.length > 1) {
                systemName = args[1];
            }
            
            //if ("FlightControl".equalsIgnoreCase(systemName)) {
                //system = target.getObjectControl(FlightControl.class);
            //}

            SystemControl system = null;
            for (int i = 0; i < target.getSpatial().getNumControls(); i++) {
                Control control = target.getSpatial().getControl(i);
                if (control.getClass().getSimpleName().equals(systemName) && control instanceof SystemControl) {
                    system = (SystemControl)control;
                    break;
                }
            }
            
            if (system != null) {
                system.setEnabled(!system.isEnabled());
                print((system.isEnabled() ? "Enabled " : "Disabled ") + system.getClass().getSimpleName() + " on " + target.getId());
            } else {
                print("System not found!");
            }
        } else {
            print("Target not found!");
        }
    }

    @Override
    public String[] getVariants() {
        return new String[] {
            "toggleSystem (SystemControl)",
            "toggleSystem target (SystemControl)"
        };
    }

    @Override
    public String getDescription() {
        return "Enables/Disables a SystemControl inside an Object.";
    }
    
}
