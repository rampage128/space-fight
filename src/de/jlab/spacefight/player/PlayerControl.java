/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.player;

import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.ui.Picture;
import de.jlab.spacefight.AbstractClientControl;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.input.ShipInput;
import de.jlab.spacefight.mission.structures.DistressCall;
import de.jlab.spacefight.systems.PhysicsControl;
import de.jlab.spacefight.systems.flight.FlightControl;
import de.jlab.spacefight.systems.perks.PerkControl;
import de.jlab.spacefight.systems.sensors.SensorControl;
import de.jlab.spacefight.systems.weapons.WeaponSystemControl;

/**
 *
 * @author rampage
 */
public class PlayerControl extends AbstractClientControl implements AnalogListener, ActionListener {

    private PhysicsControl physicsControl = null;
    private FlightControl flightControl = null;
    private WeaponSystemControl weaponControl = null;
    private SensorControl sensorControl = null;
    private PerkControl perkControl = null;
    private boolean mouseflight = true;
    private float mouseX = 0;
    private float mouseY = 0;
    private Picture laserWarning;
    private ObjectInfoControl object;
    private SpaceAppState space;
    
    private float desiredRudder = 0f;
    private float desiredElevator = 0f;
    private float deadZone = 0.025f;

    public PlayerControl(ObjectInfoControl object) {
        super(Game.get().getStateManager().getState(SpaceAppState.class).getMission());
        this.object = object;
        this.space = Game.get().getStateManager().getState(SpaceAppState.class);
    }

    private SpaceAppState getSpace() {
        return this.space;
    }
    
