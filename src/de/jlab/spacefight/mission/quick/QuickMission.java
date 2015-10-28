/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.quick;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.level.Level;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.mission.structures.Flight;
import de.jlab.spacefight.mission.structures.Spawn;
import java.util.Random;

/**
 * Generic class to supply a handler for quick missions.
 * 
 * It provides basics like level-loading and helper methods to generate random
 * spawnpoints etc...
 * 
 * @author rampage
 */
public abstract class QuickMission extends AbstractMission {
    
    private Random randomSpawnGenerator = new Random();
        
    public QuickMission(SpaceAppState space, SimpleConfig config) {
        super(space, config);
    }
    
    protected ColorRGBA computeRandomColor() {
        Random random = new Random();
        return new ColorRGBA(((random.nextInt(101)) / 100f), ((random.nextInt(101)) / 100f), ((random.nextInt(101)) / 100f), 1f);
    }
    
    protected void computeRandomSpawnPoint(Flight flight) {
        Vector3f position = new Vector3f(randomSpawnGenerator.nextInt(7000) - 3500, randomSpawnGenerator.nextInt(7000) - 3500, randomSpawnGenerator.nextInt(7000) - 3500);
        Quaternion rotation = new Quaternion();
        rotation.lookAt(position.negate(), Vector3f.UNIT_Z);
        flight.setSpawn(new Spawn(position, rotation));
    }
    
    @Override
    protected void onInit() {
        setLevel(Level.loadLevel(getConfig().getValue("level", null), getSpace()));
        onInitQuickMission();
        spawnAllObjects();
    }
    
    protected abstract void onInitQuickMission();
    
}
