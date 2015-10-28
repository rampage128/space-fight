/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission;

import de.jlab.spacefight.AbstractClientControl;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.level.Level;
import de.jlab.spacefight.mission.condition.MissionCondition;
import de.jlab.spacefight.mission.hud.MissionHudControl;
import de.jlab.spacefight.mission.structures.DistressCall;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Flight;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.mission.structures.Respawn;
import de.jlab.spacefight.network.NetworkAppState;
import de.jlab.spacefight.ui.UIAppState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

/**
 *
 * @author rampage
 */
public abstract class AbstractMission {
    
    private Game _game;
    private SpaceAppState _space;
    
    private Level _level;
    
    private SimpleConfig config;
    private int clientFlights = 0;
    
    private int ticketCount = 0;
    
    private boolean initialized = false;
    
    private MissionHudControl missionHud;
    
    //private MissionHudItem missionHud;
    
    /* NOT SURE IF THE LOGICAL MAPS (BELOW) SHOULD BE DONE BY THE MISSION...
     * 
     * LOGICALLY THIS IS CORRECT BECAUSE THE MISSION ITSELF SOMEHOW DEFINES
     * FACTIONS, FLIGHTS AND OBJECTS... AND SPACE... SPACE IS JUST WHAT IT IS...
     * 
     * OTHERWHISE THE SPACE IS OUR CENTRAL CLASS AVAILABLE ALMOST IN EVERY CONTROL
     * ATTACHED TO THE SCENEGRAPH... SO IT WOULD PROBABLY BE SMARTER TO MOVE THIS
     * INTO SPACE AND MAKE IT ACCESSIBLE IN A CONTROLLED MANNER!
     * 
     */
    private HashMap<Integer, Faction> factionMap = new HashMap<Integer, Faction>();
    private HashMap<String, Flight> flightMap = new HashMap<String, Flight>();
    //private HashMap<String, Obje> objectInfoMap = new HashMap<String, Node>();
    private LinkedHashMap<String, ObjectInfoControl> objectMap = new LinkedHashMap<String, ObjectInfoControl>();
    
    private ArrayList<Respawn> respawnQueue = new ArrayList<Respawn>();
    private ArrayList<DistressCall> distressCalls = new ArrayList<DistressCall>();
    
    private HashMap<Class<? extends MissionCondition>, MissionCondition> conditionMap = new HashMap<Class<? extends MissionCondition>, MissionCondition>();
    
    private ArrayList<KillListener> killListeners = new ArrayList<KillListener>();
    
    public AbstractMission(SpaceAppState space, SimpleConfig config) {
        this._space = space;
        this.config = config;
    }
    
    public void addKillListener(KillListener listener) {
        this.killListeners.add(listener);
    }
    
    public void removeKillListener(KillListener listener) {
        this.killListeners.remove(listener);
    }
    
    private void notifyKillListeners(Kill kill) {
        for (KillListener listener : this.killListeners) {
            listener.onKill(kill);
        }
    }
    
    private void updateKillListeners(float tpf) {
        for (KillListener listener : this.killListeners) {
            listener.update(tpf);
        }
    }
    
    public void init() {        
        // LOAD OBJECTS FROM INFOS AND ASSIGN TO THEIR FLIGHT
        for ( ObjectInfoControl objectInfo : this.objectMap.values() ) {
            //objectInfo.loadObject(this._space);
            objectInfo.updateSpawnPosition(this, true);            
            
            /*
            String flightId = (String)objectInfo.getUserData("flight");
            
            if ( flightId != null ) {
                Flight flight = this.flightMap.get(flightId);
                Node object = flight.initObject(objectInfo, this, space, getGame());
                this.objectMap.put(object.getName(), object);
            }
            */
        }
        
        // INIT CLIENTS AND ASSIGN TO THEIR FLIGHT!
        int clientNum = 0;
        int clientCount = (int)Math.ceil(this.config.getFloatValue("maxclients", 32f) / (float)this.clientFlights);        
        for ( Flight flight : this.flightMap.values() ) {
            if ( "client".equalsIgnoreCase(flight.getType()) ) {
                for ( int i = 0; i < clientCount; i++ ) {
                    ObjectInfoControl clientShip = flight.getClient(this._space, this);
                    clientShip.setId("client_" + clientNum);
                    clientShip.setCallsign(flight.getName() + " " + (i+1));
                    this.addObject(clientShip);
                    //this.objectMap.put("client_" + clientNum, clientShip);
                    clientNum++;
                }
            }
        }
        
        // SPAWN OBJECTS & CLIENTS (WHICH ALSO RESETS THEIR TASKS)!
        spawnAllObjects();

        this.missionHud = new MissionHudControl(this._space);
        this._space.getSpace().addControl(this.missionHud);
        
        onInit();        
        this.initialized = true;
        //this.missionHud = new MissionHudItem(this, getGame(), getSpace());
        
        NetworkAppState.mapChanged(this._space.getGame(), config, false);
    }
            
