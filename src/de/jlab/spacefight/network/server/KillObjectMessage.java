/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.DamageControl;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.DamageInformation;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.network.NetworkClient;

/**
 *
 * @author rampage
 */
@Serializable
public class KillObjectMessage extends ServerMessage {

    private String targetId;
    private String originId;
    private String damageType;
    private String damageName;
    private float damage;
    
    public KillObjectMessage() {}
    
    public KillObjectMessage(Kill kill) {
        this.targetId = kill.getTarget().getId();
        this.originId = kill.getOrigin().getId();
        this.damageType = kill.getDamageType();
        this.damageName = kill.getDamageName();
        this.damage = kill.getDamage();
    }
    
    @Override
    public void messageReceived(NetworkClient client) {
        SpaceAppState space = client.getGame().getStateManager().getState(SpaceAppState.class);
        if (space == null || space.getMission() == null) {
            return;
        }

        ObjectInfoControl targetObject = space.getMission().getObject(this.targetId);
        ObjectInfoControl originObject = space.getMission().getObject(this.originId);
        
        if (targetObject == null || originObject == null) {
            return;
        }
        
        Kill kill = new Kill(new DamageInformation(this.damageType, this.damageName, this.damage, originObject, targetObject, Vector3f.ZERO.clone(), Vector3f.ZERO.clone()));
        DamageControl damageControl = targetObject.getObjectControl(DamageControl.class);
        if (damageControl != null) {
            damageControl.kill(kill);
        } else {
            space.killObject(kill);
        }
    }

    @Override
    public boolean mustBeReliable() {
        return true;
    }
    
}
