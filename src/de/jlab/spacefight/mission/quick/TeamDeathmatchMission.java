/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.quick;

import com.jme3.math.Vector3f;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.mission.condition.KillCondition;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Flight;
import de.jlab.spacefight.mission.structures.Task;

/**
 * Team deathmatch mission. Allows creating a quick mission with a variable client 
 * number and a variable team number. However clientnumber may be adjusted 
 * according to team number (for even player distribution).
 * 
 * A (non team) deathmatch-mission can be created by simply supplying equaling
 * client and team numbers to the constructor (@see de.jlab.spacefight.mission.quick.DeathmatchMission).
 * 
 * @author rampage
 */
public class TeamDeathmatchMission extends QuickMission {

    private int killLimit   = 20;
    private int teamCount   = 2;
    
    public TeamDeathmatchMission(SpaceAppState space, SimpleConfig config) {
        super(space, config);
    }
    
    @Override
    protected void onInitQuickMission() {        
        this.killLimit = getConfig().getIntValue("limit", 20);
        this.teamCount = Math.min(getMaxClients(), getConfig().getIntValue("teams", 2));
        
        int playersPerTeam = (int)Math.floor((float)getMaxClients() / (float)this.teamCount);
        this.setMaxClients(playersPerTeam * this.teamCount);
        
        for ( int i = 0; i < this.teamCount; i++ ) {
            String name = "team " + (i+1);
            
            Faction faction = new Faction(i, name, computeRandomColor());
            Flight flight = new Flight(name, "client", faction);
            flight.setDefaultClientShip("predator");
            computeRandomSpawnPoint(flight);
            
            flight.getSpawn().lookAt(Vector3f.ZERO);
            Task task = new Task("patrol");
            task.setPosition(Vector3f.ZERO);
            flight.addTask(task);
            addFaction(faction);
            addFlight(flight);
            for ( int j = 0; j < playersPerTeam; j++ ) {
                ObjectInfoControl clientShip = flight.getClient(getSpace(), this);
                clientShip.setId(name + " " + (j+1));
                clientShip.setFlight(flight);
                clientShip.setFaction(faction);
                addObject(clientShip);
            }
        }
                                
        addCondition(KillCondition.class);
    }

    @Override
    protected void onUpdate(float tpf) {
        if (getCondition(KillCondition.class).checkLimit(this.killLimit)) {
            //getCondition(KillCondition.class).getLeader();
            this.end();
        }
    }

    @Override
    protected void onDestroy() {
        
    }
    
}
