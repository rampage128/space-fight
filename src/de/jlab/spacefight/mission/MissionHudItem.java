/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.mission.structures.Faction;
import de.jlab.spacefight.ui.ingame.hud.HudItem;

/**
 *
 * @author rampage
 */
@Deprecated
public class MissionHudItem extends HudItem {

    private AbstractMission mission;
    
    private BitmapText stats;
    
    private Faction[] factions;
    
    public MissionHudItem(AbstractMission mission, SpaceAppState space) {
        super("missiondata", space);
        this.mission = mission;

        this.scale(0.125f);
        this.move(0f, 1f, 0f, 1f);
        
        BitmapFont font = getSpace().getGame().getAssetManager().loadFont("ui/fonts/pirulen.fnt");
        stats = new BitmapText(font, false);
        stats.setSize(1);
        stats.setColor(ColorRGBA.Red);
        this.addPicture("stats", stats);
        this.scalePicture("stats", 0.125f, 0.125f);
        this.movePicture("stats", 0.5f, 0.5f, 0.5f, 0.5f);
        this.showPicture("stats");
        
        this.factions = this.mission.getFactions();
    }
    
    @Override
    public void updateItem(float tpf, Spatial spatial) {
        StringBuilder statsText = new StringBuilder();
        for ( Faction faction : factions ) {
            //statsText.append("\n").append(faction.getName()).append(": ").append(faction.getTicketCount());
        }
        this.stats.setText(statsText);
    }

    @Override
    public void initItem(Spatial spatial) {
        
    }

    @Override
    public void cleanupItem(Spatial spatial) {
        
    }

    @Override
    public void renderItem(Spatial spatial, RenderManager rm, ViewPort vp) {
        
    }
    
}
