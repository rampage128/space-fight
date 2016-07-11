/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.flight;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.systems.PhysicsControl;
import de.jlab.spacefight.systems.SystemControl;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 * Controls the flight characteristics of an object and enables active movement.
 * All spaceships use this!
 * 
 * But be aware: this Control is useless without a PhysicsControl!
 * 
 * @author rampage
 */
public class FlightControl extends AbstractControl implements SystemControl, XMLLoadable {

    // these values are static for all ships
    public static final float VAR_CRUISESPEED = 1000f;
    public static final float VAR_CRUISEDAMP  = 2000f;
    public static final float VAR_CRUISEACCEL = 200f;
    
    private float topspeed          = 100f;
    private float topspeedreverse   = 20f;
    private float turnrate          = 2f;
    private float angulardamp       = 0.9999f;
    private float lineardamp        = 0.75f;
       
    private ObjectInfoControl object;
        
    private float throttle    = 0f;
    private float elevator  = 0f;
    private float rudder    = 0f;
    private float aileron   = 0f;
    private float strafe   = 0f;
    private float lift     = 0f;
    
    private float cruiseEnergy = 0f;
    
    private boolean hasCruise = true;
    private boolean cruise  = false;
    private boolean glide   = false;
        
    private float forwardVelocity = 0f;
    
    private PhysicsControl physics;
    private ArrayList<Engine> engines = new ArrayList<>();
    
    public FlightControl(ObjectInfoControl object) {
        this.object = object;
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if ( spatial != null ) {
            Game.get().getStateManager().getState(SpaceAppState.class).getPhysics().getPhysicsSpace().addTickListener(this.forceUpdater);
            for (Engine engine : this.engines) {
                engine.attachTo((Node)spatial);
            }
        } else {
            Game.get().getStateManager().getState(SpaceAppState.class).getPhysics().getPhysicsSpace().removeTickListener(this.forceUpdater);
            for (Engine engine : this.engines) {
                engine.detach();
            }
        }
    }
    
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        FlightControl control = new FlightControl(this.object);
        control.setSpatial(spatial);
        return control;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if ( getSpatial() != null ) {          
            // check for required PhysicsControl before proceeding
            if (this.physics == null) {
                this.physics = getSpatial().getControl(PhysicsControl.class);
                if (this.physics == null) {
                    throw new IllegalArgumentException("Object with FlightControl needs a PhysicsControl: " + this.object.getId());
                }
            }
            
            // control engine sound (TODO should probably be outsourced into Engine)
            if (!this.engines.isEmpty()) {
                for (Engine engine : this.engines) {
                    engine.setThrottle(this.throttle);
                    if ( getSpatial().getParent() != null ) {                    
                        Game.get().getAudioManager().playSoundLoop(engine.getSound());
                    } else {
                        Game.get().getAudioManager().stopSoundLoop(engine.getSound());
                    }
                }
            }
        }
                
        // update cruise energy (for charge-up and slow down)
        if (this.getCruise()) {
            this.cruiseEnergy += tpf / 3;
            this.cruiseEnergy = Math.min(1, this.cruiseEnergy);
        } else {
            this.cruiseEnergy -= tpf / 2;
            this.cruiseEnergy = Math.max(0, this.cruiseEnergy);
        }

