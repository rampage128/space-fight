/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.structures;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.systems.BayControl;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public final class Spawn implements XMLLoadable {
    
    private Vector3f position = Vector3f.ZERO.clone();
    private Quaternion rotation = new Quaternion();
    
    private String spawnObjectId = null;
    
    public Spawn(Vector3f position, Quaternion rotation) {
        this.position.set(position);
        this.rotation.set(rotation);
    }
    
    public Spawn(String spawnObjectId) {
        this.spawnObjectId = spawnObjectId;
    }

    public Spawn(Element element, String path, GamedataManager gamedataManager) {
        loadFromElement(element, path, gamedataManager);
    }
    
    public void lookAt(Vector3f position) {
        this.rotation.lookAt(position.subtract(this.position), Vector3f.UNIT_Y);
    }
    
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        if (element != null) {
            this.spawnObjectId = XMLLoader.getStringValue(element, "object", null);
            this.position = XMLLoader.getVectorValue(element, "position", this.position);
            if ( XMLLoader.getStringValue(element, "rotation", null) != null ) {
                //this.rotation = XMLLoader.getRotationValue(element, "rotation", null);
            } else if ( XMLLoader.getStringValue(element, "lookat", null) != null ) {
                Vector3f lookAt = XMLLoader.getVectorValue(element, "lookat", null);
                lookAt(lookAt);
            } else if ( XMLLoader.getStringValue(element, "facing", null) != null ) {
                Vector3f direction = XMLLoader.getVectorValue(element, "facing", new Vector3f(0, 0, 0)).subtract(this.position);
                this.rotation.lookAt(direction, Vector3f.UNIT_Z);
            }
        }
    }

    /*
     * 
     */
    
    /**
     * Calculates the spawn position for an object.
     * Possible spawn locations:
     * 1. IF SPAWNOBJECT IS SET, ALIVE AND HAS A BAY WE SPAWN AT OBJECT
     * 2. IF WE HAVE A LEADER AND CAN RETRIEVE A FORMATION POINT FROM THE ORIGINAL SPAWN LOCATION WE SPAWN IN FORMATION   
     * 3. WE JUST SPAWN ON THE GIVEN LOCATION
     * @param objectInfo is the Object to spawn
     * @param mission is the mission object to retreive data
     * @param inFormation determines whether the object may spawn in formation (on initial spawn!)
     * @return 
     */
    public boolean setSpawnPosition(ObjectInfoControl objectInfo, AbstractMission mission, boolean inFormation) {
        //Spatial object = objectInfo.getSpatial();
        ObjectInfoControl spawnInfo = null;
        if (spawnObjectId != null) {
            spawnInfo = mission.getObject(spawnObjectId);
        }
        
        Vector3f spawnpos = new Vector3f(this.position);
        Quaternion spawnrot = new Quaternion(this.rotation);
                                
        if (spawnInfo != null && spawnInfo.isAlive()) {
            Node spawnObject = (Node)spawnInfo.getSpatial();
            /*
            BayControl bayControl = spawnObject.getControl(BayControl.class);
            if (bayControl != null) {
                spawnpos.set(bayControl.getFreeBay().getWorldTranslation());
                spawnrot.set(bayControl.getFreeBay().getWorldRotation());
                FlightControl nodeFlight = object.getControl(FlightControl.class);
                if ( nodeFlight != null ) {
                    nodeFlight.setThrottle(1f);
                }
            }
            */
            BayControl bayControl = spawnObject.getControl(BayControl.class);
            if (bayControl != null) {
                return bayControl.spawn(objectInfo);
            }
            /*
            Spatial startbay = spawnObject.getChild("startbay");
            if (startbay != null) {
                spawnpos.set(startbay.getWorldTranslation());
                spawnrot.set(startbay.getWorldRotation());
                FlightControl nodeFlight = object.getControl(FlightControl.class);
                if ( nodeFlight != null ) {
                    nodeFlight.setThrottle(1f);
                }
            } else {
                spawnpos.set(spawnObject.getWorldTranslation());
                spawnrot.set(spawnObject.getWorldRotation());
            }
            */
        }
        
        if (objectInfo.getFlight() != null && inFormation) {
            // APPLY DEFAULT POSITION BEFORE ASKING FOR FORMATIONPOINT!
            // THIS HAS TO BE DONE TO AQUIRE THE LEADER
            objectInfo.getSpatial().setLocalTranslation(spawnpos);
            objectInfo.getSpatial().setLocalRotation(spawnrot);    
            if ( objectInfo.getSpatial().getControl(RigidBodyControl.class) != null ) {
                objectInfo.getSpatial().getControl(RigidBodyControl.class).setPhysicsLocation(spawnpos);
                objectInfo.getSpatial().getControl(RigidBodyControl.class).setPhysicsRotation(spawnrot);
            }
            Vector3f formationPoint = objectInfo.getFlight().getFormationPointWorld(objectInfo);
            if (formationPoint != null) {
                spawnpos = formationPoint;
            }
        }
        
        objectInfo.getSpatial().setLocalTranslation(spawnpos);
        objectInfo.getSpatial().setLocalRotation(spawnrot);    
        if ( objectInfo.getSpatial().getControl(RigidBodyControl.class) != null ) {
            objectInfo.getSpatial().getControl(RigidBodyControl.class).setPhysicsLocation(spawnpos);
            objectInfo.getSpatial().getControl(RigidBodyControl.class).setPhysicsRotation(spawnrot);
        }
        return true;
    }
    
    public boolean canSpawn(AbstractMission mission) {
        if ( this.spawnObjectId != null ) {
            ObjectInfoControl spawnObject = mission.getObject(this.spawnObjectId);
            return spawnObject != null && spawnObject.isAlive();
        }
        return true;
    }   
    
}
