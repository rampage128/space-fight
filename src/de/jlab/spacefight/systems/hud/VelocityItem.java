/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.hud;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.systems.PhysicsControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.ui.ingame.hud.HudItem;

/**
 *
 * Displays UI for velocity and throttle control
 * 
 * @author rampage
 */
public class VelocityItem extends HudItem {

    public static final float ITEM_HEIGHT = 0.25f;
    public static final float ITEM_WIDTH = 0.04f;
    
    public static final float ITEM_X = 0.25f;
    public static final float ITEM_Y = 0.5f;
    
    private float cruiseFlash = 0f;
    
    private BitmapText speedText;
    
    public VelocityItem(SpaceAppState space) {
        super("velocity", space);
        
        scale(0.04f, 0.25f);
        move(0.25f, 0.5f, 0.5f, 0.5f);
        
        BitmapFont font = getSpace().getGame().getAssetManager().loadFont("ui/fonts/pirulen.fnt");
        speedText = new BitmapText(font, false);
        speedText.setSize(1);
        speedText.setBox(new Rectangle(-2, -1, 2, 1));
        speedText.setVerticalAlignment(BitmapFont.VAlign.Center);
        speedText.setAlignment(BitmapFont.Align.Right);
        speedText.setColor(ColorRGBA.White);
        this.addPicture("speedText", speedText);
        this.scalePicture("speedText", 0.125f);
        this.movePicture("speedText", 0f, 0f, 1.5f, 0.5f);
        this.showPicture("speedText");
        
        Picture background = new Picture("background");
        background.setImage(getSpace().getGame().getAssetManager(), "ui/hud/velocity_background.png", true);
        addPicture("background", background);
        scalePicture("background", 1f, 1f);
        movePicture("background", 0.5f, 0.5f, 0.5f, 0.5f);
        showPicture("background");
        
        Picture display = new Picture("display");
        display.setImage(getSpace().getGame().getAssetManager(), "ui/hud/velocity_display.png", true);
        addPicture("display", display);
        scalePicture("display", 0, 0);
        showPicture("display");
        
        Picture throttle = new Picture("throttle");
        throttle.setImage(getSpace().getGame().getAssetManager(), "ui/hud/velocity_throttle.png", true);
        addPicture("throttle", throttle);
        scalePicture("throttle", 0.25f, 0.125f);
        showPicture("throttle");
    }
    
    @Override
    public void updateItem(float tpf, Spatial spatial) {
        FlightControl flight = spatial.getControl(FlightControl.class);
        PhysicsControl physics = spatial.getControl(PhysicsControl.class);
        
        if (flight != null && physics != null) {
            Vector3f velocity = physics.getLinearVelocity();
            float throttle = Math.abs(flight.getThrottle());

            if (flight.getCruise()) {
                if ( cruiseFlash >= 0.5f ) {
                    showPicture("throttle");
                    showPicture("display");
                } else {
                    hidePicture("throttle");
                    hidePicture("display");
                }
                if ( cruiseFlash >= 1 ) {
                    cruiseFlash = 0;
                }
                cruiseFlash += tpf;
            } else {
                if (cruiseFlash > 0f) {
                    showPicture("throttle");
                    showPicture("display");
                    cruiseFlash = 0;
                }
            }

            physics.getPhysicsRotation().inverse().multLocal(velocity);
            float z = Math.abs(velocity.z);
            
            this.speedText.setText(Integer.toString(Math.round(velocity.z)));
            this.scalePicture("speedText", 0.125f);
            this.movePicture("speedText", 0f, Math.min(1, z / flight.getTopspeed()), 1f, 0f);

            movePicture("display", 0, 0, 0f, 0f);
            movePicture("throttle", 0f, throttle, 1f, 0.5f);
            if (flight.getCruise()) {
                scalePicture("display", Math.min(1, z / flight.getCruiseEnergy()), Math.min(1, z / flight.getCruiseEnergy()));
            } else {
                scalePicture("display", Math.min(1, z / flight.getTopspeed()), Math.min(1, z / flight.getTopspeed()));
            }
        } else {
            hidePicture("display");
            hidePicture("throttle");
            hidePicture("background");
            hidePicture("speedText");
        }
    }
    
    @Override
    public void initItem(Spatial spatial) {
        // NOTHING SPECIAL TO DO HERE
    }

    @Override
    public void cleanupItem(Spatial spatial) {
        // NOTHING SPECIAL TO DO HERE
    }

    @Override
    public void renderItem(Spatial spatial, RenderManager rm, ViewPort vp) {
        
    }
    
}
