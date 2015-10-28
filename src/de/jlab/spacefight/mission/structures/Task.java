/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.structures;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.mission.AbstractMission;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public final class Task implements XMLLoadable {
    
    private String type = null;
    private Vector3f position = new Vector3f();
    
    private String targetObjectId = null;
    private String targetFlightId = null;
    
    private ObjectInfoControl targetObject = null;
    
    public Task(Element element, String path, GamedataManager gamedataManager) {
        loadFromElement(element, path, gamedataManager);
    }
    
    public Task(String type) {
        this.type = type;
    }
    
    public String getType() {
        return this.type;
    }
    
    public Vector3f getPosition() {
        return this.position;
    }
    
    public void setPosition(Vector3f position) {
        this.position.set(position);
    }
    
    public void setTargetObjectId(String targetObjectId) {
        this.targetObjectId = targetObjectId;
    }
    
    public String getTargetObjectId() {
        return this.targetObjectId;
    }
    
    public void computeTargetObject(AbstractMission mission) {
        if ( this.targetObjectId != null ) {
            this.targetObject = mission.getObject(this.targetObjectId);
        } else {
            if ( this.targetFlightId != null ) {
                this.targetObject = mission.getObjectForFlight(this.targetFlightId);
            }
        }
    }
    
    public ObjectInfoControl getTargetObject(AbstractMission mission) {
        if ( this.targetObject == null || !this.targetObject.isAlive() ) {
            if (this.targetFlightId != null) {
                this.targetObject = mission.getObjectForFlight(this.targetFlightId);
            } else {
                this.targetObject = mission.getObject(this.targetObjectId);
            }
            if (this.targetObject == null) {
                return null;
            }
        }
        return this.targetObject;
    }
    
    public void setTargetFlightId(String targetWingId) {
        this.targetFlightId = targetWingId;
    }
    
    public String getTargetFlightId() {
        return this.targetFlightId;
    }
    
    public boolean isDone(Spatial object, AbstractMission mission) {        
        if ("attack".equalsIgnoreCase(this.type)) {
            ObjectInfoControl targetObject = getTargetObject(mission);
            if (targetObject != null && targetObject.isAlive()) {
                return false;
            } else {
                return true;
            }
        } else if ("move".equalsIgnoreCase(this.type) || "patrol".equalsIgnoreCase(this.type)) {
            return this.position.subtract(object.getWorldTranslation()).length() < 20f;
        } else if ("guard".equalsIgnoreCase(this.type)) {
            ObjectInfoControl targetObject = getTargetObject(mission);
            if (targetObject != null && targetObject.isAlive()) {
                return false;
            } else {
                return true;
            }
        }
        
        return false;
    }
    
    public String getName() {
        AbstractMission mission = Game.get().getStateManager().getState(SpaceAppState.class).getMission();
        if ("attack".equalsIgnoreCase(this.type)) {
            ObjectInfoControl targetObject = getTargetObject(mission);
            if (targetObject != null && targetObject.isAlive()) {
                return "Attack " + targetObject.getCallsign();
            } else {
                return "-";
            }
        } else if ("move".equalsIgnoreCase(this.type)) {
            return "Move";
        } else if ("patrol".equalsIgnoreCase(this.type)) {
            return "Patrol";
        } else if ("guard".equalsIgnoreCase(this.type)) {
            ObjectInfoControl targetObject = getTargetObject(mission);
            if (targetObject != null && targetObject.isAlive()) {
                return "Guard " + targetObject.getCallsign();
            } else {
                return "-";
            }
        }
        return "Unknown";
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        // SET UP TYPE
        this.type = XMLLoader.getStringValue(element, "type", "move");
        
        // SET UP POSITION
        this.position.set(XMLLoader.getVectorValue(element, "position", this.position));

        // SET UP TARGET
        setTargetObjectId(XMLLoader.getStringValue(element, "object", null));
        setTargetFlightId(XMLLoader.getStringValue(element, "flight", null));        
    }
    
}
