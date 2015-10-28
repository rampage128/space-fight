/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.hud;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.mission.structures.Kill;
import de.jlab.spacefight.ui.ingame.hud.HudItem;
import java.util.ArrayList;

/**
 * @Deprecated This is handled by nifty now (@See ingame.xml/Ingame.java)
 * @author rampage
 */
@Deprecated
public class KillHudItem extends HudItem {

    private BitmapText killText;
    
    private float removeTimer = 0f;
    
    private ArrayList<Kill> killList = new ArrayList<Kill>();
    
    public KillHudItem(SpaceAppState space) {
        super("kills", space);
        
        this.scale(0.25f);
        this.move(1f, 1f, 1f, 0.5f);
        
        BitmapFont font = getSpace().getGame().getAssetManager().loadFont("ui/fonts/pirulen.fnt");
        killText = new BitmapText(font, false);
        killText.setSize(1);
        //killText.setBox(new Rectangle(-100, -50, 100, 50));
        //killText.setVerticalAlignment(BitmapFont.VAlign.Top);
        //killText.setAlignment(BitmapFont.Align.Right);
        killText.setColor(ColorRGBA.White);
        this.addPicture("kills", killText);
        this.scalePicture("kills", 0.07f);
        this.movePicture("kills", 0f, 0.5f, 1f, 0.5f);
        this.showPicture("kills");
    }
    
    @Override
    public void initItem(Spatial spatial) {
        
    }

    @Override
    public void updateItem(float tpf, Spatial spatial) {
        boolean changed = false;
        for (int i = 0; i < this.killList.size(); ) {
            Kill kill = this.killList.get(i);
            if (i < this.killList.size() - 10) {
                this.killList.remove(kill);
                changed = true;
                continue;
            }

            float timer = (System.currentTimeMillis() - kill.getSystemTime()) / 1000f;
            if (timer >= 3) {
                this.killList.remove(kill);
                changed = true;
                continue;
            }
            i++;
        }
        
        if (changed) {
            updateText();
        }
    }

    @Override
    public void renderItem(Spatial spatial, RenderManager rm, ViewPort vp) {
        
    }

    @Override
    public void cleanupItem(Spatial spatial) {
        
    }
    
    public void addKill(Kill kill) {
        this.killList.add(kill);
        updateText();
    }
    
    private void updateText() {
        StringBuilder textBuilder = new StringBuilder();
        
        for (Kill kill : this.killList) {
            String origin = kill.getOrigin().getId();
            if (kill.getOrigin().getPlayer() != null) {
                origin = kill.getOrigin().getPlayer().getNickname();
            } else if (kill.getOrigin().getCallsign() != null) {
                origin = kill.getOrigin().getCallsign();
            }
            
            String target = kill.getTarget().getId();
            if (kill.getTarget().getPlayer() != null) {
                target = kill.getTarget().getPlayer().getNickname();
            } else if (kill.getTarget().getCallsign() != null) {
                target = kill.getTarget().getCallsign();
            }
            
            textBuilder.append(origin);
            textBuilder.append(" [").append(kill.getDamageName()).append("] ");
            textBuilder.append(target);
            textBuilder.append("\n");
        }
        
        this.killText.setText(textBuilder.toString());
        killText.setSize(1);
        //killText.setBox(new Rectangle(-100, -50, 100, 50));
        //this.killText.updateLogicalState(0);
        
        //System.out.println(this.killText.getLocalScale() + " - " + this.getPicture("kills").getLocalScale());
        
        //this.scale(0.25f);
        //this.move(1f, 1f, 1f, 1f);
        
        //this.scalePicture("kills", 0.07f);
        //this.movePicture("kills", 0.5f, 0.5f, 0.5f, 0.5f);
    }
    
}
