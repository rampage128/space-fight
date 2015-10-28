/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.condition;

import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.mission.structures.Respawn;

/**
 *
 * @author rampage
 */
public interface MissionCondition {
 
    public void init(AbstractMission mission);
    public void update(float tpf, AbstractMission mission);    
    
    public void onRespawn(Respawn respawn, AbstractMission mission);
    public void onKill(Kill kill, AbstractMission mission);
    
    public void setWinningFactions(Faction[] factions);
    
}
