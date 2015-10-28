/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.perks;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.scripting.PerkScriptWrapper;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public final class Perk implements XMLLoadable {
    
    public static final String TYPE_ONCE        = "once";
    public static final String TYPE_CONTINUOUS  = "continuous";
    
    // GENERAL VARIABLES
    private String name                     = "Unknown Perk";
    private String type                     = TYPE_ONCE;
    private float cooldownTime              = 1f;
    
    private float cooldown                  = 0f;
    private PerkScriptWrapper perkScript    = null;
    private boolean isContinuous            = false;
        
    // CONTINUOUS PERK VARIABLES
    private float loadTime      = 10f;
    private float runTime       = 5f;
    
    private boolean isEnabled   = false;
    private float energy        = 1f;    
        
    public Perk(Element element, String path, GamedataManager gamedataManager) {
        this.loadFromElement(element, path, gamedataManager);
    }
    
    public void perkUse(ObjectInfoControl origin) {
        if (this.cooldown > 0) {
            return;
        }
        
        if (isContinuous) {
            if (this.isEnabled) {
                this.disable(origin);
            } else {
                this.enable(origin);
            }
        } else {
            this.perkScript.use(origin);
            this.cooldown = this.cooldownTime;
        }
    }

    public void perkUpdate(float tpf, ObjectInfoControl origin) {
        if (this.isContinuous) {
            if (this.isEnabled) {
                if (this.energy <= 0) {
                    this.disable(origin);
                    this.energy = 0;
                    return;
                }

                this.perkScript.updateActive(tpf, origin);

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
        } else {
            if (this.cooldown > 0) {
                this.cooldown -= tpf;
            }
        }
        
        if (this.cooldown < 0) {
            this.cooldown = 0;
        }
    }
    
    public void perkReset(ObjectInfoControl origin) {
        this.cooldown = 0;
        this.energy = 1;
        this.isEnabled = false;
        this.perkScript.reset(origin);
    }
    
    public void perkAdded(ObjectInfoControl origin) {
        this.perkScript.add(origin);
    }
    
    public void perkRemoved(ObjectInfoControl origin) {
        this.perkScript.remove(origin);
    }

    private void disable(ObjectInfoControl origin) {
        if (this.isEnabled) {
            this.cooldown = this.cooldownTime;
        }
        this.isEnabled = false;
        this.perkScript.disable(origin);
    }
    
    private void enable(ObjectInfoControl origin) {
        this.isEnabled = true;
        this.perkScript.enable(origin);
    }

    public String getName() {
        return this.name;
    }
    
    public float getEnergy() {
        return this.energy;
    }
    
    public float getCooldown() {
        return this.cooldown;
    }
    
    public float getCooldownTime() {
        return this.cooldownTime;
    }
    
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.name = XMLLoader.getStringValue(element, "name", this.name);
        
        this.type = XMLLoader.getStringValue(element, "type", this.type);
        this.isContinuous = this.type.equalsIgnoreCase(TYPE_CONTINUOUS);

        this.cooldownTime = XMLLoader.getFloatValue(element, "cooldown", this.cooldownTime);
        
        // CONTINUOUS PERK INIT
        this.loadTime = XMLLoader.getFloatValue(element, "loadtime", this.loadTime);
        this.runTime = XMLLoader.getFloatValue(element, "runtime", this.runTime);
        
        String scriptName = XMLLoader.getStringValue(element, "script", null);
        if (scriptName != null) {
            this.perkScript = new PerkScriptWrapper(this);
            Game.get().getGamedataManager().loadScript(path, scriptName, this.perkScript);
        }
        
        // PROPERTIES!!!
        Element propertiesElement = element.getChild("properties");
        if (propertiesElement != null) {
            SimpleConfig properties = this.perkScript.getProperties();
            List<Element> propertyElements = propertiesElement.getChildren();
            for (Element propertyElement : propertyElements) {
                properties.setValue(propertyElement.getName(), propertyElement.getText());
            }
        }
        
    }        
    
}
