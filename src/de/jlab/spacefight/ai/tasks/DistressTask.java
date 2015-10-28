/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ai.tasks;

import de.jlab.spacefight.basic.ChanceCalculator;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.basic.TimedTask;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.mission.structures.DistressCall;
import de.jlab.spacefight.systems.ShieldControl;
import de.jlab.spacefight.systems.sensors.SensorControl;

/**
 * Task to shedule distress calls for AI. The AI should not make a distress
 * call more than each 30 seconds. To lower cost of the checks and create a 
 * more natural feel we limit the interval of this task to roughly 4-6 seconds.
 * 
 * @author rampage
 */
public class DistressTask extends TimedTask {

    private float distressTimer = 0;
    
    private ObjectInfoControl source;
    private AbstractMission mission;
    
    public DistressTask(ObjectInfoControl source, AbstractMission mission) {
        super(TASK_TYPE.ROUGH, 5f, 2f);
        this.source = source;
        this.mission = mission;
    }
    
    public void run(float tpr) {
        // TODO CALCULATE OFFENSIVE DISTRESS CALL CHANCES
        // WITH TARGET STRENGTH VS ALL ATTACKERS STRENGTH
                
        boolean shieldDamage    = source.getObjectControl(ShieldControl.class).getFrontShield() <= 0.5f || source.getObjectControl(ShieldControl.class).getRearShield() <= 0.5f;
        boolean weaponWarning   = source.getObjectControl(SensorControl.class).getLaserWarning() || source.getObjectControl(SensorControl.class).getMissileWarning();
        boolean chance          = new ChanceCalculator().calculateChance(10);
        boolean mayI            = this.distressTimer <= 0;
        
        if (weaponWarning && shieldDamage && chance && mayI) {
            makeDistressCall(DistressCall.TYPE_DEFENSIVE);
        }
        
        if (distressTimer > 0) {
            distressTimer -= tpr;
            distressTimer = Math.max(0, distressTimer);
        }
    }
    
    public void makeDistressCall(int type) {       
        if (this.mission == null) {
            return;
        }
        this.distressTimer = 30;
        if (DistressCall.TYPE_OFFENSIVE == type) {
            DistressCall.offensiveDistressCall(source, mission);
        } else {
            DistressCall.defensiveDistressCall(source, mission);
        }
    }
    
}
