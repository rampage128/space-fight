/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.structures;

import com.jme3.audio.AudioNode;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.audio.AudioEffect;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import java.util.ArrayList;

/**
 *
 * @author rampage
 */
public class DistressCall {
    
    public static final int TYPE_DEFENSIVE = 0;
    public static final int TYPE_OFFENSIVE = 1;
    
    private int type = TYPE_DEFENSIVE;
    private ObjectInfoControl[] targets;
    private ObjectInfoControl source;
    
    private long broadcastTime = 0;
    
    public DistressCall(int type, ObjectInfoControl[] targets, ObjectInfoControl source) {
        this.type = type;
        this.targets = targets;
        this.source = source;
    }

    public Faction getFaction() {
        return this.source.getFaction();
    }
    
    public ObjectInfoControl getSource() {
        return this.source;
    }
    
    public float getBroadcastTime() {
        return this.broadcastTime;
    }
    
    public ObjectInfoControl[] getTargets() {
        return this.targets;
    }
    
    public static void offensiveDistressCall(ObjectInfoControl source, AbstractMission mission) {
        WeaponSystemControl weapons = source.getObjectControl(WeaponSystemControl.class);
        TargetInformation target = weapons.getTarget();
        if (target != null) {
            new DistressCall(TYPE_OFFENSIVE, new ObjectInfoControl[] { target.getObject() }, source).broadcast(mission);
        }
    }
    
    public static void defensiveDistressCall(ObjectInfoControl source, AbstractMission mission) {
        SensorControl sensors = source.getObjectControl(SensorControl.class);
        ArrayList<ObjectInfoControl> lockers = sensors.getLockList();
        new DistressCall(TYPE_DEFENSIVE, lockers.toArray(new ObjectInfoControl[0]), source).broadcast(mission);
    }
       
    public void broadcast(AbstractMission mission) {
        StringBuilder name = new StringBuilder();
        if (TYPE_OFFENSIVE == this.type) {
            name.append("sounds/distress/offensive-1-1.ogg");
        } else {
            name.append("sounds/distress/defensive-1-1.ogg");
        }
        
        if (Game.get().getPlayer().getClient() != null && Game.get().getPlayer().getClient().getFaction() == this.source.getFaction()) {
            AudioNode launchWarningNode = new AudioNode(Game.get().getAssetManager(), name.toString(), false);
            launchWarningNode.setLooping(false);
            launchWarningNode.setVolume(1f);
            //this.targetWarning.setMaxDistance(200f);
            //this.targetWarning.setRefDistance(10f);
            launchWarningNode.setPositional(false);
            launchWarningNode.setDirectional(false);
            AudioEffect audio = new AudioEffect(launchWarningNode);
            Game.get().getAudioManager().playSound(audio);
        }
        mission.addDistressCall(this);
    }
    
    public void update(float tpf) {
        this.broadcastTime += tpf;
    }
    
}
