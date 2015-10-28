/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.hud;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.debug.DebugContext;
import de.jlab.spacefight.debug.SpaceDebugger;
import de.jlab.spacefight.mission.structures.DistressCall;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.ui.ingame.hud.HudItem;
import java.util.ArrayList;

/**
 *
 * @author rampage
 */
public class TargetIndicatorItem extends HudItem {
    
    private Vector3f screenPos = new Vector3f(0f, 0f, 0f);
    private Vector3f aimPos = new Vector3f(0f, 0f, 0f);
    
    private boolean onScreen = false;
    private boolean newTarget = true;
    
    private HealthHud targetHealth;
    
    private Picture box;
    
    private Picture braceTemplate;
    private Picture taskBraceTemplate;
    private Node braceContainer;
    
    private Picture distressDefensiveIndicatorTemplate;
    
    public TargetIndicatorItem(SpaceAppState space) {
        super("target", space);
        
        this.move(0.5f, 0.5f, 0.5f, 0.5f);
        this.scale(1f, 1f);
        
        this.box = new Picture("box");
        box.setImage(getSpace().getGame().getAssetManager(), "ui/hud/targetindicator_box.png", true);
        addPicture("box", box);
        scalePicture("box", 0.06f);
        movePicture("box", 0.5f, 0.5f, 0.5f, 0.5f);
               
        this.braceContainer = new Node("blips");
        addPicture("bracecontainer", this.braceContainer);
        scalePicture("bracecontainer", 1f, 1f);
        movePicture("bracecontainer", 0.5f, 0.5f, 0.5f, 0.5f);
        showPicture("bracecontainer");
                
        this.braceTemplate = new Picture("brace");
        this.braceTemplate.setImage(getSpace().getGame().getAssetManager(), "ui/hud/targetindicator_brace.png", true);
        
        this.taskBraceTemplate = new Picture("taskbrace");
        this.taskBraceTemplate.setImage(getSpace().getGame().getAssetManager(), "ui/hud/taskindicator_guard.png", true);
        
        this.distressDefensiveIndicatorTemplate = new Picture();
        this.distressDefensiveIndicatorTemplate.setImage(getSpace().getGame().getAssetManager(), "ui/hud/taskindicator_move.png", true);
        
        Picture missilelock = new Picture("missilelock");
        missilelock.setImage(getSpace().getGame().getAssetManager(), "ui/hud/targetindicator_box.png", true);
        addPicture("missilelock", missilelock);
        scalePicture("missilelock", 0.08f);
        movePicture("missilelock", 0.5f, 0.5f, 0.5f, 0.5f);
        rotatePicture("missilelock", 90 * FastMath.DEG_TO_RAD);
        
        Picture aim = new Picture("aim");
        aim.setImage(getSpace().getGame().getAssetManager(), "ui/hud/targetindicator_aim.png", true);
        addPicture("aim", aim);
        scalePicture("aim", 0.025f);
        movePicture("aim", 0.5f, 0.5f, 0.5f, 0.5f);        
    }
    
    public void positionCentered(float x, float y) {
        
    }

    public void attachToGui() {
        
    }

