/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.weapon;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.DamageControl;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.effect.EffectControl;
import de.jlab.spacefight.gamedata.CacheableEntity;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.mission.structures.DamageInformation;
import de.jlab.spacefight.scripting.WeaponScriptWrapper;
import de.jlab.spacefight.systems.PhysicsControl;
import de.jlab.spacefight.systems.flight.Engine;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 * Super-class for all weapons in the game. Maintains basic functions like range, speed
 * and keeps track of origin, travelled distance.
 * 
 * This is for convenience and polymorphy purposes but also implements a default
 * behaviour for a straight flying weapon.
 * 
 * @author rampage
 */
public class AbstractWeaponControl extends GhostControl implements XMLLoadable, CacheableEntity {
    
    public static final int TYPE_ENERGY = 0;
    public static final int TYPE_WARHEAD = 1;
    public static final int[] TYPE_INTS = new int[] { TYPE_ENERGY, TYPE_WARHEAD };
    public static final String[] TYPE_STRINGS = new String[] { "energy", "warhead" };
    
    private ObjectInfoControl origin;
    
    private int type = TYPE_ENERGY;
       
    private float speed;
    private float range;
    private float damage;
    private SpaceAppState space;
    private String name;
    private float size;
    private float turnRate = 0; //2.5f
    
    private Vector3f position;
    
    private float distance = 0f;
    private float closestTargetDistance = Float.MAX_VALUE;
    
    private float blastimpulse = 0f;
    private float explosionSize;
    private EffectControl explosionEffect;
    
    private float muzzleSize;
    private EffectControl muzzleEffect;

    private TargetInformation target;
    
    private ArrayList<Engine> engines = new ArrayList<Engine>();
    
    private WeaponScriptWrapper script;
    
    public AbstractWeaponControl(SpaceAppState space) {
        this.space = space;
    }
    
    public AbstractWeaponControl(SpaceAppState space, CollisionShape shape) {
        super(shape);
        this.space = space;
    }
    
    public void setOrigin(ObjectInfoControl origin) {
        this.origin = origin;        
    }
    
    public ObjectInfoControl getOrigin() {
        return this.origin;
    }
    
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        AbstractWeaponControl control = new AbstractWeaponControl(space, getCollisionShape());
        // GENERAL COLLISION STUFF
        control.setCcdMotionThreshold(getCcdMotionThreshold());
        control.setCcdSweptSphereRadius(getCcdSweptSphereRadius());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setCollisionGroup(getCollisionGroup());
        control.setPhysicsLocation(getPhysicsLocation());
        control.setPhysicsRotation(getPhysicsRotationMatrix());
        control.setApplyPhysicsLocal(isApplyPhysicsLocal());
        
        // SET ORIGIN
        control.setOrigin(this.origin);
        
        // COPY BASIC VALUES
        control.type = type;
        control.speed = speed;
        control.range = range;
        control.damage = damage;
        control.name = name;
        
        // COPY EFFECTS
        control.blastimpulse = this.blastimpulse;
        control.muzzleEffect = muzzleEffect;
        control.muzzleSize = muzzleSize;
        //control.hitSound = hitSound;
        control.explosionEffect = explosionEffect;
        control.explosionSize = explosionSize;
        
        // COPY WARHEAD VALUES
        control.turnRate = turnRate;
        control.target = this.target;
        for (Engine engine : this.engines) {
            control.engines.add(engine.clone());
        }
        
        if (this.script != null) {
            control.script = this.script.clone(control);
        }
              
