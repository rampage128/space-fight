/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.input;

/**
 *
 * @author rampage
 */
public enum ShipInput implements DisplayableEnum.Display {
    FORWARD, 
    BACKWARD, 
    ROLLLEFT, 
    ROLLRIGHT, 
    ELEVATORUP, 
    ELEVATORDOWN, 
    RUDDERLEFT, 
    RUDDERRIGHT, 
    STRAFELEFT,
    STRAFERIGHT,
    LIFTUP,
    LIFTDOWN,
    
    CRUISE,
    THROTTLE100,
    THROTTLE66,
    THROTTLE33,
    THROTTLE0,
    GLIDE,
    
    PERK_USE,
    
    WEAPON_PRIMARY,
    WEAPON_PRIMARYMODE,
    WEAPON_SECONDARY,
    WEAPON_NEXTSECONDARY,
    TARGET_NEXTENEMY,
    TARGET_NEXT,
    TARGET_PREVIOUS,
    STEERING_DEBUG,
    TARGET_SMART,
    COM_OFFENSIVE,
    COM_DEFENSIVE;

    public String getDisplayValue() {
        return this.toString();
    }
}
