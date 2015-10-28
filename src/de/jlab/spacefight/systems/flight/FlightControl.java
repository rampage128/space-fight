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

    // STATIC CRUISE-ENGINE STUFF (THIS IS STATIC BECAUSE IT IS THE SAME FOR ALL SHIPS!)
    public static final float VAR_CRUISESPEED = 1000f;
    public static final float VAR_CRUISEDAMP  = 2000f;
    public static final float VAR_CRUISEACCEL = 200f;
    
    private float acceleration      = 20f;
    private float topspeed          = 100f;
    private float topspeedreverse   = 20f;
    private float turnrate          = 2f;
    private float momentum          = 6f;
    private float angulardamp       = 0.9999f;
    private float lineardamp        = 0.75f;
       
    private ObjectInfoControl object;
    //private SpaceAppState space;
        
    private float _throttle    = 0f;
    private float _elevator  = 0f;
    private float _rudder    = 0f;
    private float _aileron   = 0f;
    
    private float cruiseEnergy = 0f;
    
    private boolean hasCruise = true;
    private boolean cruise  = false;
    private boolean glide   = false;
        
    private float forwardVelocity = 0f;
    
    private PhysicsControl physics;
    private ArrayList<Engine> engines = new ArrayList<Engine>();
    
    public FlightControl(ObjectInfoControl object) {
        this.object = object;
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if ( spatial != null ) {
            Game.get().getStateManager().getState(SpaceAppState.class).getPhysics().getPhysicsSpace().addTickListener(forceUpdater);
            for (Engine engine : this.engines) {
                engine.attachTo((Node)spatial);
            }
                        
        } else {
            Game.get().getStateManager().getState(SpaceAppState.class).getPhysics().getPhysicsSpace().removeTickListener(forceUpdater);
            for (Engine engine : this.engines) {
                engine.detach();
            }
        }
    }
    
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        FlightControl control = new FlightControl(this.object);
        //control._enginesound = this._enginesound.clone();
        control.setSpatial(spatial);
        return control;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if ( getSpatial() != null ) {
            /*
            SpaceDebugger.getInstance().removeItem(DebugContext.AI, "av_a" + getSpatial().getName());
            SpaceDebugger.getInstance().removeItem(DebugContext.AI, "av_b" + getSpatial().getName());
            SpaceDebugger.getInstance().removeItem(DebugContext.AI, "av_c" + getSpatial().getName());
            // AHEAD PROXIMITY AVOIDANCE
            Vector3f checkDirection = object.getFacing().normalize().mult(object.getLinearVelocity().length() * 2f);
            ArrayList<TargetInformation> targetList = object.getObjectControl(SensorControl.class).getTargetList(checkDirection.length() * 4, TargetInformation.FOF_FRIEND, TargetInformation.FOF_FOE, TargetInformation.FOF_NEUTRAL);
            for (TargetInformation target : targetList) {
                Vector3f targetDirection = target.getDirection(object); // target.getObject().getPosition().subtract(getAI().getObjectInfo().getPosition());
                Vector3f cv = targetDirection.subtract(checkDirection);
                if (cv.length() < target.getObject().getSize() + object.getSize()) {
                    if (object == Game.get().getPlayer().getClient()) {
                        System.out.println(cv.length() + " - " + (target.getObject().getSize() / 2 + object.getSize() / 2));
                    }
                    SpaceDebugger.getInstance().setVector(DebugContext.AI, "av_a" + getSpatial().getName(), getSpatial().getWorldTranslation(), checkDirection, ColorRGBA.Yellow);
                    SpaceDebugger.getInstance().setVector(DebugContext.AI, "av_b" + getSpatial().getName(), getSpatial().getWorldTranslation(), targetDirection, ColorRGBA.Yellow);
                    SpaceDebugger.getInstance().setVector(DebugContext.AI, "av_c" + getSpatial().getName(), getSpatial().getWorldTranslation().add(checkDirection), cv, ColorRGBA.Red);
                } else {
                    SpaceDebugger.getInstance().setVector(DebugContext.AI, "av_a" + getSpatial().getName(), getSpatial().getWorldTranslation(), checkDirection, ColorRGBA.Gray);
                    SpaceDebugger.getInstance().setVector(DebugContext.AI, "av_b" + getSpatial().getName(), getSpatial().getWorldTranslation(), targetDirection, ColorRGBA.Gray);
                    SpaceDebugger.getInstance().setVector(DebugContext.AI, "av_c" + getSpatial().getName(), getSpatial().getWorldTranslation().add(checkDirection), cv, ColorRGBA.White);
                }
            }
            */
            
            if (this.physics == null) {
                this.physics = getSpatial().getControl(PhysicsControl.class);
                if (this.physics == null) {
                    throw new IllegalArgumentException("Object with FlightControl needs a PhysicsControl: " + this.object.getId());
                }
            }
                        
            
            
            if (!engines.isEmpty()) {
                for (Engine engine : this.engines) {
                    engine.setThrottle(_throttle);
                    if ( getSpatial().getParent() != null ) {                    
                        Game.get().getAudioManager().playSoundLoop(engine.getSound());
                    } else {
                        Game.get().getAudioManager().stopSoundLoop(engine.getSound());
                    }
                }
            }
        }
                
        if (this.getCruise()) {
            cruiseEnergy += tpf / 3;
            cruiseEnergy = Math.min(1, cruiseEnergy);
        } else {
            cruiseEnergy -= tpf / 2;
            cruiseEnergy = Math.max(0, cruiseEnergy);
        }
        
        
        if (!this.engines.isEmpty()) {
            for (Engine engine : this.engines) {
            //_enginesound.setLocalTranslation(spatial.getWorldTranslation());
                engine.getSound().setPitch(Math.min(2f, Math.max(0.5f, Math.abs(this._throttle) + 1))); // 1f + (Math.abs(_velocity) / this.topspeed) * 0.5f
                engine.getSound().setVelocity(this.physics.getLinearVelocity());
                if (this.getCruise() && cruiseEnergy >= 1) {
                    engine.setWidthFactor(4);
                } else {
                    engine.setWidthFactor(1);
                }
            //System.out.println(this.physics.getLinearVelocity());
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }
    
    private void checkValues() {                             
        if ( cruise ) {
            _throttle    = 1f;
            //_elevator   *= 0f;
            //_rudder     *= 0f;
            //_aileron    *= 0f;
            //_strafe     *= 0f;
            //_lift       *= 0f;
            this.glide   = false;
        } else {
            if (this._throttle != 0) {
                this.glide = false;
            }
        }
    }
        
    //private float _velocity         = 0f;
    //private float _strafeVelocity   = 0f;
    //private float _liftVelocity     = 0f;
    
    //private float _roll     = 0f;
    //private float _pitch    = 0f;
    //private float _yaw      = 0f;
    private float _strafe   = 0f;
    private float _lift     = 0f;
      
    public boolean moveTo(Vector3f position, float tolerance, float throttle, float otherspeed, float tpf) {
        return moveTo(position, tolerance, getUpVector(), throttle, otherspeed, tpf);
    }
    
    public boolean moveTo(Vector3f position, float tolerance, Vector3f up, float throttle, float otherspeed, float tpf) {
        Vector3f steeringPosition = new Vector3f();       
        steeringPosition.set(position).subtractLocal(getSpatial().getWorldTranslation());
        
        //Vector3f route = getSpatial().getWorldRotation().inverse().mult(position.subtract(getSpatial().getWorldTranslation()));
        float distance = steeringPosition.length();
        float angle = getFacing().angleBetween(steeringPosition.normalize());
        if (distance <= tolerance) {
            setThrottle(otherspeed > 0 ? getTopspeed() / otherspeed : 0);
            return true;
        }
                
        //float turn = new Vector3f(_pitch, _yaw, _roll).length();
        
        float breakthrottle = Math.min(1, distance / (FastMath.pow((getForwardVelocity() - otherspeed), 2) / (2 * topspeed * lineardamp) + object.getSize() * 2));
        float anglethrottle = angle == 0 ? 1 : Math.max(0.1f, 1 - (angle / FastMath.HALF_PI));
        float desiredthrottle = Math.min(1, throttle);
        
        setThrottle(anglethrottle * breakthrottle * desiredthrottle);

        turnTo(position, up, tpf);
                
        return false;
    }
        
    public void turnTo(Vector3f position, float tpf) {
        turnTo(position, getUpVector(), tpf);
    }
    
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
            steeringData.x = steeringData.x / (this.turnrate * FastMath.DEG_TO_RAD * tpf); //(steeringData.x - (FastMath.pow(tspds.x, 2) / (2 * this.turnrate * angulardamp))) / (this.turnrate * FastMath.DEG_TO_RAD);
        }
        if (steeringData.y != 0 && tspds.y != 0) {
            steeringData.y = steeringData.y / (this.turnrate * FastMath.DEG_TO_RAD * tpf); //(steeringData.y - (FastMath.pow(tspds.y, 2) / (2 * (this.turnrate * FastMath.DEG_TO_RAD) * angulardamp))) / (this.turnrate * FastMath.DEG_TO_RAD);            
        }
        if (steeringData.z != 0 && tspds.z != 0) {
            steeringData.z = steeringData.z / (this.turnrate * FastMath.DEG_TO_RAD * tpf); //(steeringData.z - (FastMath.pow(tspds.z, 2) / (2 * this.turnrate * angulardamp))) / (this.turnrate * FastMath.DEG_TO_RAD);
        }
        
        /*
        if (Game.get().getCameraManager().getTarget() == this.getSpatial().getControl(ObjectInfoControl.class)) {
            System.out.println(steeringData + "(" + (this.turnrate * FastMath.DEG_TO_RAD * tpf) + ")");
        }
        * /

        /*
        if (Game.get().getPlayer().getClient() != null) {
            WeaponSystemControl weapons = Game.get().getPlayer().getClient().getObjectControl(WeaponSystemControl.class);
            if (weapons != null) {
                if (weapons.getTarget() != null && weapons.getTarget().getObject().getSpatial() == getSpatial()) {
                    System.out.println((Vector3f.UNIT_Z.angleBetween(steeringPosition.normalize()) * FastMath.RAD_TO_DEG) + " | " + steeringData);
                }
            }
        }
        */
        
        
        /* ANOTHER WAY USING QUATERNIONS
        Quaternion targetRotation = new Quaternion();
        targetRotation.lookAt(steeringPosition, upPosition);
        
        Vector3f steeringData = new Vector3f();
        float[] angles = targetRotation.toAngles(null);
        
        if (angles[0] != 0) {
            steeringData.x = angles[0] / (FastMath.pow(flightControl.getTurnrate(), 2) / (2 * flightControl.getAngulardamp()));
        }
        if (angles[1] != 0) {
            steeringData.y = angles[1] / (FastMath.pow(flightControl.getTurnrate(), 2) / (2 * flightControl.getAngulardamp()));            
        }
        if (angles[2] != 0) {
            steeringData.z = angles[2] / (FastMath.pow(flightControl.getTurnrate(), 2) / (2 * flightControl.getAngulardamp()));
        }
        
        //steeringData.x = angles[0] / flightControl.getAngulardamp();// / flightControl.getTurnrate();
        //steeringData.y = angles[1] / flightControl.getAngulardamp();// / flightControl.getTurnrate();
        //steeringData.z = angles[2] / flightControl.getAngulardamp();// / flightControl.getTurnrate();
        
        box.setLineWidth(4);
        gem.setMaterial(mat);
        mat.getAdditionalRenderState().setWireframe(true);
        //((Node)getSpatial()).attachChild(gem);
        gem.setLocalTranslation(steeringPosition);
        
        //this.getSpatial().worldToLocal(worldPosition, steeringPosition);        
        */
        
        return steeringData;
    }
            
    private float computeVelocity(float tpf, float factor, float v, float max, float min, float dampening, float acc, boolean useGlide) {        
        // I'M SURE THIS CAN BE DONE MUCH BETTER WITH LESS OPERATIONS!!!
        
        // POSITIVE AXIS
        if ( factor >= 0 ) {
            // DAMPENING
            if (!glide || !useGlide) {
                if ( v > max * factor ) {
                    v -= (dampening * tpf);
                    if ( v < max * factor )
                        v = max * factor;
                }
            }

            // ACCELERATION
            if ( v < max * factor ) {
                v += (acc * tpf);
                if ( v > max * factor ) {
                    v = max * factor;
                }
            }
            return v;
        } else {
            // DAMPENING
            if (!glide || !useGlide) {
                if ( v < min * factor ) {
                    v += (dampening * tpf);
                    if ( v > min * factor )
                        v = min * factor;
                }
            }

            // ACCELERATION
            if ( v > min * factor ) {
                v -= (acc * tpf);
                if ( v < min * factor ) {
                    v = min * factor;
                }
            }
            return v;
        }
    }
    
    /** SETTERS **/  
    public void setCruise(boolean cruise) {
        if (!hasCruise) {
            cruise = false;
        }
        this.cruise = cruise;
    }
    
    public void setGlide(boolean glide) {
        this.glide = glide;
        //if (this._velocity > this.topspeed || this.cruise) {
            //this.glide = false;
        //}
        if (this.glide) {
            this._throttle = 0;
            //this._velocity = 0;
        }
    }
    
    public void setThrottle(float throttle) {
        _throttle = throttle;
        _throttle = Math.min(1f, _throttle);
        _throttle = Math.max(-1f, _throttle);
        setCruise(false);
    }
    
    public void setRudder(float rudder) {
        _rudder = rudder;
        _rudder = Math.min(1f, _rudder);
        _rudder = Math.max(-1f, _rudder);
    }
    
    public void setElevator(float elevator) {
        _elevator = elevator;
        _elevator = Math.min(1f, _elevator);
        _elevator = Math.max(-1f, _elevator);
    }
    
    public void setAileron(float aileron) {
        _aileron = aileron;
        _aileron = Math.min(1f, _aileron);
        _aileron = Math.max(-1f, _aileron);
    }
    
    public void setStrafe(float strafe) {
        _strafe = strafe;
        _strafe = Math.min(1f, _strafe);
        _strafe = Math.max(-1f, _strafe);
    }
    
    public void setLift(float lift) {
        _lift = lift;
        _lift = Math.min(1f, _lift);
        _lift = Math.max(-1f, _lift);
    }
    
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
        return _throttle;
    }
    
    public float getRudder() {
        return _rudder;
    }
    
    public float getElevator() {
        return _elevator;
    }
    
    public float getAileron() {
        return _aileron;
    }
    
    public float getStrafe() {
        return _strafe;
    }
    
    public float getLift() {
        return _lift;
    }
    
    public float getForwardVelocity() {
        return this.forwardVelocity;
    }
    
    //public float getVelocity() {
        //return _velocity;
    //}
    
    public Vector3f getUpVector() {
        return this.physics.getUpVector();
    }
    
    public Vector3f getFacing() {
        return this.physics.getFacing();
    }
    
    //public void setVelocity(float velocity) {
        //_velocity = velocity;
    //}
    
    public boolean getHasCruise() {
        return this.hasCruise;
    }
    
    /* PHYSICAL "CONSTANTS" */
    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }
    
    public float getAcceleration() {
        return this.acceleration;
    }
    
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
    
    public void setMomentum(float momentum) {
        this.momentum = momentum;
    }
    
    public float getMomentum() {
        return this.momentum;
    }
    
    /*
    public void setAngulardamp(float angulardamp) {
        this.angulardamp = angulardamp;
    }
    
    public float getAngulardamp() {
        return this.angulardamp;
    }
    
    public void setLineardamp(float lineardamp) {
        this.lineardamp = lineardamp;
    }
    
    public float getLineardamp() {
        return this.lineardamp;
    }
    */ 
        
    /* GENERAL STUFF */   
    public void resetSystem() {
        this.cruise = false;
        _rudder = 0;
        _aileron = 0;
        _elevator = 0;
        _lift = 0;
        _strafe = 0;
        _throttle = 0;
        //_velocity = 0;
        this.physics = getSpatial().getControl(PhysicsControl.class);
        if (this.physics != null) {
            this.physics.setLinearVelocity(Vector3f.ZERO);
            this.physics.setAngularVelocity(Vector3f.ZERO);
        }
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        setTopspeed(XMLLoader.getFloatValue(element, "topspeed", 130f));
        //setLineardamp(XMLLoader.getFloatValue(element, "lineardamp", 30f));
        //setAngulardamp(XMLLoader.getFloatValue(element, "angulardamp", 5f));
        setMomentum(XMLLoader.getFloatValue(element, "momentum", 7f));
        setAcceleration(XMLLoader.getFloatValue(element, "acceleration", 25f));
        setTurnrate(XMLLoader.getFloatValue(element, "turnrate", 3f));
        setTopspeedReverse(XMLLoader.getFloatValue(element, "topspeedreverse", 20f));
        this.hasCruise = XMLLoader.getBooleanValue(element, "hascruise", this.hasCruise);
        
        List<Element> engineElems = XMLLoader.readElementList("engine", element);        
        for (Element engineElem : engineElems) {
            Engine engine = new Engine(engineElem, path, gamedataManager);
            this.engines.add(engine);
        }
    }
 
