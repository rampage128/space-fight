/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.scripting;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.random.RandomCoordinateGenerator;
import de.jlab.spacefight.weapon.AbstractWeaponControl;

/**
 *
 * @author rampage
 */
public class WeaponScriptWrapper extends ScriptWrapper {
    
    private AbstractWeaponControl weapon;
    
    public WeaponScriptWrapper(AbstractWeaponControl weapon) {
        this.weapon = weapon;
    }
    
    public AbstractWeaponControl spawnWeapon(String name, Vector3f facing) {
        SpaceAppState space = Game.get().getStateManager().getState(SpaceAppState.class);
        if (space != null) {
            AbstractWeaponControl newWeapon = Game.get().getGamedataManager().loadWeapon(name, space).getInstance(null);
            Quaternion rotation = new Quaternion();
            if (facing == null) {
                facing = new RandomCoordinateGenerator(new Vector3f(2, 2, 2)).getVector();
            }            
            rotation.lookAt(facing, Vector3f.UNIT_Y);
            newWeapon.getSpatial().setLocalRotation(rotation);
            newWeapon.getSpatial().setLocalTranslation(this.weapon.getSpatial().getWorldTranslation());
            newWeapon.setTarget(this.weapon.getTarget());
            if (this.weapon.getOrigin() != null && this.weapon.getOrigin().getFaction() != null && this.weapon.getOrigin().getFaction().getColor() != null) {
                newWeapon.setColor(this.weapon.getOrigin().getFaction().getColor());
            }
            //newWeapon.setOrigin(this.weapon.getOrigin());
            space.addWeapon(newWeapon, this.weapon.getOrigin());
            
            return newWeapon;
        }
        return null;
    }

    public void destroyWeapon() {
        this.weapon.destroyWeapon();
    }
    
    public float getDistanceToTarget() {
        if (this.weapon.getTarget() == null) {
            return Float.MAX_VALUE;
        }
        return this.weapon.getTarget().getDistance(this.weapon.getSpatial().getControl(ObjectInfoControl.class));
    }
    
    // GENERAL METHODS    
    public void update(float tpf) {
        invoke("update", tpf, this.weapon);
    }
    
    public void onDestroy() {
        invoke("destroy", this.weapon);
    }

    public WeaponScriptWrapper clone(AbstractWeaponControl weapon) {
        WeaponScriptWrapper newScript = new WeaponScriptWrapper(weapon);
        newScript.init(new JScript(this.script.getName(), this.script.getCode(), null, this.script.isCritical(), Game.get().getGamedataManager()));
        return newScript;
    }
    
}
