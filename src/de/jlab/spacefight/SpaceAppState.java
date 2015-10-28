/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import de.jlab.spacefight.debug.SpaceDebugger;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.InputListener;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.control.LightControl;
import de.jlab.spacefight.audio.AudioControl;
import de.jlab.spacefight.basic.DamageControl;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.effect.stardust.StarDustEmitter;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.input.InputManager;
import de.jlab.spacefight.input.ViewInput;
import de.jlab.spacefight.level.SkyControl;
import de.jlab.spacefight.mission.AbstractMission;
import de.jlab.spacefight.mission.quick.DeathmatchMission;
import de.jlab.spacefight.mission.structures.DamageInformation;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.player.Player;
import de.jlab.spacefight.systems.PhysicsControl;
import de.jlab.spacefight.ui.UIAppState;
import de.jlab.spacefight.weapon.AbstractWeaponControl;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Space... The final frontier. 
 * This is our physical environment where everything takes place.
 * 
 * Here all nodes are maintained and objects are added or removed.
 * There actually IS a special sheme which every Node added has to follow.
 * 
 * Currently there are some nodes which contain special objects:
 * 
 * spacenode: is the main node of the physical flyable space.
 *            (contains objectlist and weaponlist)
 * objectlist: is the node for all objects (ships, stations etc.) which are inside the scene.
 *             (this is for targeting and stuff)
 * weaponlist: is the node for all weapons and warheads 
 *             (yes these are maintained seperate!)
 * debugnode: some temporary debug shit 
 *            (the whole debugging stuff should be outsourced to another class)
 * skynode: is the node for all objects in the sky. They are automatically attached by the loaded level. 
 *          (The sky is always unreachable for the player and rendered in the background no matter how far away!)
 * uinode: a wrapper for all ui elements which is added to the engines guinode 
 *         (this is to avoid spamming of the engines uinode with lots of stuff).
 * 
 * In the future we need to organize this tree a little deeper to make some things
 * easyer in terms of CPU-usage. But please be aware that all game logic relys on a
 * certain layout of this tree! So any change must be tested extremely well!
 * 
 * @author rampage
 */
public class SpaceAppState extends AbstractAppState {

    // GAME OBJECT FOR ACCESS
    private Game game;

    // OWN INPUT MANAGER FOR INPUT CONTROL
    private InputManager inputManager;
    
    // CAMERA
    //private CameraManager cameraManager;
    //private StarDust stardust;
    private StarDustEmitter stardust;
    
    // PLAYER OBJECT
    private boolean playable;
    private Player player;
    
    // NODES FOR GENERAL HIERARCHY
    private Node spacenode;
    private Node objectlist;
    private Node weaponlist;
    private Node skynode;
    private Node uinode;
    
    // FLAG IF SPACE WAS INITIALIZED
    private boolean spaceinit = false;
    
    // MISSION INFORMATION
    private AbstractMission mission;
    private SimpleConfig missionConfig = null;
    private boolean loadMission = false;
    
    // PHYSICS
    private BulletAppState physics;
    
    // NETWORK SYNCHRONIZATION
    /*
    private NetworkAppState network;
    private float synchrate = 15f;
    private float lastsynch = 0f;
    */

