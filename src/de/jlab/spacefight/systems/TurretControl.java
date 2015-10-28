/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems;

import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.debug.SpaceDebugger;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.debug.DebugContext;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public class TurretControl extends AbstractControl implements SystemControl, XMLLoadable {
    
    public static final float VAR_DEFAULTANGLE = 180f;
    public static final float VAR_DEFAULTHORIZONTALTURNRATE = 2f;
    public static final float VAR_DEFAULTVERTICALTURNRATE = 3f;

    private Vector3f aiming = new Vector3f();
    
    private Spatial base = null;
    private Spatial sleeve = null;
    
    private float angle = VAR_DEFAULTANGLE;
    private float horizontalturnrate = VAR_DEFAULTHORIZONTALTURNRATE;
    private float verticalturnrate = VAR_DEFAULTVERTICALTURNRATE;
    
    private SpaceAppState space;
    
    private WeaponSystemControl weapons;
    private PhysicsControl physics;
    
    private float baseAngle = 0f;
    private float sleeveAngle = 0f;
       
    public TurretControl(SpaceAppState space) {
        this.space = space;
    }
    
    public void setAngle(float angle) {
        this.angle = angle;
    }
    
    @Override
    protected void controlUpdate(float tpf) {  
        if (this.physics == null) {
            this.physics = getSpatial().getControl(PhysicsControl.class);
        }
        
        if ( this.physics != null ) {
            this.physics.setApplyPhysicsLocal(false);
            this.physics.setPhysicsLocation(getSpatial().getParent().getWorldTranslation());
            this.physics.setPhysicsRotation(getSpatial().getParent().getWorldRotation());
            this.physics.setAngularVelocity(Vector3f.ZERO);
            this.physics.setLinearVelocity(Vector3f.ZERO);
        }
        
        if ( this.weapons == null ) {
            this.weapons = getSpatial().getControl(WeaponSystemControl.class);
            return;
        }
        
        TargetInformation target = this.weapons.getTarget();
        
        if ( target != null ) {
            if ( !canTarget(target) ) {
                this.weapons.setTarget(null);
                return;
            }
            this.aiming.set(target.getAimAtWorld(getSpatial().getControl(ObjectInfoControl.class), weapons.getPrimarySlot().getWeapon()).subtract(getSpatial().getWorldTranslation()));
            this.aiming.addLocal(getSpatial().getWorldTranslation().subtract(this.sleeve.getWorldTranslation()));
            SpaceDebugger.getInstance().setVector(DebugContext.WEAPONS, "aimat" + this.hashCode(), getSpatial().getWorldTranslation(), this.aiming, ColorRGBA.Pink);
        } else {
            this.aiming.set(this.sleeve.getWorldTranslation().add(getSpatial().getWorldRotation().mult(Vector3f.UNIT_Z)).subtract(this.sleeve.getWorldTranslation()));
            SpaceDebugger.getInstance().removeItem(DebugContext.WEAPONS, "aimat" + this.hashCode());
        }
        
        if ( this.base != null ) {
            // MAKE TARGET VECTOR LOCAL
            Vector3f aimat = getSpatial().getWorldRotation().inverse().mult(this.aiming);
            // REMOVE Y FROM TARGET VECTOR
            Vector3f aimBase = aimat.clone();
            aimBase.y = 0;
            // CREATE QUATERNION AND AIM IT TO THE VECTOR
            Quaternion baseRotation = new Quaternion();
            baseRotation.lookAt(aimBase, getSpatial().getWorldRotation().mult(Vector3f.UNIT_Y));
            // ROTATE TURRET BASE ON Y AXIS!
            float baseAngles[] = baseRotation.toAngles(null);
            this.baseAngle = baseAngles[1]; // TODO INTERPOLATE TURRET BASE TURN WITH GIVEN HORIZONTALTURNRATE
            baseRotation.set(new Quaternion().fromAngleAxis(this.baseAngle, new Vector3f(0,1,0)));
            this.base.setLocalRotation(baseRotation);

            if ( this.sleeve != null ) {
                Vector3f aimSleeve = aimat.clone();
                //aimSleeve.y = 0;
                // CREATE QUATERNION AND AIM IT TO THE VECTOR
                Quaternion sleeveRotation = new Quaternion();
                sleeveRotation.lookAt(aimSleeve, getSpatial().getWorldRotation().mult(Vector3f.UNIT_Y));
                // ROTATE TURRET SLEEVE ON X AXIS!
                float sleeveAngles[] = sleeveRotation.toAngles(null);
                this.sleeveAngle = sleeveAngles[0];  // TODO INTERPOLATE TURRET SLEEVE TURN WITH GIVEN VERTICALTURNRATE
                sleeveRotation.set(new Quaternion().fromAngleAxis(this.sleeveAngle, new Vector3f(1,0,0)));
                this.sleeve.setLocalRotation(sleeveRotation);
            }
        }
        
        //if ( this.weapons.mayFirePrimary() ) {
        if (mayFire()) {
            this.weapons.firePrimary();
        }
        //}
    }

    private boolean mayFire() {
        ObjectInfoControl self = getSpatial().getControl(ObjectInfoControl.class);
        
        if ( this.weapons == null || this.weapons.getTarget() == null) {
            return false;
        }
        
        TargetInformation target = this.weapons.getTarget();

        Vector3f facing = this.sleeve.getWorldRotation().mult(Vector3f.UNIT_Z);
        
        Vector3f aimAtWorld = target.getAimAtWorld(self, this.weapons.getPrimarySlot().getWeapon());        
        Vector3f crosshairVector = facing.normalize().mult(aimAtWorld.subtract(this.sleeve.getWorldTranslation()).length());
        crosshairVector.addLocal(this.sleeve.getWorldTranslation());
        crosshairVector.subtractLocal(aimAtWorld);
                       
        return this.weapons.getTarget().getDirection(self).length() < this.weapons.getPrimarySlot().getWeapon().getRange() && crosshairVector.length() - target.getObject().getSize() < target.getObject().getSize() / 2;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        TurretControl control = new TurretControl(this.space);
        control.setAngle(this.angle);
        control.setSpatial(spatial);
        return control;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if ( spatial != null ) {            
            // FIND PARTS
            this.base = ((Node)spatial).getChild("base");
            this.sleeve = ((Node)spatial).getChild("sleeve");

        }
    }
    
    @Override
    public void resetSystem() {
        
    }
    
    public TargetInformation getTarget() {
        if ( this.weapons == null ) {
            return null;
        }
        return this.weapons.getTarget();
    }
    
    public void setTarget(TargetInformation target) {
        if ( this.weapons == null ) {
            return;
        }
        this.weapons.setTarget(target);
    }
    
    public boolean canTarget(TargetInformation target) {
        if (this.weapons == null || target.getObject() == null || !target.getObject().isAlive()) {
            return false;
        }
        
        Vector3f location = target.getObject().getPosition();
        Vector3f direction = location.subtract(getSpatial().getWorldTranslation());
        
        Vector3f up = getSpatial().getWorldRotation().mult(Vector3f.UNIT_Y);

        SpaceDebugger.getInstance().setVector(DebugContext.WEAPONS, "turretup" + this.hashCode(), getSpatial().getWorldTranslation(), up.mult(10), ColorRGBA.White);
        
        return this.weapons.inPrimaryRange(target) && up.angleBetween(direction.normalizeLocal()) <= this.angle * 0.5f * FastMath.PI / 180;
    }
    
    public void setWeaponColor(ColorRGBA color) {
        if ( this.weapons == null ) {
            this.weapons = getSpatial().getControl(WeaponSystemControl.class);
            if (this.weapons == null) {
                return;
            }
        }
        this.weapons.setWeaponColor(color);
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.angle = XMLLoader.getFloatValue(element, "angle", VAR_DEFAULTANGLE);
        this.verticalturnrate = XMLLoader.getFloatValue(element, "verticalturnrate", VAR_DEFAULTANGLE);
        this.horizontalturnrate = XMLLoader.getFloatValue(element, "horizontalturnrate", VAR_DEFAULTANGLE);
    }
    
}
