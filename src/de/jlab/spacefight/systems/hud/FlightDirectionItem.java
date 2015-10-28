/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.hud;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import de.jlab.spacefight.ui.ingame.hud.HudItem;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.systems.flight.FlightControl;

/**
 *
 * @author rampage
 */
public class FlightDirectionItem extends HudItem {

    public FlightDirectionItem(SpaceAppState space) {
        super("flightdirection", space);
        
        this.scale(1f);
        this.move(0.5f, 0.5f, 0.5f, 0.5f);
        
        Picture rudder = new Picture("rudder");
        rudder.setImage(getSpace().getGame().getAssetManager(), "ui/hud/flightdirection_rudder.png", true);
        addPicture("rudder", rudder);
        scalePicture("rudder", 0.03125f, 0.03125f);
        movePicture("rudder", 0.5f, 0.5f, 0.5f, 0.5f);
        showPicture("rudder");
        
        Picture strafe = new Picture("strafe");
        strafe.setImage(getSpace().getGame().getAssetManager(), "ui/hud/flightdirection_strafe.png", true);
        addPicture("strafe", strafe);
        scalePicture("strafe", 0.03125f, 0.03125f);
        movePicture("strafe", 0.5f, 0.5f, 0.5f, 0.5f);
        showPicture("strafe");
        
/*        
        rudder.setWidth(game.getContext().getSettings().getHeight() / 16);
        rudder.setHeight(game.getContext().getSettings().getHeight() / 16);
        rudder.setPosition(game.getContext().getSettings().getWidth() / 2 - game.getContext().getSettings().getHeight() / 32, game.getContext().getSettings().getHeight() / 2 - game.getContext().getSettings().getHeight() / 32);
*/        
        
    }
    
    @Override
    public void updateItem(float tpf, Spatial spatial) {        
        FlightControl flightControl = spatial.getControl(FlightControl.class);
        if ( flightControl != null ) {
            float rudder = 1 - (flightControl.getRudder() + 1) / 2;
            float elevator = 1 - (flightControl.getElevator() + 1) / 2;
            float strafe = -flightControl.getStrafe();
            float lift = flightControl.getLift();
            movePicture("rudder", rudder, elevator, 0.5f, 0.5f);
            movePicture("strafe", rudder + strafe * (0.03125f * 0.5f), elevator + lift * (0.03125f * 0.5f), 0.5f, 0.5f);
        } else {
            hidePicture("rudder");
            hidePicture("strafe");
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
