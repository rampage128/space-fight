/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.hud;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author rampage
 */
public class HudHelper {
    
    private static Vector3f SCREENPOS = new Vector3f();
    
    public static Vector3f getScreenPosition(Vector3f position, Camera camera) {
        Vector3f finalPos = new Vector3f(0, 0, 1);
        
        camera.getScreenCoordinates(position, SCREENPOS);
        
        if ( SCREENPOS.z < 1 && SCREENPOS.x > 0 && SCREENPOS.x < camera.getWidth() && SCREENPOS.y > 0 && SCREENPOS.y < camera.getHeight() ) {
            // ON SCREEN
            finalPos.x = SCREENPOS.x / camera.getWidth();
            finalPos.y = SCREENPOS.y / camera.getHeight();
            finalPos.z = 1;
        } else {
            // OFF SCREEN
            finalPos.z = -1;
            // CALCULATE X AND Y FROM SCREEN CENTER
            finalPos.x = (SCREENPOS.x - camera.getWidth() / 2) / (camera.getWidth() / 2);
            finalPos.y = (SCREENPOS.y - camera.getHeight() / 2) / (camera.getHeight() / 2); 
            // REVERSE VALUeS IF ENEMY IS BEHIND (z > 1)
            if ( SCREENPOS.z < 1 ) {
                // z < 1 CAN NEVER HAPPEN?
                finalPos.x = Math.min(1f, Math.max(-1f, finalPos.x));
                finalPos.y = Math.min(1f, Math.max(-1f, finalPos.y));
            } else {
                finalPos.x = Math.min(1f, Math.max(-1f, -finalPos.x));
                finalPos.y = Math.min(1f, Math.max(-1f, -finalPos.y));
            }
            // CLAMP BIGGER PART TO SCREEN BORDER
            if (Math.abs(finalPos.x) > Math.abs(finalPos.y)) {
                finalPos.x = finalPos.x > 0 ? 1 : -1;   
            } else {
                finalPos.y = finalPos.y > 0 ? 1 : -1;
            }                
            // MOVE RESULTS INTO SCREEN SPACE
            finalPos.x = finalPos.x / 2 + 0.5f;
            finalPos.y = finalPos.y / 2 + 0.5f;
        }
        
        return finalPos;
    }
    
    public static Vector2f getAlignment(Vector3f screenPos) {
        Vector2f alignment = new Vector2f();
        // DETERMINE THE ALIGNMENT FOR RENDERING THE BOX
        alignment.x = screenPos.x > 0 ? 1 : 0;
        alignment.y = screenPos.y > 0 ? 1 : 0;
        return alignment;
    }
    
}
