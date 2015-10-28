/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.mission.structures.formation;

import com.jme3.math.Vector3f;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.mission.structures.Flight;

/**
 *
 * @author rampage
 */
public class ArrowFormation extends Formation {

    public Vector3f getPosition(int index, Flight flight) {
        if ( index == 0 )
            return null;
 
        Vector3f offsetVector = new Vector3f();
        int decrement = 1;
        
        if ( index > 4 ) {
            decrement = 4;
        }
        
        if ( index % 4 == 0 ) {
            offsetVector.set(0, -20, -20);            
        } else if ( index % 3 == 0 ) {
            offsetVector.set(-20, 0, -20);            
        } else if ( index % 2 == 0 ) {
            offsetVector.set(20, 0, -20);            
        } else {
            offsetVector.set(0, 20, -20);
        }
        
        ObjectInfoControl leader = getLeader(index, flight);
        return calculatePosition(leader, flight, index, offsetVector);
    }
    
}
