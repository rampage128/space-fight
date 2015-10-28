/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.structures;

import com.jme3.math.ColorRGBA;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.mission.AbstractMission;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public final class Faction implements XMLLoadable {
        
    private int id = 0;
    private String name = "Unknown";
    private ArrayList<Kill> killList = new ArrayList<Kill>();
    private ArrayList<Kill> deathList = new ArrayList<Kill>();
    
    private ColorRGBA color = ColorRGBA.White;
    
    private boolean allowRespawn = true;
        
    private int score;
    private boolean winner;
    
    private AbstractMission mission;
        
    public Faction(Element element, AbstractMission mission, String path, GamedataManager gamedataManager) {
        this.mission = mission;
        loadFromElement(element, path, gamedataManager);
    }
    
    public Faction(int id, String name, ColorRGBA color) {
        this.id             = id;
        this.name           = name;
        this.color          = color;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isWinner() {
        return this.winner;
    }
    
    public void isWinner(boolean winner) {
        this.winner = winner;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    public void setAllowRespawn(boolean allowRespawn) {
        this.allowRespawn = allowRespawn;
    }
    
    public boolean getAllowRespawn() {
        return this.allowRespawn;
    }
        
    public void addScore(int score) {
        this.score += score;
    }
    
    public int getScore() {
        return this.score;
    }
    
    public ColorRGBA getColor() {
        return this.color;
    }
    
    public void addKill(Kill kill) {
        this.killList.add(kill);
    }
    
    public int getKillCount() {
        return this.killList.size();
    }
    
    public void addDeath(Kill kill) {
        this.deathList.add(kill);
    }
    
    public int getDeathCount() {
        return this.deathList.size();
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        int id = XMLLoader.getIntValue(element, "id", 0);
        String name = XMLLoader.getStringValue(element, "name", "Faction");
        ColorRGBA color = XMLLoader.getColorValue(element, "color", null);

        this.id = id;
        this.name = name;
        this.color = color;
               
        List<Element> flightElementList = XMLLoader.readElementList("flight", element);
        for (Element flightElement : flightElementList) {
            Flight flight = new Flight(flightElement, this, mission, path, gamedataManager);
            mission.addFlight(flight);
        }
    }
    
}
