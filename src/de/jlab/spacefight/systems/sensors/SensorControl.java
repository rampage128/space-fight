/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.sensors;

import de.jlab.spacefight.mission.structures.DamageInformation;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.systems.SystemControl;
import de.jlab.spacefight.systems.TurretMasterControl;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.systems.hud.CockpitHudControl;
import de.jlab.spacefight.systems.hud.RadarItem;
import de.jlab.spacefight.weapon.AbstractWeaponControl;
import java.util.ArrayList;
import org.jdom.Element;

/**
 * Control for spaceships to manage Sensors.
 * Sensor System provides radar, targeting, aiming and warning abilities.
 * 
 * @author rampage
 */
public class SensorControl extends AbstractControl implements SystemControl, XMLLoadable {

    public static final int VAR_PROXIMITYDISTANCE_AUTO = -1;
    public static final float VAR_DEFAULTSENSORRANGE = 8000f;
    
    private float maxrange = VAR_DEFAULTSENSORRANGE;
    private float scale = 1f;
    
    private SpaceAppState space;
    
    private ArrayList<ObjectInfoControl> lockList = new ArrayList<ObjectInfoControl>();
    private ArrayList<TargetInformation> targetList = new ArrayList<TargetInformation>();   
    private ArrayList<DamageInformation> damageList = new ArrayList<DamageInformation>();
    
    private float laserProximity = 0;
    private long laserProximityTime = 0;
    
    //private float lockCount = 0;
    private ArrayList<AbstractWeaponControl> missiles = new ArrayList<AbstractWeaponControl>();
    private float missileLaunchCount = 0;
    
    private WeaponSystemControl weaponSystemControl;
    private TurretMasterControl turretMasterControl;
    
    private float proximityDistance = 10f;
    private Vector3f proximityAvoidanceVector = Vector3f.ZERO.clone();
    
    private int enemyCount = 0;
    
