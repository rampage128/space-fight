/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.weapon;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import org.jdom.Element;

/**
 * Implementation of aiming weapons. This only controls the aiming and targeting
 * of an aimed weapon like a missile. 
 * 
 * This class may be ommited in the
 * future and AbstractWeaponControll will implement this as default behaviour.
 * 
 * @author rampage
 */
@Deprecated
public class MissileWeaponControl {

    private float turnRate;
    private TargetInformation target;
    
    public MissileWeaponControl(SpaceAppState space) {
        //super(space);
    }
    
    /*
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        MissileWeaponControl control = new MissileWeaponControl(space);
        control.setTarget(this.target);
        control.setCollisionShape(getCollisionShape());
        control.setCollisionGroup(getCollisionGroup());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setOrigin(getOrigin());
        control.speed = speed;
        control.range = range;
        control.damage = damage;
        control.turnRate = turnRate;
        control.name = name;
        
        control.blastimpulse = this.blastimpulse;
        control.muzzleEffect = muzzleEffect;
        control.muzzleSize = muzzleSize;
        //control.hitSound = hitSound;
        control.explosionEffect = explosionEffect;
        control.explosionSize = explosionSize;
        control.turnRate = turnRate;
        control.setSpatial(spatial);
        return control;
    }
    */

    //@Override
    public void updateWeapon(float tpf) {
        /*
        if ( this.target != null && this.target.getObject() != null && this.distance > 20f ) {
            // CLEAR TARGET IF IT'S DEAD
            if (!this.target.getObject().isAlive()) {
                this.target = null;
                return;
            }
            
            // REFRESH TARGET INFORMATION
            //this.target.update(spatial.getControl(ObjectInfoControl.class), this);

            // GET CURRENT FACING
            Quaternion facing = spatial.getWorldRotation();

            // COMPUTE VECTOR TO TARGET BY SUBSTRACTING OUR POS FROM OTHER POS
            Vector3f targetVector = this.target.getAimAtWorld(spatial.getControl(ObjectInfoControl.class), this).subtract(spatial.getWorldTranslation());

            // COMPUTE TARGET ROTATION
            Quaternion targetFacing = new Quaternion();
            targetFacing.lookAt(targetVector, Vector3f.UNIT_Y);

            // TURN TO TARGET USING SLERP
            spatial.setLocalRotation(new Quaternion().slerp(facing, targetFacing, this.turnRate * tpf));
        }
        */
    }

    //@Override
    public void onDestroy() {
        clearTargetReport();
    }

    public void setTarget(TargetInformation target) {
        this.target = target;
        reportToTarget();
    }

    private void reportToTarget() {
        if ( this.target != null && this.target.getObject() != null ) {
            SensorControl sensors = this.target.getObject().getObjectControl(SensorControl.class);
            if ( sensors != null ) {
                //sensors.reportMissile(this);
            }
        }
    }
    
    private void clearTargetReport() {
        if ( this.target != null && this.target.getObject() != null ) {
            SensorControl sensors = this.target.getObject().getObjectControl(SensorControl.class);
            if ( sensors != null ) {
                //sensors.removeMissile(this);
            }
        }
    }
    
    //@Override
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        //super.loadFromElement(element, path, gamedataManager);
        this.turnRate = XMLLoader.getFloatValue(element, "turnrate", 2.5f);
    }
    
    //@Override
    public String getType() {
        return "missile";
    }
    
}
