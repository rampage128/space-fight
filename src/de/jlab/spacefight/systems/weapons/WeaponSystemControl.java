/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.weapons;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.SystemControl;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import de.jlab.spacefight.weapon.AbstractWeaponControl;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 * Controls weaponsystems of a spatial.
 * 
 * Static lasers are controlled by reading "laser" nodes from the spatial.
 * 
 * 
 * @author rampage
 */
public class WeaponSystemControl extends AbstractControl implements SystemControl, XMLLoadable {
    
    public static final int MODE_SINGLE = 0;
    public static final int MODE_HALF   = 1;
    public static final int MODE_FULL   = 2;
    
    public static final float VAR_PRIMARY_COOLDOWN = 0.5f;
    public static final float VAR_SECONDARY_COOLDOWN = 0.5f;
    
    public static final int VAR_PRIMARY_ENERGYPOINTS = 1500;
    public static final int VAR_SECONDARY_COUNT = 8;
    public static final float VAR_PRIMARY_CHARGETIME = 10f;
    
    private boolean firePrimary = false;
    private boolean fireSecondary = false;
    private boolean primaryFired = false;
    private boolean secondaryFired = false;
    
    private float primarycooldown = 0f;
    private float secondarycooldown = 0f;
    
    protected SpaceAppState space;
    
    private List<WeaponSlot> primarySlotList = new ArrayList<WeaponSlot>();
    //private List<Spatial> primarySlotList = new ArrayList<Spatial>();
    private int currentPrimarySlot = 0;

    //private AbstractWeaponControl primaryWeapon;
    private float primaryEnergy = 1;
    private int primaryEnergyPoints = VAR_PRIMARY_ENERGYPOINTS;
    private int primaryMode = MODE_SINGLE;
    
    private List<WeaponSlot> secondarySlotList = new ArrayList<WeaponSlot>();
    //private List<Spatial> secondarySlotList = new ArrayList<Spatial>();
    private int currentSecondarySlot = 0;
    //private int maxSecondaryCount = VAR_SECONDARY_COUNT;
    //private int secondaryCount = VAR_SECONDARY_COUNT;
    
    //private AbstractWeaponControl secondaryWeapon;    
    
    private FlightControl flightControl;
    private SensorControl sensorControl;
    
    private TargetInformation target;
        
