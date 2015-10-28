/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.console.commands;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.systems.PhysicsControl;
import de.jlab.spacefight.ui.ingame.console.CommandHandler;
import de.jlab.spacefight.ui.ingame.console.ExternalConsoleCommand;

/**
 *
 * @author rampage
 */
public class SpawnCommand extends ExternalConsoleCommand {

    public SpawnCommand(CommandHandler commandHandler, SpaceAppState space) {
        super(commandHandler, space);
    }
    
    @Override
    public void execute(String[] args) {
        ObjectInfoControl object = null;
        if (args.length > 1) {
            try {
                object = getSpace().getGame().getGamedataManager().loadObject(false, args[1], "spawned_" + System.currentTimeMillis(), getSpace());
            } catch (Exception e) {
                print(e.getMessage());
                return;
            }
        }
        
        ObjectInfoControl client = getSpace().getPlayer().getClient(); //getSpace().getMission().getObject(getSpace().getPlayer().getObjectId());       
        Vector3f position = client.getSpatial().getWorldTranslation().clone();
        Quaternion rotation = client.getSpatial().getWorldRotation().clone();
        
        position.addLocal(rotation.multLocal(Vector3f.UNIT_Z.clone().multLocal(100)));
        object.getSpatial().setLocalTranslation(position);
        object.getSpatial().setLocalRotation(rotation);
        PhysicsControl physics = object.getObjectControl(PhysicsControl.class);
        if ( physics != null ) {
            physics.setPhysicsLocation(position);
            physics.setPhysicsRotation(rotation);
        }
        getSpace().addObject(object);
    }

    @Override
    public String[] getVariants() {
        return new String[] {
            "spawn"
        };
    }

    @Override
    public String getDescription() {
        return "spawns an object";
    }
    
}
