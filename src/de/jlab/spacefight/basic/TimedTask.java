/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic;

import java.util.Random;

/**
 * Represents a task which should be executed in a more or less exact specified 
 * interval. This is pretty useful to lower the rate of expensive calculations 
 * for actions which don't need permanent realtime processing.
 * 
 * @author rampage
 */
public abstract class TimedTask {
    
    public enum TASK_TYPE {
        EXACT, ROUGH 
    }
       
    private TASK_TYPE type = TASK_TYPE.EXACT;
    private float time      = 0f;
    private float deviance  = 0f;
    
    private float checkTime = 0f;
    private float runtime   = 0f;
    
    private Random devianceFactor = new Random();
    
    public TimedTask(float time) {
        this(TASK_TYPE.EXACT, time, 0f);
    }
    
    public TimedTask(TASK_TYPE type, float time, float deviance) {
        this.type       = type;
        this.time       = time;
        this.deviance   = deviance;
    }
    
    public void later() {
        switch(this.type) {
            case ROUGH:
                checkTime = time - (0.5f * deviance) + (devianceFactor.nextFloat() * deviance);
                break;
            case EXACT:
                checkTime = time;
                break;
        }
        runtime = checkTime;
    }
    
    public void check(float tpf) {                
        runtime -= tpf;
        if (runtime <= 0) {
            run(checkTime);
            later();
        }
    }
    
    public abstract void run(float tpf);
    
}