    public SpaceAppState(Game game, boolean playable) {
        this.game     = game;
        this.playable = playable;
        //this.network  = network;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.game = (Game) app;
       
        // CREATE PLAYER
        if ( this.playable ) {
            this.player = this.game.getPlayer();
        }

        // CREATE GENERAL NODES
        this.spacenode = new Node("space");
        this.objectlist = new Node("shiplist");
        this.weaponlist = new Node("weaponlist");
        this.skynode = new Node("sky");
        this.uinode = new Node("ui");
        
        this.game.getRootNode().setShadowMode(ShadowMode.Off);
        this.skynode.setShadowMode(ShadowMode.Off);

        // ATTACH GENERAL NODES
        this.game.getRootNode().attachChild(this.spacenode);
        this.spacenode.addControl(new AudioControl(this.game.getAudioManager()));
        this.spacenode.attachChild(this.objectlist);
        this.spacenode.attachChild(this.weaponlist);
        this.game.getRootNode().attachChild(this.skynode);
        this.game.getGuiNode().attachChild(this.uinode);
        
        // ADD SKYCONTROL FOR SKYBOX
        this.skynode.addControl(new SkyControl(this.game.getCamera()));

        // INIT DEBUGGING STUFF
        SpaceDebugger.getInstance().setSpace(this);
                
        // INIT GENERAL INPUT
        this.inputManager = new InputManager(this.game);
        
        // ADD VIEW INPUT
        this.inputManager.toggleInput(ViewInput.class, Game.get().getCameraManager());
        
        // ADD STARDUST
        createStarDust();

        // SET UP GENERAL PHYSICS STUFF
        this.physics = new BulletAppState();
        this.physics.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        Game.get().getStateManager().attach(this.physics);
        this.physics.getPhysicsSpace().setGravity(new Vector3f(0, 0, 0));
        this.physics.getPhysicsSpace().addCollisionListener(this.collisionChecker);
        this.physics.getPhysicsSpace().setAccuracy(1f/45f);
                
        // FLAG SPACE AS INITIALIZED
        spaceinit = true;
    }
    
    public void loadMission(SimpleConfig config) {
        // SET CONFIG ANT TELL SPACE THAT A MISSION RELOAD IS NEEDED!
        this.missionConfig = config;
        this.loadMission = true;
    }
        
    @Override
    public void cleanup() {
        spaceinit = false;
        //dumpGraph(this.game.getRootNode());
        
        super.cleanup();
        
        System.out.println("************************");
        System.out.println("*** SPACE CLEANUP!!! ***");
        System.out.println("************************");

        this.game.getPlayer().setClient(null);
        
        if (this.mission != null) {
            this.mission.destroy();
        }
        this.setEnabled(false);
              
        this.spacenode.detachAllChildren();
        destroyObject(this.spacenode);
        this.skynode.detachAllChildren();
        this.spacenode.getLocalLightList().clear();
        this.skynode.getLocalLightList().clear();
        destroyObject(this.uinode);
        destroyObject(this.skynode);
        SpaceDebugger.getInstance().setSpace(null);
        this.physics.getPhysicsSpace().removeCollisionListener(this.collisionChecker);
        Game.get().getStateManager().detach(this.physics);

        this.inputManager.cleanup();
        
        //this.game.getRootNode().removeControl(this.cameraManager);
        //this.game.getRootNode().removeControl(this.stardust);
        
        // REMOVE VIEW INPUT
        this.inputManager.toggleInput(ViewInput.class, null);
        
        this.getGame().getCameraManager().setTarget(null);
        
        this.game.getAudioManager().cleanup();
    }

    @Override
    public void update(float tpf) {
        // LOAD/RESTART MISSION IF FLAG IS SET
        if (this.loadMission) {
            
            //Thread thread = new Thread(new Runnable() {
                //public void run() {
                    String type = missionConfig.getValue("type", "scripted");
                    if ("scripted".equalsIgnoreCase(type)) {
                        SpaceAppState.this.mission = getGame().getGamedataManager().loadMission(SpaceAppState.this.missionConfig, SpaceAppState.this);
                    } else if ("deathmatch".equalsIgnoreCase(type)) {
                        SpaceAppState.this.mission = new DeathmatchMission(SpaceAppState.this, SpaceAppState.this.missionConfig);
                    } else {
                        throw new IllegalArgumentException("Unknown mission type " + type);
                    }
                    SpaceAppState.this.mission.init();

                    Game.get().getCameraManager().setAutomatic(!SpaceAppState.this.playable);
                    // INIT INGAME UI
                    if (isPlayable()) {
                        //UIAppState.gotoScreen("spawnscreen", game);
                        //UIAppState.initGameUI(game);
                    }
                //}
            //});
            //thread.start();
                        
            this.loadMission = false;
        }
        
        super.update(tpf);
        if (this.isInitialized() && isEnabled() && this.mission != null) {
            this.mission.update(tpf);
            /*
            if (this.network != null && this.network.isServer() && this.lastsynch > this.synchrate) {
                this.network.getServer().updateObjects(this.mission.getObjects());
                this.lastsynch = 0f;
            } else {
                this.lastsynch += tpf;
            }
            */
        } else {
            // do the following while game is PAUSED, e.g. play an idle animation.
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
        } else {
        }
    }
    
