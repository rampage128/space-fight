/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.hud;

import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Line;
import com.jme3.ui.Picture;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.audio.AudioEffect;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.ui.ingame.hud.HudItem;

/**
 *
 * @author rampage
 */
public class RadarItem extends HudItem {
    
    private float radarAngle = 30f;
    
    private Node radarNode = null;
    private Node targetList = null;
    
    private Geometry blipLine;
    
    private Picture blippic_interceptor;
    private Picture blippic_fighter;
    private Picture blippic_bomber;
    private Picture blippic_capital;
    private Picture blippic_unknown;
    
    private AudioEffect targetWarning; 
    private AudioEffect launchWarning;
    
    public RadarItem(SpaceAppState space) {
        super("radar", space);
        
        this.scale(0.3f);
        this.move(0.95f, 0f, 0.5f, 0f);
        
        radarNode = new Node("radar");

        Picture gem = new Picture("sphere");
        gem.setImage(getSpace().getGame().getAssetManager(), "ui/hud/radar_grid.png", true);
        //gem.setLocalScale(1, 0.6f, 1);
        gem.rotate(-90 * FastMath.PI / 180, 0f, 0f);
        gem.setLocalTranslation(-0.5f, 0f, 0.5f);
        
        // INIT INTERCEPTOR ICON
        this.blippic_interceptor = new Picture("interceptor");
        this.blippic_interceptor.setImage(getSpace().getGame().getAssetManager(), "ui/hud/radar_interceptor.png", true);
        this.blippic_interceptor.setLocalTranslation(-0.5f, -1f, -0.5f);
        
        // INIT FIGHTER ICON
        this.blippic_fighter = new Picture("fighter");
        this.blippic_fighter.setImage(getSpace().getGame().getAssetManager(), "ui/hud/radar_fighter.png", true);
        this.blippic_fighter.setLocalTranslation(-0.5f, -1f, -0.5f);
        
        // INIT BOMBER ICON
        this.blippic_bomber = new Picture("bomber");
        this.blippic_bomber.setImage(getSpace().getGame().getAssetManager(), "ui/hud/radar_bomber.png", true);
        this.blippic_bomber.setLocalTranslation(-0.5f, -1f, -0.5f);
        
        // INIT CAPITAL ICON
        this.blippic_capital = new Picture("capital");
        this.blippic_capital.setImage(getSpace().getGame().getAssetManager(), "ui/hud/radar_capital.png", true);
        this.blippic_capital.setLocalTranslation(-0.5f, -1f, -0.5f);
        
        // INIT UNKNOWN ICON
        this.blippic_unknown = new Picture("unknown");
        this.blippic_unknown.setImage(getSpace().getGame().getAssetManager(), "ui/hud/radar_unknown.png", true);
        this.blippic_unknown.setLocalTranslation(-0.5f, -1f, -0.5f);
        
        // INIT LINE
        Line blipLineMesh = new Line(new Vector3f(0, -0.5f, 0), new Vector3f(0, 0.5f, 0)); //new Box(0.5f, 0.5f, 0.5f);
        blipLineMesh.setLineWidth(1f);
        this.blipLine = new Geometry("blipline", blipLineMesh);
        Material blipMat = new Material(getSpace().getGame().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        blipMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        blipMat.getAdditionalRenderState().setWireframe(true);
        this.blipLine.setMaterial(blipMat);
        
        radarNode.rotate(radarAngle * FastMath.DEG_TO_RAD, 0f, 0f);
        radarNode.attachChild(gem);

        targetList = new Node("targets");
        radarNode.attachChild(targetList);
        
        this.addPicture("radar", radarNode);
        this.scalePicture("radar", 1f);
        radarNode.setLocalScale(1, 1, 1);
        this.movePicture("radar", 0.5f, 0.5f, 0.5f, 0.5f);
        this.showPicture("radar");
    }
    
    public void clearForRedraw(Spatial self) {
        ObjectInfoControl selfObject = self.getControl(ObjectInfoControl.class);
        targetList.detachAllChildren();
        SensorControl sensors = self.getControl(SensorControl.class);
        WeaponSystemControl weapons = self.getControl(WeaponSystemControl.class);
        FlightControl fc = self.getControl(FlightControl.class);
        drawBlip(selfObject.getTargetInformation(selfObject, null), self, 0f, null, weapons, fc, sensors);
    }
    
    public void drawTarget(float tpf, Spatial self, TargetInformation target) {
        // GET NEEDED CONTROLS
        SensorControl sensors = self.getControl(SensorControl.class);
        WeaponSystemControl weapons = self.getControl(WeaponSystemControl.class);
        FlightControl fc = self.getControl(FlightControl.class);
        
        if ( sensors == null )
            return;
        
        drawBlip(target, self, 0f, null, weapons, fc, sensors);
    }
    
    public void drawMissile(float tpf, Spatial self, TargetInformation target) {
        // GET NEEDED CONTROLS
        SensorControl sensors = self.getControl(SensorControl.class);
        WeaponSystemControl weapons = self.getControl(WeaponSystemControl.class);
        FlightControl fc = self.getControl(FlightControl.class);
        
        if ( sensors == null )
            return;
        
        drawBlip(target, self, 0.025f, null, weapons, fc, sensors);
    }
    
    @Override
    public void updateItem(float tpf, Spatial spatial) {
        // GET NEEDED CONTROLS
        SensorControl sensors = spatial.getControl(SensorControl.class);
        //WeaponSystemControl weapons = spatial.getControl(WeaponSystemControl.class);
        //FlightControl fc = spatial.getControl(FlightControl.class);
        
        if ( sensors == null )
            return;
        
        // WARNINGS AND SOUNDS
        if ( sensors.getLockWarning()) {
            getSpace().getGame().getAudioManager().playSoundLoop(this.targetWarning);
        } else {
            getSpace().getGame().getAudioManager().stopSoundLoop(this.targetWarning);
        }
        
        if ( sensors.getMissileLaunchWarning() ) {
            getSpace().getGame().getAudioManager().playSound(this.launchWarning);
        }
        
/*        
        // COMPOSE RADAR SCREEN
        targetList.detachAllChildren();
        TargetInformation[] targets = sensors.getTargetList();
        for ( TargetInformation target : targets ) {
            drawBlip(target, spatial, 0f, null, weapons, fc, sensors);
        }
        
        // DISPLAY MISSILES ON RADAR SCREEN
        MissileWeaponControl[] missiles = sensors.getMissiles();
        for ( MissileWeaponControl missile : missiles ) {
            TargetInformation missileTarget = new TargetInformation(missile.getSpatial());
            missileTarget.update(spatial, null);
            drawBlip(missileTarget, spatial, 0.025f, ColorRGBA.White, weapons, fc, sensors);
        }
*/
    }

    private void drawBlip(TargetInformation target, Spatial spatial, float size, ColorRGBA color, WeaponSystemControl weapons, FlightControl flight, SensorControl sensors) {        
        ObjectInfoControl self = spatial.getControl(ObjectInfoControl.class);
        Vector3f vec = new Vector3f(target.getDirection(self).negate());

        float v = vec.length() / sensors.getRange();
        v = Math.min(1, v);
        v = 1 - (1 - v) * (1 - v) * (1 - v);
        float distance = 0.5f - ((0 * v) + (0.5f * (1 - v)));
        distance = Math.max(0.125f, distance);
        if ( size <= 0f ) {
            size = 0.08f * (1 - distance);
        }

        vec.normalizeLocal().multLocal(distance);
        spatial.getWorldRotation().inverse().multLocal(vec); // 
        vec.y *= -1;

        Geometry blippic = null;
        if ( "fighter".equalsIgnoreCase(target.getObject().getObjectType()) ) {
            blippic = this.blippic_fighter.clone();
        } else if ( "interceptor".equalsIgnoreCase(target.getObject().getObjectType()) ) {
            blippic = this.blippic_interceptor.clone();
        } else if ( "bomber".equalsIgnoreCase(target.getObject().getObjectType()) ) {
            blippic = this.blippic_bomber.clone();
        } else if ( "capital".equalsIgnoreCase(target.getObject().getObjectType()) ) {
            blippic = this.blippic_capital.clone();
        } else {
            blippic = this.blippic_unknown.clone();
        }
        if ( color == null ) {
            if ( weapons != null && weapons.getTarget() != null && target.getObject().equals(weapons.getTarget().getObject()) ) {
                color = ColorRGBA.Yellow;
            } else if ( target.getFOF(self) == TargetInformation.FOF_FOE ) {
                color = ColorRGBA.Red;
            } else if ( target.getFOF(self) == TargetInformation.FOF_FRIEND ) {
                if (target.getObject().getFlight() == self.getFlight()) {
                    color = ColorRGBA.Cyan;
                } else {
                    color = ColorRGBA.Green;
                }
            } else {
                color = ColorRGBA.White;
            }
        }
        blippic.getMaterial().setColor("Color", color);
        
        Node blip = new Node("blip");
        blippic.setLocalTranslation(-0.5f, -0.5f, 0f);
        blip.attachChild(blippic);
        blip.setLocalTranslation(vec);
        blip.setLocalScale(size, size, size);
        blip.rotate(-radarAngle * FastMath.DEG_TO_RAD, 0f, 0f);
        this.targetList.attachChild(blip);

        //
        Geometry blipLine = this.blipLine.clone();
        if ( color == null ) {
            if ( weapons != null && target.equals(weapons.getTarget()) ) {
                color = new ColorRGBA(1f, 1f, 0f, 0.6f);
            } else if ( target.getFOF(self) == TargetInformation.FOF_FOE ) {
                color = new ColorRGBA(1f, 0f, 0.0f, 0.6f);
            } else if ( target.getFOF(self) == TargetInformation.FOF_FRIEND ) {
                color = new ColorRGBA(0.0f, 1f, 0.0f, 0.6f);
            } else {
                color = new ColorRGBA(1f, 1f, 1f, 0.6f);
            }
        }
        blippic.getMaterial().setColor("Color", color);
        
        blipLine.setLocalScale(1f, vec.getY(), 1f);
        blipLine.setLocalTranslation(vec.x, vec.y / 2, vec.z);
        this.targetList.attachChild(blipLine);
    }
    
    @Override
    public void initItem(Spatial spatial) {
        AudioNode targetWarningNode = new AudioNode(getSpace().getGame().getAssetManager(), "sounds/radar_scan.ogg", false);
        targetWarningNode.setLooping(true);
        targetWarningNode.setVolume(1f);
        //this.targetWarning.setMaxDistance(200f);
        //this.targetWarning.setRefDistance(10f);
        targetWarningNode.setPositional(false);
        targetWarningNode.setDirectional(false);
        this.targetWarning = new AudioEffect(targetWarningNode);
        
        //((Node)spatial).attachChild(this.targetWarning);
        
        AudioNode launchWarningNode = new AudioNode(getSpace().getGame().getAssetManager(), "sounds/radar_launchwarning.ogg", false);
        launchWarningNode.setLooping(false);
        launchWarningNode.setVolume(1f);
        //this.targetWarning.setMaxDistance(200f);
        //this.targetWarning.setRefDistance(10f);
        launchWarningNode.setPositional(false);
        launchWarningNode.setDirectional(false);
        this.launchWarning = new AudioEffect(launchWarningNode);
        //((Node)spatial).attachChild(this.launchWarning);
    }

    @Override
    public void cleanupItem(Spatial spatial) {
        //((Node)spatial).detachChild(this.targetWarning);
        getSpace().getGame().getAudioManager().stopSoundLoop(this.targetWarning);
        
        //((Node)spatial).detachChild(this.launchWarning);
        getSpace().getGame().getAudioManager().stopSoundLoop(this.launchWarning);
    }

    @Override
    public void renderItem(Spatial spatial, RenderManager rm, ViewPort vp) {
        
    }
    
}
