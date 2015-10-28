/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.hud;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;
import de.jlab.spacefight.ui.ingame.hud.HudItem;

/**
 *
 * @author rampage
 */
public class WeaponItem extends HudItem {

    private BitmapText energyText;
    private BitmapText modeText;
    
    public WeaponItem(SpaceAppState space) {
        super("weapons", space);
        
        this.scale(0.125f);
        this.move(0.5f, 1f, 0.5f, 1f);
        
        BitmapFont font = getSpace().getGame().getAssetManager().loadFont("ui/fonts/pirulen.fnt");
        energyText = new BitmapText(font, false);
        energyText.setSize(1);
        energyText.setColor(ColorRGBA.Red);
        this.addPicture("energyText", energyText);
        this.scalePicture("energyText", 0.25f, 0.25f);
        this.movePicture("energyText", 1f, 1f, 0.5f, 0.5f);
        this.showPicture("energyText");
        
        modeText = new BitmapText(font, false);
        modeText.setSize(1);
        modeText.setColor(ColorRGBA.Red);
        this.addPicture("modeText", modeText);
        this.scalePicture("modeText", 0.25f, 0.25f);
        this.movePicture("modeText", 1f, 0.5f, 0.5f, 0.5f);
        this.showPicture("modeText");
    }
    
    @Override
    public void initItem(Spatial spatial) {
        
    }

    @Override
    public void updateItem(float tpf, Spatial spatial) {
        WeaponSystemControl weapons = spatial.getControl(WeaponSystemControl.class);
        if (weapons == null) {
            this.hidePicture("energyText");
        } else {
            this.showPicture("energyText");
            switch(weapons.getPrimaryMode()) {
                case WeaponSystemControl.MODE_SINGLE:
                    this.modeText.setText("SINGLE");
                    this.modeText.setColor(ColorRGBA.Green);
                    break;
                case WeaponSystemControl.MODE_HALF:
                    this.modeText.setText("HALF");
                    this.modeText.setColor(ColorRGBA.Yellow);
                    break;
                case WeaponSystemControl.MODE_FULL:
                    this.modeText.setText("FULL");
                    this.modeText.setColor(ColorRGBA.Red);
                    break;
            }
            if (weapons.getPrimaryEnergy() >= 0.6f) {
                this.energyText.setColor(ColorRGBA.Green);
            } else if (weapons.getPrimaryEnergy() >= 0.3f) {
                this.energyText.setColor(ColorRGBA.Yellow);
            } else {
                this.energyText.setColor(ColorRGBA.Red);
            }
            this.energyText.setText(Integer.toString((int)Math.ceil(weapons.getPrimaryEnergy() * 100)));
        }
    }

    @Override
    public void renderItem(Spatial spatial, RenderManager rm, ViewPort vp) {
        
    }

    @Override
    public void cleanupItem(Spatial spatial) {
        this.hidePicture("energyText");
    }
    
}
