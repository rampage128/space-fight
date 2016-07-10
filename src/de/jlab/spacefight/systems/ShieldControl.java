/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems;

import com.jme3.bullet.util.DebugShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Sphere;
import com.shaderblow.forceshield.ForceShieldControl;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.effect.EffectControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.mission.structures.DamageInformation;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public class ShieldControl extends AbstractControl implements SystemControl, XMLLoadable {

    private SpaceAppState space;
    
    private float shieldStrength = 300f;
    
    // CONDITION OF THE SHIELDS
    private float rearShield    = 1f;
    private float frontShield   = 1f;
    
    // DOWNTIMES IF SHIELDS GO TO 0
    private float frontDown = 0f;
    private float rearDown = 0f;
    
    // ENERGY VALUES TO TUNE THE SHIELDS
    private float energyBalance = 0f;
        
    // TIMING FOR REGENERATION
    private float regenerationTime = 20f;
    
    //private AudioNode hitSound;
    
    private ForceShieldControl forceShieldControl;
    private Spatial shield;
    private ColorRGBA shieldColor = new ColorRGBA(1, 1, 1, 0.75f);
    protected EffectControl hitEffect;
    private Material shieldMaterial;
    
    public ShieldControl(SpaceAppState space) {
        this.space = space;
    }
    
    public DamageInformation absorbDamage(DamageInformation damage) {
        Vector3f relativeDirection = damage.getRelativeDirection();
        
        float damageFactor = damage.getDamage() / this.shieldStrength;
        float remainingDamage = 0f;

        if ( relativeDirection.z >= 0 ) {
            // WE ALLOW CLIENT SIDE DAMAGING TO REFLECT DAMAGE VALUES EARLIER!
            // REAL SHIELD STATUS WILL BE SYNCHRONIZED OVER THIS IF WRONG VALUE IS CHEATED!
            this.frontShield -= damageFactor;
            if ( this.frontShield < 0 ) {
                remainingDamage = Math.abs(this.frontShield * this.shieldStrength);
                if ( this.frontShield <= 0 ) {
                    this.frontShield = 0;
                }
            }
            this.frontDown = this.regenerationTime;
        } else {
            // WE ALLOW CLIENT SIDE DAMAGING TO REFLECT DAMAGE VALUES EARLIER!
            // REAL SHIELD STATUS WILL BE SYNCHRONIZED OVER THIS IF WRONG VALUE IS CHEATED!
            this.rearShield -= damageFactor;
            if ( this.rearShield < 0 ) {
                remainingDamage = Math.abs(this.rearShield * this.shieldStrength);
                if ( this.rearShield <= 0 ) {
                    this.rearShield = 0;
                }
            }
            this.rearDown = this.regenerationTime;
        }

        if ( damage.getDamage() != remainingDamage ) {
            this.forceShieldControl.setEffectSize(5f); // TODO get effect size dynamically!
            this.forceShieldControl.registerHit(damage.getWorldPosition());
            if ( hitEffect != null ) {
                hitEffect.setParent((Node)getSpatial());
                hitEffect.start(damage.getRelativeDirection(), 2f, 1f);
            }
        }
        
        return new DamageInformation(damage.getDamageType(), damage.getDamageName(), remainingDamage, damage.getOrigin(), damage.getTarget(), damage.getDirection(), damage.getWorldPosition());
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        // LIMIT ENERGY BALANCE VALUE
        energyBalance = Math.max(-1, Math.min(1, energyBalance));
        
        // COMPUTE REGENERATION VALUE
        float frontValue = (1 + energyBalance) / regenerationTime * tpf;
        float rearValue = (1 - energyBalance) / regenerationTime * tpf;
        
        // APPLY REGENERATION TO SHIELDS
        if ( frontDown > 0 ) { // frontShield == 0 && 
            frontDown -= tpf;
        } else {
            frontShield = Math.min(frontShield + frontValue, 1 + energyBalance);
        }
        if ( rearDown > 0 ) { // rearShield == 0 && 
            rearDown -= tpf;
        } else {
            rearShield = Math.min(rearShield + rearValue, 1 - energyBalance);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        ShieldControl control = new ShieldControl(this.space);
        control.setShieldStrength(shieldStrength);
        control.setRegenerationTime(regenerationTime);
        spatial.addControl(control);
        return control;
    }

    public void resetSystem() {
        this.energyBalance  = 0f;
        this.frontShield    = 1f;
        this.rearShield     = 1f;
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            PhysicsControl physics = spatial.getControl(PhysicsControl.class);
            if (physics != null) {
                this.shield = DebugShapeFactory.getDebugShape(physics.getCollisionShape());
                this.shield.setName("shield");
                /* GET COLLISION SHAPE AS MESH/GEOMETRY */
            } else {
                /*throw new IllegalArgumentException("Cannot create shield without collisionhull!"); */
                Sphere sphere = new Sphere(30, 30, 4.5f);
                this.shield = new Geometry("shield", sphere);
            }
            this.shield.setQueueBucket(RenderQueue.Bucket.Transparent); // Remenber to set the queue bucket to transparent for the spatial
            this.shield.addControl(this.forceShieldControl); // Add the control to the spatial
            
            this.forceShieldControl.setVisibility(0f); // Set shield visibility.
            ((Node)spatial).attachChild(shield);
        } else {
            if (this.spatial != null) {
                this.shield.removeFromParent();
            }
            this.shield.removeControl(this.forceShieldControl);
            this.shield = null;
        }
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.shieldStrength     = XMLLoader.getFloatValue(element, "strength", 300f);
        this.regenerationTime   = XMLLoader.getFloatValue(element, "regeneration", 10f);
        
        String hitEffectName = XMLLoader.getStringValue(element, "hiteffect", null);
        if ( hitEffectName != null ) {
            this.hitEffect = gamedataManager.loadEffect(hitEffectName, space.getSpace(), space);
        }
               
        // Create ForceShieldControl
        this.shieldMaterial = new Material(Game.get().getAssetManager(), "ShaderBlow/MatDefs/ForceShield/ForceShield.j3md"); /* Common/MatDefs/Misc/Unshaded.j3md */
        this.forceShieldControl = new ForceShieldControl(this.shieldMaterial);
 
        // Set a texture to the shield
        //this.forceShieldControl.setTexture(Game.get().getAssetManager().loadTexture("effects/shieldblast/energy.png"));
    }
    
    public void setShieldColor(ColorRGBA color) {
        if (color != null) {
            this.shieldColor.set(color.getRed(), color.getGreen(), color.getBlue(), 0.5f);
            this.forceShieldControl.setColor(this.shieldColor); // Set effect color
            this.forceShieldControl.setEffectSize(0.25f);
        }
    }
    
    public void setShieldStrength(float strength) {
        this.shieldStrength = strength;
    }
    
    public void setRegenerationTime(float regenerationTime) {
        this.regenerationTime = regenerationTime;
    }
    
    public void setFrontShield(float frontShield) {
        this.frontShield = frontShield;
    }
    
    public float getFrontShield() {
        return this.frontShield;
    }
    
    public void setRearShield(float rearShield) {
        this.rearShield = rearShield;
    }
    
    public float getRearShield() {
        return this.rearShield;
    }
    
    public float getShield(Vector3f directionWorld) {
        Vector3f relativeDirection = getSpatial().getWorldRotation().inverse().mult(directionWorld);
        if (relativeDirection.z >= 0) {
            return this.frontShield;
        } else {
            return this.rearShield;
        }
    }
    
    public float getShieldStrength() {
        return this.shieldStrength;
    }
    
}
