/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.perks;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.scripting.PerkScriptWrapper;
import org.jdom.Element;

/**
 * @Deprecated Use <code>Perk</code> instead!
 * @author rampage
 */
@Deprecated
public class ContinuousPerk {
    
    //private int energyPoints = 0;
    //private int usePoints = 0;
    
    private float loadTime      = 10f;
    private float cooldownTime  = 1f;
    private float runTime       = 5f;

    private boolean isEnabled   = false;
    private float energy        = 1f;
    private float cooldown      = 0f;
    
    private PerkScriptWrapper perkScript;
        
    public ContinuousPerk(ObjectInfoControl object, float runTime, float loadTime, float cooldown, String script) {
        this.runTime = runTime;
        this.loadTime = loadTime;
        this.cooldownTime = cooldown;
        //this.perkScript = new PerkScriptWrapper(this, object);
        //Game.get().getGamedataManager().loadScript(null, script, this.perkScript);
    }
    
    
    public void perkUse(PerkControl perkControl) {
        if (this.isEnabled) {
            this.disable(perkControl);
        } else {
            this.enable(perkControl);
        }
    }

    
    public void perkUpdate(PerkControl perkControl, float tpf) {
        if (this.isEnabled) {
            if (this.energy <= 0) {
                this.disable(perkControl);
                this.energy = 0;
                return;
            }
            
            //this.perkScript.updateActive(tpf);
            
            this.energy -= tpf / this.runTime;
        } else {
            this.energy += tpf / this.loadTime;
            if (this.energy > 1) {
                this.energy = 1;
            }
            if (this.cooldown > 0) {
                this.cooldown -= tpf;
            }
        }
    }

    protected void disable(PerkControl perkControl) {
        if (this.isEnabled) {
            this.cooldown = this.cooldownTime;
        }
        this.isEnabled = false;
        //this.perkScript.disable();
    }
    
    protected void enable(PerkControl perkControl) {
        this.isEnabled = true;
        //this.perkScript.enable();
    }

    public void loadFromElement(Element element, GamedataManager gamedataManager) {
        this.loadTime = XMLLoader.getFloatValue(element, "loadtime", this.loadTime);
        this.cooldownTime = XMLLoader.getFloatValue(element, "cooldown", this.cooldownTime);
        this.runTime = XMLLoader.getFloatValue(element, "runtime", this.runTime);
    }
        
}
