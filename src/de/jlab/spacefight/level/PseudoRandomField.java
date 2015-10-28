/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.level;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.DamageControl;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.mission.structures.Spawn;
import de.jlab.spacefight.random.RandomCoordinateGenerator;
import de.jlab.spacefight.random.RandomRotationGenerator;
import de.jlab.spacefight.random.RandomScaleGenerator;
import de.jlab.spacefight.systems.PhysicsControl;
import java.util.ArrayList;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public class PseudoRandomField extends AbstractControl implements XMLLoadable {
    
    //private SpaceAppState space;
    
    private long seed = 0;
    private Vector3f size;
    private float density = 0.7f;
       
    private String objectName;
    private ObjectInfoControl objectTemplate;
    
    private float minscale = 0.5f;
    private float maxscale = 10f;
    
    private ArrayList<ObjectInfoControl> asteroids = new ArrayList<ObjectInfoControl>();
    
    public PseudoRandomField() {
        //this.space = space;
        this.seed = System.currentTimeMillis();
        this.size = new Vector3f(500, 500, 500);
        this.density = 0.7f; // this.size.x * this.size.y * this.size.z / 1000000f
    }
    
    public void setObject(String objectName) {
        this.objectName = objectName;
    }
    
    public void setDensity(float density) {
        this.density = density;
    }
    
    public void setSize(Vector3f size) {
        this.size = size;
    }
    
    public void setSeed(long seed) {
        this.seed = seed;
    }
    
    public long getSeed() {
        return this.seed;
    }
    
    public void createField(SpaceAppState space) {
        //this.space = space;
        this.objectTemplate = space.getGame().getGamedataManager().loadObject(false, objectName, "asteroid", space);
        
        for ( ObjectInfoControl asteroid : this.asteroids ) {
            asteroid.getSpatial().removeFromParent();
            if ( asteroid.getSpatial().getControl(PhysicsControl.class) != null ) {
                space.getPhysics().getPhysicsSpace().remove(asteroid.getSpatial().getControl(PhysicsControl.class));
            }
        }
        this.asteroids.clear();
        
        RandomCoordinateGenerator coordinateGenerator = new RandomCoordinateGenerator(this.seed, this.size);
        RandomScaleGenerator scaleGenerator = new RandomScaleGenerator(this.seed, this.minscale, this.maxscale);
        RandomRotationGenerator rotationGenerator = new RandomRotationGenerator(seed);
        
        float volume = this.size.x * this.size.y * this.size.z;
        int i = 0;
        
        volume -= volume * (1 - this.density);
        
        while ( volume > 0 ) {
            i++;
            
            ObjectInfoControl asteroid = this.objectTemplate.cloneObject("asteroid_" + i, false);
                        
            Vector3f position = coordinateGenerator.getVector().addLocal(getSpatial().getWorldTranslation());
            Quaternion rotation = rotationGenerator.getRotation();
            float scale = scaleGenerator.getScale();
            
            asteroid.getSpatial().setLocalTranslation(position);
            asteroid.getSpatial().setLocalRotation(rotation);
            asteroid.getSpatial().setLocalScale(scale);
            
            PhysicsControl physics = asteroid.getObjectControl(PhysicsControl.class);
            if ( physics != null ) {
                //GamedataManager.computeCollision(asteroid, null, physics);
                physics.setPhysicsLocation(position);
                physics.setPhysicsRotation(rotation);
                physics.setMass(physics.getMass() * scale);
                physics.getCollisionShape().setScale(new Vector3f(scale, scale, scale));
            }
            DamageControl damage = asteroid.getObjectControl(DamageControl.class);
            if ( damage != null ) {
                damage.setHullHP(damage.getHullHP() * scale);
                // TRIGGER SIZE RECALCULATION
                damage.resetSystem();
            }
            
            /*
            Node hull = (Node)asteroid.getSpatial().getChild("Hull");
            if ( hull != null ) {
                for ( Spatial child : hull.getChildren() ) {
                    if ( child instanceof Geometry ) {
                        FixedLodControl control = child.getControl(FixedLodControl.class);
                        if ( control != null ) {
                            child.removeControl(FixedLodControl.class);
                            child.addControl(control);
                        }
                    }
                }
            }
            */
            
            Spawn spawn = new Spawn(position, rotation);
            asteroid.setSpawn(spawn);
            //ObjectInfoControl objectInfo = new ObjectInfoControl(this.objectName, asteroid.getName(), spawn, null, false);
            //asteroid.addControl(objectInfo);
            
            volume -= asteroid.getVolume();
            
            for ( ObjectInfoControl otherAsteroid : this.asteroids ) {
                Vector3f between = asteroid.getSpatial().getWorldTranslation().subtract(otherAsteroid.getSpatial().getWorldTranslation());
                float minspacing = asteroid.getSize() + otherAsteroid.getSize() + 10;
                if ( between.length() < minspacing ) {
                    Vector3f movement = asteroid.getSpatial().getWorldTranslation().add(between.normalizeLocal().multLocal(minspacing - between.length()));
                    asteroid.getSpatial().setLocalTranslation(movement);
                    if ( physics != null ) {
                        physics.setPhysicsLocation(movement);
                    }
                }
            }
            
            this.asteroids.add(asteroid);
            space.addObject(asteroid);
            //space.getPhysicsSpace().add(asteroid.getControl(PhysicsControl.class));
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        /*
        if (getSpatial() == null || getSpatial().getParent() == null || this.space == null) {
            return;
        }
        
        Camera camera = space.getGame().getCamera();
        if ( camera.getLocation().subtract(getSpatial().getWorldTranslation()).length() < 8000 ) {
            for ( Spatial asteroid : this.asteroids ) {
                if ( camera.getLocation().subtract(asteroid.getWorldTranslation()).length() < 3000 ) {
                    if (!((Node)getSpatial()).hasChild(asteroid)) {
                        space.addObject(asteroid);
                        if ( asteroid.getControl(PhysicsControl.class) != null ) {
                            space.getPhysicsSpace().add(asteroid.getControl(PhysicsControl.class));
                        }
                    }
                } else {
                    if (((Node)getSpatial()).hasChild(asteroid)) {
                        asteroid.removeFromParent();
                        if ( asteroid.getControl(PhysicsControl.class) != null ) {
                            space.getPhysicsSpace().remove(asteroid.getControl(PhysicsControl.class));
                        }
                    }
                }
            }
        }
        */
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        PseudoRandomField control = new PseudoRandomField();
        control.setSeed(this.seed);
        control.setSize(this.size);
        control.setDensity(this.density);
        spatial.addControl(control);
        return control;
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.size = XMLLoader.getVectorValue(element, "size", this.size);
        this.seed = XMLLoader.getLongValue(element, "seed", this.seed);
        this.minscale = XMLLoader.getFloatValue(element, "minscale", this.minscale);
        this.maxscale = XMLLoader.getFloatValue(element, "maxscale", this.maxscale);
        this.density = XMLLoader.getFloatValue(element, "density", this.density);
        setObject(XMLLoader.getStringValue(element, "objecttype", null));
    }
    
}
