/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.hud;

import de.jlab.spacefight.ui.ingame.hud.HudItem;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Task;

/**
 *
 * @author rampage
 */
public class TaskIndicatorItem extends HudItem {
    
    private Vector3f screenPos = new Vector3f(0f, 0f, 0f);
    
    private boolean onScreen = false;
    private boolean newTarget = true;
    
    private Vector3f taskPosition = new Vector3f(0f, 0f, 0f);
    
    public TaskIndicatorItem(SpaceAppState space) {
        super("task", space);
        
        this.move(0.5f, 0.5f, 0.5f, 0.5f);
        this.scale(1f, 1f);
        
        Picture icon = new Picture("attack");
        icon.setImage(getSpace().getGame().getAssetManager(), "ui/hud/taskindicator_attack.png", true);
        addPicture("attack", icon);
        scalePicture("attack", 0.06f);
        movePicture("attack", 0.5f, 0.5f, 0.5f, 0.5f);
        
        icon = new Picture("move");
        icon.setImage(getSpace().getGame().getAssetManager(), "ui/hud/taskindicator_move.png", true);
        addPicture("move", icon);
        scalePicture("move", 0.06f);
        movePicture("move", 0.5f, 0.5f, 0.5f, 0.5f);
        
        icon = new Picture("patrol");
        icon.setImage(getSpace().getGame().getAssetManager(), "ui/hud/taskindicator_patrol.png", true);
        addPicture("patrol", icon);
        scalePicture("patrol", 0.06f);
        movePicture("patrol", 0.5f, 0.5f, 0.5f, 0.5f);
        
        icon = new Picture("guard");
        icon.setImage(getSpace().getGame().getAssetManager(), "ui/hud/taskindicator_guard.png", true);
        addPicture("guard", icon);
        scalePicture("guard", 0.06f);
        movePicture("guard", 0.5f, 0.5f, 0.5f, 0.5f);
        
        /*
        Picture arrow = new Picture("arrow");
        arrow.setImage(getSpace().getGame().getAssetManager(), "ui/hud/targetindicator_arrow.png", true);
        addPicture("arrow", arrow);
        scalePicture("arrow", 0.04f);
        movePicture("arrow", 0.5f, 0.5f, 0.5f, 0.5f);
        */
    }
    
    public void positionCentered(float x, float y) {
        
    }

    public void attachToGui() {
        
    }

    public void updateItem(float tpf, Spatial spatial) {
        ObjectInfoControl clientControl = spatial.getControl(ObjectInfoControl.class);
                
        boolean hasTask = false;
        
        String pictureName = "move";
        
        if ( clientControl != null ) {
            Task task = clientControl.getCurrentTask();
            if (task != null) {
                ObjectInfoControl target = task.getTargetObject(getSpace().getMission());
                if (target != null) {
                    taskPosition.set(target.getSpatial().getWorldTranslation());
                } else {
                    taskPosition.set(task.getPosition());
                }
                hasTask = true;                
                pictureName = task.getType();
                showPicture(pictureName);
            }
        }
                
        if ( !hasTask ) {           
            hidePicture("attack");
            hidePicture("move");
            hidePicture("patrol");
            hidePicture("guard");
            //hidePicture("arrow");
            onScreen = false;
            newTarget = true;
            return;
        }
                
        // RETRIEVE TARGET POSITION
        getCamera().getScreenCoordinates(this.taskPosition, this.screenPos);

        //this.screenPos.z < 1 &&  && this.screenPos.x < camera.getWidth() && this.screenPos.y > 0 && this.screenPos.y < camera.getHeight()
        
        if ( this.screenPos.z < 1 && this.screenPos.x > 0 && this.screenPos.x < getCamera().getWidth() && this.screenPos.y > 0 && this.screenPos.y < getCamera().getHeight() ) {
            if ( !onScreen ) {
                scalePicture(pictureName, 0.08f);
                //showPicture(pictureName);
                //hidePicture("arrow");
                onScreen = true;
            }
                               
            movePicture(pictureName, this.screenPos.x / getCamera().getWidth(), this.screenPos.y / getCamera().getHeight(), 0.5f, 0.5f);            
        } else {
            if ( onScreen || newTarget ) {
                scalePicture(pictureName, 0.04f);
                //hidePicture(pictureName);
                //showPicture("arrow");
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
            movePicture(pictureName, x, y, xalign, yalign);
            
            /*
            if ( this.screenPos.z < 1 ) {
                movePicture("arrow", Math.max(0, Math.min(this.screenPos.x / getCamera().getWidth(), 1f)), Math.max(0, Math.min(this.screenPos.y / getCamera().getHeight(), 1f)), 0.5f, 0.5f);
            } else {
                movePicture("arrow", 1 - Math.max(0, Math.min(this.screenPos.x / getCamera().getWidth(), 1f)), 1 - Math.max(0, Math.min(this.screenPos.y / getCamera().getHeight(), 1f)), 0.5f, 0.5f);
            }
            */
        }
    }

    @Override
    public void initItem(Spatial spatial) {
        // NOTHING SPECIAL TO DO HERE
    }

    @Override
    public void cleanupItem(Spatial spatial) {
        hidePicture("attack");
        hidePicture("move");
        hidePicture("patrol");
        hidePicture("guard");
        //hidePicture("arrow");
    }

    @Override
    public void renderItem(Spatial spatial, RenderManager rm, ViewPort vp) {
        
    }
    
}
