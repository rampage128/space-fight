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
public class ColumnFormation extends Formation {

    public Vector3f getPosition(int index, Flight flight) {
        ObjectInfoControl leader = getLeader(index, flight);
        return calculatePosition(leader, flight, index, new Vector3f(0, 0, -20));
    }
    
}
