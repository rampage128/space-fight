/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic;

import de.jlab.spacefight.effect.EffectControl;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import de.jlab.spacefight.systems.sensors.SensorControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.mission.structures.DamageInformation;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.network.NetworkAppState;
import de.jlab.spacefight.systems.ShieldControl;
import de.jlab.spacefight.systems.SystemControl;
import java.util.HashMap;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public class DamageControl extends AbstractControl implements SystemControl, XMLLoadable {

    public static final float HULL_UNLIMITED    = -1;
    public static final float HULL_DEAD         = 0;
    
    private float hull_hp = HULL_UNLIMITED;
    private float hull = 1.0f;
    private SpaceAppState space;
    private SensorControl sensorControl;
    private EffectControl explosionEffect;
    
    //private float objectSize;
    
    private boolean destroyed = false;
    
    private HashMap<String, EffectControl> hitEffects = new HashMap<String, EffectControl>();

    public DamageControl(SpaceAppState space) {
        this.space = space;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (getSpatial() != null && this.sensorControl == null) {
            this.sensorControl = getSpatial().getControl(SensorControl.class);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        DamageControl control = new DamageControl(this.space);
        control.setHullHP(hull_hp);
        control.explosionEffect = this.explosionEffect;
        control.setSpatial(spatial);
        return control;
    }

    public void damageMe(DamageInformation damage) {
        
        if (this.destroyed) {
            return;
        }
        
        ShieldControl shields = getSpatial().getControl(ShieldControl.class);
        if ( shields != null ) {
            damage = shields.absorbDamage(damage);
        }

        if (this.sensorControl != null) {
            this.sensorControl.reportDamage(damage);
        }

        if ( damage.getDamage() <= 0 ) {
            return;
        }
        
        EffectControl hitEffect = this.hitEffects.get(damage.getDamageType());
        if ( hitEffect != null ) {
            //hitEffect.setLocalTranslation(spatial.getWorldTranslation().add(damage.getDirection()));
            hitEffect.setParent(space.getSpace());
            hitEffect.start(damage.getWorldPosition(), 1, 1);
            //this.space.getGame().getAudioManager().playSound(hitEffect);
        }
        
        // WE ALLOW CLIENT SIDE DAMAGING TO REFLECT DAMAGE VALUES EARLIER!
        //if (!NetworkAppState.isClient(this.space.getGame())) {
        this.hull -= damage.getDamage() / this.hull_hp;
        //}

        if (this.hull <= 0f) {
            Kill kill = new Kill(damage);
            if (NetworkAppState.killObject(this.space.getGame(), kill)) {
                kill(kill);
            }
        }
    }

    public void kill(Kill kill) {
        if (this.destroyed) {
            return;
        }
        this.destroyed = true;
        if ( this.explosionEffect != null ) {
            float size = spatial.getControl(ObjectInfoControl.class).getSize();
            this.explosionEffect.start(getSpatial().getWorldTranslation(), size, 1);
        }
        this.space.killObject(kill);
    }
    
    public void setHull(float hull) {
        this.hull = hull;
    }
    
    public float getHull() {
        return this.hull;
    }
    
    public float getHullHP() {
        return this.hull_hp;
    }
    
    public void setHullHP(float hp) {
        this.hull_hp = hp;
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        //if (spatial != null) {
            //spatial.getControl(ObjectInfoControl.class).getSize();
            //calcObjectSize(spatial);
        //}
        super.setSpatial(spatial);
    }
    
    /*
    private void calcObjectSize(Spatial spatial) {
        BoundingVolume bound = spatial.getWorldBound();
        if (bound.getType() == BoundingVolume.Type.Sphere){
            this.objectSize = ((BoundingSphere)bound).getRadius();
        } else if (bound.getType() == BoundingVolume.Type.AABB){
            BoundingBox bb = (BoundingBox)bound;
            this.objectSize = Math.max(bb.getXExtent(), Math.max(bb.getYExtent(), bb.getZExtent()));
        } else
            this.objectSize = 0f;
    }
    */

    public void setExplosionEffect(String name) {
        this.explosionEffect = space.getGame().getGamedataManager().loadEffect(name, space.getSpace(), this.space);
    }
    
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.hull_hp = XMLLoader.getFloatValue(element, "hull_hp", 500);
        String deatheffect = XMLLoader.getStringValue(element, "deatheffect", null);
        if ( deatheffect != null ) {
            setExplosionEffect(deatheffect);
        }
        
        Element hitSoundRoot = element.getChild("effects");
        
        if ( hitSoundRoot != null ) {
            List<Element> hitEffectElements = hitSoundRoot.getChildren();
            for ( Element hitEffectElement : hitEffectElements ) {
                String hitEffectFileName   = XMLLoader.getStringValue(hitEffectElement, "file", null);
                EffectControl hitEffect = gamedataManager.loadEffect(hitEffectFileName, space.getSpace(), space);
                this.hitEffects.put(hitEffectElement.getName(), hitEffect);
            }
        }
        
        
    }

    public void resetSystem() {
        this.destroyed  = false;
        this.hull       = 1f;
        
        /*
        if ( getSpatial() != null ) {
            calcObjectSize(getSpatial());
        }
        */
    }
}
