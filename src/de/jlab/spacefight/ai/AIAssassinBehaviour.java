/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ai;

import com.jme3.math.Vector3f;
import de.jlab.spacefight.basic.ChanceCalculator;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.DistressCall;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;

/**
 *
 * @author rampage
 */
public class AIAssassinBehaviour extends AIDogfightBehaviour {

    TargetInformation targetInformation = null;
    
    public AIAssassinBehaviour(AIShipControl ai, Task task) {
        super(ai, task);
    }
    
    @Override
    protected void updateBehaviour(float tpf) {
        if ( getAI().getWeapons().getTarget() == null ) {
            setState(STATE_NOTARGET);
        }
        
        ObjectInfoControl target = getTask().getTargetObject(getAI().getMission());
        if (getTask().isDone(getAI().getSpatial(), getAI().getMission())) {
            getAI().getObjectInfo().gotoNextTask();
            return;
        }
               
        //if ( this.targetInformation == null ) {
        this.targetInformation = target.getTargetInformation(getAI().getObjectInfo(), null); //new TargetInformation(target.getSpatial());
        //}
        //this.targetInformation.update(getAI().getSpatial(), null);
        
        switch ( getState() ) {
            case STATE_NOTARGET:
                if ( target == null ) {
                    super.updateBehaviour(tpf);
                    return;
                }

                float distance = this.targetInformation.getDirection(getAI().getObjectInfo()).length();
                
                if ( distance > getAI().getSensors().getRange() ) {
                    setProximityDistance(SensorControl.VAR_PROXIMITYDISTANCE_AUTO);
                    if ( !getAI().followFormation(tpf, false) ) {
                        setProximityDistance(SensorControl.VAR_PROXIMITYDISTANCE_AUTO);
                        // IF THERE IS NO LEADER TO FOLLOW WE MOVE TO THE TARGET
                        float throttle = 1;
                        if ( getAI().getFlight() != null && !getAI().getFlight().formationComplete() )
                            throttle = 0.66f;
                        getAI().getFlightControl().moveTo(target.getSpatial().getWorldTranslation(), getAI().getObjectInfo().getSize() / 2, throttle, 0f, tpf);
                    }
                } else {
                    // WE TARGET THE ENEMY!
                    if ( getAI().getSensors().targetEnemy(target.getId()) )
                        setState(STATE_ATTACK);
                }
                break;
            case STATE_ATTACK:                
                if (target == null) {
                    super.updateBehaviour(tpf);
                    getAI().getObjectInfo().gotoNextTask();
                    return;
                }
                setProximityDistance(30);
                if ( getAI().getWeapons().getSecondarySlot().weaponCount() > 0 && getAI().getWeapons().getSecondaryRange() > 0 ) {
                    if ( this.targetInformation.getDirection(getAI().getObjectInfo()).length() <= getAI().getWeapons().getSecondaryRange() ) {
                        if (this.targetInformation.shotsForKill(getAI().getObjectInfo(), getAI().getWeapons().getSecondarySlot().getWeapon()) >= 1f) {
                            getAI().getFlightControl().setThrottle(0f);
                            getAI().getFlightControl().turnTo(target.getSpatial().getWorldTranslation(), target.getUpside(), tpf);
                            getAI().getWeapons().fireSecondary();
                        } else {
                            super.updateBehaviour(tpf);
                        }
                    } else {
                        Vector3f targetPosition = target.getFacing().negate().normalizeLocal().mult(getAI().getWeapons().getSecondaryRange() - 100);
                        getAI().getFlightControl().moveTo(targetPosition, 0f, 1f, target.getLinearVelocity().length(), tpf);
                    }
                } else {
                    super.updateBehaviour(tpf);
                }
            default:
                super.updateBehaviour(tpf);
        }
    }
    
}
