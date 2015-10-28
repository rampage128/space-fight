/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.quick;

import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.gamedata.SimpleConfig;

/**
 * Simple deathmatch with a client count and a kill limit.
 * 
 * It uses teamdeathmatch as base and just sets the number of teams equal to the
 * number of clients.
 * 
 * @author rampage
 */
public class DeathmatchMission extends TeamDeathmatchMission {
            
    public DeathmatchMission(SpaceAppState space, SimpleConfig config) {
        super(space, config);
    }

}
