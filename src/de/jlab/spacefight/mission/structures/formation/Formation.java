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
public abstract class Formation {
        
    public abstract Vector3f getPosition(int index, Flight flight);
    
    public ObjectInfoControl getLeader(int index, Flight flight) {
        /*
         * - GO THROUGH SHIPS FROM 0 TO FLIGHT SIZE
         * - FOR EACH SHIP
         *   - CHECK IF DISTANCE < 2000
         *     - RETURN HIM
         * - RETURN NULL
         */
        ObjectInfoControl self = flight.getObject(index);
        if (self == null) {
            return null;
        }
        ObjectInfoControl[] objects = flight.getObjects();
        int position = 0;
        for (ObjectInfoControl object : objects) {
            if ( position >= index ) {
                break;
            }
            float distance = self.getSpatial().getWorldTranslation().distance(object.getSpatial().getWorldTranslation());
            if ( distance <= 2000 ) {
                return object;
            }
            position++;
        }
        return null;
    }
    
    protected Vector3f calculatePosition(ObjectInfoControl leader, Flight flight, int index, Vector3f offset) {
        if (leader == null || !leader.isAlive())
            return null;
        
        float multiplyer = 0;
        
        for ( int i = 0; i <= index; i++ ) {
            ObjectInfoControl object = flight.getObject(i);
            multiplyer += object.getSize() * 4;
        }
        
        Vector3f formationVector = new Vector3f(leader.getSpatial().getWorldTranslation());
        return formationVector.addLocal(leader.getSpatial().getLocalRotation().multLocal(offset.normalizeLocal().multLocal(multiplyer)));
    }
    
    /* FORMATIONFACTORY */
    public static Formation createFormation(String name) {
        if ( name != null ) {
            if ( "line".equalsIgnoreCase(name) ) {
                return new LineFormation();
            } else if ( "echelon".equalsIgnoreCase(name) ) {
                return new EchelonFormation();
            } else if ( "arrow".equalsIgnoreCase(name) ) {
                return new ArrowFormation();
            }
        }

        return new ColumnFormation();
    }
    
}