    public void updateItem(float tpf, Spatial spatial) {
        ObjectInfoControl self = spatial.getControl(ObjectInfoControl.class);
        WeaponSystemControl weapons = spatial.getControl(WeaponSystemControl.class);
        
        TargetInformation target = null;
        if ( weapons != null ) {
            target = weapons.getTarget();
        }
              
        this.braceContainer.detachAllChildren();
        createCalls(self);
        createBraces(self, target, spatial);
        
        if ( target == null ) {
            
            SpaceDebugger.getInstance().setTextFloat(DebugContext.TARGET, "X", spatial.getWorldTranslation().x); 
            SpaceDebugger.getInstance().setTextFloat(DebugContext.TARGET, "Y", spatial.getWorldTranslation().y);
            SpaceDebugger.getInstance().setTextFloat(DebugContext.TARGET, "Z", spatial.getWorldTranslation().z);
            
            hidePicture("box");
            hidePicture("aim");
            //hidePicture("boxsmall");
            hidePicture("missilelock");
            if ( this.targetHealth != null ) {
                this.targetHealth.cleanupItem(null);
                this.targetHealth.cleanup();
                this.targetHealth = null;
            }
            onScreen = false;
            newTarget = true;
            return;
        } else {
            
            showPicture("box");
            
            SpaceDebugger.getInstance().setTextFloat(DebugContext.TARGET, "X", target.getObject().getPosition().x); 
            SpaceDebugger.getInstance().setTextFloat(DebugContext.TARGET, "Y", target.getObject().getPosition().y);
            SpaceDebugger.getInstance().setTextFloat(DebugContext.TARGET, "Z", target.getObject().getPosition().z);
            
            if ( this.targetHealth == null ) {
                this.targetHealth = new HealthHud(getSpace(), spatial);
                this.targetHealth.move(0.25f, 0f, 0.5f, 0f);
            }
        }
        
        if ( this.targetHealth != null ) {
            this.targetHealth.updateItem(tpf, target.getObject().getSpatial());
        }
        
        // RETRIEVE TARGET POSITION
        getCamera().getScreenCoordinates(target.getObject().getPosition(), this.screenPos);
        getCamera().getScreenCoordinates(target.getAimAtWorld(self, weapons.getPrimarySlot().getWeapon()), this.aimPos);

        //this.screenPos.z < 1 &&  && this.screenPos.x < camera.getWidth() && this.screenPos.y > 0 && this.screenPos.y < camera.getHeight()
        
        if ( this.screenPos.z < 1 && this.screenPos.x > 0 && this.screenPos.x < getCamera().getWidth() && this.screenPos.y > 0 && this.screenPos.y < getCamera().getHeight() ) {
            if ( !onScreen ) {
                //showPicture("box");
                scalePicture("box", 0.08f);
                showPicture("aim");
                //hidePicture("boxsmall");
                hidePicture("missilelock");
                
                ColorRGBA color = ColorRGBA.White;
                if (target.getFOF(self) == TargetInformation.FOF_FOE) {
                    color = ColorRGBA.Red;
                } else if (target.getFOF(self) == TargetInformation.FOF_FRIEND) {
                    color = ColorRGBA.Green;
                }
                box.getMaterial().setColor("Color", color);
                
                onScreen = true;
            }
        
            //WeaponSystemControl weapons = spatial.getControl(WeaponSystemControl.class);
            if ( weapons == null || !weapons.inPrimaryRange() ) {
                hidePicture("aim");
            } else {
                showPicture("aim");
                movePicture("aim", this.aimPos.x / getCamera().getWidth(), this.aimPos.y / getCamera().getHeight(), 0.5f, 0.5f);
            }
            
            if ( weapons == null || !weapons.inSecondaryRange() || weapons.getSecondarySlot().weaponCount() < 1 ) {
                hidePicture("missilelock");
            } else {
                showPicture("missilelock");
                movePicture("missilelock", this.screenPos.x / getCamera().getWidth(), this.screenPos.y / getCamera().getHeight(), 0.5f, 0.5f);
            }
            
            movePicture("box", this.screenPos.x / getCamera().getWidth(), this.screenPos.y / getCamera().getHeight(), 0.5f, 0.5f);            
        } else {
            if ( onScreen || newTarget ) {
                scalePicture("box", 0.06f);
                //hidePicture("box");
                hidePicture("aim");
                hidePicture("missilelock");
                //showPicture("boxsmall");
                onScreen = false;
                newTarget = false;
            }
                        
            // CALCULATE X AND Y FROM SCREEN CENTER
            float x = (this.screenPos.x - getCamera().getWidth() / 2) / (getCamera().getWidth() / 2);
            float y = (this.screenPos.y - getCamera().getHeight() / 2) / (getCamera().getHeight() / 2); 
            // REVERSE VALUeS IF ENEMY IS BEHIND (z > 1)
            if ( this.screenPos.z < 1 ) {                                               
                x = Math.min(1f, Math.max(-1f, x));
                y = Math.min(1f, Math.max(-1f, y));
            } else {
                x = Math.min(1f, Math.max(-1f, -x));
                y = Math.min(1f, Math.max(-1f, -y));
            }
            // CLAMP BIGGER PART TO SCREEN BORDER
            if (Math.abs(x) > Math.abs(y)) {
                x = x > 0 ? 1 : -1;   
            } else {
                y = y > 0 ? 1 : -1;
            }                
            // DETERMINE THE ALIGNMENT FOR RENDERING THE BOX
            float xalign = x > 0 ? 1 : 0;
            float yalign = y > 0 ? 1 : 0;
            // MOVE RESULTS INTO SCREEN SPACE
            x = x / 2 + 0.5f;
            y = y / 2 + 0.5f;
            // MOVE BOX FROM RESULTS AND ALIGNMENT
            movePicture("box", x, y, xalign, yalign);
        }
    }

    private void createCalls(ObjectInfoControl self) {
        DistressCall[] calls = getSpace().getMission().getDistressCalls(self);
        for (DistressCall call : calls) {
            for (ObjectInfoControl target : call.getTargets()) {
                Picture brace = (Picture)this.taskBraceTemplate.clone();
                brace.getMaterial().setColor("Color", ColorRGBA.White);
                Node node = new Node("brace");
                node.attachChild(brace);

                Vector3f screenPos = HudHelper.getScreenPosition(target.getPosition(), getCamera());
                Vector2f alignment = new Vector2f(0.5f, 0.5f);
                if (screenPos.z < 0) {
                    alignment = HudHelper.getAlignment(screenPos);
                }

                node.setLocalTranslation(screenPos.x, screenPos.y, 0);
                brace.setPosition(-alignment.x, -alignment.y);
                
                //getCamera().getScreenCoordinates(target.getPosition(), this.screenPos);

                
                //brace.setPosition(this.screenPos.x / getCamera().getWidth(), this.screenPos.y / getCamera().getWidth());
                //brace.setPosition(this.screenPos.x, this.screenPos.y);

                node.setLocalScale(0.06f, 0.06f, 1f);
                this.braceContainer.attachChild(node);
            }
        }
    }
    