    protected void spawnAllObjects() {
        for ( ObjectInfoControl objectInfo : this.objectMap.values() ) {
            //objectInfo.spawnObject(this, true);
            Respawn respawn = new Respawn(objectInfo, this, 0f, true);
            this.respawnQueue.add(respawn);
            //Flight flight = this.flightMap.get((String)object.getUserData("flight"));
            //flight.spawnObject(object, this, getSpace());
        }
    }
    
    private void updateRespawns(float tpf) {
        Respawn[] respawns = this.respawnQueue.toArray(new Respawn[0]);
        for (Respawn respawn : respawns) {
            if ( respawn.checkRespawn(tpf, this) ) {
                if (respawn.spawn(this)) {
                    this.respawnQueue.remove(respawn);
                    for ( MissionCondition condition : this.conditionMap.values() ) {
                        condition.onRespawn(respawn, this);
                    }
                }
            }
        }
    }
    
    private void updateDistressCalls(float tpf) {
        DistressCall[] calls = this.distressCalls.toArray(new DistressCall[0]);
        for (DistressCall call : calls) {
            call.update(tpf);
            if (call.getBroadcastTime() > 10) {
                this.distressCalls.remove(call);
            }
        }
    }
    
    public void kill(Kill kill) {
        // ADD DEATHS TO TARGET
        AbstractClientControl targetControl = kill.getTarget().getSpatial().getControl(AbstractClientControl.class);
        if (targetControl != null) {
            targetControl.addDeath(kill);
            Faction faction = kill.getTarget().getFaction();
            if (faction != null) {
                faction.addDeath(kill);
            }
        }
        
        // ADD KILLS TO ORIGIN
        AbstractClientControl originControl = kill.getOrigin().getSpatial().getControl(AbstractClientControl.class);
        if (originControl != null) {
            originControl.addKill(kill);
            originControl.addScore(100);
            Faction faction = kill.getOrigin().getFaction();
            if (faction != null) {
                faction.addKill(kill);
                faction.addScore(100);
            }
        }
        
        Flight flight = kill.getTarget().getFlight();
        if ( flight.getFaction().getAllowRespawn() ) {
            Respawn respawn = new Respawn(kill.getTarget(), this, 5f, false);
            this.respawnQueue.add(respawn);
        }
        for ( MissionCondition condition : this.conditionMap.values() ) {
            condition.onKill(kill, this);
        }
        
        /*
        KillHudItem item = (KillHudItem)this.missionHud.getItem(MissionHudControl.ITEM_KILLS);
        if (item != null) {
            item.addKill(kill);
        }
        */
        
        notifyKillListeners(kill);
    }
    
    /* UPDATE */
    public void update(float tpf) {
        if (!this.initialized) {
            return;
        }
        
        updateConditions(tpf);
        updateRespawns(tpf);
        updateDistressCalls(tpf);

        onUpdate(tpf);
        
        updateKillListeners(tpf);
        //this.missionHud.update(tpf);
    }
    
    public void destroy() {
        getSpace().getSpace().removeControl(this.missionHud);
        onDestroy();
        _level.cleanup();
    }
    
    /* ABSTRACT METHODS */
    protected abstract void onInit();
    protected abstract void onUpdate(float tpf);
    protected abstract void onDestroy();
    
    /* OBJECT HANDLING STUFF */
    public void addFaction(Faction faction) {
        factionMap.put(faction.getId(), faction);
    }
    
    public void addFlight(Flight flight) {
        flightMap.put(flight.getId(), flight);
        if ( "client".equalsIgnoreCase(flight.getType()) ) {
            this.clientFlights++;
        }
    }
    
    public Faction[] getFactions() {
        return this.factionMap.values().toArray(new Faction[0]);
    }
        
    public Flight[] getFlights(Faction faction, String type) {
        Collection<Flight> flights = this.flightMap.values();
        ArrayList<Flight> flightList = new ArrayList<Flight>();
        for ( Flight flight : flights ) {
            if ( flight.getFaction() == faction && (type == null || flight.getType().equalsIgnoreCase(type)) ) {
                flightList.add(flight);
            }
        }
        return flightList.toArray(new Flight[0]);
    }
       
