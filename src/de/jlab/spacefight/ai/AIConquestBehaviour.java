/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ai;

import de.jlab.spacefight.ai.tasks.ConquestPointTask;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.systems.sensors.SensorControl;
import java.util.Random;

/**
 *
 * @author rampage
 */
public class AIConquestBehaviour extends AIBehaviour {
    
    private AINavigationBehaviour navigation = null;
    private AIDogfightBehaviour dogfight = null;
    
    private ConquestPointTask pointTask;
    
    private float selectionRate = 5;
    private float selectionTime = 0;
    
    public AIConquestBehaviour(AIShipControl aicontrol, Task currentTask) {
        super(aicontrol, currentTask);
        
        pointTask = new ConquestPointTask(this);
    }

    public void setPoint(Task conquestPointTask) {
        this.navigation = new AINavigationBehaviour(getAI(), conquestPointTask, true, false);
        this.dogfight   = new AIDogfightBehaviour(getAI(), conquestPointTask);
    }
    
    @Override
    protected void updateBehaviour(float tpf) {

        if (this.dogfight != null) {
            this.dogfight.updateBehaviour(tpf);
                
            if (AIDogfightBehaviour.STATE_NOTARGET == dogfight.getState()) {
                pointTask.check(tpf);
                if (this.navigation != null) {
                    this.navigation.updateBehaviour(tpf);
                }
            }
        }        
    }
    
}
