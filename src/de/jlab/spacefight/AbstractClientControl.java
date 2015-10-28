/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.mission.structures.Flight;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.player.PlayerControl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * super class for for controlled objects wich follow tasks (like AI-ships or playable ships).
 * sub-classes implement this to create new "controlling" methods which rely on ingame tasks
 * or mission objectives.
 * 
 * @author rampage
 */
public abstract class AbstractClientControl extends AbstractControl {

    private AbstractMission mission;
    private Flight flight;
    
    private int score;
    private ArrayList<Kill> killList = new ArrayList<Kill>();
    private ArrayList<Kill> deathList = new ArrayList<Kill>();
    
    public AbstractClientControl(AbstractMission mission) {
        this.mission = mission;
    }
    
    @Override
    protected abstract void controlUpdate(float tpf);

    @Override
    protected abstract void controlRender(RenderManager rm, ViewPort vp);

    public abstract Control cloneForSpatial(Spatial spatial);
    
    public AbstractMission getMission() {
        return this.mission;
    }
    
    public Flight getFlight() {
        if ( this.flight == null ) {
            updateFlight();
        }
        return this.flight;
    }
            
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if ( spatial != null ) {
            updateFlight();
        } else {
            this.flight = null;
        }
    }

    private void updateFlight() {
        ObjectInfoControl objectInfo = getSpatial().getControl(ObjectInfoControl.class);
        if ( objectInfo != null ) {
            this.flight = objectInfo.getFlight();
        }
    }
    
    public void addScore(int score) {
        this.score += score;
    }
    
    public int getScore() {
        return this.score;
    }
    
    public void addKill(Kill kill) {
        this.killList.add(kill);
    }
    
    public int getKillCount() {
        return this.killList.size();
    }
    
    public void addDeath(Kill kill) {
        this.deathList.add(kill);
    }
    
    public int getDeathCount() {
        return this.deathList.size();
    }
    
    public abstract String getName();
        
    public Kill[] getEventLog() {
        ArrayList<Kill> eventLog = new ArrayList<Kill>();
        for (Kill kill : this.killList) {
            eventLog.add(kill);
        }
        for (Kill kill : this.deathList) {
            eventLog.add(kill);
        }
        
        Collections.sort(killList, new Comparator<Kill>() {
            public int compare(Kill k1, Kill k2) {
                return (int)Math.round(k2.getTime() - k1.getTime());
            }
        });
        
        return eventLog.toArray(new Kill[0]);
    }
    
}
