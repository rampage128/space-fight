/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.effect;

import com.jme3.effect.ParticleEmitter;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.audio.AudioEffect;
import de.jlab.spacefight.debug.DebugContext;
import de.jlab.spacefight.debug.SpaceDebugger;
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
public class EffectControl extends AbstractControl implements XMLLoadable {

    public static final String TYPE_ONCE = "once";
    public static final String TYPE_PERMANENT = "permanent";
    
    private String type = TYPE_ONCE;
    
    private SpaceAppState space;
    private Node parent;
    
    private ArrayList<EffectEmitterDescriptor> emitterList = new ArrayList<EffectEmitterDescriptor>();
    private float duration = 0f;
    private float speed = 1f;
    
    private float playTime = 0f;
    private boolean started = false;
    
    private AudioEffect sound;
    
    public EffectControl(Node parent, SpaceAppState space) {
        this.parent = parent;
        this.space = space;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if (started) {
            if (TYPE_ONCE.equalsIgnoreCase(type) && playTime >= duration * (1 / speed)) {
                stop();
                return;
            }
            playTime += tpf;
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        EffectControl control = new EffectControl(this.parent, this.space);
        control.setSpatial(spatial);
        return control;
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        if ( spatial != null ) {
            computeEffect(spatial);
        } else {
            this.duration = 0;
            this.emitterList.clear();
        }
        super.setSpatial(spatial);
    }
    
    private void computeEffect(Spatial spatial) {
        if (spatial instanceof Node) {
            List<Spatial> childList = ((Node) spatial).getChildren();
            for (int i = 0; i < childList.size(); i++) {
                computeEffect(childList.get(i));
            }
        } else if (spatial instanceof ParticleEmitter) {
            EffectEmitterDescriptor descriptor = new EffectEmitterDescriptor((ParticleEmitter)spatial, TYPE_PERMANENT.equalsIgnoreCase(this.type));
            this.emitterList.add(descriptor);
            this.duration = Math.max(this.duration, descriptor.getDuration());
        }
    }
    
    public void stop() {
        for ( EffectEmitterDescriptor emitter : this.emitterList ) {
            emitter.stop();
        }
        //this.space.destroyObject(this.spatial);
        if ( this.sound != null ) {
            if ( this.sound.isLooping() ) {
                this.space.getGame().getAudioManager().stopSoundLoop(this.sound);
            }
        }
        this.playTime = 0;
        
        SpaceDebugger.getInstance().removeItem(DebugContext.EFFECT, "" + this.hashCode());
        
        this.started = false;
    }
    
    public void setParent(Node parent) {
        this.parent = parent;
    }
    
    public void setVelocity(Vector3f velocity) {
        if (this.sound != null) {
            this.sound.setVelocity(velocity);
        }
    }
    
    public void start(Vector3f position, float scale, float speed) {
        if ( this.started )
            this.stop();

        this.speed = speed;
        
        this.spatial.setLocalTranslation(position);
        for ( EffectEmitterDescriptor emitter : this.emitterList ) {     
            emitter.start(scale, speed);
        }
        
        if ( this.sound != null ) {
            //this.sound.attachTo(this.parent);
            this.sound.setLocalTranslation(this.parent.getWorldTranslation().add(position));
            if ( this.sound.isLooping() ) {
                this.space.getGame().getAudioManager().playSoundLoop(this.sound);
            } else {
                this.space.getGame().getAudioManager().playSound(this.sound);
            }
        }

        SpaceDebugger.getInstance().setBlip(DebugContext.EFFECT, "" + this.hashCode(), position, 1f, ColorRGBA.Blue);
        
        this.started = true;
        this.parent.attachChild(this.spatial);
    }
    
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.type = XMLLoader.getStringValue(element, "type", TYPE_ONCE);
               
        String model = XMLLoader.getStringValue(element, "model", null);
        if (model != null && model.length() > 0) {
            Node object = (Node)gamedataManager.loadModel(model);
            setSpatial(object);
        } else {
            setSpatial(new Node("effect"));
        }
        
        Element soundElement = element.getChild("sound");
        if ( soundElement != null ) {
            this.sound = new AudioEffect(soundElement, path, gamedataManager);
        }
    }
    
}