    /* SPACE FUNCTIONS */
    public boolean isPlayable() {
        return this.playable;
    }    

    /*
    public CameraManager getCameraManager() {
        return this.cameraManager;
    }
    */
    
    public Game getGame() {
        return this.game;
    }
    
    public Node getSpace() {
        return this.spacenode;
    }

    public Node getSky() {
        return this.skynode;
    }
    
    public void addObject(final ObjectInfoControl object) {
        //this.game.enqueue(new Callable() {
            //public Object call() throws Exception {
                PhysicsControl physics = object.getObjectControl(PhysicsControl.class);
                if ( physics != null ) {
                    this.physics.getPhysicsSpace().add(physics);
                }

                SpaceAppState.this.objectlist.attachChild(object.getSpatial());
                
                //return null;
            //}
        //});
    }
    
    public void removeObject(ObjectInfoControl object) {
        object.getSpatial().removeFromParent();
        Control physics = object.getSpatial().getControl(PhysicsControl.class);
        if ( physics != null ) {
            SpaceAppState.this.physics.getPhysicsSpace().remove(physics);
        }
    }
    
    public List<Spatial> getObjects() {
        return this.objectlist.getChildren();
    }

    public int getNumFlights() {
        return this.objectlist.getQuantity();
    }

    public List<Spatial> getShips() {
        return this.objectlist.getChildren();
    }

    public void addWeapon(AbstractWeaponControl weapon, ObjectInfoControl origin) {
        //AbstractWeaponControl control = weapon.getControl(AbstractWeaponControl.class);
        weapon.setOrigin(origin);
        this.weaponlist.attachChild(weapon.getSpatial());
        //this.getPhysicsSpace().addCollisionListener(control);
        this.physics.getPhysicsSpace().add(weapon);
    }

