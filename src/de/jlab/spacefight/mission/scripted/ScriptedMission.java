/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.scripted;

import com.jme3.scene.Node;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.level.Level;
import de.jlab.spacefight.level.PseudoRandomField;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Flight;
import de.jlab.spacefight.mission.structures.Spawn;
import de.jlab.spacefight.scripting.MissionScriptWrapper;
import java.util.List;
import org.jdom.Element;

/**
 * The first dynamic mission implementation. 
 * It allows loading of XML-based scripted missions which contain more than
 * plain pew-pew-action.
 * 
 * For examples of XML-based missions look in "gamedata/base/missions/"
 * 
 * 
 * @author rampage
 */
public class ScriptedMission extends AbstractMission implements XMLLoadable {

    private String levelName = null;
    private String name = "Unknown";
    
    private MissionScriptWrapper script;   
       
    public ScriptedMission(SpaceAppState space, SimpleConfig config) {
        super(space, config);
    }
        
    @Override
    protected void onInit() {        
        // LOAD LEVEL
        setLevel(Level.loadLevel(levelName, getSpace()));
        
        // CALL SCRIPT INIT
        if ( this.script != null ) {
            this.script.onMissionInit();
        }        
    }

    @Override
    protected void onUpdate(float tpf) {
        // CALL SCRIPT UPDATE
        if ( this.script != null ) {
            this.script.onMissionUpdate();
        }
    }

    @Override
    protected void onDestroy() {
        // CALL SCRIPT END
        if ( this.script != null ) {
            this.script.onMissionDestroy();
            this.script.cleanup();
        }       
    }    

    /* GETTERS & SETTERS */
    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(String levelName) {
        this.levelName = levelName;
    }

    public void setScript(MissionScriptWrapper script) {
        this.script = script;
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        String scriptName = element.getChildTextTrim("script");
        if ( scriptName != null ) {
            MissionScriptWrapper wrapper = new MissionScriptWrapper(this);
            gamedataManager.loadScript(getConfig().getValue("path", null), scriptName, wrapper);
            setScript(wrapper);
        }
        setName(XMLLoader.getStringValue(element, "name", "Mission"));
        setLevel(XMLLoader.getStringValue(element, "level", "Mission"));
        
        List<Element> factionElementList = XMLLoader.readElementList("faction", element);
        for (Element factionElement : factionElementList) {
            Faction faction = new Faction(factionElement, this, path, gamedataManager);
            addFaction(faction);
        }
        
        readObjectsXML(element, null, null, path, gamedataManager);
    }
    
    public void readObjectsXML(Element parentElement, Faction faction, Flight flight, String path, GamedataManager gamedataManager) {
        List<Element> objectElementList = XMLLoader.readElementList("object", parentElement);
        for ( int i = 0; i < objectElementList.size(); i++ ) {
            Element objectElement = objectElementList.get(i);
            String id = XMLLoader.getStringValue(objectElement, "id", null);
            if (id == null) {
                throw new IllegalArgumentException("Object id is NULL in mission " + this.name + "(flight:" + flight + ")");
            }
            
            String objectName = XMLLoader.getStringValue(objectElement, "type", null);
                
            ObjectInfoControl objectInfo = null;
            if ( "field".equalsIgnoreCase(objectName) ) {
                objectInfo = new ObjectInfoControl(false, "field", id);
                PseudoRandomField field = new PseudoRandomField();
                field.loadFromElement(objectElement, path, gamedataManager);
                Node fieldNode = new Node("field");
                fieldNode.addControl(field);
                fieldNode.addControl(objectInfo);
                objectInfo.loadObject(getSpace());
            } else {
                objectInfo = gamedataManager.loadObject(false, objectName, id, getSpace());
                System.out.println("loaded object " + objectName);
            }
            
            Element spawnElement = objectElement.getChild("spawn");
            Spawn spawn = new Spawn(spawnElement, path, gamedataManager);
            objectInfo.setSpawn(spawn);

            objectInfo.setFlight(flight);
            if (faction == null) {
                int factionId = XMLLoader.getIntValue(objectElement, "faction", 0);
                if (factionId != 0) {
                    objectInfo.setFaction(getFaction(factionId));
                }
            } else {
                objectInfo.setFaction(faction);
            }
            if (flight != null) {
                if (objectInfo.getCallsign() == null) {
                    objectInfo.setCallsign(flight.getName() + " " + (i+1));
                }
                flight.addObject(objectInfo);
            }
            addObject(objectInfo);
        }
    }

}
