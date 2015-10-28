/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.scripting;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.mission.condition.KillCondition;
import de.jlab.spacefight.mission.condition.MissionCondition;
import de.jlab.spacefight.mission.condition.TicketCondition;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Flight;

/**
 *
 * @author rampage
 */
public class MissionScriptWrapper extends ScriptWrapper {
    
    private AbstractMission mission;
    
    public MissionScriptWrapper(AbstractMission mission) {
        this.mission = mission;
        this.addReference("TicketCondition", TicketCondition.class);
        this.addReference("KillCondition", KillCondition.class);
    }
    
    public void onMissionInit() {
        invoke("onInit");
    }
    
    public void onMissionUpdate() {
        invoke("onUpdate");
    }
    
    public void onMissionDestroy() {
        invoke("onDestroy");
    }
        
    public boolean isFlightDestroyed(String flightid) {
        Flight flight = this.mission.getFlight(flightid);
        for (ObjectInfoControl object : flight.getObjects()) {
            if (object.isAlive()) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isObjectDestroyed(String objectid) {
        ObjectInfoControl object = this.mission.getObject(objectid);
        return object == null || !object.isAlive();
    }
    
    public void endMission() {
        mission.end();
    }
    
    public void addFactionScore(int factionid, int score) {
        Faction faction = this.mission.getFaction(factionid);
        faction.addScore(score);
    }
    
    // TODO THIS IS SLOW... NO PROBLEM IF EXECUTED ONCE A WHILE... BUT BAD FOR REALTIME!
    public int getActiveClientFlights(int factionid) {
        Faction faction = this.mission.getFaction(factionid);
        Flight[] flights = this.mission.getFlights(faction, "client");
        int count = 0;
        for (Flight flight : flights) {
            for (ObjectInfoControl object : flight.getObjects()) {
                if (object.isAlive()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /*
    public void setFactionTicketDrop(int factionid, float ticketDrop) {
        Faction faction = this.mission.getFaction(factionid);
        faction.setTicketDrop(ticketDrop);
    }
    */
    
    public void addCondition(Class<? extends MissionCondition> conditionClass) {
        this.mission.addCondition(conditionClass);
    }
    
    public MissionCondition getCondition(Class<? extends MissionCondition> conditionClass) {
        return this.mission.getCondition(conditionClass);
    }
    
    public void playMusic(String name, float volume, boolean loop) {
        this.mission.getSpace().getGame().getAudioManager().setMusicVolume(volume);
        this.mission.getSpace().getGame().getAudioManager().playMusic(name, 0, loop);
    }
    
    @Override
    public MissionScriptWrapper clone() {
        MissionScriptWrapper newScript = new MissionScriptWrapper(this.mission);
        newScript.init(new JScript(this.script.getName(), this.script.getCode(), null, this.script.isCritical(), Game.get().getGamedataManager()));
        return newScript;
    }
    
}
