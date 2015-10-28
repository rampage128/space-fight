/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.scripting;

import com.jme3.bullet.control.PhysicsControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.DamageControl;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.mission.condition.KillCondition;
import de.jlab.spacefight.mission.condition.MissionCondition;
import de.jlab.spacefight.mission.condition.TicketCondition;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.systems.ShieldControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;

/**
 *
 * @author rampage
 */
public class ObjectScriptWrapper extends ScriptWrapper {
    
    private ObjectInfoControl object;
    private SimpleConfig properties = new SimpleConfig();
    private MissionWrapper missionWrapper;
    private SpaceAppState space;
    
    public ObjectScriptWrapper(ObjectInfoControl object, SpaceAppState space) {
        this.space = space;
        this.object = object;
        this.missionWrapper = new MissionWrapper(space);
        this.addReference("FlightControl", FlightControl.class);
        this.addReference("SensorControl", SensorControl.class);
        this.addReference("PhysicsControl", PhysicsControl.class);
        this.addReference("ShieldControl", ShieldControl.class);
        this.addReference("WeaponSystemControl", WeaponSystemControl.class);
        this.addReference("DamageControl", DamageControl.class);
        
        this.addReference("TicketCondition", TicketCondition.class);
        this.addReference("KillCondition", KillCondition.class);
    }

    // GENERAL METHODS    
    public void update(float tpf) {
        invoke("update", tpf, this.object);
    }
        
    public void hidePart(String name) {
        Spatial child = ((Node)this.object.getSpatial()).getChild(name);
        if (child != null) {
            child.setCullHint(Spatial.CullHint.Always);
        }
    }
    
    public void showPart(String name) {
        Spatial child = ((Node)this.object.getSpatial()).getChild(name);
        if (child != null) {
            child.setCullHint(Spatial.CullHint.Inherit);
        }
    }
    
    // SCRIPT ACCESS METHODS
    public SimpleConfig getProperties() {
        return this.properties;
    }
    
    public void remove() {
        this.object.remove();
    }
    
    public Vector3f getVector3f(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }
    
    public MissionWrapper getMission() {
        return this.missionWrapper;
    }
       
    public class MissionWrapper {
        
        private SpaceAppState space;
        
        public MissionWrapper(SpaceAppState space) {
            this.space = space;
        }
        
        public ObjectInfoControl[] getObjectsByType(String type) {
            return this.space.getMission().getObjectsByType(type);
        }
        
        public MissionCondition getCondition(Class<? extends MissionCondition> conditionClass) {
            return this.space.getMission().getCondition(conditionClass);
        }
        
        public void addCondition(Class<? extends MissionCondition> conditionClass) {
            this.space.getMission().addCondition(conditionClass);
        }
        
        public void end() {
            this.space.getMission().end();
        }
        
        public Faction[] getFactions() {
            return this.space.getMission().getFactions();
        }
        
        public void playMusic(String name, float volume, boolean loop) {
            this.space.getGame().getAudioManager().setMusicVolume(volume);
            this.space.getGame().getAudioManager().playMusic(name, 0, loop);
        }
        
        public void setMusicVolume(float volume) {
            this.space.getGame().getAudioManager().setMusicVolume(volume);
        }
        
    };
    
    @Override
    public ObjectScriptWrapper clone() {
        ObjectScriptWrapper newScript = new ObjectScriptWrapper(this.object, this.space);
        newScript.init(new JScript(this.script.getName(), this.script.getCode(), null, this.script.isCritical(), Game.get().getGamedataManager()));
        return newScript;
    }
    
}
