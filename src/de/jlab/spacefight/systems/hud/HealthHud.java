/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.hud;

import de.jlab.spacefight.ui.ingame.hud.HudItem;
import com.jme3.effect.ParticleEmitter;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.DamageControl;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.systems.ShieldControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import java.util.List;

/**
 * Displays health-status of an object with overhead view and percentage of
 * hull and shields.
 * 
 * @author Stefan
 */
public class HealthHud extends HudItem {

    private String shipid;
    private Node testship;
    
    //private Picture ship;
    private BitmapText healthText;
    private BitmapText frontShieldText;
    private BitmapText rearShieldText;
    
    private BitmapText additionalText;
    
    private Picture frontShieldIcon;
    private Picture rearShieldIcon;
    
    private Spatial player;
    
/*    
    private Game game;
    private SpaceAppState space;
    private Node healthNode;
    private float initialize;
*/    

    public HealthHud(SpaceAppState space, Spatial player) {
        super("health", space);

        this.player = player;
        
        this.scale(0.125f);
        this.move(0.1f, 0f, 0.5f, 0f);
        
        this.testship = new Node("hull");
        this.addPicture("hull", testship);
        this.scalePicture("hull", 0.5f);
        this.movePicture("hull", 0.5f, 0.5f, 0f, 0f);
        this.showPicture("hull");
        
        /*
        ship = new Picture("Health HUD");
        ship.setImage(game.getAssetManager(), "ui/hud/ship.png", true);
        this.addPicture("ship", ship);
        this.scalePicture("ship", 1, 1);
        this.movePicture("ship", 0.5f, 0.5f, 0.5f, 0.5f);
        this.showPicture("ship");
         */
        
        //ship.setWidth(game.getContext().getSettings().getWidth() / 10);
        //ship.setHeight(game.getContext().getSettings().getHeight() / 10);
        //ship.setLocalTranslation(ship.getLocalScale().getX() * -0.5f, ship.getLocalScale().getY() * -0.5f, 0);
        
        BitmapFont font = getSpace().getGame().getAssetManager().loadFont("ui/fonts/pirulen.fnt");
        healthText = new BitmapText(font, false);
        healthText.setSize(1);
        healthText.setColor(ColorRGBA.Red);
        this.addPicture("hullText", healthText);
        this.scalePicture("hullText", 0.25f, 0.25f);
        this.movePicture("hullText", 1f, 1f, 0.5f, 0.5f);
        this.showPicture("hullText");
        
        frontShieldText = new BitmapText(font, false);
        frontShieldText.setSize(1);
        frontShieldText.setColor(ColorRGBA.Cyan);
        this.addPicture("frontShieldText", frontShieldText);
        this.scalePicture("frontShieldText", 0.25f, 0.25f);
        this.movePicture("frontShieldText", 1f, 1f, 0.5f, 0.5f);
        this.showPicture("frontShieldText");
        
        rearShieldText = new BitmapText(font, false);
        rearShieldText.setSize(1);
        rearShieldText.setColor(ColorRGBA.Cyan);
        this.addPicture("rearShieldText", rearShieldText);
        this.scalePicture("rearShieldText", 0.25f, 0.25f);
        this.movePicture("rearShieldText", 1f, 1f, 0.5f, 0.5f);
        this.showPicture("rearShieldText");
        
        if (player != null) {
            additionalText = new BitmapText(font, false);
            additionalText.setSize(0.75f);
            additionalText.setColor(ColorRGBA.White);
            this.addPicture("additionalText", additionalText);
            this.scalePicture("additionalText", 0.25f, 0.25f);
            this.movePicture("additionalText", 1.2f, 1f, 0.0f, 0.5f);
            this.showPicture("additionalText");
        }
        
        frontShieldIcon = new Picture("frontShieldIcon");
        frontShieldIcon.setImage(getSpace().getGame().getAssetManager(), "ui/hud/health_shield.png", true);
        this.addPicture("frontShieldIcon", frontShieldIcon);
        this.scalePicture("frontShieldIcon", 1f, 0.5f);
        this.movePicture("frontShieldIcon", 0.5f, 0.75f, 0.5f, 0.5f);
        this.showPicture("frontShieldIcon");
        
        rearShieldIcon = new Picture("rearShieldIcon");
        rearShieldIcon.setImage(getSpace().getGame().getAssetManager(), "ui/hud/health_shield.png", true);
        this.addPicture("rearShieldIcon", rearShieldIcon);
        this.scalePicture("rearShieldIcon", 1f, 0.5f);
        this.movePicture("rearShieldIcon", 0.5f, 0.25f, 0.5f, 0.5f);
        this.rotatePicture("rearShieldIcon", 180f * FastMath.DEG_TO_RAD);
        this.showPicture("rearShieldIcon");
        
        //healthText.setLocalTranslation(ship.getLocalTranslation().getX() + ship.getLocalScale().getX() - healthText.getLineWidth(), ship.getLocalTranslation().getY() + ship.getLocalScale().getY(), 0);
        
        /*
        healthNode = new Node("healthHud");
        healthNode.attachChild(ship);
        healthNode.attachChild(healthText);
        healthNode.setLocalTranslation(game.getContext().getSettings().getWidth() * 0.05f, game.getContext().getSettings().getHeight() * 0.1f, 0);
         */
    }

