/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission;

import de.jlab.spacefight.mission.structures.Kill;

/**
 *
 * @author rampage
 */
public interface KillListener {
    
    public void onKill(Kill kill);
    public void update(float tpf);
    
}