        // update engine behaviour
        if (!this.engines.isEmpty()) {
            for (Engine engine : this.engines) {
                engine.getSound().setPitch(Math.min(2f, Math.max(0.5f, Math.abs(this.throttle) + 1)));
                engine.getSound().setVelocity(this.physics.getLinearVelocity());
                if (this.getCruise() && this.cruiseEnergy >= 1) {
                    engine.setWidthFactor(4);
                } else {
                    engine.setWidthFactor(1);
                }
            }
        }
    }

    @Override 
    protected void controlRender(RenderManager rm, ViewPort vp) { /* intentionally blank */ }
    
    private void checkValues() {                             
        if (this.cruise) {
            this.throttle    = 1f;
            this.glide   = false;
        } else {
            if (this.throttle != 0) {
                this.glide = false;
            }
        }
    }
        
    public boolean moveTo(Vector3f position, float tolerance, float throttle, float otherspeed, float tpf) {
        return moveTo(position, tolerance, getUpVector(), throttle, otherspeed, tpf);
    }
    
    /**
     * Main method for all active AI/autopilot ship movement to a world point.
     * For a proper move-to this method has to be called subsequently every 
     * frame with the same target vector until it returns true. 
     * @param position the desired target position to move to
     * @param tolerance the distance within which the target point will be considered as reached
     * @param up the desired upside of the ship while navigating
     * @param throttle the desired throttle setting for travel
     * @param otherspeed if this is > 0 throttle will be set to match the targets speed
     * @param tpf time per frame
     * @return false if target point is not reached. true if target point is reached
     */
    public boolean moveTo(Vector3f position, float tolerance, Vector3f up, float throttle, float otherspeed, float tpf) {
        Vector3f steeringPosition = new Vector3f();       
        steeringPosition.set(position).subtractLocal(getSpatial().getWorldTranslation());
        
        float distance = steeringPosition.length();
        float angle = getFacing().angleBetween(steeringPosition.normalize());
        if (distance <= tolerance) {
            setThrottle(otherspeed > 0 ? getTopspeed() / otherspeed : 0);
            return true;
        }
                        
        float breakthrottle = Math.min(1, distance / (FastMath.pow((getForwardVelocity() - otherspeed), 2) / (2 * this.topspeed * this.lineardamp) + this.object.getSize() * 2));
        float anglethrottle = angle == 0 ? 1 : Math.max(0.1f, 1 - (angle / FastMath.HALF_PI));
        float desiredthrottle = Math.min(1, throttle);
        
        setThrottle(anglethrottle * breakthrottle * desiredthrottle);

        turnTo(position, up, tpf);
                
        return false;
    }
        
    public void turnTo(Vector3f position, float tpf) {
        turnTo(position, getUpVector(), tpf);
    }
    
    /**
     * Main method for all active AI/autopilot ship rotation towards a world point.
     * For proper turning this has to be called subsequently every frame.
     * @param position the desired target position to turn to
     * @param up the desired upside of the ship while turning 
     * @param tpf time per frame
     */
    public void turnTo(Vector3f position, Vector3f up, float tpf) {        
        // NOW WITH REAL TURNING, USING FLIGHTCONTROL!!!
        Vector3f steeringData = getSteeringData(position, up, tpf);
        setElevator(steeringData.x);
        setRudder(steeringData.y);
        setAileron(steeringData.z);
    }
    
    private Vector3f getSteeringData2(Vector3f worldPosition, Vector3f up) {
        Vector3f steeringPosition = new Vector3f();       
        getSpatial().getWorldRotation().inverse().multLocal(steeringPosition.set(worldPosition).subtractLocal(getSpatial().getWorldTranslation()));

        Vector3f upPosition = new Vector3f(up);
        getSpatial().getWorldRotation().inverse().multLocal(upPosition);
        
        Vector3f elevatorPos = new Vector3f(steeringPosition).normalizeLocal();
        elevatorPos.x = 0;        
        Vector3f rudderPos = new Vector3f(steeringPosition).normalizeLocal();
        rudderPos.y = 0;
        Vector3f aileronPos = new Vector3f(upPosition).normalizeLocal();
        aileronPos.z = 0;
        
        Vector3f steeringData = new Vector3f();
        steeringData.x = Vector3f.UNIT_Z.angleBetween(elevatorPos);
        if (elevatorPos.y > 0) {
            steeringData.x *= -1;
        }
        steeringData.y = Vector3f.UNIT_Z.angleBetween(rudderPos);
        if (rudderPos.x < 0) {
            steeringData.y *= -1;
        }
        steeringData.z = Vector3f.UNIT_Y.angleBetween(aileronPos);
        if (aileronPos.x > 0) {
            steeringData.z *= -1;
        }       
               
        Vector3f tspds = getSpatial().getWorldRotation().inverse().mult(this.physics.getAngularVelocity());
        
        if (steeringData.x != 0 && tspds.x != 0) {
            steeringData.x = -steeringData.x / (this.turnrate);
        }
        if (steeringData.y != 0 && tspds.y != 0) {
            steeringData.y = -steeringData.y / (this.turnrate);
        }
        if (steeringData.z != 0 && tspds.z != 0) {
            steeringData.z = -steeringData.z / (this.turnrate);
        }
     
        return steeringPosition;        
    }
    
    private Vector3f getSteeringData(Vector3f worldPosition, Vector3f up, float tpf) {
        Vector3f steeringPosition = new Vector3f();       
        getSpatial().getWorldRotation().inverse().multLocal(steeringPosition.set(worldPosition).subtractLocal(getSpatial().getWorldTranslation()));

        Vector3f upPosition = new Vector3f(up);
        getSpatial().getWorldRotation().inverse().multLocal(upPosition);
        
        Vector3f elevatorPos = new Vector3f(steeringPosition).normalizeLocal();
        elevatorPos.x = 0;        
        Vector3f rudderPos = new Vector3f(steeringPosition).normalizeLocal();
        rudderPos.y = 0;
        Vector3f aileronPos = new Vector3f(upPosition).normalizeLocal();
        aileronPos.z = 0;
        
        Vector3f steeringData = new Vector3f();
        steeringData.x = Vector3f.UNIT_Z.angleBetween(elevatorPos);
        if (elevatorPos.y > 0) {
            steeringData.x *= -1;
        }
        steeringData.y = Vector3f.UNIT_Z.angleBetween(rudderPos);
        if (rudderPos.x < 0) {
            steeringData.y *= -1;
        }
        steeringData.z = Vector3f.UNIT_Y.angleBetween(aileronPos);
        if (aileronPos.x > 0) {
            steeringData.z *= -1;
        }       
               
        Vector3f tspds = getSpatial().getWorldRotation().inverse().mult(this.physics.getAngularVelocity());
        
        if (steeringData.x != 0 && tspds.x != 0) {
            steeringData.x = steeringData.x / (this.turnrate * FastMath.DEG_TO_RAD * tpf);
        }
        if (steeringData.y != 0 && tspds.y != 0) {
            steeringData.y = steeringData.y / (this.turnrate * FastMath.DEG_TO_RAD * tpf);
        }
        if (steeringData.z != 0 && tspds.z != 0) {
            steeringData.z = steeringData.z / (this.turnrate * FastMath.DEG_TO_RAD * tpf);
        }      
        
        return steeringData;
    }
               
    /** SETTERS **/  
    
    /**
     * Toggles the cruise engines.
     * @param cruise true to activate cruise mode, false to deactivate it
     */
    public void setCruise(boolean cruise) {
        if (!this.hasCruise) {
            cruise = false;
        }
        this.cruise = cruise;
    }
    
    /**
     * Toggles glide mode.
     * @param glide true to activate glide mode, false to deactivate it
     */
    public void setGlide(boolean glide) {
        this.glide = glide;
        if (this.glide) {
            this.throttle = 0;
        }
    }
    
    /**
     * Set the throttle of the engines
     * @param throttle value between 0 and 1 to set percentage of throttle
     */
    public void setThrottle(float throttle) {
        this.throttle = throttle;
        this.throttle = Math.min(1f, this.throttle);
        this.throttle = Math.max(-1f, this.throttle);
        setCruise(false);
    }
    
    /**
     * For turning left/right.
     * @param rudder value between -1 and 1 to set percentage 
     */
    public void setRudder(float rudder) {
        this.rudder = rudder;
        this.rudder = Math.min(1f, this.rudder);
        this.rudder = Math.max(-1f, this.rudder);
    }
    
    /**
     * For turning up/down.
     * @param elevator value between -1 and 1 to set percentage
     */
    public void setElevator(float elevator) {
        this.elevator = elevator;
        this.elevator = Math.min(1f, this.elevator);
        this.elevator = Math.max(-1f, this.elevator);
    }
    
    /**
     * For rolling the ship.
     * @param aileron value between -1 and 1 to set percentage
     */
    public void setAileron(float aileron) {
        this.aileron = aileron;
        this.aileron = Math.min(1f, this.aileron);
        this.aileron = Math.max(-1f, this.aileron);
    }
    
    /**
     * For strafing left/right.
     * @param strafe value between -1 and 1 to set percentage
     */
    public void setStrafe(float strafe) {
        this.strafe = strafe;
        this.strafe = Math.min(1f, this.strafe);
        this.strafe = Math.max(-1f, this.strafe);
    }
    
    /**
     * For strafing up/down.
     * @param lift value between -1 and 1 to set percentage
     */
    public void setLift(float lift) {
        this.lift = lift;
        this.lift = Math.min(1f, this.lift);
        this.lift = Math.max(-1f, this.lift);
    }
    
    /**
     * Sets the color of the engine-effects. Mainly used by missions to make
     * teams easily distinguishable.
     * @param color the desired engine color
     */
    public void setEngineColor(ColorRGBA color) {
        for (Engine engine : this.engines) {
            engine.setColor(color);
        }
    }
    
    public boolean getGlide() {
        return this.glide;
    }
    
    public float getCruiseEnergy() {
        return this.cruiseEnergy;
    }
    
    public boolean getCruise() {
        return this.cruise;
    }
    
    public float getThrottle() {
        return this.throttle;
    }
    
    public float getRudder() {
        return this.rudder;
    }
    
    public float getElevator() {
        return this.elevator;
    }
    
    public float getAileron() {
        return this.aileron;
    }
    
    public float getStrafe() {
        return this.strafe;
    }
    
    public float getLift() {
        return this.lift;
    }
    
    public float getForwardVelocity() {
        return this.forwardVelocity;
    }
    
    public Vector3f getUpVector() {
        return this.physics.getUpVector();
    }
    
    public Vector3f getFacing() {
        return this.physics.getFacing();
    }
    
    public boolean getHasCruise() {
        return this.hasCruise;
    }
    
    /* PHYSICAL "CONSTANTS" */   
    public void setTopspeed(float topspeed) {
        this.topspeed = topspeed;
    }
    
    public float getTopspeed() {
        return this.topspeed;
    }
    
    public void setTopspeedReverse(float topspeedreverse) {
        this.topspeedreverse = topspeedreverse;
    }
    
    public float getTopspeedReverse() {
        return this.topspeedreverse;
    }

    public void setTurnrate(float turnrate) {
        this.turnrate = turnrate;
    }
    
    public float getTurnrate() {
        return this.turnrate;
    }
              
    /* GENERAL STUFF */   

    @Override
    public void resetSystem() {
        this.cruise = false;
        this.rudder = 0;
        this.aileron = 0;
        this.elevator = 0;
        this.lift = 0;
        this.strafe = 0;
        this.throttle = 0;
        this.physics = getSpatial().getControl(PhysicsControl.class);
        if (this.physics != null) {
            this.physics.setLinearVelocity(Vector3f.ZERO);
            this.physics.setAngularVelocity(Vector3f.ZERO);
        }
    }

    @Override
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        setTopspeed(XMLLoader.getFloatValue(element, "topspeed", 130f));
        setTurnrate(XMLLoader.getFloatValue(element, "turnrate", 3f));
        setTopspeedReverse(XMLLoader.getFloatValue(element, "topspeedreverse", 20f));
        this.hasCruise = XMLLoader.getBooleanValue(element, "hascruise", this.hasCruise);
        
        List<Element> engineElems = XMLLoader.readElementList("engine", element);        
        for (Element engineElem : engineElems) {
            Engine engine = new Engine(engineElem, path, gamedataManager);
            this.engines.add(engine);
        }
    }
 
    private final PhysicsTickListener forceUpdater = new PhysicsTickListener() {
       
        @Override
        public void prePhysicsTick(PhysicsSpace space, float tpf) {            
            checkValues();

            lineardamp = 0.75f;
            angulardamp = 0.9999f;

            if (FlightControl.this.physics == null) {
                return;
            }
            
            if (!FlightControl.this.glide) {
                FlightControl.this.physics.setDamping(lineardamp, angulardamp);
                FlightControl.this.physics.setFriction(0.2f);
            } else {
                FlightControl.this.physics.setDamping(0f, angulardamp);
                FlightControl.this.physics.setFriction(0f);
            }

            /*
                linear force calculations ahead
            */

            // update the current forward velocity member
            Vector3f currentLinearVelocity = FlightControl.this.physics.getLinearVelocity();
            forwardVelocity = FlightControl.this.object.getRotation().inverse().mult(currentLinearVelocity).getZ();

            // compute forces for lateral (x), vertical (y) and forward (Z) components
            Vector3f linearForce = new Vector3f();
            linearForce.setX(computeForce(topspeedreverse, topspeedreverse, strafe, physics.getMass(), lineardamp));
            linearForce.setY(computeForce(topspeedreverse, topspeedreverse, lift, physics.getMass(), lineardamp));
            if ( FlightControl.this.cruise && FlightControl.this.cruiseEnergy >= 1 ) {
                linearForce.setZ(computeForce(VAR_CRUISESPEED, VAR_CRUISESPEED, 1, physics.getMass(), lineardamp));
            } else {
                linearForce.setZ(computeForce(topspeed, topspeedreverse, throttle, physics.getMass(), lineardamp));
            }

            // convert linear force to world coordinates and apply
            FlightControl.this.object.getRotation().multLocal(linearForce);
            FlightControl.this.physics.applyCentralForce(linearForce);

            /*
                angular force calculations ahead
            */
           
            // compute forces for pitch (x), yaw (y) and roll (z) components
            Vector3f angularForce = new Vector3f();
            angularForce.setX(computeForce(turnrate, turnrate, elevator, physics.getMass(), angulardamp));
            angularForce.setY(computeForce(turnrate, turnrate, rudder, physics.getMass(), angulardamp));
            angularForce.setZ(computeForce(turnrate, turnrate, aileron, physics.getMass(), angulardamp));

            // convert angular force to world coordinates and apply
            FlightControl.this.object.getRotation().multLocal(angularForce);
            FlightControl.this.physics.applyTorque(angularForce);
        }

        @Override
        public void physicsTick(PhysicsSpace space, float tpf) { /* intentionally blank */ }
        
        private float computeForce(float topSpeed, float topSpeedReverse, float factor, float mass, float dampening) {
            // skip calculation if factor is 0
            if (factor == 0) {
                return 0;
            }
            
            // calculate physical velocity
            float v;
            if (factor > 0) {
                v = factor * topSpeed / (dampening - 0.05f);
            } else {
                v = factor * topSpeedReverse / (dampening - 0.05f);
            }

            // skip force calculation if velocity is 0
            if (v == 0) {
                return 0;
            }
                        
            // calculate force
            float t = 1;
            float a = v / t;
            float f = mass * a;        
                        
            return f;
        }
        
    };
    
}