    public void addToSky(Spatial spatial) {
        this.skynode.attachChild(spatial);
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public AbstractMission getMission() {
        return this.mission;
    }
    
    public InputManager getInputManager() {
        return this.inputManager;
    }
    
    public void addToUI(final Spatial spatial) {
        this.game.enqueue(new Callable() {
            public Object call() throws Exception {
                SpaceAppState.this.uinode.attachChild(spatial);                
                return null;
            }
        });
    }
    
    public void removeFromUI(final Spatial spatial) {
        this.game.enqueue(new Callable() {
            public Object call() throws Exception {
                spatial.removeFromParent();                
                return null;
            }
        });
    }
    
    public BulletAppState getPhysics() {
        return this.physics;
    }
    
    /*
    public Node getUINode() {
        return this.uinode;
    }
    */
   
    public void killObject(final Kill kill) {
        //ObjectInfoControl targetInfo = kill.getTarget().getControl(ObjectInfoControl.class);
        if ( kill.getTarget() != null && kill.getTarget().getClient() ) {
            kill.getTarget().getSpatial().removeFromParent();
            Control physics = kill.getTarget().getObjectControl(PhysicsControl.class);
            if ( physics != null ) {
                SpaceAppState.this.physics.getPhysicsSpace().remove(physics);
            }
            SpaceAppState.this.mission.kill(kill);
        } else {
            destroyObject(kill.getTarget().getSpatial());
        }
    }
    
    public void destroyObject(final Spatial object) {        
        if (object == null) {
            return;
        }

        this.game.enqueue(new Callable() {

            public Object call() throws Exception {
                int numControls = object.getNumControls();
                object.removeFromParent();
                        
                for (int i = 0; i < numControls; i++) {
                    Control control = object.getControl(i);
                    if (control instanceof GhostControl) {
                        SpaceAppState.this.physics.getPhysicsSpace().remove(control);
                    }
                    if (control instanceof PhysicsControl) {
                        SpaceAppState.this.physics.getPhysicsSpace().remove(control);
                    }
                    if (control instanceof PhysicsCollisionListener) {
                        SpaceAppState.this.physics.getPhysicsSpace().removeCollisionListener((PhysicsCollisionListener) control);
                    }
                    if (control instanceof ActionListener) {
                        SpaceAppState.this.game.getInputManager().removeListener((InputListener) control);
                    }
                    if (control instanceof AnalogListener) {
                        SpaceAppState.this.game.getInputManager().removeListener((AnalogListener)control);
                    }
                    if (control instanceof LightControl) {
                        SpaceAppState.this.game.getRootNode().removeLight(((LightControl) control).getLight());
                    }
                    //object.removeControl(control);
                    //numControls--;
                }
                
                return null;
            }
        });
    }
    
    private void createStarDust() {
        this.stardust = new StarDustEmitter(100, 2, 0.125f, this.game.getCamera(), this.game.getAssetManager().loadMaterial("effects/explosion/flame.j3m"));
        this.spacenode.attachChild(this.stardust);
        this.stardust.start();
    }
    
    private PhysicsCollisionListener collisionChecker = new PhysicsCollisionListener() {

        public void collision(PhysicsCollisionEvent event) {
            DamageControl damageControlA = event.getNodeA().getControl(DamageControl.class);
            
            if (damageControlA != null) {
                // CHECK WEAPON COLLISIONS
                AbstractWeaponControl weaponB = event.getNodeB().getControl(AbstractWeaponControl.class);
                if (weaponB != null && weaponB.getOrigin().getSpatial() != event.getNodeA()) {
                    weaponB.collide(event.getNodeA().getControl(ObjectInfoControl.class), event);
                    return;
                }

                // COLLISION-DAMAGE BETWEEN RIGID BODYS (SHIPS, ASTEROIDS AND OBJECTS)       
                RigidBodyControl physicsA = event.getNodeA().getControl(RigidBodyControl.class);
                RigidBodyControl physicsB = event.getNodeB().getControl(RigidBodyControl.class);
                if (physicsA != null && physicsB != null) {  
                    float damageMag = 10f;
                    float impulse   = event.getAppliedImpulse();
                    
                    float massA      = physicsA.getMass() != 0 ? physicsA.getMass() : Float.MAX_VALUE;
                    float damageA = (impulse / massA) * damageMag;                  
                    
                    if (damageA > 0) {
                        Vector3f direction = event.getLocalPointA(); //event.getPositionWorldOnA().subtract(event.getNodeA().getWorldTranslation());
                        Vector3f position = event.getLocalPointA().add(event.getNodeA().getWorldTranslation());
                        damageControlA.damageMe(new DamageInformation("collision", "collision", damageA, event.getNodeB().getControl(ObjectInfoControl.class), event.getNodeA().getControl(ObjectInfoControl.class), direction, position));
                        //System.out.println("C: " + event.getNodeA().getName() + ", D: " + damageA);
                    }
                    
                    float massB   = physicsB.getMass() != 0 ? physicsB.getMass() : Float.MAX_VALUE;
                    float damageB = (impulse / massB) * damageMag;
                    
                    if (damageB > 0) {
                        DamageControl damageControlB = event.getNodeB().getControl(DamageControl.class);
                        if (damageControlB != null) {
                            Vector3f direction = event.getLocalPointB(); //event.getPositionWorldOnB().subtract(event.getNodeB().getWorldTranslation());
                            Vector3f position = event.getLocalPointB().add(event.getNodeB().getWorldTranslation());
                            damageControlB.damageMe(new DamageInformation("collision", "collision", damageB, event.getNodeA().getControl(ObjectInfoControl.class), event.getNodeB().getControl(ObjectInfoControl.class), direction, position));
                        }
                        //System.out.println("C: " + event.getNodeB().getName() + ", D: " + damageB);
                    }
                }
            } else {
                // COLLISION BETWEEN GHOSTS (WEAPONS AND OTHER STUFF)!
                
                // CHECK WEAPON COLLISIONS
                AbstractWeaponControl weaponA = event.getNodeA().getControl(AbstractWeaponControl.class);
                AbstractWeaponControl weaponB = event.getNodeB().getControl(AbstractWeaponControl.class);
                if (weaponA != null && weaponB != null && weaponA.getOrigin() != weaponB.getOrigin()) {
                    weaponA.destroyWeapon();
                    weaponB.destroyWeapon();
                }
            }
        }
    
    };
    
      //////////////////////////////////////////////////////////////////////////
     // STATIC ACCESS METHODS /////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public static void startGame(SimpleConfig missionConfig, Game game) {
        leaveSpace(game);
        
        SpaceAppState newSpace = new SpaceAppState(game, true);
        newSpace.loadMission(missionConfig);
        
        /*
        if (!NetworkAppState.isClient(game)) {
            // OBSOLETE
        }
        */
        
        game.getAudioManager().stopMusic(0);
        game.getAudioManager().setSoundVolume(1);
        game.getStateManager().attach(newSpace);
    }
    
