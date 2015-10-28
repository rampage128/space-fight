/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems;

import de.jlab.spacefight.systems.flight.FlightControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public class BayControl extends AbstractControl implements SystemControl, XMLLoadable {

    public static final float DEFAULT_ACCELERATIONTIME = 1f;
    public static final float DEFAULT_USETIME = 2f;
    public static final Vector3f DEFAULT_VELOCITY = new Vector3f(0, 0, 800);
    
    private ArrayList<StartingBay> startingbayList = new ArrayList<StartingBay>();
    private ArrayList<StartingbayUse> usedStartingbayList = new ArrayList<StartingbayUse>();
    
    @Override
    protected void controlUpdate(float tpf) {
        StartingbayUse[] uses = this.usedStartingbayList.toArray(new StartingbayUse[0]);
        for (StartingbayUse use : uses) {
            if (use.isFree(tpf)) {
                this.usedStartingbayList.remove(use);
            }
        }        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        BayControl control = new BayControl();
        control.setSpatial(spatial);
        return control;
    }

    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            for (StartingBay startingBay : this.startingbayList) {
                startingBay.getNode().removeFromParent();
                ((Node)getSpatial()).attachChild(startingBay.getNode());
            }
            
            /*// FIND BAYS FROM MODEL (DEPRECATED!)
            Spatial container = ((Node)spatial).getChild("bays");
            if ( container != null ) {
                List<Spatial> bayList = ((Node)container).getChildren();
                for ( int i = 0; i < bayList.size(); i++ ) {
                    Spatial child = bayList.get(i);
                    if ("startbay".equalsIgnoreCase(child.getName()))
                        this.startingbayList.add((Node)child);
                }
            }
            */
        }
    }
    
    public void resetSystem() {
        this.usedStartingbayList.clear();
    }
    
    public boolean spawn(ObjectInfoControl spawner) {
        if (this.startingbayList.size() == this.usedStartingbayList.size()) {
            return false;
        }
        
        for (StartingBay bay : this.startingbayList) {
            boolean used = false;
            for (StartingbayUse use : this.usedStartingbayList) {
                if (use.check(bay)) {
                    used = true;
                }
            }
            if (!used) {
                StartingbayUse use = new StartingbayUse(bay, spawner);
                return this.usedStartingbayList.add(use);
            }
        }
        return false;
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        List<Element> bayElements = element.getChildren("startbay");
        for (Element bayElement : bayElements) {
            Node startbayNode = new Node("startbay");
            startbayNode.setLocalTranslation(XMLLoader.getVectorValue(bayElement, "position", Vector3f.ZERO));
            startbayNode.setLocalRotation(XMLLoader.getAngleValue(bayElement, "angle", new Quaternion()));
            float usagetime = XMLLoader.getFloatValue(bayElement, "usagetime", DEFAULT_USETIME);
            float accelerationtime = XMLLoader.getFloatValue(bayElement, "accelerationtime", DEFAULT_ACCELERATIONTIME);
            Vector3f velocity = XMLLoader.getVectorValue(bayElement, "velocity", DEFAULT_VELOCITY);
            
            StartingBay startBay = new StartingBay(startbayNode, usagetime, accelerationtime, velocity);
            this.startingbayList.add(startBay);
        }
    }
    
    private class StartingBay {
        
        private Node node;
        private float usagetime = DEFAULT_USETIME;
        private float accelerationtime = DEFAULT_ACCELERATIONTIME;
        private Vector3f velocity = DEFAULT_VELOCITY;
        
        public StartingBay(Node node, float usagetime, float accelerationtime, Vector3f velocity) {
            this.node = node;
            this.usagetime = usagetime;
            this.velocity = velocity;
            this.accelerationtime = accelerationtime;
        }
        
        public Node getNode() {
            return this.node;
        }
        
        public float getUsageTime() {
            return this.usagetime;
        }
        
        public float getAccelerationTime() {
            return this.accelerationtime;
        }
        
        public Vector3f getVelocity() {
            return this.velocity;
        }
                
    }
    
    private class StartingbayUse {
               
        private float time;
        
        private StartingBay bay;
        
        private ObjectInfoControl spawner;
        private PhysicsControl spawnerPhysics;
        private FlightControl spawnerFlight;
                
        public StartingbayUse(StartingBay bay, ObjectInfoControl spawner) {
            this.bay    = bay;
            this.spawner = spawner;
            this.spawnerPhysics = spawner.getObjectControl(PhysicsControl.class);
            this.spawnerFlight = spawner.getObjectControl(FlightControl.class);
            
            Object usagetime = bay.getUsageTime();
            if (usagetime != null && usagetime instanceof Float) {
                this.time   = (Float)usagetime;
            }
                       
            this.spawner.getSpatial().setLocalTranslation(bay.getNode().getWorldTranslation());
            this.spawner.getSpatial().setLocalRotation(bay.getNode().getWorldRotation());
            if (this.spawnerPhysics != null) {
                this.spawnerPhysics.setPhysicsLocation(bay.getNode().getWorldTranslation());
                this.spawnerPhysics.setPhysicsRotation(bay.getNode().getWorldRotation());
            }
            this.time = 0;
        }
        
        public boolean isFree(float tpf) {
            // REDUCE TIME
            this.time += tpf;

            // INIT MOVEMENT POSITION FOR LAUNCH MOVEMENT
            if (this.time <= bay.getAccelerationTime()) {
                float movementTime = Math.min(bay.getAccelerationTime(), this.time);
                Vector3f relativePosition = this.bay.getVelocity().mult(movementTime);
                Vector3f absolutePosition = bay.getNode().getWorldRotation().multLocal(relativePosition).addLocal(bay.getNode().getWorldTranslation());                
                // UPDATE SPAWNERS POSITION AND ROTATION!
                this.spawner.getSpatial().setLocalTranslation(absolutePosition);
                this.spawner.getSpatial().setLocalRotation(bay.getNode().getWorldRotation());
                if (this.spawnerPhysics != null) {
                    this.spawnerPhysics.setPhysicsLocation(absolutePosition);
                    this.spawnerPhysics.setPhysicsRotation(bay.getNode().getWorldRotation());
                    this.spawnerPhysics.setLinearVelocity(this.bay.getVelocity());
                }
                if (this.spawnerFlight != null) {
                    this.spawnerFlight.setThrottle(0);
                    //this.spawnerFlight.setVelocity(this.bay.getVelocity().z);
                }
            }
            return this.time >= bay.getUsageTime();
        }
        
        public boolean check(StartingBay otherBay) {
            return this.bay == otherBay;
        }
        
    }
    
}