/*
    private PhysicsTickListener velocityUpdater = new PhysicsTickListener() {
       
        private Vector3f linearVelocity = new Vector3f();
        private Vector3f angularVelocity = new Vector3f();

        private Vector3f applyLinearVelocity = new Vector3f();
        private Vector3f applyAngularVelocity = new Vector3f();
    
        public void prePhysicsTick(PhysicsSpace space, float tpf) {
            // GET PHYSICS CONTROL
            checkValues();
            
            // SECURITY CHECK FOR GLIDE MODE
            if (FlightControl.this._velocity > FlightControl.this.topspeed || FlightControl.this._throttle != 0) {
                glide = false;
            }

            this.linearVelocity     = FlightControl.this.physics.getLinearVelocity();
            this.angularVelocity    = FlightControl.this.physics.getAngularVelocity();

            // GENERAL DAMPING
            if (!glide) {
                this.dampVector(this.linearVelocity, _velocity > FlightControl.this.topspeed ? FlightControl.this.lineardamp * (10 + _velocity / FlightControl.this.topspeed) : FlightControl.this.lineardamp, tpf);
            }

            // CALCULATE FORWARD VELOCITY FROM INPUT
            if ( FlightControl.this.cruise ) {
                _velocity = computeVelocity(tpf, 1, _velocity, VAR_CRUISESPEED, 0f, VAR_CRUISEDAMP, VAR_CRUISEACCEL, false);            
            } else {
                _velocity = computeVelocity(tpf, _throttle, _velocity, FlightControl.this.topspeed, FlightControl.this.topspeedreverse, _velocity > FlightControl.this.topspeed ? FlightControl.this.lineardamp * (10 + _velocity / FlightControl.this.topspeed) : FlightControl.this.lineardamp, FlightControl.this.acceleration, true);
            }

            // CALCULATE STRAFE VELOCITY FROM INPUT
            _strafeVelocity = computeVelocity(tpf, _strafe, _strafeVelocity, FlightControl.this.topspeedreverse * 2, FlightControl.this.topspeedreverse * 2, FlightControl.this.lineardamp, FlightControl.this.acceleration, true);
            _liftVelocity = computeVelocity(tpf, _lift, _liftVelocity, FlightControl.this.topspeedreverse * 2, FlightControl.this.topspeedreverse * 2, FlightControl.this.lineardamp, FlightControl.this.acceleration, true);
            // GET SHIPS CURRENT ROTATION
            Quaternion orientation = FlightControl.this.physics.getPhysicsRotation();
            // CREATE LINEAR VELOCITY VECTOR
            this.applyLinearVelocity.set(_strafeVelocity, _liftVelocity, _velocity);
            // ALIGN VECTOR WITH SHIPS CURRENT ROTATION (LOCAL FORWARD)
            orientation.multLocal(this.applyLinearVelocity);
            // APPLY LINEAR VELOCITY ON SHIP
            //this.physics.setLinearVelocity(this.linearVelocity);

            this.combineVectors(this.applyLinearVelocity, this.linearVelocity);
            //this.linearVelocity.addLocal(this.physics.getLinearVelocity());

            //this.physics.applyLinearVelocity(this.applyLinearVelocity); IMPORTANT!
            //this.physics.applyForce(this.object.getRotation().multLocal(new Vector3f(0, 0, this.acceleration * this._throttle * this.physics.getMass())), Vector3f.ZERO);
            //this.physics.applyCentralForce(linearVelocity);

            // CALCULATE ROTATION SPEEDS (LOCAL AXIS)
            _pitch   = computeVelocity(tpf, _elevator, _pitch, FlightControl.this.turnrate, FlightControl.this.turnrate, FlightControl.this.angulardamp, FlightControl.this.momentum, false);
            _yaw     = computeVelocity(tpf, _rudder, _yaw, FlightControl.this.turnrate, FlightControl.this.turnrate, FlightControl.this.angulardamp, FlightControl.this.momentum, false);
            _roll    = computeVelocity(tpf, _aileron, _roll, FlightControl.this.turnrate, FlightControl.this.turnrate, FlightControl.this.angulardamp, FlightControl.this.momentum, false);

            // CREATE ANGULAR VELOCITY VECTOR
            this.applyAngularVelocity.set(_pitch, _yaw ,_roll);
            // ALIGN VECTOR WITH SHIPS CURRENT ROTATION (LOCAL AXES)
            orientation.multLocal(this.applyAngularVelocity);
            // APPLY ANGULAR VELOCITY
            //combineVectors(av, this.physics.getAngularVelocity());
            //this.physics.applyAngularVelocity(applyAngularVelocity); IMPORTANT! 
        }

        public void physicsTick(PhysicsSpace space, float tpf) {
            // NOTHING TO DO HERE
        }
        
          //////////////////////
         // HELPER FUNCTIONS //
        //////////////////////
        
        // DAMPENING
        private void dampVector(Vector3f vector, float dampening, float tpf) {
            vector.setX(dampVectorComponent(vector.getX(), dampening, tpf));
            vector.setY(dampVectorComponent(vector.getY(), dampening, tpf));
            vector.setZ(dampVectorComponent(vector.getZ(), dampening, tpf));
        }

        private float dampVectorComponent(float component, float dampening, float tpf) {
            // POSITIVE AXIS
            if (component >= 0) {
                // DAMPENING
                component -= (dampening * tpf);
                if (component < 0) {
                    component = 0;
                }
            } else {
                // DAMPENING
                component += (dampening * tpf);
                if (component < 0) {
                    component = 0;
                }
            }
            return component;
        }
        
        // COMBINATION
        private void combineVectors(Vector3f destination, Vector3f source) {
            // FIXME FLIGHT VELOCITY COMBINATION IS SOMEWHAT BUGGY... 
            // WHEN FLYING AGAINST THE PHYSICS-VELOCITY DIRECTION FLIGHT-VELOCITY IS APPLYED INSTANTLY...
            destination.x = combineVectorComponents(destination.x, source.x);
            destination.y = combineVectorComponents(destination.y, source.y);
            destination.z = combineVectorComponents(destination.z, source.z);
        }

        private float combineVectorComponents(float destination, float source) {
            float result = destination;
            if (destination >= 0 && source >= 0) {
                result = Math.max(destination, source);
            } else if (destination < 0 && source < 0) {
                result = Math.min(destination, source);
            } else {
                result = source + destination;
            }
            return result;
        }
        
    };
*/

    private PhysicsTickListener forceUpdater = new PhysicsTickListener() {
       
        public void prePhysicsTick(PhysicsSpace space, float tpf) {
            // GET PHYSICS CONTROL
            //float linearDampingFactor = 10;
            
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

              //////////////////
             // LINEAR STUFF //
            //////////////////

            // GET CURRENT LINEAR VELOCITY FOR FORCE CALCULATIONS
            Vector3f currentLinearVelocity = FlightControl.this.physics.getLinearVelocity();
            // GET CURRENT LINEAR VELOCITY COMPONENTS
            //float strafeVelocity  = FlightControl.this.object.getRotation().inverse().mult(currentLinearVelocity).getX();
            //float liftVelocity    = FlightControl.this.object.getRotation().inverse().mult(currentLinearVelocity).getY();
            forwardVelocity = FlightControl.this.object.getRotation().inverse().mult(currentLinearVelocity).getZ();
            // CALCULATE COMMON LINEAR DAMPING FORCE
            //Vector3f linearDampingForce = currentLinearVelocity.negate().normalizeLocal().multLocal(this.glide ? 0 : this.lineardamp * this.physics.getMass());
            // INIT LINEAR FORCE
            Vector3f linearForce = new Vector3f();
            // CONVERT ANGULAR DAMPENING TO OBJECT COORDINATES
            //this.object.getRotation().inverse().multLocal(linearDampingForce);
            //if (FastMath.abs(strafeVelocity) < FastMath.abs(FlightControl.this.topspeedreverse * FlightControl.this._strafe)) {
                // CANCEL OUT SIDE DAMPENING
                //linearDampingForce.setX(0);
                // SET STRAFE FORCE
                linearForce.setX(computeForce(topspeedreverse, topspeedreverse, acceleration, _strafe, physics.getMass(), lineardamp, tpf)); //FlightControl.this.acceleration * 7.05f * FlightControl.this._strafe * FlightControl.this.physics.getMass());
            //}
            //if (FastMath.abs(liftVelocity) < FastMath.abs(FlightControl.this.topspeedreverse * FlightControl.this._lift)) {
                // CANCEL OUT SIDE DAMPENING
                //linearDampingForce.setY(0);
                // SET STRAFE FORCE
                linearForce.setY(computeForce(topspeedreverse, topspeedreverse, acceleration, _lift, physics.getMass(), lineardamp, tpf));//FlightControl.this.acceleration * 7.05f * FlightControl.this._lift * FlightControl.this.physics.getMass());
            //}
            //if (FastMath.abs(forwardVelocity) < FastMath.abs(FlightControl.this.topspeed * FlightControl.this._throttle)) {
                // CANCEL OUT SIDE DAMPENING
                //linearDampingForce.setZ(0);
                // SET STRAFE FORCE                               
                if ( FlightControl.this.cruise && FlightControl.this.cruiseEnergy >= 1 ) {
                    linearForce.setZ(computeForce(VAR_CRUISESPEED, VAR_CRUISESPEED, VAR_CRUISEACCEL, 1, physics.getMass(), lineardamp, tpf)); //FlightControl.VAR_CRUISEACCEL * 7.05f * FlightControl.this.physics.getMass());
                    //_velocity = computeVelocity(tpf, 1, _velocity, VAR_CRUISESPEED, 0f, VAR_CRUISEDAMP, VAR_CRUISEACCEL, false);            
                } else {
                    linearForce.setZ(computeForce(topspeed, topspeedreverse, acceleration, _throttle, physics.getMass(), lineardamp, tpf));
                    //linearForce.setZ(FlightControl.this.acceleration * FlightControl.this._throttle * FlightControl.this.physics.getMass());
                }
                
                /*
                if (Game.get().getPlayer().getClient() == FlightControl.this.object) {
                    float topVelocity = ((linearForce.getZ() / lDamp) - 1 * linearForce.getZ()) / FlightControl.this.physics.getMass();
                    System.out.println(topVelocity + " | " + forwardVelocity);
                }
                */
            //}
            // CONVERT LINEAR DAMPENING BACK TO WORLD COORDINATES
            //this.object.getRotation().multLocal(linearDampingForce);
            // CONVERT LINEAR FORCE TO WORLD COORDINATES
            FlightControl.this.object.getRotation().multLocal(linearForce);
            // APPLY ANGULAR FORCE
            FlightControl.this.physics.applyCentralForce(linearForce);
            //this.physics.applyCentralForce(linearDampingForce);

              ///////////////////
             // ANGULAR STUFF //
            ///////////////////

            // GET CURRENT ANGULAR VELOCITY FOR FORCE CALCULATIONS
            Vector3f currentAngularVelocity = FlightControl.this.physics.getAngularVelocity();
            // GET CURRENT VELOCITY COMPONENTS
            float pitchVelocity = FlightControl.this.object.getRotation().inverse().mult(currentAngularVelocity).getX();
            float yawVelocity = FlightControl.this.object.getRotation().inverse().mult(currentAngularVelocity).getY();
            float rollVelocity = FlightControl.this.object.getRotation().inverse().mult(currentAngularVelocity).getZ();
            
            //if (Game.get().getPlayer().getClient() == FlightControl.this.object) {
                //System.out.println("p: " + _elevator + " | y: " + _rudder + " | r: " + _aileron);
                //System.out.println("pV: " + pitchVelocity + " | yV: " + yawVelocity + " | rV: " + rollVelocity);
            //}
            // CALCULATE COMMON ANGULAR DAMPING FORCE (WORLD COORDINATES)
            //Vector3f angularDampingForce = currentAngularVelocity.negate().normalizeLocal().multLocal(this.angulardamp * 10 * this.physics.getMass());
            // INIT ANGULAR FORCE
            Vector3f angularForce = new Vector3f();
            // CONVERT ANGULAR DAMPENING TO OBJECT COORDINATES
            //this.object.getRotation().inverse().multLocal(angularDampingForce);
            //if (FastMath.abs(pitchVelocity) < FastMath.abs(FlightControl.this.turnrate * FlightControl.this._elevator)) {
                // CANCEL OUT PITCH DAMPENING
                //angularDampingForce.setX(0);
                // SET ANGULAR FORCE'S PITCH COMPONENT
                angularForce.setX(computeForce(turnrate, turnrate, momentum, _elevator, physics.getMass(), angulardamp, tpf)); //FlightControl.this.momentum * 100 * FlightControl.this._elevator * FlightControl.this.physics.getMass());
            //}
            //if (FastMath.abs(yawVelocity) < FastMath.abs(FlightControl.this.turnrate * FlightControl.this._rudder)) {
                // CANCEL OUT PITCH DAMPENING
                //angularDampingForce.setY(0);
                // SET ANGULAR FORCE'S YAW COMPONENT
                angularForce.setY(computeForce(turnrate, turnrate, momentum, _rudder, physics.getMass(), angulardamp, tpf)); //FlightControl.this.momentum * 100 * FlightControl.this._rudder * FlightControl.this.physics.getMass());
            //}
            //if (FastMath.abs(rollVelocity) < FastMath.abs(FlightControl.this.turnrate * FlightControl.this._aileron)) {
                // CANCEL OUT PITCH DAMPENING
                //angularDampingForce.setZ(0);
                // SET ANGULAR FORCE'S ROLL COMPONENT
                angularForce.setZ(computeForce(turnrate, turnrate, momentum, _aileron, physics.getMass(), angulardamp, tpf)); //FlightControl.this.momentum * 50 * FlightControl.this._aileron * FlightControl.this.physics.getMass());
            //}
            // CONVERT ANGULAR DAMPENING BACK TO WORLD COORDINATES
            //this.object.getRotation().multLocal(angularDampingForce);
            // CONVERT ANGULAR FORCE TO WORLD COORDINATES
            FlightControl.this.object.getRotation().multLocal(angularForce);
            // APPLY ANGULAR FORCE
            FlightControl.this.physics.applyTorque(angularForce);
            //this.physics.applyTorque(angularDampingForce);
        }

        public void physicsTick(PhysicsSpace space, float tpf) {
            // NOTHING TO DO HERE
        }
        
        private float computeForce(float tSpd, float tSpdR, float tAcc, float factor, float mass, float damp, float tpf) {
            
            if (factor == 0) {
                return 0;
            }
            
            float v = 0;
            if (factor > 0) {
                v = factor * tSpd / (damp - 0.05f); // + tSpd * damp * tpf;
            } else {
                v = factor * tSpdR / (damp - 0.05f); // - tSpdR * damp * tpf;
            }

            if (v == 0) {
                return 0;
            }
                        
            float t = 1; //tAcc / v;
            float a = v / t;
            float f = mass * a;        
            
            /*
            if (Game.get().getPlayer().getClient() == FlightControl.this.object) {
                System.out.println("v: " + v + " | t:" + t + " | a:" + a + " | f:" + f);            
            }
            */
            
            return f;
        }
        
    };
    
}
