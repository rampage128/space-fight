/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.structures;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.mission.structures.formation.Formation;
import de.jlab.spacefight.mission.structures.formation.ColumnFormation;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.mission.scripted.ScriptedMission;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public final class Flight implements XMLLoadable {
    
    private String id = null;
    
    private Faction faction;
    private String type;
    private String name = "Unknown";
    
    private Spawn spawn;
    //private String spawnObjectId;
    private String defaultClientShip;
    
    private ArrayList<Task> taskList = new ArrayList<Task>();
    //private Vector3f position = Vector3f.ZERO.clone();
    //private Quaternion rotation = Quaternion.ZERO.clone();

    private Formation formation = new ColumnFormation();
    
    private ArrayList<ObjectInfoControl> objectList = new ArrayList<ObjectInfoControl>();
    
    private int inFormation = 0;
        
    private AbstractMission mission;
    
    public Flight(Element element, Faction faction, AbstractMission mission, String path, GamedataManager gamedataManager) {
        this.mission = mission;
        this.faction = faction;
        loadFromElement(element, path, gamedataManager);
    }
    
    public Flight(String id, String type, Faction faction) {
        this.id = id;
        this.type = type;
        this.faction = faction;
    }
    
    public void addObject(ObjectInfoControl object) {
        this.objectList.add(object);
    }
    
    public ObjectInfoControl getObject(int index) {
        if ( this.objectList.size() < index )
            return null;
        return this.objectList.get(index);
    }
    
    public ObjectInfoControl[] getObjects() {
        return this.objectList.toArray(new ObjectInfoControl[0]);
    }
    
    public int getObjectCount() {
        return this.objectList.size();
    }
    
    public Faction getFaction() {
        return this.faction;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void addTask(Task task) {
        this.taskList.add(task);
    }
        
    public Task[] getTasks() {
        return this.taskList.toArray(new Task[0]);
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setDefaultClientShip(String defaultClientShip) {
        this.defaultClientShip = defaultClientShip;
    }
    
    public String getDefaultClientShip() {
        return this.defaultClientShip;
    }
    
    public void setSpawn(Spawn spawn) {
        this.spawn = spawn;
    }
    
    public Spawn getSpawn() {
        return this.spawn;
    }
        
    public String getId() {
        return this.id;
    }
        
    public void setFormation(Formation formation) {
        this.formation = formation;
    }
            
    public ObjectInfoControl getClient(SpaceAppState space, AbstractMission mission) {
        //Node clientShip = game.getGamedataManager().loadClientShip(this.getDefaultClientShip(), space, false);
        //ObjectInfoControl objectInfo = new ObjectInfoControl(this.getDefaultClientShip(), clientShip.getName(), this);
        //clientShip.addControl(objectInfo);
        
        ObjectInfoControl clientShip = space.getGame().getGamedataManager().loadObject(true, this.getDefaultClientShip(), "client", space);
        clientShip.setSpawn(spawn);
        clientShip.setFlight(this);
        clientShip.setFaction(faction);
        this.addObject(clientShip);
        return clientShip;
    }
    
    public Vector3f getFormationPointWorld(ObjectInfoControl unit) {
        int index = this.objectList.indexOf(unit);
        if (index < 0)
            return null;
        return this.formation.getPosition(index, this);
    }
    
    public ObjectInfoControl getFormationLeader(ObjectInfoControl unit) {
        int index = this.objectList.indexOf(unit);
        if (index < 0)
            return null;
        return this.formation.getLeader(index, this);
    }
    
    public void joinFormation() {
        if ( this.inFormation < this.objectList.size() - 1 )
            this.inFormation++;
    }
    
    public void leaveFormation() {
        if ( this.inFormation > 0 )
            this.inFormation--;
    }
    
    public boolean formationComplete() {
        return this.inFormation >= this.objectList.size() - 1;
    }
    
    @Override
    public String toString() {
        return this.name;
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.id     = XMLLoader.getStringValue(element, "id", "0");
        this.type   = XMLLoader.getStringValue(element, "type", "object");
                
        // SET UP NAME
        this.name = XMLLoader.getStringValue(element, "name", "Flight");
        
        // SET UP SPAWN
        Element spawnElement = element.getChild("spawn");       
        this.spawn = new Spawn(spawnElement, path, gamedataManager);
                        
        // SET UP FORMATION
        setFormation(Formation.createFormation(XMLLoader.getStringValue(element, "formation", "echelon")));        
        
        // READ OBJECT DATA
        if ( "object".equalsIgnoreCase(type) ) {
            ((ScriptedMission)mission).readObjectsXML(element, faction, this, path, gamedataManager);
        } else if ( "client".equalsIgnoreCase(type) ) {
            setDefaultClientShip(XMLLoader.getStringValue(element, "defaultship", "predator"));
        } else {
            throw new IllegalArgumentException("Unknown flight type " + type + " for flight " + id);
        }
        
        // SET UP TASKS
        List<Element> taskElementList = XMLLoader.readElementList("task", element);
        for (Element taskElement : taskElementList) {
            Task task = new Task(taskElement, path, gamedataManager);
            addTask(task);
        }
    }
    
}
