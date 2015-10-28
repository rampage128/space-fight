/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.perks;

import com.jme3.scene.Spatial;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.sensors.TargetInformation;
import de.jlab.spacefight.ui.ingame.hud.HudItem;
import java.util.ArrayList;
import java.util.Random;

/**
 * @Deprecated Use <code>Perk</code> instead!
 * @author rampage
 */
@Deprecated
public class ECMPerk {

    private float period = 0f;
    
    public ECMPerk() {
        
    }
    
    
    protected void initHud(HudItem hudItem) {
        
    }

    
    protected void onActiveUpdate(PerkControl perkControl, float tpf) {
        if (period >= 1) {
            Spatial spatial = perkControl.getSpatial();
            if (spatial != null) {
                // CHECK If WE HAVE SENSORS AND DISABLE PERK IF NOT... (SHOULD NEVER HAPPEN!)
                SensorControl sensorControl = spatial.getControl(SensorControl.class);
                if (sensorControl == null) {
                    //this.disable(perkControl);
                    return;
                }

                // GET FRIENDLIES WITHIN SENSOR RANGE
                Random rnd = new Random();
                ArrayList<TargetInformation> friendlyList = sensorControl.getTargetList(500, TargetInformation.FOF_FRIEND);
                if (friendlyList.isEmpty()) {
                    return;
                }

                // GET LIST OF PEOPLE LOCKING ME
                ArrayList<ObjectInfoControl> lockList = sensorControl.getLockList();
                for (ObjectInfoControl locker : lockList) {
                    // CHECK IF LOCKER HAS SENSORS
                    SensorControl lockSensors = locker.getObjectControl(SensorControl.class);
                    if (lockSensors != null) {
                        // GET A RANDOM FRIENDLY FROM FRIENDLY LIST
                        int index = rnd.nextInt(friendlyList.size());
                        TargetInformation friendly = friendlyList.get(index);
                        // ASSIGN FRIENDLY AS TARGET (NASTY!)
                        lockSensors.targetEnemy(friendly.getObject().getId());
                    }
                }
            }
            period = 0f;
        } else {
            period += tpf;
        }
    }
    
}
