/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
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
public class PhysicsControl extends RigidBodyControl implements XMLLoadable {
   
    public static final float VAR_DEFAULTMASS = 10000f;
    public static final String VAR_DEFAULTSHAPE = "custom";
    
    private String shape = VAR_DEFAULTSHAPE;
    private Vector3f forward = new Vector3f(0, 0, 1);
    private Vector3f up = new Vector3f(0, 1, 0);

    private boolean linearVelocityApplied = false;
    private Vector3f applyLinearVelocity = new Vector3f();
    private boolean angularVelocityApplied = false;
    private Vector3f applyAngularVelocity = new Vector3f();
    
    public PhysicsControl() {
        super(VAR_DEFAULTMASS);
        //this.setApplyPhysicsLocal(false);
    }
        
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            computeCollision();
        }
    }
    
    @Override
    public void update(float tpf) {       
        super.update(tpf);

        //System.out.println("p: " + System.currentTimeMillis());
        
        /*
        if (this.linearVelocityApplied) {
            //this.applyCentralForce(this.applyLinearVelocity.mult(this.getMass()));
            this.setLinearVelocity(this.applyLinearVelocity);
        }
        if (this.angularVelocityApplied) {
            this.setAngularVelocity(this.applyAngularVelocity);
        }
        */
        
        this.forward.set(spatial.getWorldRotation().multLocal(this.forward.set(Vector3f.UNIT_Z))).normalizeLocal();
        this.up.set(spatial.getWorldRotation().multLocal(this.up.set(Vector3f.UNIT_Y))).normalizeLocal();
        
        //System.out.println(this.getPhysicsLocation().subtract(this.spatial.getWorldTranslation()).length());
        
        SpaceDebugger.getInstance().setVector(DebugContext.PHYSICS, "v_" + this.hashCode(), spatial.getWorldTranslation(), getLinearVelocity(), ColorRGBA.Blue);
        SpaceDebugger.getInstance().setVector(DebugContext.PHYSICS, "f_" + this.hashCode(), spatial.getWorldTranslation(), this.forward, ColorRGBA.Yellow);
        SpaceDebugger.getInstance().setVector(DebugContext.PHYSICS, "u_" + this.hashCode(), spatial.getWorldTranslation(), this.up, ColorRGBA.Green);
    }
    
    /*
    public void applyLinearVelocity(Vector3f linearVelocity) {
        this.applyLinearVelocity = linearVelocity;
        this.linearVelocityApplied = true;
    }
    
    public void applyAngularVelocity(Vector3f angularVelocity) {
        this.applyAngularVelocity = angularVelocity;
        this.angularVelocityApplied = true;
    }
    */
    
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        PhysicsControl control = new PhysicsControl();
        control.setMass(getMass());
        control.setSpatial(spatial);
        return control;
    }
    
    @Override   
    public Object jmeClone() {
        return this.cloneForSpatial(this.spatial);
    }
    
    private void computeCollision() {
        Node object = (Node)spatial;
        
        if (shape != null && shape.indexOf("sphere") == 0) {
            float radius = Float.parseFloat(shape.substring(shape.indexOf(",") + 1, shape.length()));
            setCollisionShape(new SphereCollisionShape(radius));
            //shipLength = radius * 2;
            //shipDiameter = radius * 2;
        } else if (shape != null && shape.indexOf("box") == 0) {
            String[] values = shape.split(",");
            float x = Float.parseFloat(values[1]);
            float y = Float.parseFloat(values[2]);

            //shipDiameter = x > y ? x : y;

            float z = Float.parseFloat(values[3]);
            //shipLength = z;
            setCollisionShape(new BoxCollisionShape(new Vector3f(x / 2, y / 2, z / 2)));
        } else {
            Node collisionNode = (Node) object.getChild("Collision");

            if ( collisionNode != null ) {
                object.detachChild(collisionNode);
            } else {
                Node hull = (Node) object.getChild("Hull");
                collisionNode = (Node) hull.getChild("LOD100");
                if ( collisionNode == null ) {
                    collisionNode = hull;
                }
            }
            
            ArrayList<CollisionShape> shapeList = computeCollisionShape(collisionNode);
            CollisionShape collisionShape = null;
            if (shapeList.size() > 1) {
                collisionShape = new CompoundCollisionShape();
                for (int i = 0; i < shapeList.size(); i++) {
                    ((CompoundCollisionShape) collisionShape).addChildShape(shapeList.get(i), Vector3f.ZERO);
                }
            } else {
                if (shapeList.size() == 1) {
                    collisionShape = shapeList.get(0);
                }
            }
            setCollisionShape(collisionShape);
        }
    }

    private static ArrayList<CollisionShape> computeCollisionShape(Spatial spatial) {
        ArrayList<CollisionShape> shapeList = new ArrayList<CollisionShape>();
        if (spatial instanceof Node) {
            List<Spatial> childList = ((Node) spatial).getChildren();
            for (int i = 0; i < childList.size(); i++) {
                Spatial child = childList.get(i);
                shapeList.addAll(computeCollisionShape(child));
            }
        } else if ((spatial instanceof Geometry) && !(spatial instanceof ParticleEmitter)) {
            Mesh mesh = ((Geometry) spatial).getMesh();
            if (mesh != null) {
                shapeList.add(new HullCollisionShape(mesh));
            }
        }

        return shapeList;
    }
    
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.shape = XMLLoader.getStringValue(element, "shape", "custom");
        setMass(XMLLoader.getFloatValue(element, "mass", VAR_DEFAULTMASS));
    }
    
    public Vector3f getUpVector() {
        return up;
    }
    
    public Vector3f getFacing() {
        return forward;
    }
    
}
