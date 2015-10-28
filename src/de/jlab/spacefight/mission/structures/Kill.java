/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.structures;

/**
 *
 * @author rampage
 */
public class Kill extends DamageInformation {
    
    public Kill(DamageInformation damage) {
        super(damage.getDamageType(), damage.getDamageName(), damage.getDamage(), damage.getOrigin(), damage.getTarget(), damage.getDirection(), damage.getWorldPosition());
    }
    
}
