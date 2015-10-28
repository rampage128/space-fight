/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.condition;

import de.jlab.spacefight.mission.structures.Faction;

/**
 *
 * @author rampage
 */
public class TicketCounter {
    
    private Faction faction;
    private float ticketDrop = 0f;
    private float ticketCount = 0f;
    private int maxTickets = 500;
    
    public TicketCounter(Faction faction, int maxTickets) {
        this.faction = faction;
        this.maxTickets = maxTickets;
        this.ticketCount = maxTickets;
    }
    
    public void setTicketDrop(float ticketDrop) {
        this.ticketDrop = ticketDrop;
    }
    
    public void removeTickets(int ticketCount) {
        if ( ticketCount < 0 ) {
            ticketCount *= -1;
        }
        this.ticketCount -= ticketCount;
    }
    
    public void removeUnusedTickets() {
        // TODO WE NEED TO REMOVE ALL UNUSED TICKETS
        // UNUSED TICKETS ARE ALL TICKETS - ACTIVE CLIENTS IN FACTION
    }
    
    public void update(float tpf) {
        this.ticketCount = Math.min(this.maxTickets, Math.max(0, this.ticketCount - (1 * ticketDrop * tpf)));
        if ( this.ticketCount <= 0 ) {
            this.faction.setAllowRespawn(false);
            this.faction.isWinner(false);
        } else {
            this.faction.isWinner(true);
            this.faction.setAllowRespawn(true);
        }
        //System.out.println(this.faction.getId() + ": " + this.ticketCount);
    }
    
    public int getTicketCount() {
        return (int)Math.ceil(this.ticketCount);
    }
    
}
