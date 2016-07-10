/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ai;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.AbstractClientControl;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.ai.tasks.DistressTask;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Flight;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.systems.PhysicsControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;

/**
 *
 * @author rampage
 */
public class AIShipControl extends AbstractClientControl {
    
    private SpaceAppState _space;
    
    private FlightControl flightControl = null;
    private WeaponSystemControl weaponControl = null;
    private SensorControl sensorControl = null;
    private ObjectInfoControl objectInfo = null;
    private PhysicsControl physicsControl;
    
    private AIBehaviour behaviour;
    
    private Task currentTask;
    
    private DistressTask distressTask;
                            
    public AIShipControl(SpaceAppState space) {
        super(space.getMission());
        _space = space;
    }
    
    public Control cloneForSpatial(Spatial spatial) {
        AIShipControl newControl = new AIShipControl(_space);
        newControl.setSpatial(spatial);
        return newControl;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
    }
    
    @Override
    protected void controlUpdate(float tpf) {

        if (getObjectInfo() != null && getObjectInfo().getPlayer() != null) {
            return;
        }
                
        if ( getSpatial() != null ) {
            if (this.physicsControl == null) {
                this.physicsControl = getSpatial().getControl(PhysicsControl.class);
                return;
            }
            if ( objectInfo == null ) {
                objectInfo = getSpatial().getControl(ObjectInfoControl.class);
                return;
            }
            if ( flightControl == null ) {
                flightControl = getSpatial().getControl(FlightControl.class);
                return;
            }
            if ( weaponControl == null ) {
                weaponControl = getSpatial().getControl(WeaponSystemControl.class);
            }
            if ( sensorControl == null ) {
                sensorControl = getSpatial().getControl(SensorControl.class);
            }
            
            if (distressTask == null) {
                distressTask = new DistressTask(objectInfo, getMission());
            }
        }
        
        if (_space.isEnabled()) {
           
            this.distressTask.check(tpf);
            
            if (getObjectInfo().numTasks() > 0) {
                Task currentTask = getObjectInfo().getCurrentTask();
                if ( this.currentTask != currentTask ) {
                    this.currentTask = currentTask;
                    this.behaviour = null;
                } //else if ( currentTask.isReached() ) {
                    
                //}
                
                if ( this.behaviour == null ) {
                    if ( "move".equalsIgnoreCase(this.currentTask.getType()) ) {
                        this.behaviour = new AINavigationBehaviour(this, this.currentTask, true, true);
                    } else if ( "patrol".equalsIgnoreCase(this.currentTask.getType()) ) {
                        this.behaviour = new AIPatrolBehaviour(this, this.currentTask);
                    } else if ( "guard".equalsIgnoreCase(this.currentTask.getType()) ) {
                        this.behaviour = new AIEscortBehaviour(this, this.currentTask);
                    } else if ( "attack".equalsIgnoreCase(this.currentTask.getType()) ) {
                        this.behaviour = new AIAssassinBehaviour(this, this.currentTask);
                    } else {
                        if (this.getMission().getObjectsByType("conquestpoint").length > 0) {
                            this.behaviour = new AIConquestBehaviour(this, null);
                        } else {
                            this.behaviour = new AIDogfightBehaviour(this, this.currentTask);
                        }
                    }
                }
            } else {
                if ( this.behaviour == null ) {
                    if (this.getObjectInfo().getClient() && this.getMission().getObjectsByType("conquestpoint").length > 0) {
                        this.behaviour = new AIConquestBehaviour(this, null);
                    } else {
                        if (this.getWeapons() != null) {
                            this.behaviour = new AIDogfightBehaviour(this, this.currentTask);
                        }
                    }
                }
            }

            this.flightControl.setElevator(0f);
            this.flightControl.setAileron(0f);
            this.flightControl.setRudder(0f);
            this.flightControl.setStrafe(0f);
            this.flightControl.setLift(0f);
            this.flightControl.setThrottle(0f);
            
            if ( this.behaviour == null ) {
                if ( weaponControl != null ) {
                    this.behaviour = new AIDogfightBehaviour(this, this.currentTask);
                }
            } else {
                this.behaviour.update(tpf);
            }
        }
    }
   
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }
      
    /*
    public Vector3f checkProximity(float checkDistance) {
        //System.out.println(checkDistance);
        ObjectInfoControl info = getObjectInfo();
        if ( checkDistance <= 0 ) {
            return null;
        }
        
        ArrayList<TargetInformation> targets = getSensors().getTargetList(checkDistance, null);
        Vector3f avoidVector = new Vector3f(0, 0, 0);
        for ( TargetInformation target : targets ) {
            float removal = target.getSize() / 2 + info.getSize() / 2;
            float distance = target.getDistance() - removal;
            if ( distance <= checkDistance && target.getSize() >= info.getSize() / 2 ) {
                System.out.println(distance);
                avoidVector.addLocal(target.getDirection());
            }
        }
        if ( avoidVector.length() > 0 ) {
            return avoidVector.normalizeLocal().multLocal(checkDistance);
        }
        return null;
    }
    */
    
    /* AI FUNCTIONS */     
    public boolean followFormation(float tpf, boolean forceFollow) {        
        Flight flight = getFlight();
        if ( flight == null ) {
            return false;
        }
        
        Vector3f targetPos = flight.getFormationPointWorld(getObjectInfo());
        
        if ( targetPos == null ) {
           return false;
        }
        
        if ( !forceFollow && targetPos.subtract(getSpatial().getWorldTranslation()).length() > 3000 )
            return false;

        
        
        FlightControl leaderFlight = flight.getFormationLeader(getObjectInfo()).getObjectControl(FlightControl.class); //flight.getObject(0).getObjectControl(FlightControl.class);
        
        getFlightControl().setCruise(leaderFlight.getCruise());
        
        float leaderspeed = 0f;
        Vector3f leaderUp = new Vector3f();
        if ( leaderFlight != null ) {
            leaderUp = leaderFlight.getUpVector();
            leaderspeed = leaderFlight.getForwardVelocity();
        }
        
        if ( getFlightControl().moveTo(targetPos, getObjectInfo().getSize(), leaderUp, 1, leaderspeed, tpf) ) {
            // LEADER REACHED!
            
            getFlight().joinFormation();
            
            if ( leaderFlight != null ) {
                getFlightControl().turnTo(getSpatial().getWorldTranslation().add(leaderFlight.getFacing()), tpf);
            }
            
            flightControl.setThrottle(leaderspeed / flightControl.getTopspeed());            
        } else {
            getFlight().leaveFormation();
        }
        
        return true;
    }
    
    /* SYSTEM GETTERS */
    public SensorControl getSensors() {
        return sensorControl;
    }
    
    public WeaponSystemControl getWeapons() {
        return weaponControl;
    }
    
    public FlightControl getFlightControl() {
        return flightControl;
    }
    
    public ObjectInfoControl getObjectInfo() {
        return objectInfo;
    }
              
    public SpaceAppState getSpace() {
        return _space;
    }
           
    public void clearCurrentTask() {
        this.currentTask = null;
        this.behaviour = null;
    }

    @Override
    public String getName() {
        return "AI";
    }
    
}
