/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems;

import com.jme3.math.ColorRGBA;
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
import de.jlab.spacefight.systems.sensors.TargetInformation;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public class TurretMasterControl extends AbstractControl implements SystemControl, XMLLoadable {

    private SpaceAppState space;
    
    private ArrayList<TurretControl> turrets = new ArrayList<TurretControl>();
    
    public TurretMasterControl(SpaceAppState space) {
        this.space = space;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        // NOTHING TO DO HERE ?!
        /*
        for ( TurretControl turret : turrets ) {
            SensorControl sensors = getSpatial().getControl(SensorControl.class);
            if ( sensors != null && turret.getTarget() == null ) {
                turret.setTarget(sensors.getCurrentTarget());
            }
            //turret.update(tpf);
        }
        */
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    public Control cloneForSpatial(Spatial spatial) {
        TurretMasterControl control = new TurretMasterControl(this.space);
        control.setSpatial(spatial);
        return control;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if ( spatial != null ) {            
            // FIND TURRETS
            Spatial weaponChild = ((Node)spatial).getChild("turrets");
            if ( weaponChild != null ) {
                List<Spatial> childList = ((Node)weaponChild).getChildren();
                for ( int i = 0; i < childList.size(); i++ ) {
                    Spatial child = childList.get(i);
                    if ( "turret".equalsIgnoreCase(child.getName()) ) {
                        ObjectInfoControl turretInfo = this.space.getGame().getGamedataManager().loadObject(false, "laserturret", getSpatial().getName() + "_turret_" + i, space);
                        
                        float angle = (Float)child.getUserData("angle");                        
                        TurretControl turret = turretInfo.getObjectControl(TurretControl.class);
                        if (turret != null) {
                            turret.setAngle(angle);
                        }

                        ((Node)child).attachChild(turretInfo.getSpatial());
                        
                        PhysicsControl physics = turretInfo.getObjectControl(PhysicsControl.class);
                        if ( physics != null ) {
                            physics.setApplyPhysicsLocal(false);
                            physics.setPhysicsLocation(child.getWorldTranslation());
                            physics.setPhysicsRotation(child.getWorldRotation());
                            
                            this.space.getPhysics().getPhysicsSpace().add(physics);
                        }
                                                
                        //this.space.getMission().addObject(turretInfo);
                        this.turrets.add(turret);
                    }
                }
            }  
        } else {
            // CLEAR TURRETS
            for (TurretControl turret : this.turrets) {
                space.destroyObject(turret.getSpatial());
            }
            this.turrets.clear();
        }
    }
    
    public void resetSystem() {
        
    }
    
    public void assignTarget(TargetInformation target) {
         for ( TurretControl turret : turrets ) {
            if ( turret.getTarget() == null && turret.canTarget(target) ) {
                turret.setTarget(target);
            }
        }
    }
    
    public void setWeaponColor(ColorRGBA color) {
        for (TurretControl turret : this.turrets) {
            turret.setWeaponColor(color);
        }
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        // TODO LET IT READ THE TURRETS FROM XML!
        List<Element> turretElements = XMLLoader.readElementList("turret", element);
        for (Element turretElement : turretElements) {
            String turretType = XMLLoader.getStringValue(element, "type", null);
        }
    }
            
}