    public void addObject(ObjectInfoControl objectInfo) {      
        this.objectMap.put(objectInfo.getId(), objectInfo);
    }
    
    public ObjectInfoControl getObject(String id) {
        return this.objectMap.get(id);
    }
    
    public ObjectInfoControl[] getObjects() {
        return this.objectMap.values().toArray(new ObjectInfoControl[0]);
    }
    
    public ObjectInfoControl[] getObjectsByType(String type) {
        ArrayList<ObjectInfoControl> resultList = new ArrayList<ObjectInfoControl>();
        for (ObjectInfoControl object : this.objectMap.values()) {
            if (type.equalsIgnoreCase(object.getObjectType())) {
                resultList.add(object);
            }
        }
        return resultList.toArray(new ObjectInfoControl[0]);
    }
    
    public ObjectInfoControl[] getClients() {
        ArrayList<ObjectInfoControl> clientList = new ArrayList<ObjectInfoControl>();
        for (ObjectInfoControl object : this.objectMap.values()) {
            if (object.getClient()) {
                clientList.add(object);
            }
        }
        return clientList.toArray(new ObjectInfoControl[0]);
    }
    
    public ObjectInfoControl getObjectForFlight(String flightId) {
        for (ObjectInfoControl objectInfo : this.objectMap.values()) {
            if (objectInfo.getFlight() != null && flightId.equalsIgnoreCase(objectInfo.getFlight().getId()) && objectInfo.isAlive())
                return objectInfo;
        }
        //System.out.println("COULD NOT FIND FLIGHT OBJECT FOR FLIGHT " + flightId);
        return null;
    }
    
    public Flight getFlight(String id) {
        return this.flightMap.get(id);
    }
    
    public Faction getFaction(int factionid) {
        return this.factionMap.get(factionid);
    }
        
    public boolean isInitialized() {
        return this.initialized;
    }
    
    /* CONDITIONHANDLING */
    public void addCondition(Class<? extends MissionCondition> conditionClass) {
        if ( this.conditionMap.containsKey(conditionClass) ) {
            return;
        }
        
        try {   
            MissionCondition condition = conditionClass.newInstance();
            condition.init(this);
            this.conditionMap.put(conditionClass, condition);
        } catch (InstantiationException ex) {
            Logger.getLogger(AbstractMission.class.getName()).log(java.util.logging.Level.SEVERE, "Cannot instantiate MissionCondition " + conditionClass.getSimpleName(), ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AbstractMission.class.getName()).log(java.util.logging.Level.SEVERE, "Cannot access MissionCondition " + conditionClass.getSimpleName(), ex);
        }
    }
    
    public <T extends MissionCondition> T getCondition(Class<T> conditionClass) {
        return (T)this.conditionMap.get(conditionClass);
    }
    
    private void updateConditions(float tpf) {
        for (MissionCondition condition : this.conditionMap.values()) {
            condition.update(tpf, this);
        }
    }
               
    /* GETTERS AND SETTERS */
    protected void setMaxClients(int maxClients) {
        this.config.setValue("maxclients", Integer.toString(maxClients));
    }
    
    public int getMaxClients() {
        return this.config.getIntValue("maxclients", 32);
    }
    
    public SimpleConfig getConfig() {
        return this.config;
    }
    
    public int getTicketCount() {
        return this.ticketCount;
    }
    
    public Game getGame() {
        return _game;
    }
    
    public SpaceAppState getSpace() {
        return _space;
    }
    
    protected void setLevel(Level level) {
        _level = level;
        _level.attach(_space);
    }
        
    public void end() {
        this.initialized = false;        
        if (this._space.isPlayable()) {
            for (MissionCondition condition : this.conditionMap.values()) {
                condition.setWinningFactions(this.getFactions());
            }
            UIAppState.gotoScreen("endgame", Game.get());
        } else {
            getSpace().getGame().gotoMainMenu();
        }
    }
        
    public void addDistressCall(DistressCall call) {
        this.distressCalls.add(call);
    }
    
    public DistressCall[] getDistressCalls(ObjectInfoControl object) {
        ArrayList<DistressCall> sortedDistressCalls = new ArrayList<DistressCall>();
        for (Iterator<DistressCall> dcit = this.distressCalls.iterator(); dcit.hasNext(); ) {
            DistressCall call = dcit.next();
            if (object == null || (call.getSource() != object && call.getFaction() == object.getFaction())) {
                sortedDistressCalls.add(call);
            }
        }
        return sortedDistressCalls.toArray(new DistressCall[0]);
    }
    
}
