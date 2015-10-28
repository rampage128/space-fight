/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ai;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.systems.sensors.SensorControl;

/**
 *
 * @author rampage
 */
public class AINavigationBehaviour extends AIBehaviour {
    
    private boolean allowCruise = false;
    private boolean useFormation = true;
    
    public AINavigationBehaviour(AIShipControl aicontrol, Task target, boolean allowCruise, boolean useFormation) {
        super(aicontrol, target);
        this.allowCruise = allowCruise;
        this.useFormation = useFormation;
    }
    
    @Override
    protected void updateBehaviour(float tpf) {

        //float distance = getAI().getSpatial().getWorldTranslation().subtract(getTask().getPosition()).length();
                
        setProximityDistance(SensorControl.VAR_PROXIMITYDISTANCE_AUTO);
        // FOLLOW MY LEADER IF NEEDED
        if ( !useFormation || !getAI().followFormation(tpf, false) ) {        
            setProximityDistance(SensorControl.VAR_PROXIMITYDISTANCE_AUTO);
            
            float throttle = 1;
            
            if ( getAI().getFlight() != null && !getAI().getFlight().formationComplete() )
                throttle = 0.66f;

            // CHECK IF WE MAY USE CRUISE ENGINE
            if (allowCruise && getAI().getFlightControl().getHasCruise()) {
                Vector3f route = getTask().getPosition().subtract(getAI().getObjectInfo().getPosition());
                if (route.length() >= 8000 && getAI().getObjectInfo().getFacing().angleBetween(route.normalize()) < 0.2 * FastMath.DEG_TO_RAD) {
                    getAI().getFlightControl().setCruise(true);
                } else {
                    getAI().getFlightControl().setCruise(false);
                }
            }
            // IF THERE IS NO LEADER TO FOLLOW AND NO CRUISE ENGINE WE MOVE TO THE WAYPOINT
            if (!getAI().getFlightControl().getCruise()) {
                if ( getAI().getFlightControl().moveTo(getTask().getPosition(), getAI().getObjectInfo().getSize() / 2, throttle, 0f, tpf) ) {
                    // WAYPOINT REACHED!
                    getAI().getFlightControl().setThrottle(0);
                    if ( getAI().getObjectInfo().hasMoreTasks() ) {
                        getAI().getObjectInfo().gotoNextTask();
                    } else {
                        getAI().getObjectInfo().clearTasks();
                        getAI().clearCurrentTask();
                    }
                }
            }
        } else {
            ObjectInfoControl leader = getAI().getFlight().getFormationLeader(getAI().getObjectInfo());
            if (leader != null) {
                //AbstractClientControl control = leader.getObjectControl(AbstractClientControl.class);
                if (leader.getCurrentTaskNum() != getAI().getObjectInfo().getCurrentTaskNum()) {
                    getAI().getObjectInfo().gotoTask(leader.getCurrentTaskNum());
                }
            }
        }
    }
    
}