    public static void startDemo(Game game) {
        leaveSpace(game);
                        
        // CREATE RANDOM GENERATOR
        Random randomGenerator = new Random();
        // PICK A RANDOM LEVEL
        String[] levels = game.getGamedataManager().listLevels();
        String level = levels[randomGenerator.nextInt(levels.length)];
        
        // LIST ALL SCRIPTED MISSIONS
        String[] missions = game.getGamedataManager().listMissions();
        // CREATE A RANDOM FOR MISSION SELECTION 
        // (NUMBER OF SCRIPTED MISSIONS + NUMBER OF QUICKMATCH TYPES)
        int random = randomGenerator.nextInt(missions.length + 2);
        
        SimpleConfig config = new SimpleConfig();        
        if ( random < missions.length ) {
            // SELECT A SCRIPTED MISSION IF RANDOM IS IN MISSIONLIST-BOUNDS
            config.setValue("type", "scripted");
            config.setValue("name", missions[random]);
            config.setValue("maxclients", "32");
        } else {
            // SELECT A QUICKMISSION IF RANDOM IS OUT OF RANGE
            random -= missions.length;
            System.out.println(random);
            if ( random == 0 ) {
                // LOAD DEATHMATCHMISSION IF RANDOM EQUALS MISSIONLIST-SIZE
                config.setValue("type", "deathmatch");
                config.setValue("level", level);
                config.setValue("maxclients", "64");
                config.setValue("teams", "64");
                config.setValue("limit", "20");
            } else {
                // LOAD TEAMDEATHMATCH IF RANDOM IS LARGER MISSIONLIST-SIZE
                int teams = 2 + 2 * randomGenerator.nextInt(2+1);
                
                config.setValue("type", "deathmatch");
                config.setValue("level", level);
                config.setValue("maxclients", "64");
                config.setValue("teams", Integer.toString(teams));
                config.setValue("limit", "100");
            }
        }
        
        // LOAD THE GENERATED DEMO
        SpaceAppState newSpace = new SpaceAppState(game, false);
        newSpace.loadMission(config);
        
        game.getAudioManager().stopMusic(0);
        game.getAudioManager().setSoundVolume(0);
        // _game.getAudioManager().playMusic("mainmenu.ogg", 0, true);
        game.getStateManager().attach(newSpace);
        
        UIAppState.gotoScreen("loading", game);
        
        /*
        if (NetworkAppState.isActive(Game.get())) {
            UIAppState.gotoScreen("multiplayerlobby", game);
        } else {
            UIAppState.gotoScreen("mainmenu", game);    
        }
        */
        //UIAppState.initMenuUI(game);
    }
    
    public static void leaveSpace(Game game) {
        SpaceAppState oldspace = game.getStateManager().getState(SpaceAppState.class);
        if (oldspace != null) {
            game.getStateManager().detach(oldspace);
        }
    }
    
}
