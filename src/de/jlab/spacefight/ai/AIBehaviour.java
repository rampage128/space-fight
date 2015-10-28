/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ai;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import de.jlab.spacefight.debug.DebugContext;
import de.jlab.spacefight.debug.SpaceDebugger;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.systems.sensors.SensorControl;

/**
 *
 * @author rampage
 */
public abstract class AIBehaviour {
    
    public static final int STATE_IDLE = 0;
    
    private int state = STATE_IDLE;
    private AIShipControl aicontrol;
    
    private Task currentTask;
    
    //private float proximityDistance = 0f;
    
    public AIBehaviour(AIShipControl aicontrol, Task currentTask) {
        this.aicontrol = aicontrol;
        this.currentTask = currentTask;
    }
    
    public Task getTask() {
        return this.currentTask;
    }
    
    public void setState(int state) {
        this.state = state;
    }
    
    public int getState() {
        return this.state;
    }
    
    public AIShipControl getAI() {
        return this.aicontrol;
    }
    
    public void update(float tpf) {
               
        // NEW "PROXIMITY AVOIDANCE"!        
        Vector3f avoidance = getAI().getSensors().getProximityAvoidance();        
        if ( avoidance != null && avoidance.length() > 0 ) {
            SpaceDebugger.getInstance().setVector(DebugContext.AI, "avoidance" + getAI().getSpatial().getName(), getAI().getSpatial().getWorldTranslation(), avoidance, ColorRGBA.Pink);
            //getAI().turnTo(avoidance.addLocal(getAI().getSpatial().getWorldTranslation()), getAI().getFlightControl().getUpVector(), tpf);
            getAI().getFlightControl().moveTo(avoidance.addLocal(getAI().getSpatial().getWorldTranslation()), 0, 1, 0f, tpf);
        } else {
            SpaceDebugger.getInstance().removeItem(DebugContext.AI, "avoidance" + getAI().getSpatial().getName());
            updateBehaviour(tpf);
        }
        
        /* THIS SEEMS TO BE PRETTY SLOW!
        if ( getAI().getFlight().getVelocity() > 0 ) {
            if ( _collisionAvoidanceVector != null ) {
                if ( _collisionAvoidancePosition.subtract(getAI().getSpatial().getWorldTranslation()).length() < getAI().getFlight().getShipSize() / 2 ) {
                    _collisionAvoidanceVector = null;
                    _collisionAvoidancePosition = null;
                    getAI().getSpace().removeVector("avoidance_" + hashCode());
                    getAI().getSpace().removeVector("collision_" + hashCode());
                } else {
                    getAI().getFlight().setThrottle(0.25f);
                    getAI().turnTo(_collisionAvoidancePosition, tpf);
                }
            } else if ( checkPath(getAI().getFlight().getFacing(), getAI().getFlight().getVelocity() / 8) ) {
                updateBehaviour(tpf);
            } else {
                getAI().getSpace().drawVector("collision_" + hashCode(), getAI().getSpatial().getWorldTranslation(), _collisionRay.getDirection(), ColorRGBA.Red);
                _collisionAvoidanceVector = calculateAvoidanceVector();
                _collisionAvoidancePosition = _collisionAvoidanceVector.add(getAI().getSpatial().getWorldTranslation());
                getAI().getSpace().drawVector("avoidance_" + hashCode(), getAI().getSpatial().getWorldTranslation(), _collisionAvoidanceVector, ColorRGBA.Green);
            }
        } else {     
            updateBehaviour(tpf);
        }
        */
        
    }
    
    protected abstract void updateBehaviour(float tpf);
    
    /* PROXIMITY STUFF */
    public void setProximityDistance(float proximityDistance) {
        SensorControl sensors = getAI().getSensors();
        if (sensors != null) {
            sensors.setProximityDistance(proximityDistance);
        }
    }
    
    /** COLLISION CHECKING **/
    /* DEPRECATED BECAUSE SLOW?!
    Ray _collisionRay = new Ray();
    CollisionResults _collisionResults = new CollisionResults();
    Quaternion _collisionAvoidanceRotation = Quaternion.DIRECTION_Z;
    Vector3f _collisionAvoidanceVector = null;
    Vector3f _collisionAvoidancePosition = null;
    CollisionResult _collisionResult;
    
    private boolean checkPath(Vector3f vector, float distance) {
        //BoundingVolume bound = spatial.getWorldBound();
        long time = System.currentTimeMillis();
        _collisionRay.setOrigin(getAI().getSpatial().getWorldTranslation().add(getAI().getFlightControl().getFacing().mult(getAI().getFlightControl().getShipSize() / 4)));
        _collisionRay.setDirection(vector.mult(vector.length() + (getAI().getFlightControl().getShipSize() )));
        _collisionRay.setLimit(_collisionRay.getDirection().length());
        getAI().getSpace().getSpace().collideWith(_collisionRay, _collisionResults);
        SpaceDebugger.getInstance().drawVector("collisiontest_" + hashCode(), _collisionRay.getOrigin(), _collisionRay.getDirection(), ColorRGBA.Gray);
        if ( _collisionResults.size() > 0 ) {
            _collisionResult = _collisionResults.getClosestCollision();
            return _collisionResult.getDistance() > distance;
        } else {
            return true;
        }
    }
    
    private Vector3f calculateAvoidanceVector() {
        
        for ( int i = 0; i <= 90; i += 30 ) {
            _collisionRay.setDirection(_collisionAvoidanceRotation.fromAngleAxis(i, Vector3f.UNIT_X).mult(Vector3f.UNIT_Z.mult(getAI().getFlightControl().getVelocity() + getAI().getFlightControl().getShipSize())));
            getAI().getSpace().getSpace().collideWith(_collisionRay, _collisionResults);
            if ( _collisionResults.size() < 1 ) {
                return _collisionRay.getDirection();
            }
            
            _collisionResults.clear();
            
            _collisionRay.setDirection(_collisionAvoidanceRotation.fromAngleAxis(-i, Vector3f.UNIT_X).mult(Vector3f.UNIT_Z.mult(getAI().getFlightControl().getVelocity() + getAI().getFlightControl().getShipSize())));
            getAI().getSpace().getSpace().collideWith(_collisionRay, _collisionResults);
            if ( _collisionResults.size() < 1 ) {
                return _collisionRay.getDirection();
            }
            
            _collisionResults.clear();
            
            _collisionRay.setDirection(_collisionAvoidanceRotation.fromAngleAxis(i, Vector3f.UNIT_Y).mult(Vector3f.UNIT_Z.mult(getAI().getFlightControl().getVelocity() + getAI().getFlightControl().getShipSize())));
            getAI().getSpace().getSpace().collideWith(_collisionRay, _collisionResults);
            if ( _collisionResults.size() < 1 ) {
                return _collisionRay.getDirection();
            }
            
            _collisionResults.clear();
            
            _collisionRay.setDirection(_collisionAvoidanceRotation.fromAngleAxis(-i, Vector3f.UNIT_Y).mult(Vector3f.UNIT_Z.mult(getAI().getFlightControl().getVelocity() + getAI().getFlightControl().getShipSize())));
            getAI().getSpace().getSpace().collideWith(_collisionRay, _collisionResults);
            if ( _collisionResults.size() < 1 ) {
                return _collisionRay.getDirection();
            }
            
            _collisionResults.clear();
        }
                
        return Vector3f.UNIT_Z.clone();
        
    }
    */
    
}