    /*** ACTIONLISTENER ***/
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!this.isEnabled()) {
            return;
        }
        
        if (flightControl != null) {
            if (ShipInput.STRAFELEFT.toString().equalsIgnoreCase(name)) {
                if ( isPressed ) {
                    flightControl.setStrafe(1);
                } else {
                    flightControl.setStrafe(0);
                }
            } else if (ShipInput.STRAFERIGHT.toString().equalsIgnoreCase(name)) {
                if ( isPressed ) {
                    flightControl.setStrafe(-1);
                } else {
                    flightControl.setStrafe(0);
                }
            } else if (ShipInput.LIFTUP.toString().equalsIgnoreCase(name)) {
                if ( isPressed ) {
                    flightControl.setLift(1);
                } else {
                    flightControl.setLift(0);
                }
            } else if (ShipInput.LIFTDOWN.toString().equalsIgnoreCase(name)) {
                if ( isPressed ) {
                    flightControl.setLift(-1);
                } else {
                    flightControl.setLift(0);
                }
            } else if (ShipInput.ROLLLEFT.toString().equalsIgnoreCase(name)) {
                if ( isPressed ) {
                    flightControl.setAileron(-1);
                } else {
                    flightControl.setAileron(0);
                }
            } else if (ShipInput.ROLLRIGHT.toString().equalsIgnoreCase(name)) {
                if ( isPressed ) {
                    flightControl.setAileron(1);
                } else {
                    flightControl.setAileron(0);
                }
            } else if (ShipInput.THROTTLE0.toString().equalsIgnoreCase(name) && isPressed) {
                flightControl.setThrottle(0f);
            } else if (ShipInput.THROTTLE33.toString().equalsIgnoreCase(name) && isPressed) {
                flightControl.setThrottle(0.33f);
            } else if (ShipInput.THROTTLE66.toString().equalsIgnoreCase(name) && isPressed) {
                flightControl.setThrottle(0.66f);
            } else if (ShipInput.THROTTLE100.toString().equalsIgnoreCase(name) && isPressed) {
                flightControl.setThrottle(1f);
            } else if (ShipInput.CRUISE.toString().equalsIgnoreCase(name) && isPressed) {
                flightControl.setCruise(!flightControl.getCruise());
            } else if (ShipInput.GLIDE.toString().equalsIgnoreCase(name) && isPressed) {
                flightControl.setGlide(true);
            }
        }
         
        if (sensorControl != null) {
            if (ShipInput.TARGET_NEXTENEMY.toString().equalsIgnoreCase(name) && isPressed) {
                this.sensorControl.targetNextEnemy();
            } else if (ShipInput.TARGET_NEXT.toString().equalsIgnoreCase(name) && isPressed) {
                this.sensorControl.targetNext();
            } else if (ShipInput.TARGET_SMART.toString().equalsIgnoreCase(name) && isPressed) {
                this.sensorControl.targetSmart();
            }  else if (ShipInput.COM_DEFENSIVE.toString().equalsIgnoreCase(name) && isPressed) {
                DistressCall.defensiveDistressCall(getSpatial().getControl(ObjectInfoControl.class), getMission());
            }
        }
         
        if (weaponControl != null) {
            if (ShipInput.WEAPON_SECONDARY.toString().equalsIgnoreCase(name) && isPressed) {
                weaponControl.fireSecondary();
            } else if (ShipInput.WEAPON_PRIMARYMODE.toString().equalsIgnoreCase(name) && isPressed) {
                weaponControl.switchPrimaryMode();
            } else if (ShipInput.WEAPON_NEXTSECONDARY.toString().equalsIgnoreCase(name) && isPressed) {
                weaponControl.nextSecondarySlot();
            } else if (ShipInput.COM_OFFENSIVE.toString().equalsIgnoreCase(name) && isPressed) {
                DistressCall.offensiveDistressCall(getSpatial().getControl(ObjectInfoControl.class), getMission());
            }
        }
        
        if (perkControl != null) {
            if (ShipInput.PERK_USE.toString().equalsIgnoreCase(name) && isPressed) {
                perkControl.usePerk();
            }
        }
    }

    public void onAnalog(String name, float value, float tpf) {
        if (!this.isEnabled()) {
            return;
        }
        
        if (flightControl != null) {
            if (mouseflight) {
                if (ShipInput.RUDDERLEFT.toString().equalsIgnoreCase(name)) {
                    //if (value > 0.25) {
                        desiredRudder = Math.min(1, desiredRudder + value * 2);
                        //flightControl.setRudder(flightControl.getRudder() + value * 2);
                    //} else {
                        //flightControl.setRudder(0);
                    //}
                } else if (ShipInput.RUDDERRIGHT.toString().equalsIgnoreCase(name)) {
                    //if (value > 0.25) {
                        desiredRudder = Math.max(-1, desiredRudder - value * 2);
                        //flightControl.setRudder(flightControl.getRudder() - value * 2);
                    //} else {
                        //flightControl.setRudder(0);
                    //}
                } else if (ShipInput.ELEVATORUP.toString().equalsIgnoreCase(name)) {
                    //if (value > 0.25) {
                        desiredElevator = Math.max(-1, desiredElevator - value * 2);
                        //flightControl.setElevator(flightControl.getElevator() - value * 2);
                    //} else {
                        //flightControl.setElevator(0);
                    //}
                } else if (ShipInput.ELEVATORDOWN.toString().equalsIgnoreCase(name)) {
                    //if (value > 0.25) {
                        desiredElevator = Math.min(1, desiredElevator + value * 2);
                        //flightControl.setElevator(flightControl.getElevator() + value * 2);
                    //} else {
                        //flightControl.setElevator(0);
                    //}
                } else if (ShipInput.STEERING_DEBUG.toString().equalsIgnoreCase(name)) {
                    flightControl.turnTo(getSpatial().getControl(ObjectInfoControl.class).getCurrentTask().getPosition(), tpf);
                }                
            } else {
                if (ShipInput.RUDDERLEFT.toString().equalsIgnoreCase(name)) {
                    flightControl.setRudder(1);
                } else if (ShipInput.RUDDERRIGHT.toString().equalsIgnoreCase(name)) {
                    flightControl.setRudder(-1);
                } else if (ShipInput.ELEVATORUP.toString().equalsIgnoreCase(name)) {
                    flightControl.setElevator(-1);
                } else if (ShipInput.ELEVATORDOWN.toString().equalsIgnoreCase(name)) {
                    flightControl.setElevator(1);
                }
            }

            if (ShipInput.FORWARD.toString().equalsIgnoreCase(name)) {
                flightControl.setThrottle(flightControl.getThrottle() + 0.5f * tpf);
            } else if (ShipInput.BACKWARD.toString().equalsIgnoreCase(name)) {
                flightControl.setThrottle(flightControl.getThrottle() - 0.5f * tpf);
            }
        }
        
        if (weaponControl != null) {
            if (ShipInput.WEAPON_PRIMARY.toString().equalsIgnoreCase(name)) {
                weaponControl.firePrimary();
            }
        }
    }

    public Control cloneForSpatial(Spatial spatial) {
        PlayerControl newControl = new PlayerControl(this.object);
        newControl.setSpatial(spatial);
        return newControl;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        // TODO NOT GOOD TO CONTROL HUD APPEARANCE IN PLAYER
        if (spatial != null) {
            laserWarning = new Picture("HUD Picture");
            laserWarning.setImage(Game.get().getAssetManager(), "ui/hud/flightdirection_strafe.png", true);
            laserWarning.setWidth(Game.get().getContext().getSettings().getHeight() / 16);
            laserWarning.setHeight(Game.get().getContext().getSettings().getHeight() / 16);
            laserWarning.setPosition(Game.get().getContext().getSettings().getWidth() - Game.get().getContext().getSettings().getHeight() / 16, Game.get().getContext().getSettings().getHeight() / 16);

            //this.space.getGame().getAudioManager().setListenerSpatial(spatial);
                    
            if (this.flightControl == null) {
                this.flightControl = spatial.getControl(FlightControl.class);
            }
            
            if (this.flightControl != null) {
                this.flightControl.setRudder(0);
                this.flightControl.setAileron(0);
                this.flightControl.setElevator(0);
                this.flightControl.setLift(0);
                this.flightControl.setStrafe(0);
            }

            getSpace().getInputManager().toggleInput(ShipInput.class, this);
        } else {            
            getSpace().getInputManager().toggleInput(ShipInput.class, null);
            
            if (laserWarning != null) {
                getSpace().removeFromUI(laserWarning);
            }
            //this.space.getGame().getAudioManager().setListenerSpatial(null);
            //this.space.getGame().getAudioManager().setListenerControl(null);
            weaponControl = null;
            sensorControl = null;
            flightControl = null;
            getSpace().getGame().getInputManager().removeListener(this);
        }
        super.setSpatial(spatial);
    }

    @Override
    public void controlUpdate(float tpf) {
        if ( getSpace().isEnabled() ) {
            if (getSpatial() != null) {
                if (perkControl == null) {
                    perkControl = getSpatial().getControl(PerkControl.class);
                }
                
                if (flightControl == null) {
                    flightControl = getSpatial().getControl(FlightControl.class);
                }
                
                if (physicsControl == null) {
                    physicsControl = getSpatial().getControl(PhysicsControl.class);
                    //this.space.getGame().getAudioManager().setListenerControl(physicsControl);
                }

                if (weaponControl == null) {
                    weaponControl = getSpatial().getControl(WeaponSystemControl.class);
                }

                if (sensorControl == null) {
                    sensorControl = getSpatial().getControl(SensorControl.class);
                } else {
                    if (sensorControl.getLaserWarning()) {
                        getSpace().addToUI(laserWarning);
                    } else {
                        getSpace().removeFromUI(laserWarning);
                    }
                }
            }

            if (flightControl != null) {
                if (!mouseflight) {
                    flightControl.setAileron(0);
                    flightControl.setElevator(0);
                    flightControl.setRudder(0);
                } else {
                    if (Math.abs(desiredElevator) > deadZone) {
                        flightControl.setElevator(desiredElevator);
                    } else {
                        flightControl.setElevator(0);
                    }
                    if (Math.abs(desiredRudder) > deadZone) {
                        flightControl.setRudder(desiredRudder);
                    } else {
                        flightControl.setRudder(0);
                    }
                }
            }

            /*
            if (this.weaponControl != null && this.weaponControl.getTarget() != null) {
                if (this.weaponControl.getTarget().getObject().getObjectControl(AbstractClientControl.class).getCurrentTask() != null) {
                    //System.out.println(this.weaponControl.getTarget().getObject().getObjectControl(AbstractClientControl.class).getCurrentTask().getType());
                    System.out.println(this.weaponControl.getTarget().getObject().getLinearVelocity().length() + ", " + Game.get().getPlayer().getClient().getLinearVelocity().length());
                }
            }
            */
            
            /*
            if (space.getGame().getNetworkClient() != null) {
                PlayerActionMessage message = new PlayerActionMessage(this);
                space.getGame().getNetworkClient().send(message);
            } else if (space.getGame().getNetworkServer() != null) {
                PlayerActionBroadcastMessage message = new PlayerActionBroadcastMessage(this);
                space.getGame().getNetworkServer().broadcast(message);
            }
            */
            
            /*
            if (flightControl != null) {
                if (physicsControl != null) {
                    System.out.println(flightControl.getVelocity() + ", " + physicsControl.getLinearVelocity());
                }
            }
            */
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public String getName() {
        ObjectInfoControl control = this.getSpatial().getControl(ObjectInfoControl.class);
        return control.getPlayer().getNickname();
    }
    
}