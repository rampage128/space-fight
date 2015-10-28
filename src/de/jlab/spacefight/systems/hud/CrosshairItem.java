/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.hud;

import de.jlab.spacefight.ui.ingame.hud.HudItem;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import de.jlab.spacefight.weapon.EnergyWeaponControl;

/**
 *
 * @author rampage
 */
public class CrosshairItem extends HudItem {

    private Vector3f crosshairVector = new Vector3f();
    
    public CrosshairItem(SpaceAppState space) {
        super("crosshair", space);
        
        scale(0.07f);
        
        Picture crosshair = new Picture("crosshair");
        crosshair.setImage(getSpace().getGame().getAssetManager(), "ui/hud/crosshair.png", true);
        addPicture("crosshair", crosshair);
        scalePicture("crosshair", 1f, 1f);
        showPicture("crosshair");
    }
    
    @Override
    public void updateItem(float tpf, Spatial spatial) {
        WeaponSystemControl weapons = spatial.getControl(WeaponSystemControl.class);
        if ( weapons != null ) {
            TargetInformation target = weapons.getTarget();

            crosshairVector.set(spatial.getWorldRotation().mult(Vector3f.UNIT_Z));
            if ( target == null ) {
                crosshairVector.multLocal(EnergyWeaponControl.MAX_RANGE);
            } else {
                crosshairVector.multLocal(target.getDirection(spatial.getControl(ObjectInfoControl.class)).length());
            }
            crosshairVector.addLocal(spatial.getWorldTranslation());

            getCamera().getScreenCoordinates(crosshairVector, this.crosshairVector);
            if ( this.crosshairVector.z < 1 ) {
                showPicture("crosshair");
                move(crosshairVector.getX() / getCamera().getWidth(), crosshairVector.getY() / getCamera().getHeight(), 0.5f, 0.5f);            
            } else {
                hidePicture("crosshair");
            }
        } else {
            hidePicture("crosshair");
        }
    }
 
    @Override
    public void initItem(Spatial spatial) {
        // NOTHING SPECIAL TO DO HERE
    }

    @Override
    public void cleanupItem(Spatial spatial) {
        // NOTHING SPECIAL TO DO HERE
    }

    @Override
    public void renderItem(Spatial spatial, RenderManager rm, ViewPort vp) {
        
    }
    
}
