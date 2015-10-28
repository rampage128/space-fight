/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ai;

import de.jlab.spacefight.mission.structures.Task;

/**
 *
 * @author rampage
 */
public class AIPatrolBehaviour extends AIBehaviour {
    
    private AINavigationBehaviour navigation = null;
    private AIDogfightBehaviour dogfight = null;
    
    public AIPatrolBehaviour(AIShipControl aicontrol, Task currentTask) {
        super(aicontrol, currentTask);
        Task movement = new Task("move");
        movement.setPosition(getTask().getPosition());
        this.navigation = new AINavigationBehaviour(aicontrol, movement, false, true);
        this.dogfight   = new AIDogfightBehaviour(aicontrol, movement);
    }

    @Override
    protected void updateBehaviour(float tpf) {

        this.dogfight.updateBehaviour(tpf);
        
        if (AIDogfightBehaviour.STATE_NOTARGET == dogfight.getState()) {
            this.navigation.updateBehaviour(tpf);
        }
        
    }
    
}
