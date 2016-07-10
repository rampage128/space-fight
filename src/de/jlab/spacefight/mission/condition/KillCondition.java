/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.condition;

import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.mission.structures.Respawn;
import java.util.HashMap;

/**
 *
 * @author rampage
 */
public class KillCondition implements MissionCondition {

    private Faction leader = null;
    private int leaderKills = 0;
    private HashMap<Integer, Faction> killMap = new HashMap<Integer, Faction>();
       
    public void init(AbstractMission mission) {
        Faction[] factions = mission.getFactions();
        for ( Faction faction : factions ) {
            this.killMap.put(faction.getId(), faction);
        }
    }

    public void update(float tpf, AbstractMission mission) {

    }

    public void onRespawn(Respawn respawn, AbstractMission mission) {
        // NOTHING TO DO HERE
    }

    public void onKill(Kill kill, AbstractMission mission) {
        if (kill.getOrigin() != null && kill.getOrigin().getClient() && kill.getTarget().getClient() && kill.getOrigin().getFaction().getId() != kill.getTarget().getFaction().getId()) {
            Faction faction = this.killMap.get(kill.getOrigin().getFaction().getId());
            if (faction != null) {
                //faction.addKill(kill);
                if (faction.getKillCount() > this.leaderKills) {
                    this.leaderKills = faction.getKillCount();
                    this.leader = faction;
                }
            }
        }
    }
        
    public int getKills(int factionId) {
        Faction faction = this.killMap.get(factionId);
        if ( faction != null ) {
            return faction.getKillCount();
        }
        return 0;
    }
    
    public boolean checkLimit(int killLimit) {
        return this.leader != null && this.leader.getKillCount() >= killLimit;
    }
    
    public Faction getLeader() {
        return this.leader;
    }

    public void setWinningFactions(Faction[] factions) {
        int leadingKillCount = 0;
        for (Faction faction : factions) {
            leadingKillCount = Math.max(faction.getKillCount(), leadingKillCount);
        }
        
        for (Faction faction : factions) {
            faction.isWinner(faction.getKillCount() >= leadingKillCount);
        }
    }
    
}
