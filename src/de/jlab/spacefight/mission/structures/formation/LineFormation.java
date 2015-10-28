/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.structures.formation;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Flight;

/**
 *
 * @author rampage
 */
public class LineFormation extends Formation {
     
    public Vector3f getPosition(int index, Flight flight) {
        if ( index == 0 )
            return null;
 
        int decrement = 1;
        if ( index != 1 ) {
            decrement = 2;
        }

        Vector3f offsetVector = new Vector3f();
        if ( index % 2 == 0 ) {
            offsetVector.set(20, 0, 0);            
        } else {
            offsetVector.set(-20, 0, 0);
        }
        
        ObjectInfoControl leader = getLeader(index, flight);
        return calculatePosition(leader, flight, index, offsetVector);
    }
    
}