    public WeaponSystemControl(SpaceAppState space) {
        this.space = space;
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if ( spatial != null ) {
            ObjectInfoControl object = spatial.getControl(ObjectInfoControl.class);
            /* OLD METHOD TO FIND WEAPONSLOTS
            Spatial weaponChild = ((Node)spatial).getChild("weapons");
            if ( weaponChild != null ) {
                List<Spatial> childList = ((Node)weaponChild).getChildren();
                for ( int i = 0; i < childList.size(); i++ ) {
                    Spatial child = childList.get(i);
                    if ( "primary".equalsIgnoreCase(child.getName()) )
                        this.primarySlotList.add(new WeaponSlot(child, object));
                    if ( "secondary".equalsIgnoreCase(child.getName()) ) {
                        this.secondarySlotList.add(child);
                    }
                }
            }
            */
            
            // ATTACH WEAPON SLOTS
            for (WeaponSlot primarySlot : this.primarySlotList) {
                primarySlot.attach((Node)spatial);
            }
            
            for (WeaponSlot secondarySlot : this.secondarySlotList) {
                secondarySlot.attach((Node)spatial);
            }
        }     
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if (this.sensorControl == null) {
            this.sensorControl = getSpatial().getControl(SensorControl.class);
        }
        
        if (this.primaryEnergy < 1) {
            this.primaryEnergy += 1 / VAR_PRIMARY_CHARGETIME * tpf;
            if (this.primaryEnergy > 1) {
                this.primaryEnergy = 1;
            }
        }
        
        if (this.target != null) {
            //this.target.update(getSpatial().getControl(ObjectInfoControl.class), this.primaryWeapon);
            if (this.sensorControl != null) {
                if (!this.sensorControl.mayTarget(this.target)) {
                    target = null;
                }
            }
        }
        
        if (this.space.isEnabled()) {
            if (primarycooldown > 0)
                primarycooldown -= tpf;
            if (primarycooldown < 0f)
                primarycooldown = 0f;
            if (secondarycooldown > 0)
                secondarycooldown -= tpf;
            if (secondarycooldown < 0f)
                secondarycooldown = 0f;
        }
        
        this.primaryFired = this.firePrimary;
        if (this.firePrimary) {
            doFirePrimary();
        }
        this.secondaryFired = this.fireSecondary;
        if (this.fireSecondary) {
            doFireSecondary();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        WeaponSystemControl newControl = new WeaponSystemControl(this.space);
        newControl.setSpatial(spatial);
        return newControl;
    }
    
    public int getPrimaryMode() {
        return this.primaryMode;
    }
    
    public void setPrimaryMode(int primaryMode) {
        this.primaryMode = primaryMode;
    }
    
    public void switchPrimaryMode() {
        switch(this.primaryMode) {
            case MODE_SINGLE:
                this.primaryMode = MODE_HALF;
                if (this.primarySlotList.size() < 4) {
                    this.primaryMode = MODE_FULL;
                }
                break;
            case MODE_HALF:
                this.primaryMode = MODE_FULL;
                break;
            default:
                this.primaryMode = MODE_SINGLE;
        }
    }
       
    public WeaponSlot[] getPrimarySlots() {
        return this.primarySlotList.toArray(new WeaponSlot[0]);
    }
    
    public WeaponSlot[] getSecondarySlots() {
        return this.secondarySlotList.toArray(new WeaponSlot[0]);
    }
    
    public float getPrimaryEnergy() {
        return this.primaryEnergy;
    }
    
    public int getPrimaryEnergyPoints() {
        return this.primaryEnergyPoints;
    }
    
    public void setPrimaryEnergyPoints(int primaryEnergyPoints) {
        this.primaryEnergyPoints = primaryEnergyPoints;
    }
    
    public boolean inPrimaryRange(TargetInformation target) {
        AbstractWeaponControl weapon = this.getPrimarySlot().getWeapon();
        if (weapon == null) {
            return false;
        }
        return target.getDirection(getSpatial().getControl(ObjectInfoControl.class)).length() < weapon.getRange();
    }
    
    public boolean inPrimaryRange() {
        AbstractWeaponControl weapon = this.getPrimarySlot().getWeapon();
        if ( this.target == null || weapon == null ) {
            return false;
        }
        
        return this.target.getDirection(getSpatial().getControl(ObjectInfoControl.class)).length() < weapon.getRange();
    }
    
    public boolean mayFirePrimary() {
        AbstractWeaponControl weapon = this.getPrimarySlot().getWeapon();
        ObjectInfoControl self = getSpatial().getControl(ObjectInfoControl.class);
        
        if ( this.primarySlotList.isEmpty() || this.target == null || weapon == null ) {
            return false;
        }
        
        //Spatial currentSlot = this.primarySlotList.get(this.currentPrimarySlot);
                       
        return this.target.getDirection(self).length() < weapon.getRange() && this.target.getAimAtTolerance(self, weapon) < this.target.getObject().getSize() / 2 && this.target.getAimAtAngle(self, weapon) < 30;
    }
    
    public boolean isPrimaryFiring() {
        return this.firePrimary || this.primaryFired;
    }
    
    public void firePrimary() {
        this.firePrimary = true;
    }
    
    private void doFirePrimary() {       
        if ( this.flightControl == null ) {
            this.flightControl = getSpatial().getControl(FlightControl.class);
        }
        
        if ( this.flightControl != null ) {
            if ( this.flightControl.getCruise() ) {
                return;
            }
        }
        
        if ( getSpatial() == null || getSpatial().getParent() == null ) {
            return;
        }

        int slotcount = 1;
        if (this.primaryMode == MODE_HALF) {
            slotcount = Math.max(1, this.primarySlotList.size() / 2);
        } else if (this.primaryMode == MODE_FULL) {
            slotcount = this.primarySlotList.size();
        }
        
        /*
        if (this.primaryEnergy * this.primaryEnergyPoints < weapon.getDamage() * slotcount) {
            return;
        }
        */
        
        if ( primarycooldown <= 0 && this.primarySlotList.size() > 0 ) {
            for (int i = 0; i < slotcount; i++) {                
                WeaponSlot laserSlot = this.primarySlotList.get(this.currentPrimarySlot);

                if (this.primaryEnergy * this.primaryEnergyPoints < laserSlot.getWeapon().getDamage()) {
                    continue;
                }
                
                laserSlot.fireWeapon(this.target, this.space);
                
                //Vector3f shotLocation = laserSlot.getWorldTranslation(); //getSpatial().getControl(FlightControl.class).getPhysicsLocation().add(
                //Quaternion shotDirection = laserSlot.getWorldRotation(); //getSpatial().getControl(FlightControl.class).getPhysicsRotation();

                /*
                FlightControl fc = getSpatial().getControl(FlightControl.class);
                if ( fc != null ) {
                    Vector3f shipVelocity = fc.getLinearVelocity();
                }
                */

                //this.primaryWeapon.getSpatial().setLocalTranslation(shotLocation);
                //this.primaryWeapon.getSpatial().setLocalRotation(shotDirection);
                //this.space.addWeapon(this.primaryWeapon.getSpatial().clone().getControl(AbstractWeaponControl.class), getSpatial().getControl(ObjectInfoControl.class));

                primaryEnergy -= laserSlot.getWeapon().getDamage() / this.primaryEnergyPoints;

                if ( this.currentPrimarySlot >= this.primarySlotList.size() - 1 )
                    this.currentPrimarySlot = 0;
                else
                    this.currentPrimarySlot++;
            }
            primarycooldown += VAR_PRIMARY_COOLDOWN / this.primarySlotList.size();
        }
        this.firePrimary = false;
    }
    
    /*
    public int getSecondaryCount() {
        return this.secondaryCount;
    }
    
    public void setMaxSecondaryCount(int count) {
        this.maxSecondaryCount = count;
    }
    */
    
    public void setMaxPrimarySize(float size) {
        for (WeaponSlot primarySlot : this.primarySlotList) {
            //if (primarySlot.getSize() > size) {
                primarySlot.setSize(size);
            //}
        }
    }
    
    public void setMaxSecondarySize(float size) {
        for (WeaponSlot secondarySlot : this.secondarySlotList) {
            //if (secondarySlot.getSize() > size) {
                secondarySlot.setSize(size);
            //}
        }
    }
    
    public float getSecondaryRange() {
        AbstractWeaponControl weapon = this.getSecondarySlot().getWeapon();
        if (weapon == null) {
            return 0f;
        }
        
        return weapon.getRange();
    }
    
    public boolean inSecondaryRange() {        
        AbstractWeaponControl weapon = this.getSecondarySlot().getWeapon();
        if ( this.target == null || weapon == null ) {
            return false;
        }
        
        return this.target.getDirection(getSpatial().getControl(ObjectInfoControl.class)).length() < weapon.getRange();
    }
    
    public boolean mayFireSecondary() {
        WeaponSlot weaponSlot = this.getSecondarySlot();
        ObjectInfoControl self = getSpatial().getControl(ObjectInfoControl.class);
        if ( this.secondarySlotList.isEmpty() || this.target == null || weaponSlot.getWeapon() == null ) {
            return false;
        }
        
        //Spatial currentSlot = this.secondarySlotList.get(this.currentSecondarySlot);
                          
        return this.target != null && weaponSlot.weaponCount() > 0 && this.target.getDirection(self).length() < weaponSlot.getWeapon().getRange() && this.target.getAimAtTolerance(self, weaponSlot.getWeapon()) < this.target.getObject().getSize() * 2 && this.target.getAimAtAngle(self, weaponSlot.getWeapon()) < 60;
    }   
    
    public boolean isSecondaryFiring() {
        return this.fireSecondary || this.secondaryFired;
    }
    
    public void fireSecondary() {
        this.fireSecondary = true;
    }
    
    private void doFireSecondary() {
        if ( this.flightControl == null ) {
            this.flightControl = getSpatial().getControl(FlightControl.class);
        }
        
        if ( this.flightControl != null ) {
            if ( this.flightControl.getCruise() ) {
                return;
            }
        }
        
        if ( getSpatial() == null || getSpatial().getParent() == null ) {
            return;
        }
                
        if (secondarycooldown <= 0 && this.secondarySlotList.size() > 0 ) {
            WeaponSlot missileSlot = this.secondarySlotList.get(this.currentSecondarySlot);
            if (missileSlot.weaponCount() > 0) {
            
                missileSlot.fireWeapon(target, space);

                if (missileSlot.weaponCount() < 1) {
                    nextSecondarySlot();
                }
            
            /*
            Vector3f shotLocation = missileSlot.getWorldTranslation();
            Quaternion shotDirection = missileSlot.getWorldRotation();
            
            this.secondaryWeapon.getSpatial().setLocalTranslation(shotLocation);
            this.secondaryWeapon.getSpatial().setLocalRotation(shotDirection);
            if ( this.target != null ) {
                ((MissileWeaponControl)this.secondaryWeapon).setTarget(this.target);
            } else {
                ((MissileWeaponControl)this.secondaryWeapon).setTarget(null);
            }
            Spatial missile = this.secondaryWeapon.getSpatial().clone();
            this.space.addWeapon(missile.getControl(AbstractWeaponControl.class), getSpatial().getControl(ObjectInfoControl.class));
            */
            
            //secondaryCount--;
            secondarycooldown += VAR_SECONDARY_COOLDOWN; // / this.secondarySlotList.size();
            
            //if ( this.currentSecondarySlot >= this.secondarySlotList.size() - 1 )
                //this.currentSecondarySlot = 0;
            //else
                //this.currentSecondarySlot++;
            }
        }
        this.fireSecondary = false;
    }

    public void nextSecondarySlot() {
        this.currentSecondarySlot++;
        if (this.currentSecondarySlot >= this.secondarySlotList.size()) {
            this.currentSecondarySlot = 0;
        }
    }
    
    public void resetSystem() {
        this.primarycooldown    = 0;
        this.primaryEnergy      = 1;
        this.secondarycooldown  = 0;
        this.target             = null;
        for (WeaponSlot primarySlot : this.primarySlotList) {
            primarySlot.reset();
        }
        for (WeaponSlot secondarySlot : this.secondarySlotList) {
            secondarySlot.reset();
        }
    }
    
    public float getSecondaryCooldown() {
        return this.secondarycooldown;
    }
    
    public void setTarget(TargetInformation target) {
        this.target = target;
    }
    
    public TargetInformation getTarget() {
        return this.target;
    }

    public int getPrimarySlotCount() {
        return this.primarySlotList.size();
    }
    
    public WeaponSlot getPrimarySlot() {
        return this.primarySlotList.get(this.currentPrimarySlot);
    }
    
    public WeaponSlot getSecondarySlot() {
        return this.secondarySlotList.get(this.currentSecondarySlot);
    }
    
    /*
    public void setPrimaryWeapon(AbstractWeaponControl weapon) {
        this.primaryWeapon = weapon;
    }
    
    public AbstractWeaponControl getPrimaryWeapon() {
        //return this.primaryWeapon;
        return this.primarySlotList.get(this.currentPrimarySlot).getWeapon();
    }
    
    public void setSecondaryWeapon(AbstractWeaponControl weapon) {
        this.secondaryWeapon = weapon;
    }
    
    public AbstractWeaponControl getSecondaryWeapon() {
        return this.secondaryWeapon;
    }
    */
    
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        List<Element> slotList = XMLLoader.readElementList("slot", element);
        if (slotList.isEmpty()) {
            throw new RuntimeException("Slotlist for weaponsystem may not be empty!");
        }
        
        for (Element slotElement : slotList) {
            String type = XMLLoader.getStringValue(slotElement, "type", null);
            String defaultweapon = XMLLoader.getStringValue(slotElement, "defaultweapon", null);
            String parentId = XMLLoader.getStringValue(slotElement, "parent", null);
            float size = XMLLoader.getFloatValue(slotElement, "size", 1f);
            Vector3f position = XMLLoader.getVectorValue(slotElement, "position", null);
            Quaternion rotation = XMLLoader.getAngleValue(element, "rotation", null);            
            
            if (type == null) {
                throw new RuntimeException("Weaponslot needs a type (primary|secondary|perk)!");
            }
            if (defaultweapon == null) {
                throw new RuntimeException("Weaponslot needs a defaultweapon (primary|secondary|perk)!");
            }
            
            Spatial slotSpatial = new Node("weapon_" + type);
            if (position != null) {
                slotSpatial.setLocalTranslation(position);
            }
            if (rotation != null) {
                slotSpatial.setLocalRotation(rotation);
            }
            WeaponSlot slot = new WeaponSlot(slotSpatial, parentId, size);
            slot.setWeapon(this.space.getGame().getGamedataManager().loadWeapon(defaultweapon, this.space));
            if ("secondary".equalsIgnoreCase(type)) {
                this.secondarySlotList.add(slot);
            } else {
                this.primarySlotList.add(slot);
            }
        }
        
        /*
        String primary = XMLLoader.getStringValue(element, "primary", null);
        if ( primary != null ) {
            this.primaryWeapon = this.space.getGame().getGamedataManager().loadWeapon(primary, this.space);
            this.primaryEnergyPoints = XMLLoader.getIntValue(element, "primaryenergy", VAR_PRIMARY_ENERGYPOINTS);
        }
        String secondary = XMLLoader.getStringValue(element, "secondary", null);
        if ( secondary != null ) {
            this.secondaryWeapon = this.space.getGame().getGamedataManager().loadWeapon(secondary, this.space);
            this.maxSecondaryCount = XMLLoader.getIntValue(element, "secondarycount", VAR_SECONDARY_COUNT);
            this.secondaryCount = maxSecondaryCount;
        }
        */
    }

    public void changeWeapon(int slotNumber, String weaponName) {
        if (slotNumber < this.primarySlotList.size()) {
            WeaponSlot slot = this.primarySlotList.get(slotNumber);
            slot.setWeapon(this.space.getGame().getGamedataManager().loadWeapon(weaponName, this.space));
            return;
        }
        
        slotNumber -= this.primarySlotList.size();
        WeaponSlot slot = this.secondarySlotList.get(slotNumber);
        slot.setWeapon(this.space.getGame().getGamedataManager().loadWeapon(weaponName, this.space));
        return;
    }
    
    public void setWeaponColor(ColorRGBA color) {
        for (WeaponSlot slot : this.primarySlotList) {
            slot.setWeaponColor(color);
        }
        for (WeaponSlot slot : this.secondarySlotList) {
            slot.setWeaponColor(color);
        }
    }
        
}
