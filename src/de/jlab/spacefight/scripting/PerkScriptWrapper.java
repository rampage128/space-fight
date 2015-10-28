/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.scripting;

import com.jme3.bullet.control.PhysicsControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.effect.EffectControl;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.mission.structures.Spawn;
import de.jlab.spacefight.systems.ShieldControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.perks.Perk;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;

/**
 *
 * @author rampage
 */
public class PerkScriptWrapper extends ScriptWrapper {
    
    private Perk perk;
    private SimpleConfig properties = new SimpleConfig();
    
    public PerkScriptWrapper(Perk perk) {
        this.perk = perk;
        this.addReference("FlightControl", FlightControl.class);
        this.addReference("SensorControl", SensorControl.class);
        this.addReference("PhysicsControl", PhysicsControl.class);
        this.addReference("ShieldControl", ShieldControl.class);
        this.addReference("WeaponSystemControl", WeaponSystemControl.class);
    }

    // GENERAL METHODS
    public void reset(ObjectInfoControl origin) {
        invoke("reset", this.perk, origin);
    }

    public void add(ObjectInfoControl origin) {
        invoke("add", this.perk, origin);
    }
    
    public void remove(ObjectInfoControl origin) {
        invoke("remove", this.perk, origin);
    }
    
    // ONCE-PERK METHODS
    public void use(ObjectInfoControl origin) {
        invoke("use", this.perk, origin);
    }
    
    // CONTINUOUS-PERK METHODS
    public void enable(ObjectInfoControl origin) {
        invoke("enable", this.perk, origin);
    }
    
    public void updateActive(float tpf, ObjectInfoControl origin) {
        invoke("updateActive", tpf, this.perk, origin);
    }
    
    public void disable(ObjectInfoControl origin) {
        invoke("disable", this.perk, origin);
    }
    
    // SCRIPT ACCESS METHODS
    public SimpleConfig getProperties() {
        return this.properties;
    }
    
    public ObjectInfoControl spawnObject(String name, ObjectInfoControl origin) {
        SpaceAppState space = Game.get().getStateManager().getState(SpaceAppState.class);
        if (space != null) {
            //ObjectInfoControl origin = space.getMission().getObject(originId);
            ObjectInfoControl object = Game.get().getGamedataManager().loadObject(false, name, this.perk.getName() + "_" + name.hashCode(), space);
            object.setFaction(origin.getFaction());
            object.setSpawn(new Spawn(origin.getPosition().add(origin.getRotation().mult(Vector3f.UNIT_Z.negate().multLocal(origin.getSize() * 2 + object.getSize() * 2))), origin.getRotation()));
            object.spawnObject();
            if (space.getMission() != null) {
                space.getMission().addObject(object);
            }
            return object;
        }
        return null;
    }
    
    public void playEffect(String name, Vector3f pos, ObjectInfoControl parent, float scale, float speed) {
        SpaceAppState space = Game.get().getStateManager().getState(SpaceAppState.class);
        if (space != null) {
            Node parentNode = space.getSpace();
            if (parent != null) {
                parentNode = (Node)parent.getSpatial();
            }
            EffectControl effect = Game.get().getGamedataManager().loadEffect(name, parentNode, space);
            effect.start(pos, scale, speed);
        }
    }
    
    @Override
    public PerkScriptWrapper clone() {
        PerkScriptWrapper newScript = new PerkScriptWrapper(this.perk);
        newScript.init(new JScript(this.script.getName(), this.script.getCode(), null, this.script.isCritical(), Game.get().getGamedataManager()));
        return newScript;
    }
    
}
