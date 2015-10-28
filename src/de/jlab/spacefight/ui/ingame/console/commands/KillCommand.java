/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console.commands;

import com.jme3.math.Vector3f;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.DamageInformation;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.network.NetworkAppState;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import de.jlab.spacefight.ui.ingame.console.CommandHandler;
import de.jlab.spacefight.ui.ingame.console.ExternalConsoleCommand;

/**
 *
 * @author rampage
 */
public class KillCommand extends ExternalConsoleCommand {

    public KillCommand(CommandHandler commandHandler, SpaceAppState space) {
        super(commandHandler, space);
    }

    @Override
    public void execute(String[] args) {
        ObjectInfoControl target = null;
        ObjectInfoControl client = getSpace().getPlayer().getClient(); //getSpace().getMission().getObject(getSpace().getPlayer().getObjectId());
        if (client != null) {
            if (args.length > 1) {
                if ("target".equalsIgnoreCase(args[1])) {
                    WeaponSystemControl weapons = client.getObjectControl(WeaponSystemControl.class);
                    if (weapons != null) {
                        TargetInformation targetInfo = weapons.getTarget();
                        if (targetInfo != null) {
                            target = targetInfo.getObject();
                        }
                    }
                } else {
                    ObjectInfoControl objectInfo = getSpace().getMission().getObject(args[1]);
                    if (objectInfo != null) {
                        target = objectInfo;
                    }
                }
            } else {
                target = client;
            }
        }
        
        if (target != null) {
            Kill kill = new Kill(new DamageInformation("weapon", "ADMIN", Float.MAX_VALUE, client, target, Vector3f.ZERO, target.getPosition()));
            if (NetworkAppState.killObject(getSpace().getGame(), kill)) {
                print("Killing " + target.getId());
                getSpace().killObject(kill);
            } else {
                print("Sent kill request to Server for " + target.getId() + "...");
            }
        } else {
            print("Target not found!");
        }
    }

    @Override
    public String[] getVariants() {
        return new String[] {
            "kill",
            "kill target"
        };
    }

    @Override
    public String getDescription() {
        return "kills yourself or another target";
    }
}
