/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic.camera;

import com.jme3.input.controls.ActionListener;
import com.jme3.renderer.Camera;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.basic.ObjectInfoControl;
import de.jlab.spacefight.debug.SpaceDebugger;
import de.jlab.spacefight.input.ViewInput;
import de.jlab.spacefight.systems.hud.CockpitHudControl;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rampage
 */
public class CameraManager implements ActionListener {
    
    public static final float AUTOMATIC_VIEW_TIMELIMIT = 10f;
    public static final float AUTOMATIC_TARGET_TIMELIMIT = 30f;
    
    //private SpaceAppState space;
    
    private Camera camera;
    private View view;   
    private ObjectInfoControl target;
    
    private boolean automatic = false;
    private float automaticViewTime = AUTOMATIC_VIEW_TIMELIMIT;
    private float automaticTargetTime = AUTOMATIC_TARGET_TIMELIMIT;
    private ArrayList<Class<? extends View>> automaticViewList = new ArrayList<Class<? extends View>>();
        
    private CockpitHudControl hudControl = null;
    
    public CameraManager(Camera camera) {
        //this.space = space;
        this.camera = camera;
        // MAKES ALL EFFECKTS FLICKER THROUGH GEOMETRY
        //this.camera.setFrustumPerspective(45f, (float) this.camera.getWidth() / this.camera.getHeight(), 0.01f, 200000f);
        this.camera.setFrustumFar(200000);
        this.camera.onFrameChange();
        initAutomaticViews();
    }
    
    public ObjectInfoControl getTarget() {
        return this.target;
    }
    
    public void setTarget(ObjectInfoControl target) {
        this.target = target;
        if ( this.view != null && this.target != null ) {
            this.view.cleanup();
            this.view.init(this.camera, target);
        }
    }
    
    public void setView(Class<? extends View> viewClass) {
        try {
            if (this.hudControl != null) {
                if (this.hudControl.getSpatial() != null) {
                    this.hudControl.cleanupHud();
                    this.hudControl.getSpatial().removeControl(this.hudControl);
                }
            }
            
            SpaceAppState space = getSpace();
            if (space != null) {
                hudControl = new CockpitHudControl(getSpace());
            } else {
                return;
            }
           
            if ( this.view != null ) {
                this.view.cleanup();
            }
            this.view = viewClass.newInstance();
            this.view.init(this.camera, this.target);
            if ( this.view.displayHud() ) {
                if ( this.target != null ) {
                    this.target.getSpatial().addControl(this.hudControl);
                }
            }
        } catch (InstantiationException ex) {
            Logger.getLogger(CameraManager.class.getName()).log(Level.SEVERE, "Cannot instantiate view " + viewClass.getSimpleName(), ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CameraManager.class.getName()).log(Level.SEVERE, "Cannot access view " + viewClass.getSimpleName(), ex);
        }
    }

    public void update(float tpf) {
        //System.out.println("c: " + System.currentTimeMillis());
        if ( this.automatic ) {
            updateAutomatic(tpf);
        }
        if ( this.view != null && this.target != null ) {
            this.view.update(this.camera, this.target, tpf);
        }
    }

    /*
    public Control cloneForSpatial(Spatial spatial) {
        CameraManager cameraManager = new CameraManager(this.space, this.camera);
        spatial.addControl(cameraManager);
        return cameraManager;
    }
    */
    
    // AUTOMATIC SPECTATING
    public final void initAutomaticViews() {
        this.automaticViewList.add(ChaseView.class);
        this.automaticViewList.add(PadlockChaseView.class);
        this.automaticViewList.add(CockpitView.class);
        this.automaticViewList.add(PadlockCockpitView.class);
        this.automaticViewList.add(FlyByView.class);
    }
    
    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }
    
    public boolean getAutomatic() {
        return this.automatic;
    }
    
    private void updateAutomatic(float tpf) {
        automaticViewTime += tpf;
        automaticTargetTime += tpf;
                
        if (this.target == null || !this.target.isAlive() || automaticTargetTime >= AUTOMATIC_TARGET_TIMELIMIT) {
            SpaceAppState space = getSpace();
            if (space != null) {
                ObjectInfoControl[] objects = space.getMission().getObjects();
                Random randomGenerator = new Random();
                if (objects != null && objects.length != 0) {
                    int targetNum = randomGenerator.nextInt(objects.length);
                    setTarget(objects[targetNum]);
                    this.automaticTargetTime = 0;
                }
            }
        }
        
        if (automaticViewTime >= AUTOMATIC_VIEW_TIMELIMIT) {
            Random randomGenerator = new Random();
            int viewNum = randomGenerator.nextInt(this.automaticViewList.size());
            setView(this.automaticViewList.get(viewNum));
            this.automaticViewTime = 0;
        }       
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if ( ViewInput.VIEW01.toString().equalsIgnoreCase(name) && isPressed ) {
            setAutomatic(false);
            setView(CockpitView.class);
        } else if ( ViewInput.VIEW02.toString().equalsIgnoreCase(name) && isPressed ) {
            setAutomatic(false);
            setView(PadlockCockpitView.class);
        } else if ( ViewInput.VIEW03.toString().equalsIgnoreCase(name) && isPressed ) {
            setAutomatic(false);
            setView(EndGameView.class);
        } else if ( ViewInput.VIEW04.toString().equalsIgnoreCase(name) && isPressed ) {
            
        } else if ( ViewInput.VIEW05.toString().equalsIgnoreCase(name) && isPressed ) {
            setAutomatic(false);
            setView(ChaseView.class);
        } else if ( ViewInput.VIEW06.toString().equalsIgnoreCase(name) && isPressed ) {
            setAutomatic(false);
            setView(PadlockChaseView.class);
        } else if ( ViewInput.VIEW07.toString().equalsIgnoreCase(name) && isPressed ) {
            setAutomatic(false);
            setView(FlyByView.class);
        } else if ( ViewInput.VIEW08.toString().equalsIgnoreCase(name) && isPressed ) {
            
        } else if ( ViewInput.VIEW09.toString().equalsIgnoreCase(name) && isPressed ) {
            //SpaceDebugger.getInstance().dumpGraph(Game.get().getRootNode());
        } else if ( ViewInput.VIEW10.toString().equalsIgnoreCase(name) && isPressed ) {
            //SpaceDebugger.getInstance().dumpGraph(Game.get().getGuiNode());
        } else if ( ViewInput.VIEW11.toString().equalsIgnoreCase(name) && isPressed ) {
            
        } else if ( ViewInput.VIEW12.toString().equalsIgnoreCase(name) && isPressed ) {
            setAutomatic(true);
        }
    }
    
    private SpaceAppState getSpace() {
        return Game.get().getStateManager().getState(SpaceAppState.class);
    }
    
}
