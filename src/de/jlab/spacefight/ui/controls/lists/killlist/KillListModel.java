/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.controls.lists.killlist;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Kill;
import de.lessvoid.nifty.tools.Color;

/**
 *
 * @author rampage
 */
public final class KillListModel {
    
    private Kill kill;
    private long time = System.currentTimeMillis();

    public KillListModel(Kill kill) {
        this.kill = kill;
    }

    public Color getOriginColor() {
        ObjectInfoControl client = Game.get().getPlayer().getClient();
        if (this.kill.getOrigin().getFaction() == null || client == null) {
            return Color.WHITE;
        }
        
        if (this.kill.getOrigin().getFaction() != client.getFaction()) {
            return new Color("#f00f");
        } else {
            return new Color("#0f0f");
        }
    }
    
    public String getOrigin() {
        if (kill.getOrigin().getPlayer() != null) {
            return kill.getOrigin().getPlayer().getNickname();
        } else if (kill.getOrigin().getCallsign() != null) {
            return kill.getOrigin().getCallsign();
        }
        return kill.getOrigin().getId();
    }

    public Color getTargetColor() {
        ObjectInfoControl client = Game.get().getPlayer().getClient();
        if (this.kill.getTarget().getFaction() == null || client == null) {
            return Color.WHITE;
        }
        
        if (this.kill.getTarget().getFaction() != client.getFaction()) {
            return new Color("#f00f");
        } else {
            return new Color("#0f0f");
        }
    }
    
    public String getTarget() {
        if (kill.getTarget().getPlayer() != null) {
            return kill.getTarget().getPlayer().getNickname();
        } else if (kill.getTarget().getCallsign() != null) {
            return kill.getTarget().getCallsign();
        }
        return kill.getTarget().getId();
    }   
    
    public String getDamage() {
        return new StringBuilder("[").append(kill.getDamageName()).append("]").toString();
    }
    
    public boolean isTimedOut(float limit) {
        return limit * 1000 < System.currentTimeMillis() - this.time;
    }
    
}