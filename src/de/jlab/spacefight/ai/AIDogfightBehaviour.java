/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ai;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import de.jlab.spacefight.basic.ChanceCalculator;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.debug.DebugContext;
import de.jlab.spacefight.debug.SpaceDebugger;
import de.jlab.spacefight.mission.structures.DistressCall;
import de.jlab.spacefight.mission.structures.Task;
import de.jlab.spacefight.weapon.AbstractWeaponControl;

/**
 *
 * @author rampage
 */
public class AIDogfightBehaviour extends AIBehaviour {

    public static final float VAR_EVASIONTIMEOUT = 15f;
    
    public static final int STATE_NOTARGET  = 0;
    public static final int STATE_ATTACK    = 1;
    public static final int STATE_EVADE     = 2;
    
    private Vector3f _evadeVector = null;
    private float _evasionTime = 0f;
    
    private boolean useSecondary = false;
    
    public AIDogfightBehaviour(AIShipControl aicontrol, Task task) {
        super(aicontrol, task);
    }
    
    @Override
    protected void updateBehaviour(float tpf) {
                
        if ( getAI().getWeapons().getTarget() == null )
            setState(STATE_NOTARGET);

        if ( getAI().getWeapons().getTarget() != null ) {
            //SpaceDebugger.getInstance().setVector(DebugContext.AI, "target" + getAI().getSpatial().getName(), getAI().getSpatial().getWorldTranslation(), getAI().getWeapons().getTarget().getAimAtWorld().subtract(getAI().getSpatial().getWorldTranslation()), ColorRGBA.Red);
            if ( getAI().getWeapons().mayFirePrimary() )
                getAI().getWeapons().firePrimary();
            if ( useSecondary && getAI().getWeapons().mayFireSecondary() && !getAI().getWeapons().mayFirePrimary()) { //  && getAI().getWeapons().getTarget().getAimAtAngle() < 90
                getAI().getWeapons().fireSecondary();
                useSecondary = false;
            }
        }
              
        switch(getState()) {
            case STATE_NOTARGET:
                setProximityDistance(3);
                if ( !getAI().followFormation(tpf, true) ) {
                    getAI().getFlightControl().setThrottle(0f);
                    setProximityDistance(30);
                }
                if ( getAI().getSensors().targetNextEnemy() ) {
                    setState(STATE_ATTACK);
                    this.useSecondary = new ChanceCalculator().calculateChance(25);
                }
                break;
            case STATE_ATTACK:
                setProximityDistance(30);
                //turnToEnemy(tpf);
                if ( followEnemy(tpf) ) {
                    SpaceDebugger.getInstance().removeItem(DebugContext.AI, "v_" + this.hashCode());
                    setState(STATE_EVADE);
                }
/*                
                getAI().getFlightControl().setThrottle(1f);
                if ( getAI().getSensors().getCurrentTarget().getDistance() < getAI().getFlightControl().getVelocity() )
                    getAI().getFlightControl().setThrottle(0.5f);
                if ( getAI().getSensors().getCurrentTarget().getDistance() < getAI().getFlightControl().getVelocity() / 2f + getAI().getSensors().getCurrentTarget().getSize() )
                    setState(STATE_EVADE);
 */
                    
                break;
            case STATE_EVADE:
                setProximityDistance(30);
                _evasionTime += tpf;
                if ( evadeEnemy(tpf) || _evasionTime >= VAR_EVASIONTIMEOUT ){
                    SpaceDebugger.getInstance().removeItem(DebugContext.AI, "v_" + this.hashCode());
                    setState(STATE_ATTACK);
                    _evadeVector = null;
                    _evasionTime = 0;
                    this.useSecondary = new ChanceCalculator().calculateChance(25);
                }
/*                
                if ( getAI().getSensors().getCurrentTarget().getDistance() > getAI().getSensors().getCurrentTarget().getSize() / 2 + 30 )
                    getAI().getFlightControl().setThrottle(1f);
                if ( getAI().getSensors().getCurrentTarget().getDistance() > getAI().getSensors().getCurrentTarget().getSize() * 2 + getAI().getFlightControl().getVelocity() * 3 || _evasionTime >= VAR_EVASIONTIMEOUT ) {
                    setState(STATE_ATTACK);
                    _evadeVector = null;
                    _evasionTime = 0;
                }
*/
                break;
        }
    }
       
    private boolean evadeEnemy(float tpf) {
        if ( _evadeVector == null )
            _evadeVector = getAI().getSensors().getTargetEvadeVector().mult(getAI().getFlightControl().getForwardVelocity() * 3 + getAI().getWeapons().getTarget().getObject().getSize() * 4);
        //getAI().turnTo(_evadeVector, tpf);
        
        SpaceDebugger.getInstance().setVector(DebugContext.AI, "v_" + this.hashCode(), getAI().getSpatial().getWorldTranslation(), _evadeVector, ColorRGBA.White);
        
        return getAI().getFlightControl().moveTo(_evadeVector, getAI().getObjectInfo().getSize() * 3, 1.0f, 0.0f, tpf);
    }
    
    private boolean followEnemy(float tpf) {           
        ObjectInfoControl myself = getAI().getObjectInfo();
        AbstractWeaponControl primaryWeapon = getAI().getWeapons().getPrimarySlot().getWeapon();
        Vector3f targetLocation = getAI().getWeapons().getTarget().getAimAtWorld(myself, primaryWeapon);
        
        float tolerance = Math.max(50, getAI().getWeapons().getTarget().getObject().getSize() * 4 + getAI().getObjectInfo().getSize() * 0.75f);
        Vector3f up = getAI().getWeapons().getTarget().getObject().getUpside();
        float throttle = 1.0f;
        float otherSpeed = getAI().getWeapons().getTarget().getObject().getLinearVelocity().length();
        
        SpaceDebugger.getInstance().setVector(DebugContext.AI, "v_" + this.hashCode(), getAI().getSpatial().getWorldTranslation(), targetLocation.subtract(getAI().getSpatial().getWorldTranslation()), ColorRGBA.Red);
        
        return getAI().getFlightControl().moveTo(targetLocation, tolerance, up, throttle, otherSpeed, tpf);
    }
    
}
