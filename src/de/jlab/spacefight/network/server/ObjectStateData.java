/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network.server;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import de.jlab.spacefight.basic.DamageControl;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.network.NetworkClientControl;
import de.jlab.spacefight.player.Player;
import de.jlab.spacefight.player.PlayerControl;
import de.jlab.spacefight.systems.ShieldControl;

/**
 *
 * @author rampage
 */
@Serializable
public final class ObjectStateData {

    public static final String PROPERTY_PLAYER  = "player";
    
    public static final String PROPERTY_POSITION = "position";
    public static final String PROPERTY_ROTATION = "rotation";
    public static final String PROPERTY_LINEARVELOCITY = "linearvelocity";
    public static final String PROPERTY_ANGULARVELOCITY = "angularvelocity";
    
    public static final String PROPERTY_ALIVE = "damage";
    public static final String PROPERTY_DAMAGE = "damage";
    public static final String PROPERTY_SHIELDFRONT = "shieldfront";
    public static final String PROPERTY_SHIELDREAR  = "shieldrear";
    
    private String key;
    private Object value;
    
    public ObjectStateData() {}
    
    public ObjectStateData(String key, ObjectInfoControl object) {
        this.key = key;
        this.updateValue(object);
    }
    
    public String getKey() {
        return this.key;
    }
    
    public boolean getBooleanValue() {
        return (Boolean)value;
    }
    
    public int getIntValue() {
        return (Integer)value;
    }
    
    public float getFloatValue() {
        return (Float)value;
    }
    
    public String getStringValue() {
        return (String)value;
    }
    
    public Vector3f getVectorValue() {
        return (Vector3f)value;
    }
    
    public Quaternion getQuaternionValue() {
        return (Quaternion)value;
    }
    
    public Player getPlayerValue() {
        return (Player)value;
    }
    
    public void updateValue(ObjectInfoControl object) {
        if (PROPERTY_PLAYER.equalsIgnoreCase(this.key)) {
            this.value = object.getPlayer();
            //PlayerControl plControl = object.getSpatial().getControl(PlayerControl.class);
            //NetworkClientControl netControl = object.getSpatial().getControl(NetworkClientControl.class);
            //this.value = netControl != null || plControl != null;
        } else if (PROPERTY_POSITION.equalsIgnoreCase(this.key)) {
            this.value = object.getPosition().clone();
        } else if (PROPERTY_ROTATION.equalsIgnoreCase(key)) {
            this.value = object.getRotation().clone();
        } else if (PROPERTY_LINEARVELOCITY.equalsIgnoreCase(key)) {
            this.value = object.getLinearVelocity().clone();
        } else if (PROPERTY_ANGULARVELOCITY.equalsIgnoreCase(key)) {
            this.value = object.getAngularVelocity().clone();
        } else if (PROPERTY_ALIVE.equalsIgnoreCase(key)) {
            this.value = object.isAlive();
        } else if (PROPERTY_DAMAGE.equalsIgnoreCase(key)) {
            DamageControl damage = object.getObjectControl(DamageControl.class);
            if (damage != null) {
                this.value = damage.getHull();
            }
        } else if (PROPERTY_SHIELDFRONT.equalsIgnoreCase(key)) {
            ShieldControl shield = object.getObjectControl(ShieldControl.class);
            if (shield != null) {
                this.value = shield.getFrontShield();
            }
        } else if (PROPERTY_SHIELDREAR.equalsIgnoreCase(key)) {
            ShieldControl shield = object.getObjectControl(ShieldControl.class);
            if (shield != null) {
                this.value = shield.getRearShield();
            }
        }
    }
    
    public void apply(ObjectInfoControl object) {
        if (PROPERTY_PLAYER.equalsIgnoreCase(this.key)) {
            object.setPlayer(getPlayerValue());
        } else if (PROPERTY_POSITION.equalsIgnoreCase(this.key)) {
            object.setPosition(getVectorValue());
        } else if (PROPERTY_ROTATION.equalsIgnoreCase(key)) {
            object.setRotation(getQuaternionValue());
        } else if (PROPERTY_LINEARVELOCITY.equalsIgnoreCase(key)) {
            object.setLinearVelocity(getVectorValue());
        } else if (PROPERTY_ANGULARVELOCITY.equalsIgnoreCase(key)) {
            object.setAngularVelocity(getVectorValue());
        } else if (PROPERTY_ALIVE.equalsIgnoreCase(key)) {
            if (!object.isAlive() && this.value.equals(true)) {
                object.spawnObject();
            } else if (object.isAlive() && this.value.equals(false)) {
                object.remove();
            }
        } else if (PROPERTY_DAMAGE.equalsIgnoreCase(key)) {
            DamageControl damage = object.getObjectControl(DamageControl.class);
            if (damage != null) {
                damage.setHull(getFloatValue());
            }
        } else if (PROPERTY_SHIELDFRONT.equalsIgnoreCase(key)) {
            ShieldControl shield = object.getObjectControl(ShieldControl.class);
            if (shield != null) {
                shield.setFrontShield(getFloatValue());
            }
        } else if (PROPERTY_SHIELDREAR.equalsIgnoreCase(key)) {
            ShieldControl shield = object.getObjectControl(ShieldControl.class);
            if (shield != null) {
                shield.setRearShield(getFloatValue());
            }
        }
    }
        
    public boolean hasValueChanged(ObjectInfoControl object) {
        if (PROPERTY_PLAYER.equalsIgnoreCase(this.key)) {
            // CURRENTLY THE PLAYER STATE IS SYNCHRONIZED EVERY SECOND!
            //PlayerControl plControl = object.getSpatial().getControl(PlayerControl.class);
            //NetworkClientControl netControl = object.getSpatial().getControl(NetworkClientControl.class);
            return (object.getPlayer() == null && this.value == null) || (object.getPlayer() != null && this.value != null && object.getPlayer().getNetworkId() == ((Player)this.value).getNetworkId()); //!this.value.equals((Boolean)(netControl != null || plControl != null));
        } else if (PROPERTY_POSITION.equalsIgnoreCase(this.key)) {
            return !this.value.equals(object.getPosition());
        } else if (PROPERTY_ROTATION.equalsIgnoreCase(key)) {
            return !this.value.equals(object.getRotation());
        } else if (PROPERTY_LINEARVELOCITY.equalsIgnoreCase(key)) {
            return !this.value.equals(object.getLinearVelocity());
        } else if (PROPERTY_ANGULARVELOCITY.equalsIgnoreCase(key)) {
            return !this.value.equals(object.getAngularVelocity());
        } else if (PROPERTY_ALIVE.equalsIgnoreCase(key)) {
            return !this.value.equals(object.isAlive());
        } else if (PROPERTY_DAMAGE.equalsIgnoreCase(key)) {
            DamageControl damage = object.getObjectControl(DamageControl.class);
            if (damage != null) {
                return !this.value.equals(damage.getHull());
            }
        } else if (PROPERTY_SHIELDFRONT.equalsIgnoreCase(key)) {
            ShieldControl shield = object.getObjectControl(ShieldControl.class);
            if (shield != null) {
                return !this.value.equals(shield.getFrontShield());
            }
        } else if (PROPERTY_SHIELDREAR.equalsIgnoreCase(key)) {
            ShieldControl shield = object.getObjectControl(ShieldControl.class);
            if (shield != null) {
                return !this.value.equals(shield.getRearShield());
            }
        }
        return false;
    }
    
}