    public SensorControl(SpaceAppState game) {
        this.space = game;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        ObjectInfoControl self = getSpatial().getControl(ObjectInfoControl.class);
        
        if ( this.turretMasterControl == null ) {
            this.turretMasterControl = this.getSpatial().getControl(TurretMasterControl.class);
        }
        
        if ( this.weaponSystemControl == null ) {
            this.weaponSystemControl = this.getSpatial().getControl(WeaponSystemControl.class);
        }

        CockpitHudControl hud = getSpatial().getControl(CockpitHudControl.class);
        RadarItem radarDisplay = null;
        if (hud != null) {
            radarDisplay = (RadarItem)hud.getItem(CockpitHudControl.RADAR);
            if (radarDisplay != null) {
                radarDisplay.clearForRedraw(getSpatial());
            }
        }
        
        scan(tpf, radarDisplay);
                
        for ( int i = 0; i < this.missiles.size(); i++ ) {
            AbstractWeaponControl missile = this.missiles.get(i);
            if ( missile.getSpatial() == null || missile.getSpatial().getParent() == null ) {
                this.missiles.remove(missile);
                i--;
            } else {
                TargetInformation target = missile.getSpatial().getControl(ObjectInfoControl.class).getTargetInformation(self, null);
                //TargetInformation target = new TargetInformation(missile.getSpatial());
                //target.update(getSpatial(), null);
                if (radarDisplay != null) {
                    radarDisplay.drawMissile(tpf, getSpatial(), target);                    
                }
            }
        }
        
        // UPDATE DAMAGELIST
        for ( int i = 0; i < this.damageList.size(); i++ ) {
            DamageInformation info = this.damageList.get(i);
            info.update(tpf);
            if ( info.getTime() > 1f )
                this.damageList.remove(i);
        }
        
        if ( System.currentTimeMillis() > this.laserProximityTime + 2000 ) {
            this.laserProximity = 0;
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        SensorControl control = new SensorControl(this.space);
        control.setSpatial(spatial);
        return control;
    }
     
    // TARGETING

    /*
    public TargetInformation getCurrentTarget() {
        return this.currentTarget;
    }
    */
    
    public ArrayList<TargetInformation> getTargetList(float distance, int... fof) {
        ObjectInfoControl self = getSpatial().getControl(ObjectInfoControl.class);
        ArrayList<TargetInformation> resultList = new ArrayList<TargetInformation>();
        for (TargetInformation targetInfo : this.targetList) {
            if (distance > 0 && targetInfo.getDirection(self).length() > distance) {
                continue;
            }
            if (fof != null) {
                boolean isDesired = false;
                for (int i = 0; i < fof.length; i++) {
                    if (fof[i] >= 0 && fof[i] <= 2 && targetInfo.getFOF(self) == fof[i]) {
                        isDesired = true;
                    }
                }
                if (!isDesired) {
                    continue;
                }
            }
            resultList.add(targetInfo);
        }
        return resultList;
    }
    
    public Vector3f getProximityAvoidance() {
        return this.proximityAvoidanceVector;
    }
    
    public void setProximityDistance(float proximityDistance) {
        this.proximityDistance = proximityDistance;
    }
    
    /*
    private void checkProximity(float checkDistance) {
        //System.out.println(checkDistance);
        ObjectInfoControl info = getSpatial().getControl(ObjectInfoControl.class);
        if ( checkDistance <= 0 ) {
            return null;
        }
        
        ArrayList<TargetInformation> targets = getTargetList(checkDistance, null);
        this.proximityAvoidanceVector.set(0f, 0f, 0f);
        for ( TargetInformation target : targets ) {
            float removal = target.getSize() / 2 + info.getSize() / 2;
            float distance = target.getDistance() - removal;
            if ( distance <= checkDistance && target.getSize() >= info.getSize() / 2 ) {
                System.out.println(distance);
                avoidVector.addLocal(target.getDirection());
            }
        }
        if ( avoidVector.length() > 0 ) {
            this.proximityAvoidanceVector.normalizeLocal().multLocal(checkDistance + info.getSize());
        }
        return null;
    }
    */
    
    /**
     * SCANS FOR SHIPS
     */
    public void scan(float tpf, RadarItem radarDisplay) {        
        ObjectInfoControl self = getSpatial().getControl(ObjectInfoControl.class);
        this.targetList.clear();
                        
        ObjectInfoControl info = getSpatial().getControl(ObjectInfoControl.class);
        this.proximityAvoidanceVector.set(0f, 0f, 0f);        
        
        this.lockList.clear();
        
        // ORIGINALLY SCANNER RETRIEVED SPATIAL LIST FROM SPACE-NODE!
        this.enemyCount = 0;
        ObjectInfoControl[] objects = this.space.getMission().getObjects();
        for (ObjectInfoControl object : objects) {
            if (!object.isAlive()) {
                continue;
            }
            TargetInformation target = object.getTargetInformation(self, null); //new TargetInformation(shipList.get(i));
            if ( object == self )
                continue;
            
            Vector3f direction = target.getDirection(self);
            float tdistance = direction.length();
            //target.update(getSpatial(), null);
            if ( target.getObject().getAlwaysVisible() || tdistance <= scale * maxrange ) {
                Vector3f checkDirection = self.getFacing().normalize().mult(object.getLinearVelocity().length() * 2f);
                Vector3f cv = direction.subtract(checkDirection);
                if (cv.length() < target.getObject().getSize() + object.getSize()) {
                    this.proximityAvoidanceVector.addLocal(direction.negate());
                } else {
                    float removal = object.getSize() / 2 + info.getSize() / 2;
                    float distance = tdistance - removal;                
                    if ( distance <= ((this.proximityDistance == VAR_PROXIMITYDISTANCE_AUTO) ? (self.getSize() * 2) : this.proximityDistance) && object.getSize() >= info.getSize() / 4 ) {
                        //System.out.println(distance);
                        this.proximityAvoidanceVector.addLocal(direction.negate());
                    }
                }
                
                this.targetList.add(target);
                WeaponSystemControl targetWeapons = object.getObjectControl(WeaponSystemControl.class);
                if ( targetWeapons != null ) {
                    if ( targetWeapons.getTarget() != null && targetWeapons.getTarget().getObject() == self) {
                        if (target.getObject() != null) {
                            this.lockList.add(target.getObject());
                        }
                    }
                }
                if ( target.getFOF(self) == TargetInformation.FOF_FOE && this.turretMasterControl != null ) {
                    this.turretMasterControl.assignTarget(target);
                }
                if (target.getFOF(self) == TargetInformation.FOF_FOE) {
                    this.enemyCount++;
                }
                if (radarDisplay != null) {
                    radarDisplay.drawTarget(tpf, getSpatial(), target);
                }
            }
        }
        
        if ( this.proximityAvoidanceVector.length() > 0 ) {
            this.proximityAvoidanceVector.normalizeLocal().multLocal(this.proximityDistance + info.getSize());
        }
    }
    
    public boolean target(String targetId) {
        if ( this.weaponSystemControl == null ) {
            return false;
        }
        
        for ( int i = 0; i < targetList.size(); i++ ) {            
            TargetInformation target = targetList.get(i);
            
            if (target.getObject().getId().equalsIgnoreCase(targetId)) {
                this.weaponSystemControl.setTarget(target);
                return true;
            }
        }
        return false;
    }
    
    public boolean targetEnemy(String enemyId) {
        if ( this.weaponSystemControl == null ) {
            return false;
        }
        
        for ( int i = 0; i < targetList.size(); i++ ) {            
            TargetInformation target = targetList.get(i);

            if ( target.getFOF(getSpatial().getControl(ObjectInfoControl.class)) != TargetInformation.FOF_FOE )
                continue;
            
            if ( target.getObject().getId().equalsIgnoreCase(enemyId) ) {
                this.weaponSystemControl.setTarget(target);
                return true;
            }
        }
        return false;
    }
    
    /*
     * TARGETS THE CLOSEST SHIP IN SENSORRANGE
     */
    public boolean targetNextEnemy() {
        if ( this.weaponSystemControl == null ) {
            return false;
        }
        
        float targetDistance = scale * maxrange;
        for ( int i = 0; i < targetList.size(); i++ ) {            
            TargetInformation target = targetList.get(i);
            
            /*
            Integer factionid = target.getTarget().getUserData("faction");
            if ( factionid != null && factionid.equals(this.getSpatial().getUserData("faction")) )
                continue;
            */
            if ( target.getFOF(getSpatial().getControl(ObjectInfoControl.class)) != TargetInformation.FOF_FOE )
                continue;
            
            float distance = target.getDirection(getSpatial().getControl(ObjectInfoControl.class)).length();
            if ( distance <= targetDistance ) {
                targetDistance = distance;
                this.weaponSystemControl.setTarget(target);
            }
        }
        
        return this.weaponSystemControl.getTarget() != null;
    }
    
    public boolean targetNext() {
        if ( this.weaponSystemControl == null ) {
            return false;
        }

        if ( this.targetList.isEmpty() ) {
            return false;
        }
        
        int index = 0;
        if (this.weaponSystemControl.getTarget() != null) {
            for (int i = 0; i < this.targetList.size(); i++) {
                TargetInformation target = this.targetList.get(i);
                if (target.getObject().equals(this.weaponSystemControl.getTarget().getObject())) {
                    index = i+1;
                    break;
                }
            }
        }
        
        if ( index > this.targetList.size() - 1 ) {
            index = 0;
        }
        
        this.weaponSystemControl.setTarget(this.targetList.get(index++));
        return true;
    }
    
    public boolean targetSmart() {
        ObjectInfoControl self = getSpatial().getControl(ObjectInfoControl.class);
        
        if ( this.weaponSystemControl == null ) {
            return false;
        }
        
        // TARGET NEXT MISSILE
        if ( !this.missiles.isEmpty() ) {
            float targetDistance = scale * maxrange;
            for ( AbstractWeaponControl missile : this.missiles ) {
                TargetInformation target = missile.getSpatial().getControl(ObjectInfoControl.class).getTargetInformation(self, null);
                //TargetInformation target = new TargetInformation(missile.getSpatial());
                //target.update(getSpatial(), null);
                float distance = target.getDirection(self).length();
                if ( distance <= targetDistance ) {
                    targetDistance = distance;
                    this.weaponSystemControl.setTarget(target);
                }
            }
            return true;
        }
        
        // TARGET NEXT ATTACKING ENEMY
        if ( !this.damageList.isEmpty() ) {
            float targetDistance = scale * maxrange;
            boolean targeted = false;
            for ( DamageInformation damage : this.damageList ) {            
                TargetInformation target = damage.getOrigin().getTargetInformation(self, null);
                //target.update(getSpatial(), null);

                if ( target.getFOF(self) != TargetInformation.FOF_FOE )
                    continue;

                float distance = target.getDirection(self).length();
                if ( distance <= targetDistance ) {
                    targetDistance = distance;
                    this.weaponSystemControl.setTarget(target);
                    targeted = true;
                }
            }
            if ( targeted ) {
                return true;
            }
        }
                
        // TARGET MISSION GOAL
        ObjectInfoControl clientControl = getSpatial().getControl(ObjectInfoControl.class);
        if ( clientControl != null ) {
            Task task = clientControl.getCurrentTask();
            if ( task != null ) {
                ObjectInfoControl targetObject = task.getTargetObject(space.getMission());
                if ( targetObject != null ) {
                    TargetInformation target = targetObject.getTargetInformation(self, null);
                    //target.update(getSpatial(), null);
                    float targetDistance = scale * maxrange;
                    float distance = target.getDirection(self).length();
                    if ( distance <= targetDistance ) {
                        this.weaponSystemControl.setTarget(target);
                        return true;
                    }
                }
            }
        }
        
        // NO NEED TO TARGET SOMETHING
        this.weaponSystemControl.setTarget(null);
        return false;
    }
    
    public Vector3f getTargetEvadeVector() {
        Quaternion rotation = new Quaternion().fromAngles(90 * FastMath.PI / 180, 90 * FastMath.PI / 180, 0).add(getSpatial().getWorldRotation());
        return rotation.mult(Vector3f.UNIT_Z);
    }
       
    // HIT SENSING
    
    /*
     * CALL THIS TO REPORT DAMAGE TO THE SENSORS
     */
    public void reportDamage(DamageInformation damageInformation) {
        this.damageList.add(damageInformation);
    }
    
    /*
     * CALL THIS TO REPORT LASER-FIRE TO THE SENSORS
     */
    public void reportLaser(ObjectInfoControl origin) {
        this.laserProximityTime = System.currentTimeMillis();
        this.laserProximity++;
    }
    
    /*
     * CALL THIS TO REPORT MISSILES TO THE SENSORS
     */
    public void reportWeapon(AbstractWeaponControl control) {
        if (control.getType() == AbstractWeaponControl.TYPE_WARHEAD) {
            this.missileLaunchCount++;
            this.missiles.add(control);
        }
    }
    
    public void removeWeapon(AbstractWeaponControl control) {
        this.missiles.remove(control);
    }
    
    public AbstractWeaponControl[] getMissiles() {
        return this.missiles.toArray(new AbstractWeaponControl[0]);
    }
        
    /*
     * CHECKS IF A LASER WARNING IS ACTIVE
     */
    public boolean getLaserWarning() {
        return this.laserProximity > 0;
    }
    
    public boolean getMissileWarning() {
        return this.missiles.size() > 0;
    }
    
    public boolean getMissileLaunchWarning() {
        boolean result = this.missileLaunchCount > 0;
        this.missileLaunchCount = 0;
        return result;
    }
    
    public boolean getLockWarning() {
        return !this.lockList.isEmpty();
    }
    
    public int getEnemyCount() {
        return this.enemyCount;
    }
    
    public ArrayList<ObjectInfoControl> getLockList() {
        return this.lockList;
    }

    public void resetSystem() {
        this.targetList.clear();
        this.missiles.clear();
        this.lockList.clear();
        this.missileLaunchCount = 0;
        this.laserProximity     = 0;
    }
    
    public float getRange() {
        return this.maxrange * this.scale;
    }
    
    public void setMaxRange(float maxrange) {
        this.maxrange = maxrange;
    }
    
    public void increaseRange() {
        this.scale += 0.25f;
        this.scale = Math.min(1f, this.scale);
    }
    
    public void decreaseRange() {
        this.scale -= 0.25f;
        this.scale = Math.max(0.25f, this.scale);
    }
    
    public void setRange(float distance) {
        this.scale = distance / this.maxrange;
        this.scale = Math.min(1f, this.scale);
        this.scale = Math.max(0.25f, this.scale);
    }
    
    public boolean mayTarget(TargetInformation target) {
        return target.getObject().isAlive() && target.getDirection(getSpatial().getControl(ObjectInfoControl.class)).length() <= this.maxrange * this.scale;
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.maxrange = XMLLoader.getFloatValue(element, "maxrange", 8000f);
    }
    
}
