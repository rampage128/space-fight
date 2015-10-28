/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.perks;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.systems.SystemControl;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public class PerkControl extends AbstractControl implements SystemControl, XMLLoadable {

    private ObjectInfoControl origin;
    private Perk perk;
    
    public PerkControl(ObjectInfoControl origin) {
        this.origin = origin;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if (this.perk != null) {
            this.perk.perkUpdate(tpf, this.origin);
        }
    }
    
    public void usePerk() {
        if (this.perk != null) {
            this.perk.perkUse(this.origin);
        }
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        PerkControl control = new PerkControl(this.origin);
        control.setSpatial(spatial);
        control.setPerk(this.perk);
        return control;
    }

    public void resetSystem() {
        if (this.perk != null) {
            this.perk.perkReset(this.origin);
        }
    }
    
    public void setPerk(Perk perk) {
        if (this.perk != null) {
            this.perk.perkRemoved(this.origin);
        }
        this.perk = perk;
        if (this.perk != null) {
            this.perk.perkAdded(this.origin);
        }
    }
    
    public Perk getPerk() {
        return this.perk;
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        String defaultPerkName = XMLLoader.getStringValue(element, "defaultperk", null);
        if (defaultPerkName != null) {
            setPerk(gamedataManager.loadPerk(defaultPerkName));
        }
    }
    
}
