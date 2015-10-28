/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.effect.stardust;

import com.jme3.effect.Particle;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.effect.shapes.EmitterShape;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.io.IOException;

/**
 *
 * @author rampage
 */
public class StardustInfluencer implements ParticleInfluencer {

    private Camera camera;
    private float radius;
    private float particleSize;
    
    public StardustInfluencer(float radius, float particleSize, Camera camera) {
        this.radius = radius;
        this.camera = camera;
        this.particleSize = particleSize;
    }
    
    public void updateParticle(Particle particle, Vector3f differenceVector, float tpf) {
        // House keeping!

        /* BUGGY
        if ( differenceVector.length() > 0 ) {
            particle.size = particleSize * 100;
        } else {
            particle.size = particleSize;
        }
        */
       
        // Move all dust particles based on their current location, and the difference
        // in movement of the camera position.
        Vector3f d = particle.position;

        // calculate where the dust should now be along the X axis
        d.x = d.x - differenceVector.x;
        // Make minor modifications if d.X is now out of bounds
        if (d.x < - this.radius) {
            d.x = d.x + this.radius * 2;
        } else if (d.x > this.radius) {
            d.x = d.x - this.radius * 2;
        }

        // calculate where the dust should now be along the Y axis
        d.y = d.y - differenceVector.y;
        // Make minor modifications if d.Y is now out of bounds
        if (d.y < - this.radius) {
            d.y = d.y + this.radius * 2;
        } else if (d.y > this.radius) {
            d.y = d.y - this.radius * 2;
        }

        // calculate where the dust should now be along the Z axis
        d.z = d.z - differenceVector.z;
        // Make minor modifications if d.Z is now out of bounds
        if (d.z < - this.radius) {
            d.z = d.z + this.radius * 2;
        } else if (d.z > this.radius) {
            d.z = d.z - this.radius * 2;
        }

        /*
        float distance = this.camera.getLocation().distance(d);

        // once we have the distance to the viewer, we can calculate the new alpha
        float alpha = 0;
        if (distance > (this.radius) - 0.001f) { // anything thats a tiny bit less than the distance between the camera and the edge of the star field (or further)
            alpha = 0f;                     // ... and so now we cant see this one
        } else {
            alpha = 1f - ((distance + (this.radius) - (this.radius / 16)) / this.radius);
        }
        */
        particle.color.a = 1f;

        particle.velocity.set(differenceVector);
        particle.position.set(d);
    }
    
    public void influenceParticle(Particle particle, EmitterShape emitterShape) {
        
        emitterShape.getRandomPoint(particle.position);
        

            
            /*
        dustMesh.clearBuffer(VertexBuffer.Type.Position);
        dustMesh.clearBuffer(VertexBuffer.Type.Color);
        FloatBuffer vertices = BufferUtils.createFloatBuffer(dustLocations3f.toArray(new Vector3f[dustLocations3f.size()]));
        dustMesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
        dustMesh.setBuffer(VertexBuffer.Type.Color, 4, dustRGBAs);
        dustMesh.updateBound();

        dustGeometry.updateModelBound();
        this.container.setLocalTranslation(currentCamLocation);
        
        if (doAlphaBlending) {
            updateAlpha();         // update the alpha on all bits of dust..
        }
             */
    }

    @Override
    public ParticleInfluencer clone() {
        return new StardustInfluencer(this.radius, this.particleSize, this.camera);
    }

    public void setInitialVelocity(Vector3f initialVelocity) {
        // NOTHING TO DO
    }

    public Vector3f getInitialVelocity() {
        return Vector3f.ZERO;
    }

    public void setVelocityVariation(float variation) {
        // NOTHING TO DO;
    }

    public float getVelocityVariation() {
        return 0;
    }

    public void write(JmeExporter ex) throws IOException {
        
    }

    public void read(JmeImporter im) throws IOException {
        
    }
    
}
