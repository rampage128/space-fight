/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ai.tasks;

import de.jlab.spacefight.ai.AIConquestBehaviour;
import de.jlab.spacefight.ai.AIDogfightBehaviour;
import de.jlab.spacefight.ai.AINavigationBehaviour;
import de.jlab.spacefight.ai.AIShipControl;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.basic.TimedTask;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.systems.sensors.SensorControl;
import java.util.Random;

/**
 *
 * @author rampage
 */
public class ConquestPointTask extends TimedTask {

    private ObjectInfoControl[] conquestPoints;
    private Task currentPoint = null;
    private int currentPointNum = -1;
    
    private Random randomGenerator = new Random();
    
    private AIConquestBehaviour behaviour;
    
    public ConquestPointTask(AIConquestBehaviour behaviour) {
        super(TASK_TYPE.ROUGH, 10f, 8f);
        this.behaviour = behaviour;
                
        this.conquestPoints = behaviour.getAI().getMission().getObjectsByType("conquestpoint");
        selectPoint();
    }
    
    @Override
    public void run(float tpf) {
        
    }
    
    private void selectPoint() {
        if (this.currentPoint == null || isPointSafe() || isGuardingPointSafe()) {
            this.currentPointNum = randomGenerator.nextInt(this.conquestPoints.length);
            this.currentPoint = new Task("move");
            this.currentPoint.setPosition(this.conquestPoints[this.currentPointNum].getPosition());
            behaviour.setPoint(this.currentPoint);
            System.out.println(behaviour.getAI().getObjectInfo().getCallsign() + " selected new point " + this.conquestPoints[this.currentPointNum].getId());
        }
    }
    
    private boolean isPointSafe() {
        return this.conquestPoints[this.currentPointNum].getFaction() == behaviour.getAI().getObjectInfo().getFaction() && this.conquestPoints[this.currentPointNum].getObjectControl(SensorControl.class).getEnemyCount() < 1;
    }

    private boolean isGuardingPointSafe() {
        return behaviour.getAI().getObjectInfo().distanceTo(this.conquestPoints[this.currentPointNum]) < 3000 && behaviour.getAI().getSensors().getEnemyCount() < 1; 
    }
    
}