    private void createBraces(ObjectInfoControl self, TargetInformation target, Spatial spatial) {
        SensorControl sensorControl = spatial.getControl(SensorControl.class);
        if (sensorControl != null) {
            ArrayList<TargetInformation> targetList = sensorControl.getTargetList(0, new int[] { TargetInformation.FOF_FOE, TargetInformation.FOF_FRIEND, TargetInformation.FOF_NEUTRAL });
            float fac = (float)getCamera().getWidth() / (float)getCamera().getHeight();
            for (TargetInformation otherTarget : targetList) {
                getCamera().getScreenCoordinates(otherTarget.getObject().getPosition(), this.screenPos);
                if (target != null && otherTarget.getObject().equals(target.getObject())) {
                    continue;
                }
                if (this.screenPos.z < 1 && this.screenPos.x > 0 && this.screenPos.x < getCamera().getWidth() && this.screenPos.y > 0 && this.screenPos.y < getCamera().getHeight()) {
                    Picture brace = null;
                    if ("conquestpoint".equalsIgnoreCase(otherTarget.getObject().getObjectType())) {
                        brace = (Picture)this.taskBraceTemplate.clone();
                    } else {
                        brace = (Picture)this.braceTemplate.clone();
                    }
                    ColorRGBA color = ColorRGBA.White;
                    if ( otherTarget.getFOF(self) == TargetInformation.FOF_FOE ) {
                        color = ColorRGBA.Red;
                    } else if ( otherTarget.getFOF(self) == TargetInformation.FOF_FRIEND ) {
                        if (otherTarget.getObject().getFlight() == self.getFlight()) {
                            color = ColorRGBA.Cyan;
                        } else {
                            color = ColorRGBA.Green;
                        }
                    }
                    //
                    brace.getMaterial().setColor("Color", color);
                    Node node = new Node("brace");
                    node.attachChild(brace);
                    node.setLocalTranslation(this.screenPos.x / getCamera().getWidth(), this.screenPos.y / getCamera().getHeight(), 0);
                    brace.setPosition(-0.5f, -0.5f);
                    //brace.setPosition(this.screenPos.x / getCamera().getWidth(), this.screenPos.y / getCamera().getWidth());
                    //brace.setPosition(this.screenPos.x, this.screenPos.y);
                    
                    node.setLocalScale(0.06f, 0.06f * fac, 1f);
                    this.braceContainer.attachChild(node);
                }
            }
        }
    }
    
    @Override
    public void initItem(Spatial spatial) {
        // NOTHING SPECIAL TO DO HERE
    }

    @Override
    public void cleanupItem(Spatial spatial) {
        if ( this.targetHealth != null ) {
            this.targetHealth.cleanupItem(null);
            this.targetHealth.cleanup();
            this.targetHealth = null;
        }
        this.braceContainer.detachAllChildren();
    }
    
    @Override
    public void renderItem(Spatial spatial, RenderManager rm, ViewPort vp) {
        /*
        if (this.brace == null) {
            return;
        }
        SensorControl sensorControl = spatial.getControl(SensorControl.class);
        if (sensorControl != null) {
            ArrayList<TargetInformation> targetList = sensorControl.getTargetList(0, new int[] { TargetInformation.FOF_FOE, TargetInformation.FOF_FRIEND });
            for (TargetInformation target : targetList) {
                getCamera().getScreenCoordinates(target.getTarget().getWorldTranslation(), this.screenPos);
                if (this.screenPos.z < 1 && this.screenPos.x > 0 && this.screenPos.x < getCamera().getWidth() && this.screenPos.y > 0 && this.screenPos.y < getCamera().getHeight()) {
                    //movePicture("brace", this.screenPos.x / getCamera().getWidth(), this.screenPos.y / getCamera().getHeight(), 0.5f, 0.5f);
                    //getPicture("brace").updateGeometricState();
                    
                    Vector3f pos = new Vector3f(this.screenPos.x, this.screenPos.y, 5f);
                    vp.getCamera().getRotation().multLocal(pos);
                    
                    this.brace.setLocalTranslation(vp.getCamera().getLocation().add(pos));
                    this.brace.setLocalRotation(vp.getCamera().getRotation());
                    
                    //this.brace.setPosition(this.screenPos.x, this.screenPos.y);
                    this.brace.setQueueBucket(Bucket.Gui);
                    this.brace.updateGeometricState();
                    rm.renderGeometry(this.brace);
                }
            }
        }
        */
    }
    
}