    @Override
    public void updateItem(float tpf, Spatial spatial) {
        if (shipid == null || !shipid.equalsIgnoreCase(spatial.getName())) {
            this.testship.detachAllChildren();
            this.testship.getLocalLightList().clear();
            
            Spatial lod100 = ((Node)spatial).getChild("LOD100");
            if (lod100 == null) {
                lod100 = ((Node)spatial).getChild("Hull");
            }
            if (lod100 != null) {
                Spatial body = lod100.clone();
                //TargetInformation targetInfo = new TargetInformation(body);
                //targetInfo.update(body, null);
                float shipsize = ObjectInfoControl.calcSize(body);
                shipid = ((Node)spatial).getName();

                //Material mat = new Material(getGame().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                //mat.setColor("Color", ColorRGBA.Blue);
                
                body.setCullHint(Spatial.CullHint.Never);
                body.setLocalScale(0.75f / shipsize); 
                body.rotate(-90 * FastMath.DEG_TO_RAD, 180 * FastMath.DEG_TO_RAD, 0f);
                body.setQueueBucket(Bucket.Gui);
                body.setLocalTranslation(0f, 0f, 0f);
                
                AmbientLight light = new AmbientLight();             
                light.setColor(ColorRGBA.White);
                this.testship.addLight(light);
                
                List<Spatial> children = ((Node)body).getChildren();
                for ( Spatial child : children ) {
                    //child.setMaterial(mat);
                    child.setShadowMode(ShadowMode.Off);
                    child.setQueueBucket(Bucket.Gui);
                    if ( child instanceof ParticleEmitter ) {
                        child.removeFromParent();
                    } if ( child instanceof Geometry ) {
                        if (((Geometry)child).getMesh().getNumLodLevels() > 0) {
                            ((Geometry)child).setLodLevel(0);
                        }
                    }
                }
                
                testship.attachChild(body);
            }
        }
        
        if (spatial.getControl(DamageControl.class) != null) {
            healthText.setText(String.valueOf(Math.max(0, (int)Math.ceil(spatial.getControl(DamageControl.class).getHull() * 100))));
        }
        if ( spatial.getControl(ShieldControl.class) != null) {
            frontShieldText.setText(String.valueOf(Math.max(0, (int)Math.ceil(spatial.getControl(ShieldControl.class).getFrontShield() * 100))));
            rearShieldText.setText(String.valueOf(Math.max(0, (int)Math.ceil(spatial.getControl(ShieldControl.class).getRearShield() * 100))));
            
            ColorRGBA color = ColorRGBA.Cyan.clone();
            ColorRGBA color2 = ColorRGBA.Cyan.clone();
            
            color.a = spatial.getControl(ShieldControl.class).getFrontShield();
            color2.a = spatial.getControl(ShieldControl.class).getRearShield();
            
            frontShieldIcon.getMaterial().setColor("Color", color);
            rearShieldIcon.getMaterial().setColor("Color", color2);
        } else {
            hidePicture("frontShieldText");
            hidePicture("rearShieldText");
            hidePicture("frontShieldIcon");
            hidePicture("rearShieldIcon");
        }
        
        this.movePicture("hullText", 0f, 0.62f, 1.9f, 0f);
        this.movePicture("frontShieldText", 0f, 1f, 1.5f, 0f);
        this.movePicture("rearShieldText", 0f, 0.22f, 1.5f, 0f);
        
        if (player != null) {
            Task task = spatial.getControl(ObjectInfoControl.class).getCurrentTask();
            
            StringBuilder addT = new StringBuilder();
            addT.append("CSN: ").append(spatial.getControl(ObjectInfoControl.class).getCallsign()).append("\n");
            addT.append("TSK: ").append(task == null ? "-" : task.getName()).append("\n");
            if (spatial.getControl(FlightControl.class) != null) {
                addT.append("SPD: ").append((int)Math.round(spatial.getControl(FlightControl.class).getForwardVelocity())).append("m/s\n");
                if (player.getControl(FlightControl.class) != null) {
                    addT.append("ASP: ").append((int)Math.round(spatial.getControl(FlightControl.class).getFacing().angleBetween(player.getControl(FlightControl.class).getFacing()) * FastMath.RAD_TO_DEG)).append("°\n");
                }
            }

            addT.append("DST: ").append((int)Math.round(player.getWorldTranslation().subtract(spatial.getWorldTranslation()).length())).append("m\n");
                        
            this.additionalText.setText(addT);
            this.movePicture("additionalText", 1.2f, 1.1f, 0.0f, 0.5f);
        }
        
        
        
/*        
        if (initialize > 0) {
            initialize -= tpf;
            healthNode.rotate(0.0f, 0.0f, Math.max(initialize * 7, 3f) * tpf);
        } else {
            if (healthNode.getLocalRotation().getZ() > -0.1f && healthNode.getLocalRotation().getZ() <= 0f) {
                healthNode.rotate(0.0f, 0.0f, Math.abs(healthNode.getLocalRotation().getZ()));
            } else {
                healthNode.rotate(0.0f, 0.0f, 3.0f * tpf);
            }
        }
  */
    }
    
    @Override
    public void initItem(Spatial spatial) {
        // NOTHING SPECIAL TO DO HERE
    }

    @Override
    public void cleanupItem(Spatial spatial) {
        this.testship.detachAllChildren();
        // NOTHING SPECIAL TO DO HERE
    }

    @Override
    public void renderItem(Spatial spatial, RenderManager rm, ViewPort vp) {
        
    }
}
