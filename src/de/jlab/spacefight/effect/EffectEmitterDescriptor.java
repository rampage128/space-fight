/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.effect;

import com.jme3.effect.ParticleEmitter;
import com.jme3.math.Vector3f;

/**
 *
 * @author rampage
 */
public class EffectEmitterDescriptor {
    
    private boolean permanent = false;
    
    private ParticleEmitter emitter;
    
    private float speed = 1f;
    private float scale = 1f;
    
    private float highLife  = 0f;
    private float lowLife   = 0f;
    
    private float startSize = 0f;
    private float endSize   = 0f;
    
    private Vector3f initialVelocity = new Vector3f();
    
    public EffectEmitterDescriptor(ParticleEmitter emitter, boolean permanent) {
        this.permanent = permanent;
        
        this.emitter = emitter;
        
        this.lowLife            = emitter.getLowLife();
        this.highLife           = emitter.getHighLife();
        
        this.startSize          = emitter.getStartSize();
        this.endSize            = emitter.getEndSize();
        
        this.initialVelocity.set(emitter.getParticleInfluencer().getInitialVelocity());
    }
    
    public void start(float scale, float speed) {
        this.scale = scale;
        this.speed = speed;
        
        emitter.setLowLife(this.lowLife * (1 / speed));
        emitter.setHighLife(this.highLife * (1 / speed));
        
        emitter.setStartSize(this.startSize * scale);
        emitter.setEndSize(this.endSize * scale);
        
        emitter.getParticleInfluencer().setInitialVelocity(this.initialVelocity.mult(scale));
        
        if ( this.permanent ) {
            emitter.setEnabled(true);
        } else {
            emitter.emitAllParticles();
        }
    }
    
    public void stop() {
        if ( this.permanent ) {
            emitter.setEnabled(false);
        } else {
            // INFO PRODUCES A BUG WITH MULTIPLE INSTANCES
            //emitter.killAllParticles();
        }
    }
    
    public float getDuration() {
        return this.highLife / this.speed;
    }
    
    public float getSize() {
        return this.endSize * this.scale;
    }
    
}
