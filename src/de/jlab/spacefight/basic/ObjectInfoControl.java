/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.ai.AIShipControl;
import de.jlab.spacefight.basic.camera.ChaseView;
import de.jlab.spacefight.gamedata.CacheableEntity;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import de.jlab.spacefight.level.PseudoRandomField;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.mission.structures.Flight;
import de.jlab.spacefight.mission.structures.Spawn;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.network.NetworkAppState;
import de.jlab.spacefight.player.Player;
import de.jlab.spacefight.player.PlayerControl;
import de.jlab.spacefight.scripting.ObjectScriptWrapper;
import de.jlab.spacefight.systems.BayControl;
import de.jlab.spacefight.systems.PhysicsControl;
import de.jlab.spacefight.systems.ShieldControl;
import de.jlab.spacefight.systems.SystemControl;
import de.jlab.spacefight.systems.TurretControl;
import de.jlab.spacefight.systems.TurretMasterControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.perks.PerkControl;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.weapon.AbstractWeaponControl;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public final class ObjectInfoControl extends AbstractControl implements XMLLoadable, CacheableEntity {
        
    public static final String TYPE_UNKNOWN     = null;
    public static final String TYPE_INTERCEPTOR = "interceptor";
    public static final String TYPE_FIGHTER     = "fighter";
    public static final String TYPE_BOMBER      = "bomber";
    public static final String TYPE_CAPITAL     = "capital";
           
    private String id = null;
    
    private boolean isClient;
    private Faction faction;
    private Flight flight;
    private String modelName;
    private String objectType;    
    private float size;
    private String callsign;
    private boolean alwaysvisible = false;
    
    private Spawn spawn;
    
    private SpaceAppState space;
        
    private LinkedHashMap<Class<? extends Control>, Control> controlMap = new LinkedHashMap<Class<? extends Control>, Control>();
                
    private PhysicsControl physics = null;
    private TargetInformation targetInfo;
    
    private Vector3f lastPosition = null;
    private Vector3f linearVelocity = new Vector3f(Vector3f.ZERO);
    
    private Player player = null;
    
    private ArrayList<Task> taskList = new ArrayList<Task>();
    private int currentTask = 0;
    
    private ObjectScriptWrapper script;
    
    private float disableTimer = 0f;
    
    public ObjectInfoControl(boolean isClient, Element element, SpaceAppState space, String path, GamedataManager gamedataManager) {
        this.space = space;
        this.isClient = isClient;
        this.loadFromElement(element, path, gamedataManager);
    }
    
    public ObjectInfoControl(boolean isClient, String modelName, String id) {
        this.isClient = isClient;
        this.modelName  = modelName;
        this.id         = id;
    }
            
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
        if (getSpatial() != null) {
            getSpatial().setName(id);
        }
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public String getCallsign() {
        return this.callsign;
    }
    
    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }
    
    public Faction getFaction() {
        return this.faction;
    }
    
    public void setFaction(Faction faction) {
        this.faction = faction;
        if (this.faction != null && this.faction.getColor() != null) {
            WeaponSystemControl weapons = getSpatial().getControl(WeaponSystemControl.class);
            if (weapons != null) {
                weapons.setWeaponColor(this.faction.getColor());
            }
            TurretMasterControl turretMaster = getSpatial().getControl(TurretMasterControl.class);
            if (turretMaster != null) {
                turretMaster.setWeaponColor(this.faction.getColor());
            }
            FlightControl flight = getSpatial().getControl(FlightControl.class);
            if (flight != null) {
                flight.setEngineColor(this.faction.getColor());
            }
            ShieldControl shield = getSpatial().getControl(ShieldControl.class);
            if (shield != null) {
                shield.setShieldColor(this.faction.getColor());
            }
        }
    }
    
    public Flight getFlight() {
        return this.flight;
    }
    
    public void setFlight(Flight flight) {
        this.flight = flight;
        if (flight != null) {
            this.spawn = flight.getSpawn();
            this.isClient = "client".equalsIgnoreCase(this.flight.getType());
        }
    }
    
    public boolean getClient() {
        return this.isClient;
    }

    public void setClient(boolean isClient) {
        this.isClient = isClient;
    }
    
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
    
    public String getObjectType() {
        return this.objectType;
    }
    
    public boolean getAlwaysVisible() {
        return this.alwaysvisible;
    }
    
    //public float getHullHP() {
        //return this.hull_hp;
    //}
    
    public float getSize() {
        return this.size;
    }
        
    public float getVolume() {
        BoundingVolume bound = getSpatial().getWorldBound();
        if ( bound == null ) {
            return 0f;
        }
        return bound.getVolume();
    }
    
    public Spawn getSpawn() {
        return this.spawn;
    }
    
    public void setSpawn(Spawn spawn) {
        this.spawn = spawn;
    }
    
    public boolean isAlive() {
        return getSpatial() != null && getSpatial().getParent() != null;
    }
            
    public void addObjectControl(Control control) {
        this.controlMap.put(control.getClass(), control);
        if (getSpatial() != null) {
            getSpatial().addControl(control);
        }
    }
    
    public <T extends Control> T getObjectControl(Class<T> controlClass) {
        T control = (T) this.controlMap.get(controlClass);
        if (control != null) {
            return control;
        }
        
        for (Control otherControl : this.controlMap.values()) {
            if (controlClass.isAssignableFrom(otherControl.getClass())) {
                return (T)otherControl;
            }
        }
        
        return null;
    }
    
    public void disable(float time) {
        this.disableTimer = time;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if (space != null && getCurrentTask() != null && getCurrentTask().isDone(getSpatial(), space.getMission())) {
            gotoNextTask();
        }
            
        if (this.targetInfo != null) {
            this.targetInfo.updateLocal();
        }
        
        if (this.lastPosition == null) {
            this.lastPosition = this.getPosition();
        }
        this.linearVelocity.set(getPosition()).subtractLocal(lastPosition);
        
        if (this.script != null) {
            this.script.update(tpf);
        }
        
        if (this.disableTimer > 0) {
            this.disableTimer -= tpf;
            
            if (this.disableTimer <= 0) {
                this.disableTimer = 0;
                for (Control control : this.controlMap.values()) {
                    if (control instanceof SystemControl) {
                        ((SystemControl)control).setEnabled(true);
                    }
                }
            } else {
                for (Control control : this.controlMap.values()) {
                    if (control instanceof SystemControl) {
                        ((SystemControl)control).setEnabled(false);
                    }
                }
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        ObjectInfoControl control = null;
        control = new ObjectInfoControl(this.isClient, this.modelName, this.id);
        control.setSpatial(spatial);
        return control;
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if ( spatial != null ) {           
            this.size = calcSize(getSpatial());
        }
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public void setPlayer(Player player) {
        // REMOVE OLD PLAYER IF NEEDED
        PlayerControl playerControl = getSpatial().getControl(PlayerControl.class);
        if (playerControl != null) {
            getSpatial().removeControl(playerControl);
        }
        
        this.player = player;
        if (player != null) {
            // REMOVE POSSIBLE OLD AI
            AIShipControl aiControl = getSpatial().getControl(AIShipControl.class);
            if (aiControl != null) {
                getSpatial().removeControl(aiControl);
            }
            
            if (player.isLocal()) {
                playerControl = new PlayerControl(this);
                getSpatial().addControl(playerControl);
                Game.get().getCameraManager().setTarget(this);
                Game.get().getCameraManager().setView(ChaseView.class);
            }
        } else {
            AIShipControl aiControl = new AIShipControl(this.space);
            getSpatial().addControl(aiControl);
        }
        /*
        // MAKE OLD SHIP AN AI SHIP AGAIN!
        if (this.player != null) {
            
            
            this.client.getSpatial().removeControl(this.control);
            AIShipControl aiControl = new AIShipControl(this.space);
            aiControl.copyTasks(this.control);
            this.client.getSpatial().addControl(aiControl);
        }
        
        // RETRIEVE OLD CONTROL OF NEW CLIENT
        if ( newClient != null ) {
            this.setFaction(newClient.getFaction());
            AbstractClientControl oldClientControl = newClient.getSpatial().getControl(AbstractClientControl.class);
            if ( oldClientControl != null ) {
                newClient.getSpatial().removeControl(oldClientControl);
            }
            if ( this.control == null )
                this.control = new PlayerControl(game, space);
            this.control.clearTasks();
            this.control.copyTasks(oldClientControl);
            newClient.getSpatial().addControl(this.control);
        }
                        
        // SET CLIENT
        this.client = newClient;
        */
    }
    
    public static float calcSize(Spatial spatial) {
        BoundingVolume bound = spatial.getWorldBound();
        if ( bound == null ) {
            return 0f;
        }
        
        if (bound.getType() == BoundingVolume.Type.Sphere) {
            return ((BoundingSphere)bound).getRadius();
        } else if (bound.getType() == BoundingVolume.Type.AABB) {
            BoundingBox bb = (BoundingBox)bound;
            return Math.max(bb.getXExtent(), Math.max(bb.getYExtent(), bb.getZExtent()));
        } else {
            return 0f;
        }
    }
           
    public ObjectInfoControl cloneObject(String id, boolean cloneMaterials) {
        Spatial newSpatial = getSpatial().clone(cloneMaterials);
        ObjectInfoControl newObject = newSpatial.getControl(ObjectInfoControl.class);

        newObject.setId(id);
        newSpatial.removeControl(newObject);
        newObject.controlMap.clear();
        for (int i = 0; i < newSpatial.getNumControls(); i++) {
            Control control = newSpatial.getControl(i);
            newObject.controlMap.put(control.getClass(), control);
            if (control instanceof PhysicsControl) {
                newObject.physics = (PhysicsControl)control;
            }
        }
        newSpatial.addControl(newObject);
        return newObject;
    }
    
    public void loadObject(SpaceAppState space) {
        if ( "field".equalsIgnoreCase(modelName) ) {
            if (NetworkAppState.generateField(space.getGame(), this)) {
                PseudoRandomField field = getSpatial().getControl(PseudoRandomField.class);
                field.createField(space);
            }
            return;
        }

        Node object = null;
        if (this.modelName != null && this.modelName.hashCode() != 0) {       
            object = (Node)space.getGame().getGamedataManager().loadModel(this.modelName);       
        } else {
            object = new Node(this.objectType);
        }
        
        for (Control control : this.controlMap.values()) {
            if (getSpatial() != null) {
                getSpatial().removeControl(control);
            }
            object.addControl(control);
            if (control instanceof PhysicsControl) {
                this.physics = (PhysicsControl)control;
            }
        }
        if (getSpatial() != null) {
            getSpatial().removeControl(this);
        }
        object.addControl(this);
    }
    
    public void remove() {
        if (this.isClient) {
            this.space.removeObject(this);
        } else {
            this.space.destroyObject(this.getSpatial());
        }
    }
    
    public void spawnObject() {
        spawnObject(null, true);
    }
    
    /**
     * Spawns the object
     * @param object
     * @param objectInfo
     * @param mission
     * @param space 
     */
    public boolean spawnObject(AbstractMission mission, boolean initial) {
        if (mission == null) {
            mission = this.space.getMission();
            if (mission == null) {
                throw new NullPointerException("No mission given and mission cannot be retrieved from space!");
            }
        }
        
        
        if (!spawn.canSpawn(mission)) {
            return false;
        }
        
        if (!spawn.setSpawnPosition(this, mission, initial)) {
            return false;
        }
        copyTasks(mission);
        mission.getSpace().addObject(this);
        /*
        Control physics = getSpatial().getControl(RigidBodyControl.class);
        if ( physics != null ) {
            mission.getSpace().getPhysicsSpace().remove(physics);
            mission.getSpace().getPhysicsSpace().add(physics);
        }
        */
        
        int numControls = getSpatial().getNumControls();
        for (int i = 0; i < numControls; i++) {
            Control control = getSpatial().getControl(i);
            if ( control instanceof SystemControl ) {
                ((SystemControl)control).resetSystem();
            }
        }
        //System.out.println("Spawning " + getSpatial().getName() + " @ " + getSpatial().getWorldTranslation());
        return true;
    }
    
    public void updateSpawnPosition(AbstractMission mission, boolean inFormation) {
        spawn.setSpawnPosition(this, mission, inFormation);
    }
    
    public float distanceTo(ObjectInfoControl otherObject) {
        if (otherObject == null || !this.isAlive() || !otherObject.isAlive()) {
            return Float.NaN;
        }
        
        return getPosition().distance(otherObject.getPosition());
    }
    
    
    // TASK HANDLING
    private void copyTasks(AbstractMission mission) {
        if ( this.flight == null ) {
            return;
        }
        
        //AbstractClientControl clientControl = getSpatial().getControl(AbstractClientControl.class);
        //if ( clientControl != null ) {
        clearTasks();
        Task[] tasks = this.flight.getTasks();
        for ( Task task : tasks ) {
            task.computeTargetObject(mission);
            addTask(task);
        }
        //}
    }
    /*
    public void copyTasks(AbstractClientControl otherControl) {
        this.taskList.clear();
        for ( Task task : otherControl.taskList ) {
            this.taskList.add(task);
        }
    }
    */
    
    public void addTask(Task task) {
        this.taskList.add(task);
    }

    public boolean hasMoreTasks() {
        return this.currentTask < this.taskList.size() - 1;
    }
    
    public int numTasks() {
        return this.taskList.size();
    }
    
    public void clearTasks() {
        this.taskList.clear();
    }
    
    public Task getCurrentTask() {
        if ( taskList.isEmpty() ) {
            return null;
        }
        if (this.currentTask < this.taskList.size()) {
            return this.taskList.get(this.currentTask);
        } else {
            return null;
        }
    }
    
    public int getCurrentTaskNum() {
        return this.currentTask;
    }
    
    public void gotoTask(int taskNum) {
        this.currentTask = taskNum;
    }
    
    public void gotoNextTask() {
        if ( hasMoreTasks() )
            this.currentTask++;
        else
            this.currentTask = 0;
    }
    
    public void gotoPreviousTask() {
        if ( this.currentTask > 0 )
            this.currentTask--;
    }
    
    // XML LOADING
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.modelName = XMLLoader.getStringValue(element, "model", null);
        this.objectType = XMLLoader.getStringValue(element, "type", "unknown");
        this.alwaysvisible = XMLLoader.getBooleanValue(element, "alwaysvisible", false);
        
        String scriptName = XMLLoader.getStringValue(element, "script", null);
        if (scriptName != null) {
            this.script = new ObjectScriptWrapper(this, this.space);
            gamedataManager.loadScript(path, scriptName, this.script);
        }
        
        Element controlRoot = element.getChild("controls");
        if ( controlRoot != null ) {
            List<Element> controlElements = controlRoot.getChildren();
            for ( Element controlElement : controlElements ) {
                Control control = null;

                if ("damage".equalsIgnoreCase(controlElement.getName())) {
                    DamageControl damageControl = new DamageControl(space);
                    damageControl.loadFromElement(controlElement, path, gamedataManager);
                    control = damageControl;
                } else if ("shield".equalsIgnoreCase(controlElement.getName())) {
                    ShieldControl shieldControl = new ShieldControl(space);
                    shieldControl.loadFromElement(controlElement, path, gamedataManager);
                    control = shieldControl;
                } else if ("sensor".equalsIgnoreCase(controlElement.getName())) {
                    SensorControl sensorControl = new SensorControl(space);
                    sensorControl.loadFromElement(controlElement, path, gamedataManager);
                    control = sensorControl;
                } else if ("flight".equalsIgnoreCase(controlElement.getName())) {
                    FlightControl flightControl = new FlightControl(this);
                    flightControl.loadFromElement(controlElement, path, gamedataManager);
                    control = flightControl;
                } else if ("physics".equalsIgnoreCase(controlElement.getName())) {
                    PhysicsControl physicsControl = new PhysicsControl();
                    physicsControl.loadFromElement(controlElement, path, gamedataManager);
                    control = physicsControl;
                } else if ("weapons".equalsIgnoreCase(controlElement.getName())) {
                    WeaponSystemControl weapons = new WeaponSystemControl(space);
                    weapons.loadFromElement(controlElement, path, gamedataManager);
                    control = weapons;
                } else if ("bays".equalsIgnoreCase(controlElement.getName())) {
                    BayControl bayControl = new BayControl();
                    bayControl.loadFromElement(controlElement, path, gamedataManager);
                    control = bayControl;
                } else if ("ai".equalsIgnoreCase(controlElement.getName())) {
                    AIShipControl aiControl = new AIShipControl(space);
                    control = aiControl;
                } else if ("turret".equalsIgnoreCase(controlElement.getName())) {
                    TurretControl turretControl = new TurretControl(space);
                    turretControl.loadFromElement(element, path, gamedataManager);
                    control = turretControl;
                } else if ("turrets".equalsIgnoreCase(controlElement.getName())) {
                    TurretMasterControl turretMasterControl = new TurretMasterControl(space);
                    turretMasterControl.loadFromElement(element, path, gamedataManager);
                    control = turretMasterControl;
                } else if ("perk".equalsIgnoreCase(controlElement.getName())) {
                    PerkControl perkControl = new PerkControl(this);
                    perkControl.loadFromElement(element, path, gamedataManager);
                    control = perkControl;
                }

                if ( control != null ) {
                    this.controlMap.put(control.getClass(), control);
                }
            }
        }
        
        if (this.isClient) {
            // RETREIVE PHYSICS
            PhysicsControl physics = getObjectControl(PhysicsControl.class);
            if ( physics == null ) {
                physics = new PhysicsControl();
            }
            
            // RETREIVE FLIGHT
            FlightControl flight = getObjectControl(FlightControl.class);
            if ( flight == null ) {
                flight = new FlightControl(this);
            }
            
            // RETREIVE WEAPONS
            WeaponSystemControl weapons = getObjectControl(WeaponSystemControl.class);
            if ( weapons == null ) {
                weapons = new WeaponSystemControl(space);
            }

            // RETREIVE SENSORS
            SensorControl sensors = getObjectControl(SensorControl.class);
            if ( sensors == null ) {
                sensors = new SensorControl(space);
            }
            sensors.setMaxRange(8000f);

            // RETREIVE DAMAGE CONTROL
            DamageControl damage = getObjectControl(DamageControl.class);
            if ( damage == null ) {
                damage = new DamageControl(space);
            }

            // RETREIVE SHIELDS
            ShieldControl shields = getObjectControl(ShieldControl.class);
            if ( shields == null ) {
                shields = new ShieldControl(space);
            }
            
            // RETREIVE PERK CONTROL
            PerkControl perkControl = getObjectControl(PerkControl.class);
            if ( perkControl == null ) {
                perkControl = new PerkControl(this);
            }

            AIShipControl ai = getObjectControl(AIShipControl.class);
            if ( ai == null ) {
                ai = new AIShipControl(space);
            }
            
            String shipType = XMLLoader.getStringValue(element, "type", "fighter");
            //weapons.setPrimaryWeapon(this.space.getGame().getGamedataManager().loadWeapon("laser", space));
            //weapons.setSecondaryWeapon(this.space.getGame().getGamedataManager().loadWeapon("missile", space));
            //computeCollision(getSpatial(), XMLLoader.getStringValue(element, "shape", "custom"), physics);
            if ("bomber".equalsIgnoreCase(shipType)) {
                damage.setHullHP(800f);
                flight.setAcceleration(20f);
                flight.setTopspeed(100f);
                flight.setTopspeedReverse(20f);
                flight.setTurnrate(200f);
                flight.setMomentum(110f);
                //flight.setAngulardamp(4f);
                //flight.setLineardamp(20f);
                physics.setMass(10000f);
                shields.setShieldStrength(800);
                shields.setRegenerationTime(18f);
                weapons.setPrimaryEnergyPoints(1800);
                weapons.setMaxPrimarySize(3);
                weapons.setMaxSecondarySize(10);
            } else if ("interceptor".equalsIgnoreCase(shipType)) {
                damage.setHullHP(300f);
                flight.setAcceleration(30f);
                flight.setTopspeed(160f);
                flight.setTopspeedReverse(20f);
                flight.setTurnrate(270f); // 4f
                flight.setMomentum(180f); // 8f
                //flight.setAngulardamp(6f);
                //flight.setLineardamp(50f);
                physics.setMass(6000f);
                shields.setShieldStrength(200);
                shields.setRegenerationTime(10f);
                weapons.setPrimaryEnergyPoints(1200);
                weapons.setMaxPrimarySize(1);
                weapons.setMaxSecondarySize(4);
            } else {
                damage.setHullHP(500f);
                flight.setAcceleration(25f);
                flight.setTopspeed(130f);
                flight.setTopspeedReverse(20f);
                flight.setTurnrate(230f);
                flight.setMomentum(170f);
                //flight.setAngulardamp(5f);
                //flight.setLineardamp(30f);
                physics.setMass(8000f);
                shields.setShieldStrength(500);
                shields.setRegenerationTime(14f);
                weapons.setPrimaryEnergyPoints(1500);
                weapons.setMaxPrimarySize(2);
                weapons.setMaxSecondarySize(6);
            }

            // CLEAR ALL CONTROLS AND ONLY READD THE WANTED!
            this.controlMap.clear();
            this.controlMap.put(physics.getClass(), physics);
            this.controlMap.put(flight.getClass(), flight);
            this.controlMap.put(weapons.getClass(), weapons);
            this.controlMap.put(sensors.getClass(), sensors);
            this.controlMap.put(damage.getClass(), damage);
            this.controlMap.put(shields.getClass(), shields);
            this.controlMap.put(perkControl.getClass(), perkControl);
            this.controlMap.put(ai.getClass(), ai);
        }
    
        //this.physics = physics;
        //this.loadObject(null);
    }
 
    /*
    public void updateClientControl(boolean isPlayer) {
        if (getClient()) {
            PlayerControl plControl = getSpatial().getControl(PlayerControl.class);
            AIShipControl aiControl = getSpatial().getControl(AIShipControl.class);
            NetworkClientControl netControl = getSpatial().getControl(NetworkClientControl.class);

            if (isPlayer) {
                //Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, "Client " + object + " is a player!");
                if (aiControl != null) {
                    getSpatial().removeControl(aiControl);
                }
                if (this.space.getPlayer().getClient() != this) {
                    if (getClient() && netControl == null) {
                        getSpatial().addControl(new NetworkClientControl(this.space.getMission()));
                    }
                } else {
                    if (plControl == null) {
                        this.space.getPlayer().setClient(this);
                    }
                }
            } else {
                //Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, "Client " + object + " is AI!");
                if (plControl != null) {
                    getSpatial().removeControl(plControl);
                }
                if (netControl != null) {
                    getSpatial().removeControl(netControl);
                }
                if (aiControl == null) {
                    getSpatial().addControl(new AIShipControl(this.space));
                }
            
            }
        }
    }
    */
    
    @Override
    public String toString() {
        String name = this.id;
        if (this.player != null) {
            name = this.player.getNickname();
        } else if (this.callsign != null) {
            name = this.callsign;
        }
        
        String control = "static";
        if (this.player != null && this.player.isLocal()) {
            control = "player";
        } else if (this.player != null) {
            control = "client";
        } else if (getSpatial().getControl(AIShipControl.class) != null) {
            control = "AI";
        } else {
            control = "empty";
        }
        
        return new StringBuilder(name).append(" (").append(control).append(")").toString();
    }
    
    public void setPosition(Vector3f position) {
       getSpatial().setLocalTranslation(position);
       if (this.physics != null) {
           this.physics.setPhysicsLocation(position);
       }
    }
    
    public void setRotation(Quaternion rotation) {
       getSpatial().setLocalRotation(rotation);
       if (this.physics != null) {
           this.physics.setPhysicsRotation(rotation);
       }
    }
    
    public void setLinearVelocity(Vector3f velocity) {
       if (this.physics != null) {
           this.physics.setLinearVelocity(velocity);
       }
    }
    
    public void setAngularVelocity(Vector3f velocity) {
       if (this.physics != null) {
           this.physics.setAngularVelocity(velocity);
       }
    }
    
    public Vector3f getPosition() {
        /*
        if (this.physics != null) {
            return this.physics.getPhysicsLocation();
        }
        */
        return this.spatial.getWorldTranslation();
    }
    
    public Quaternion getRotation() {
        /*
        if (this.physics != null) {
            return this.physics.getPhysicsRotation();
        }
        */
        return this.spatial.getWorldRotation();
    }
    
    public Vector3f getLinearVelocity() {
        if (this.physics != null) {
            return this.physics.getLinearVelocity();
        }
        return this.linearVelocity;
    }
    
    public Vector3f getAngularVelocity() {
        if (this.physics != null) {
            return this.physics.getLinearVelocity();
        }
        return Vector3f.ZERO;
    }
    
    public Vector3f getFacing() {
        if (this.physics != null) {
            return this.physics.getFacing();
        }
        return Vector3f.UNIT_Z;
    }
    
    public Vector3f getUpside() {
        if (this.physics != null) {
            return this.physics.getUpVector();
        }
        return Vector3f.UNIT_Y;
    }
    
    public TargetInformation getTargetInformation(ObjectInfoControl source, AbstractWeaponControl weapon) {
        if (this.targetInfo == null) {
            this.targetInfo = new TargetInformation(this);
        }
        //this.targetInfo.update(source, weapon);
        return this.targetInfo;
    }

    public CacheableEntity getInstance(String id) {
        return cloneObject(id, true);
    }
    
}