        return control;
    }
       
    @Override   
    public Object jmeClone() {
        AbstractWeaponControl control = new AbstractWeaponControl(space, getCollisionShape());
        // GENERAL COLLISION STUFF
        control.setCcdMotionThreshold(getCcdMotionThreshold());
        control.setCcdSweptSphereRadius(getCcdSweptSphereRadius());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setCollisionGroup(getCollisionGroup());
        control.setPhysicsLocation(getPhysicsLocation());
        control.setPhysicsRotation(getPhysicsRotationMatrix());
        control.setApplyPhysicsLocal(isApplyPhysicsLocal());
        
        // SET ORIGIN
        control.setOrigin(this.origin);
        
        // COPY BASIC VALUES
        control.type = type;
        control.speed = speed;
        control.range = range;
        control.damage = damage;
        control.name = name;
        
        // COPY EFFECTS
        control.blastimpulse = this.blastimpulse;
        control.muzzleEffect = muzzleEffect;
        control.muzzleSize = muzzleSize;
        //control.hitSound = hitSound;
        control.explosionEffect = explosionEffect;
        control.explosionSize = explosionSize;
        
        // COPY WARHEAD VALUES
        control.turnRate = turnRate;
        control.target = this.target;
        for (Engine engine : this.engines) {
            control.engines.add(engine.clone());
        }
        
        if (this.script != null) {
            control.script = this.script.clone(control);
        }
              
        // SET SPATIAL AND RETURN
        control.setSpatial(this.spatial);
        return control;
    }
        
    private void logStuff(String caller, AbstractWeaponControl control) {
        System.out.println(caller + ": " + control + " [" + control.getSpatial() + " / " + control.getObjectId() + "]");
        StringBuilder stackBuilder = new StringBuilder();
        StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackElement : stackElements) {
            stackBuilder.append(stackElement.getClassName()).append(".").append(stackElement.getMethodName()).append(" <- ");
        }
        System.out.println(stackBuilder.toString());
        System.out.println("----------------------");
    }  
        
    //public abstract void updateWeapon(float tpf);
    //public abstract void onDestroy();
           
    @Override
    public void update(float tpf) {
        if ( this.space.isEnabled() ) {
            if ( this.target != null && this.target.getObject() != null && this.distance > 20f ) {
                // CLEAR TARGET IF IT'S DEAD
                if (!this.target.getObject().isAlive()) {
                    this.target = null;
                    return;
                }
                
                // REFRESH TARGET INFORMATION
                if (this.turnRate > 0) {
                    // GET CURRENT FACING
                    Quaternion facing = spatial.getWorldRotation();

                    // COMPUTE VECTOR TO TARGET BY SUBSTRACTING OUR POS FROM OTHER POS
                    Vector3f targetVector = this.target.getAimAtWorld(spatial.getControl(ObjectInfoControl.class), this).subtract(spatial.getWorldTranslation());

                    // COMPUTE TARGET ROTATION
                    Quaternion targetFacing = new Quaternion();
                    targetFacing.lookAt(targetVector, Vector3f.UNIT_Y);

                    // TURN TO TARGET USING SLERP
                    spatial.setLocalRotation(new Quaternion().slerp(facing, targetFacing, this.turnRate * tpf));
                    
                    if (this.closestTargetDistance < 200 && this.closestTargetDistance < targetVector.length()) {
                        this.target = null;
                    }
                    this.closestTargetDistance = targetVector.length();
                }
            }

            if ( this.position == null ) {
                //PhysicsControl physics = this.origin.getControl(PhysicsControl.class);
                //if ( physics != null ) {
                    this.muzzleEffect.setVelocity(this.origin.getLinearVelocity());
                //}
                this.muzzleEffect.start(spatial.getWorldTranslation(), this.muzzleSize, 1);
                this.position = new Vector3f(spatial.getWorldTranslation());
            }

            // CALCULATE TRAVELLED DISTANCE
            this.distance += this.speed * tpf;

            // CREATE LINEAR VELOCITY VECTOR
            Vector3f lv = new Vector3f(0, 0, this.speed * tpf);
            // ALIGN VECTOR WITH SHOOTING ORIENTATION (LOCAL FORWARD)
            getPhysicsRotation().multLocal(lv);
            // SET NEW LOCATION
            spatial.move(lv);
            
            if (!this.engines.isEmpty()) {
                for (Engine engine : this.engines) {
                    engine.getSound().setVelocity(lv);
                    engine.setThrottle(1);
                    if ( getSpatial().getParent() != null ) {                    
                        Game.get().getAudioManager().playSoundLoop(engine.getSound());
                    } else {
                        Game.get().getAudioManager().stopSoundLoop(engine.getSound());
                    }
                }
            }
            
            if (this.script != null) {
                this.script.update(tpf);
            }

            if ( this.distance > this.range ) {
                destroyWeapon();
            }
                        
            super.update(tpf);
        }
    }
        
    public void collide(ObjectInfoControl target, PhysicsCollisionEvent event) {
        DamageControl tdc = target.getObjectControl(DamageControl.class);
        Vector3f direction = spatial.getWorldTranslation().subtract(target.getPosition());
        
        if ( tdc != null ) {
            tdc.damageMe(new DamageInformation("weapon", this.name, this.damage, this.origin, target, direction, this.spatial.getWorldTranslation()));
        }
        destroyWeapon();

        PhysicsControl targetPhysics = target.getObjectControl(PhysicsControl.class);
        if (targetPhysics != null) {
            Vector3f force = this.getPhysicsRotation().multLocal(new Vector3f(Vector3f.UNIT_Z)).multLocal(this.blastimpulse);
            targetPhysics.applyImpulse(force, direction);
        }
        
        SensorControl sensors = target.getObjectControl(SensorControl.class);
        if (sensors != null) {
            sensors.reportLaser(getOrigin());
        }
    }
    
    public void destroyWeapon() {
        //if (this.distance < this.size * 4) {
            //return;
        //}
        
        clearTargetReport();
        explosionEffect.start(spatial.getWorldTranslation(), this.explosionSize, 1f);
        
        if (this.script != null) {
            this.script.onDestroy();
        }
        
        if ( spatial != null ) {
            this.space.destroyObject(spatial);
        }       
    }
    
    /* HELPERS */
    private void reportToTarget() {
        if ( this.target != null && this.target.getObject() != null ) {
            SensorControl sensors = this.target.getObject().getObjectControl(SensorControl.class);
            if ( sensors != null ) {
                sensors.reportWeapon(this);
            }
        }
    }
    
    private void clearTargetReport() {
        if ( this.target != null && this.target.getObject() != null ) {
            SensorControl sensors = this.target.getObject().getObjectControl(SensorControl.class);
            if ( sensors != null ) {
                sensors.removeWeapon(this);
            }
        }
    }
    
    public void setColor(ColorRGBA color) {
        setColorOnSpatial(getSpatial(), color);
        for (Engine engine : this.engines) {
            engine.setColor(color);
        }
    }
    
    private void setColorOnSpatial(Spatial spatial, ColorRGBA color) {
        if (spatial instanceof Node) {
            Node node = (Node)spatial;
            List<Spatial> childList = node.getChildren();
            for (Spatial child : childList) {
                setColorOnSpatial(child, color);
            }
        } else if (spatial instanceof Geometry) {
            Geometry gem = (Geometry)spatial;
            try {
                gem.getMaterial().setColor("Color", color);
            } catch (IllegalArgumentException e) {}
            try {
                gem.getMaterial().setColor("GlowColor", color);
            } catch (IllegalArgumentException e) {}
            if (gem.getMaterial().getAdditionalRenderState().isWireframe()) {
                gem.getMesh().setLineWidth(2);
            }
        }
    }
    
    /* GETTERS */
    public void setTarget(TargetInformation target) {
        this.target = target;
        reportToTarget();
    }
    
    public TargetInformation getTarget() {
        return this.target;
    }
    
    public int getType() {
        return this.type;
    }
        
    public SpaceAppState getSpace() {
        return this.space;
    }
    
    public float getRange() {
        return this.range;
    }
    
    public Spatial getSpatial() {
        return this.spatial;
    }
    
    public float getSpeed() {
        return this.speed;
    }
    
    public float getDamage() {
        return this.damage;
    }
    
    public String getName() {
        return this.name;
    }
    
    public float getSize() {
        return this.size;
    }
    
    public float getBlastImpulse() {
        return this.blastimpulse;
    }
    
    public float getDistance() {
        return this.distance;
    }
    
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.type   = XMLLoader.getIntConstValue(element, "type", TYPE_INTS, TYPE_STRINGS, TYPE_ENERGY);
        
        this.damage = XMLLoader.getFloatValue(element, "damage", 50f);
        this.range  = XMLLoader.getFloatValue(element, "range", 3000f);
        this.speed  = XMLLoader.getFloatValue(element, "speed", 500f);
        this.name   = XMLLoader.getStringValue(element, "name", null);
        this.size   = XMLLoader.getFloatValue(element, "size", 1f);
        this.turnRate = XMLLoader.getFloatValue(element, "turnrate", this.turnRate);
        setCollisionShape(new SphereCollisionShape(XMLLoader.getFloatValue(element, "collisionradius", 2f)));
               
        Element blastElement = element.getChild("blast");
        if (blastElement != null) {
            this.blastimpulse = XMLLoader.getFloatValue(blastElement, "impulse", this.blastimpulse);
            String blasteffect = XMLLoader.getStringValue(blastElement, "effect", null);
            this.explosionSize = XMLLoader.getFloatValue(blastElement, "size", 1f);
            this.explosionEffect = gamedataManager.loadEffect(blasteffect, space.getSpace(), space);
        }
        
        Element muzzleElement = element.getChild("muzzle");
        if (muzzleElement != null) {
            String muzzleeffect = XMLLoader.getStringValue(muzzleElement, "effect", null);
            this.muzzleSize = XMLLoader.getFloatValue(muzzleElement, "size", 1f);
            this.muzzleEffect = gamedataManager.loadEffect(muzzleeffect, space.getSpace(), space);
        }
        
        List<Element> engineElems = XMLLoader.readElementList("engine", element);        
        for (Element engineElem : engineElems) {
            Engine engine = new Engine(engineElem, path, gamedataManager);
            this.engines.add(engine);
        }
        
        String scriptName = XMLLoader.getStringValue(element, "script", null);
        if (scriptName != null) {
            this.script = new WeaponScriptWrapper(this);
            gamedataManager.loadScript(path, scriptName, this.script);
        }
    }

    @Override
    public AbstractWeaponControl getInstance(String id) {
        Spatial newSpatial = this.getSpatial().clone();
        AbstractWeaponControl originalControl = newSpatial.getControl(AbstractWeaponControl.class);
        newSpatial.removeControl(originalControl);
        AbstractWeaponControl newControl = (AbstractWeaponControl)originalControl.cloneForSpatial(newSpatial);
        newSpatial.addControl(newControl);
               
        if ( newControl.getSpatial() != null ) {
            for (Engine engine : newControl.engines) {
                engine.attachTo((Node)newControl.getSpatial());
            }
        }
        /*
        else {
            for (Engine engine : this.engines) {
                engine.detach();
            }
        }
        */
        
        logStuff("Original", originalControl);
        logStuff("Clone", newControl);
        
        return newControl;
        
    }
  
}
