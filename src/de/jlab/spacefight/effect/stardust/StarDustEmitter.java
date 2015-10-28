/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.effect.stardust;

import com.jme3.effect.Particle;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author rampage
 */
public class StarDustEmitter extends ParticleEmitter {
        
    private float density = 1f;
       
    private StardustInfluencer influencer;
    
    public StarDustEmitter(float radius, float density, float particleSize, Camera camera, Material material) {
        super("stardust", Type.Triangle, Math.round(200 * density));
        this.density = density;
        this.influencer = new StardustInfluencer(radius, particleSize, camera);
        
        addControl(new StarDustControl(camera));
        setParticleInfluencer(influencer);
        
        setMaterial(material);
        setParticlesPerSec(0);
        setStartSize(particleSize);
        setEndSize(particleSize);
        setGravity(0, 0, 0);
        setHighLife(Float.MAX_VALUE);
        setLowLife(Float.MAX_VALUE);
        setInWorldSpace(false);
        setShape(new EmitterSphereShape(Vector3f.ZERO, radius));
        setImagesX(2);
        setImagesY(2);
    }
    
    public void start() {
        //setMesh(new ParticleTriMesh());
        //setNumParticles(Math.round(200 * density));
        emitAllParticles();
    }
    
    public class StarDustControl extends AbstractControl {

        private Vector3f currentCamLocation;
        private Vector3f previousCamLocation;  
        
        private Camera camera;
        
        public StarDustControl(Camera camera) {
            this.camera = camera;
            this.currentCamLocation = camera.getLocation().clone();
        }
        
        @Override
        protected void controlUpdate(float tpf) {
            StarDustEmitter.this.setLocalTranslation(this.camera.getLocation());
            
            previousCamLocation = currentCamLocation;
            currentCamLocation = camera.getLocation().clone();

            Vector3f differenceVector;
            differenceVector = currentCamLocation.subtract(previousCamLocation);
              
            // BUGGY IN SIZE?!
            //StarDustEmitter.this.setFacingVelocity(differenceVector.length() > 0);
            
            Particle[] particles = StarDustEmitter.this.getParticles();
            for ( int i = 0; i < particles.length; i++ ) {
                Particle particle = particles[i];
                StarDustEmitter.this.influencer.updateParticle(particle, differenceVector, tpf);
            }
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
            
        }

        public Control cloneForSpatial(Spatial spatial) {
            StarDustControl control = new StarDustControl(camera);
            spatial.addControl(control);
            return control;
        }
    
    }
    
}
