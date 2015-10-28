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
public class TicketCondition implements MissionCondition {

    private int survivingFactionCount = 0;
    private float tension = 0;
    private HashMap<Integer, TicketCounter> ticketMap = new HashMap<Integer, TicketCounter>();
    
    public void init(AbstractMission mission) {
        Faction[] factions = mission.getFactions();
        for ( Faction faction : factions ) {
            TicketCounter tc = new TicketCounter(faction, 500);
            this.ticketMap.put(faction.getId(), tc);
        }
        this.survivingFactionCount = factions.length;
    }
    
    public void update(float tpf, AbstractMission mission) {
        survivingFactionCount = 0;
        float potentialTension = 0;
        for ( Integer faction : this.ticketMap.keySet() ) {
            TicketCounter tc = this.ticketMap.get(faction);
            tc.update(tpf);
            if ( tc.getTicketCount() > 0 ) {
                survivingFactionCount++;
                if (tc.getTicketCount() <= 30) {
                    potentialTension = 1 - tc.getTicketCount() / 30f;
                }
            }
        }
        
        if (this.survivingFactionCount < 3 && potentialTension > 0) {
            this.tension = potentialTension;
        }
    }
    
    public int getTickets(int factionId) {
        TicketCounter tc = this.ticketMap.get(factionId);
        if ( tc == null ) {
            return -1;
        }
        return tc.getTicketCount();
    }
    
    public int survivingFactions() {
        return this.survivingFactionCount;
    }
    
    public float tension() {
        return this.tension;
    }
    
    public void removeTickets(int factionid, int count) {
        TicketCounter tc = this.ticketMap.get(factionid);
        if ( tc != null ) {
            tc.removeTickets(count);
        }
    }
    
    public void setFactionTicketDrop(int factionid, float ticketDrop) {
        TicketCounter tc = this.ticketMap.get(factionid);
        if ( tc != null ) {
            tc.setTicketDrop(ticketDrop);
        }
    }

    public void onRespawn(Respawn respawn, AbstractMission mission) {
        
    }
    
    public void onKill(Kill kill, AbstractMission mission) {
        //ObjectInfoControl objectInfo = kill.getTarget().getControl(ObjectInfoControl.class);
        if (kill.getTarget() != null && kill.getTarget().getClient() && kill.getTarget().getFaction() != null) {
            int factionid = (Integer)kill.getTarget().getFaction().getId();
            TicketCounter tc = this.ticketMap.get(factionid);
            if ( tc != null ) {
                tc.removeTickets(1);
            }
        }
    }

    public void setWinningFactions(Faction[] factions) {
        for (Faction faction : factions) {
            faction.isWinner(getTickets(faction.getId()) > 0);
        }
    }
    
}
