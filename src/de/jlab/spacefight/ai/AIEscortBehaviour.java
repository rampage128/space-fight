/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ai;

import com.jme3.math.Vector3f;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;

/**
 *
 * @author rampage
 */
public class AIEscortBehaviour extends AIDogfightBehaviour {

    TargetInformation targetInformation = null;
    
    public AIEscortBehaviour(AIShipControl ai, Task task) {
        super(ai, task);
    }
    
    @Override
    protected void updateBehaviour(float tpf) {        
        super.updateBehaviour(tpf);

        ObjectInfoControl target = getTask().getTargetObject(getAI().getMission());
        if (getTask().isDone(getAI().getSpatial(), getAI().getMission())) {
            getAI().getObjectInfo().gotoNextTask();
            return;
        }
        
        float distance = target.distanceTo(getAI().getObjectInfo());
        if (distance > getAI().getSensors().getRange()) {
            getAI().getWeapons().setTarget(null);
        }
        
        switch (getState()) {
            case STATE_NOTARGET:
                setProximityDistance(SensorControl.VAR_PROXIMITYDISTANCE_AUTO);

                this.targetInformation = target.getTargetInformation(getAI().getObjectInfo(), null);
                
                Vector3f targetPosition = new Vector3f(0f, 1f, 1f).normalizeLocal().multLocal(target.getSize() * 2 + getAI().getObjectInfo().getSize() * 2).addLocal(target.getPosition());
                if ( !getAI().followFormation(tpf, false) ) {
                    if (getAI().getFlightControl().moveTo(targetPosition, getAI().getObjectInfo().getSize() / 2, 1, 0f, tpf)) {
                        getAI().getFlightControl().turnTo(getAI().getSpatial().getWorldTranslation().add(target.getFacing()), target.getUpside(), tpf);
                        float targetVelocity = this.targetInformation.getObject().getLinearVelocity().length();
                        if (targetVelocity > 0) {
                            getAI().getFlightControl().setThrottle(targetVelocity / getAI().getFlightControl().getTopspeed());
                        } else {
                            getAI().getFlightControl().setThrottle(0);
                        }
                    }
                }

                /*
                if ( distance > getAI().getObjectInfo().getSize() + target.getSize() + 200 ) {
                    
                        getAI().getFlightControl().moveTo(target.getSpatial().getWorldTranslation(), getAI().getObjectInfo().getSize() / 2, 1, 0f, tpf);
                    }
                } else {
                    // WAYPOINT REACHED!
                    getAI().getFlightControl().turnTo(getAI().getSpatial().getWorldTranslation().add(target.getFacing()), target.getUpside(), tpf);
                    getAI().getFlightControl().setThrottle(this.targetInformation.getObject().getLinearVelocity().length() / getAI().getFlightControl().getTopspeed());
                }
                */
                break;
        }
    }
    
}
