/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.gamedata;

import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.CustomLodControl;
import de.jlab.spacefight.basic.FixedLodControl;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.effect.EffectControl;
import de.jlab.spacefight.mission.scripted.ScriptedMission;
import de.jlab.spacefight.scripting.JScript;
import de.jlab.spacefight.scripting.ScriptWrapper;
import de.jlab.spacefight.systems.perks.Perk;
import de.jlab.spacefight.weapon.AbstractWeaponControl;
import de.lessvoid.nifty.Nifty;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public class GamedataManager {
    
    public static final String DIR_GAMEDATA = "gamedata";
    public static final String DIR_BASEMOD  = "base";
    public static final String DIR_CUSTOM   = "custom";
        
    private AssetManager assetManager;
    //private Game game;
    
    private String _currentMod = DIR_BASEMOD;
    
    private EntityCache<ObjectInfoControl> objectInfoCache = new EntityCache<ObjectInfoControl>();
    private EntityCache<ObjectInfoControl> weaponCache = new EntityCache<ObjectInfoControl>();
    
    public GamedataManager(Game game) {
        //this.game = game;
        this.assetManager = game.getAssetManager();
    }
    
    /*
    @Deprecated
    public AssetManager getAssetManager() {
        return this.assetManager;
    }
    */
    
      //////////////////////////////////////////////////////////////////////////
     // MODEL LOADING ///////////////////////////////////////////////////////// 
    //////////////////////////////////////////////////////////////////////////  
    
    public Material loadMaterial(String name) {
        return this.assetManager.loadMaterial(name);
    }
    
    public Spatial loadModel(String file) {
        Spatial model = this.assetManager.loadModel(file);
        
        if (model instanceof Node) {
            Node hull = (Node)((Node)model).getChild("Hull");
            if (hull == null) {
                throw new RuntimeException("Model " + file + " does not define a hull!");
            } else {
                Spatial lod100 = hull.getChild("LOD100");
                if ( lod100 != null ) {
                    model.addControl(new CustomLodControl());
                } else {
                    Logger.getLogger(GamedataManager.class.getSimpleName()).log(Level.WARNING, "Model " + file + " is using FixedLodControl which might be pretty slow! ");
                    List<Spatial> children = hull.getChildren();
                    for ( Spatial child : children ) {
                        if ( child instanceof Geometry ) {
                            if ( ((Geometry)child).getMesh().getNumLodLevels() > 0 ) {
                                FixedLodControl lodControl = new FixedLodControl();
                                lodControl.setTrisPerPixel(0.01f); // CURRENTLY FIXED VALUE
                                //lodControl.setDistTolerance(5f);
                                child.addControl(lodControl);
                            }
                        }
                    }
                }
            }
        
            Spatial cockpit = ((Node)model).getChild("Cockpit");
            if (cockpit != null) {
                cockpit.setCullHint(Spatial.CullHint.Always);
            }
        }            
        
        return model;
    }
    
      //////////////////////////////////////////////////////////////////////////
     // GENERAL OBJECT LOADING //////////////////////////////////////////////// 
    //////////////////////////////////////////////////////////////////////////  
    
    public ObjectInfoControl loadObject(boolean client, String name, String id, SpaceAppState space) {                
        String path = resolveGamedataPath(client ? "ships" : "objects", name, true);
               
        ObjectInfoControl objectInfo = this.objectInfoCache.getEntityInstance(name);
        if (objectInfo == null) {
            this.assetManager.registerLocator(path, FileLocator.class);
            Element objectElement = XMLLoader.loadXML(path, name);
            objectInfo = new ObjectInfoControl(client, objectElement, space, path, this);
            objectInfo.loadObject(space);
            //this.objectInfoCache.putEntity(name, objectInfo);
            this.assetManager.unregisterLocator(path, FileLocator.class);
        }
        objectInfo.setId(id);
        
        return objectInfo;
    }
    
      //////////////////////////////////////////////////////////////////////////
     // WEAPONS LOADING /////////////////////////////////////////////////////// 
    //////////////////////////////////////////////////////////////////////////  
    
    public String[] listWeapons(final int weaponType, final float maxSize) {
        // TODO WE NEED TO ENHANCE WEAPON LISTING WITH A CUSTOM DATA-CLASS AND MORE STUFF!
        final String path = resolveGamedataPath("weapons", "", true);
        File weaponDir = new File(path);
        return weaponDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);
                
                boolean allowedSize = true;
                int type = AbstractWeaponControl.TYPE_ENERGY;
                if (new File(file, name + ".xml").exists()) {
                    Element rootElement = XMLLoader.loadXML(path + name, name);
                    type = XMLLoader.getIntConstValue(rootElement, "type", AbstractWeaponControl.TYPE_INTS, AbstractWeaponControl.TYPE_STRINGS, type);
                    
                    if (maxSize > 0) {
                        allowedSize = maxSize >= XMLLoader.getFloatValue(rootElement, "size", 0f);
                    }
                }
                
                return file.isDirectory() && new File(file, name + ".xml").exists() && weaponType == type && allowedSize;
            }
        });
    }
    
    public AbstractWeaponControl loadWeapon(String name, SpaceAppState space) {
       String path = resolveGamedataPath("weapons", name, true);
        
        // FIXME OBJECT CLONING WON'T WORK
        //ObjectInfoControl objectInfo = this.weaponCache.getEntityInstance(name);
        //AbstractWeaponControl weapon = null;
        //if (objectInfo == null) {
            this.assetManager.registerLocator(path, FileLocator.class);
            Element objectElement = XMLLoader.loadXML(path, name);
            ObjectInfoControl objectInfo = new ObjectInfoControl(false, objectElement, space, path, this);
            objectInfo.loadObject(space);
            AbstractWeaponControl weapon = new AbstractWeaponControl(space);
            weapon.loadFromElement(objectElement, path, this);
            objectInfo.addObjectControl(weapon);
            this.assetManager.unregisterLocator(path, FileLocator.class);
        //} else {
            //weapon = objectInfo.getObjectControl(AbstractWeaponControl.class);
        //}
        objectInfo.setId("name_" + objectInfo.hashCode());
        //String model = objectElement.getChildTextTrim("model");
        //Node weapon = (Node)this.assetManager.loadModel("weapons" + "/" + name + "/" + model);
        
        /*
        Spatial lod100 = weapon.getChild("LOD100");
        if ( lod100 != null ) {
            weapon.addControl(new CustomLodControl());
        }
        */       
        
        
        return weapon;
    }
    
      //////////////////////////////////////////////////////////////////////////
     // PERK LOADING ////////////////////////////////////////////////////////// 
    //////////////////////////////////////////////////////////////////////////  
    
    public String[] listPerks() {
        // TODO WE NEED TO ENHANCE WEAPON LISTING WITH A CUSTOM DATA-CLASS AND MORE STUFF!
        final String path = resolveGamedataPath("perks", "", true);
        File weaponDir = new File(path);
        return weaponDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);
                
                return file.isDirectory() && new File(file, name + ".xml").exists();
            }
        });
    }
    
    public Perk loadPerk(String name) {
       String path = resolveGamedataPath("perks", name, true);
        
        this.assetManager.registerLocator(path, FileLocator.class);
        
        Element perkElement = XMLLoader.loadXML(path, name);
        Perk perk = new Perk(perkElement, path, this);
        
        this.assetManager.unregisterLocator(path, FileLocator.class);
        
        return perk;
    }
    
      //////////////////////////////////////////////////////////////////////////
     // SPECIAL EFFECTS LOADING /////////////////////////////////////////////// 
    //////////////////////////////////////////////////////////////////////////  
    
    public EffectControl loadEffect(String name, Node parent, SpaceAppState space) {
        String path = resolveGamedataPath("effects", name, false);
        
        this.assetManager.registerLocator(path, FileLocator.class);
        
        Element objectElement = XMLLoader.loadXML(path, name);
        EffectControl control = new EffectControl(parent, space);
        control.loadFromElement(objectElement, path, this);
                
        this.assetManager.unregisterLocator(path, FileLocator.class);
        
        return control;
    }
    
      //////////////////////////////////////////////////////////////////////////
     // SOUND & MUSIC LOADING ///////////////////////////////////////////////// 
    //////////////////////////////////////////////////////////////////////////  
    
    public AudioNode loadMusic(String name) {
        return new AudioNode(this.assetManager, "music/" + name, true);
    }
    
    public AudioNode loadSound(String name) {
        return new AudioNode(this.assetManager, "sounds/" + name, false);
    }
    
      //////////////////////////////////////////////////////////////////////////
     // LEVEL LOADING ///////////////////////////////////////////////////////// 
    //////////////////////////////////////////////////////////////////////////  
    
    public String[] listLevels() {
        // TODO WE NEED TO ENHANCE MISSION LISTING WITH A CUSTOM DATA-CLASS AND MORE STUFF!
        String path = resolveGamedataPath("level", "", true);
        File missionDir = new File(path);
        return missionDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File level = new File(dir, name);
                return level.isDirectory() && new File(level, name + ".j3o").exists();
            }
        });
    }
    
    public Node loadLevel(String name) {
        String path = resolveGamedataPath("level", name, false);
        
        this.assetManager.registerLocator(path, FileLocator.class);
        
        Object levelAsset = this.assetManager.loadModel("level/" + name + "/" + name + ".j3o");
        
        if ( !(levelAsset instanceof Node) )
            throw new RuntimeException("Error loading level " + name + "! level must have a Node as top-level Spatial.");

        this.assetManager.unregisterLocator(path, FileLocator.class);
        
        return (Node)levelAsset;
    }
    
      //////////////////////////////////////////////////////////////////////////
     // MISSION LOADING /////////////////////////////////////////////////////// 
    //////////////////////////////////////////////////////////////////////////  
    
    public String[] listMissions() {
        // TODO WE NEED TO ENHANCE MISSION LISTING WITH A CUSTOM DATA-CLASS AND MORE STUFF!
        String path = resolveGamedataPath("mission", "", true);
        File missionDir = new File(path);
        return missionDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File level = new File(dir, name);
                return level.isDirectory() && new File(level, name + ".xml").exists();
            }
        });
    }
    
    public ScriptedMission loadMission(SimpleConfig config, SpaceAppState space) {
        String name = config.getValue("name", null);
        String path = resolveGamedataPath("mission", name, true);
        config.setValue("path", path);
        Element missionElement = XMLLoader.loadXML(path, name);
                
        ScriptedMission mission = new ScriptedMission(space, config);
        mission.loadFromElement(missionElement, path, this);
        
        return mission;
    }
    
      //////////////////////////////////////////////////////////////////////////
     // SCRIPT LOADING //////////////////////////////////////////////////////// 
    //////////////////////////////////////////////////////////////////////////  
    
    public static final String HEADER           = "#script";
    public static final String HEADER_EXTENSION = HEADER + " extends";
    public static final String HEADER_CRITICAL  = HEADER + " critical";

    public ScriptWrapper loadScript(String path, String scriptName, ScriptWrapper scriptWrapper) {
        scriptWrapper.init(loadScript(path, scriptName));
        return scriptWrapper;
    }
    
    public JScript loadScript(String path, String scriptName) {
        try {
            FileInputStream in = new FileInputStream(path != null ? new File(path, scriptName) : new File(scriptName));
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String superScriptName 	= null;
            StringBuilder code 	= new StringBuilder();
            boolean critical 	= false;

            code.append("importClass(Packages.").append(SpaceAppState.class.getCanonicalName()).append(");");

            while( reader.ready() ) {
                String line = reader.readLine();
                String simpleLine = line.trim();

                if ( simpleLine.length() > 0 ) {
                    int headerIndex = simpleLine.toLowerCase().indexOf(HEADER);
                    if ( headerIndex >= 0 ) {
                        if ( simpleLine.toLowerCase().indexOf(HEADER_EXTENSION) >= 0 )
                            superScriptName = simpleLine.substring(headerIndex + HEADER_EXTENSION.length()).trim();
                        if ( simpleLine.toLowerCase().indexOf(HEADER_CRITICAL) >= 0 )
                            critical = true;
                    } else
                        code.append(line).append("\n");
                }
            }

            JScript superScript = null;
            if ( superScriptName != null ) {
                superScript = loadScript(path, superScriptName);
            }
            return new JScript(scriptName, code.toString(), superScript, critical, this);
        } catch (Exception ex) {
            Logger.getLogger(GamedataManager.class.getSimpleName()).log(Level.SEVERE, "Error loading script " + scriptName, ex);
        }
        return null;
    }
    
    /* HELPER METHODS */
        
    private String resolveGamedataPath(String context, String name, boolean includeCustom) {      
        String path = null;
        if ( includeCustom ) {
            path = buildGamedataPath(DIR_CUSTOM, context, name);               
            if ( new File(path).exists() )
                return path;
        }
        
        path = buildGamedataPath(_currentMod, context, name);
        if ( new File(path).exists() )
            return path;
        
        path = buildGamedataPath(DIR_BASEMOD, context, name);
        if ( new File(path).exists() )
            return path;
        
        return null;
    }
    
    private String buildGamedataPath(String mod, String context, String name) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder .append(DIR_GAMEDATA).append("/")
                    .append(mod).append("/")
                    .append(context).append("/")
                    .append(name);
        return pathBuilder.toString();
    }
    
      //////////////////////////////////////////////////////////////////////////
     // NIFTY UI LOADING ////////////////////////////////////////////////////// 
    //////////////////////////////////////////////////////////////////////////  
    
    public void loadUI(Nifty nifty) {
        String path = resolveGamedataPath("ui/screens", "", true);
        File screenDir = new File(path);
        String[] screens = screenDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        
        for (String screen : screens) {
            nifty.addXml("ui/screens/" + screen);
        }
    }
    
}
