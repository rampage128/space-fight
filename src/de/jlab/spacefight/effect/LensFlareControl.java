/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.effect;

import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Quad;
import de.jlab.spacefight.SpaceAppState;

/**
 *
 *
 * TODO: ADD SCALEMODE PARAMETER TO DECIDE WHETHER TO SCALE ACCORDING TO DISTANCE FROM CAMERA OR DISTANCE FROM CENTER
 * 
 * @author rampage
 */
public class LensFlareControl extends AbstractControl {

    public static final float OCCLUSION_TIME = 0.25f;
    
    private SpaceAppState space;
    
    private Camera camera;
    
    private float minScale = 0f;
    private float maxScale = 512f;
    
    private float scale = 1f;
    private Geometry flare;
    private String texture;
    
    private Vector3f center = new Vector3f(0f, 0f, 0f);
    private Vector3f screenPos = new Vector3f(0f, 0f, 0f);
    private Vector3f screenWorld = new Vector3f(0f, 0f, 0f);
    
    private Ray occlusionRay;
    private CollisionResults occlusionResults = new CollisionResults();
    private Vector3f occlusionVector = new Vector3f(0f, 0f, 0f);
    private float occlusionTime = 0f;
    private float occlusionScale = 1f;
    
    private Material material;
    private ColorRGBA color;
    
    private boolean visible = false;
        
    public LensFlareControl(SpaceAppState space, Camera camera, String texture, float maxScale, float minScale, ColorRGBA color) {
        this.space      = space;
        this.camera     = camera;
        this.texture    = texture;
        this.minScale   = Math.max(this.camera.getWidth(), this.camera.getHeight()) * minScale;
        this.maxScale   = Math.max(this.camera.getWidth(), this.camera.getHeight()) * maxScale;
        this.scale      = this.minScale;
        this.color      = color;
        
        this.center.set(this.camera.getWidth() / 2, this.camera.getHeight() / 2, 0);
        
        this.material = new Material(this.space.getGame().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        this.material.setTexture("ColorMap", this.space.getGame().getAssetManager().loadTexture(texture));
        this.material.setTexture("GlowMap", this.space.getGame().getAssetManager().loadTexture(texture));
        this.material.setColor("Color", this.color);
        this.material.getAdditionalRenderState().setPointSprite(true);
        this.material.getAdditionalRenderState().setBlendMode(BlendMode.AlphaAdditive);
        
        this.flare = new Geometry("flare", new Quad(1, 1));
        this.flare.setMaterial(this.material);
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        
        this.camera.getScreenCoordinates(getSpatial().getWorldTranslation(), this.screenPos);
        
        // IF LIGHT IS ON SCREEN 
        if ( this.screenPos.z < 1 && this.screenPos.x > -this.scale && this.screenPos.x < this.camera.getWidth() + this.scale / 2 && this.screenPos.y > -this.scale && this.screenPos.y < this.camera.getHeight() + this.scale / 2 ) {
            
            // CHECK OCCLUSION
            if ( isOccluded() ) {
                // CALCULATE OCCLUSION TIME
                if ( this.occlusionTime < OCCLUSION_TIME ) {
                    this.occlusionTime += tpf;
                    if ( this.occlusionTime > OCCLUSION_TIME ) {
                        this.occlusionTime = OCCLUSION_TIME;
                        if ( this.visible ) {
                            this.space.removeFromUI(this.flare);
                            // UPDATE STATUS
                            this.visible = false;
                        }
                    }
                    this.occlusionScale = 1f - (this.occlusionTime / OCCLUSION_TIME);
                }
            } else {                
                if ( !this.visible ) {
                    // SHOW LENSFLARE IF HIDDEN
                    this.space.addToUI(this.flare);
                    this.visible = true;
                }
                
                // CALCULATE OCCLUSION TIME
                if ( this.occlusionTime > 0 ) {
                    this.occlusionTime -= tpf;
                    if ( this.occlusionTime < 0 ) {
                        this.occlusionTime = 0;
                    }
                    this.occlusionScale = 1f - (this.occlusionTime / OCCLUSION_TIME);
                }
            }
            
            if ( this.visible ) {
                // SET LENSFLARE SCALE ACCORDING TO DISTANCE TO CENTER (IN CENTER ITS 100%)
                this.scale = Math.max(this.minScale, this.maxScale * (1 - (this.screenPos.subtract(this.center).length() / (Math.max(this.camera.getWidth(), this.camera.getHeight()) / 2))));

                // APPLY SCALE
                this.flare.setLocalScale(this.scale * this.occlusionScale, this.scale * this.occlusionScale, 1);

                // MOVE LENSFLARE TO POSITION ON SCREEN
                this.flare.setLocalTranslation(this.screenPos.x - (this.scale * this.occlusionScale) / 2, this.screenPos.y - (this.scale * this.occlusionScale) / 2, 0);
            }
            
            /*
            // CHECK OCCLUSION
            if ( isOccluded() ) {
                // CALCULATE OCCLUSION TIME
                if ( this.occlusionTime < 1f ) {
                    this.occlusionTime += tpf;
                    if ( this.occlusionTime > 1f ) {
                        this.occlusionTime = 1f;
                        if ( this.visible ) {
                            this.space.getUINode().detachChild(this.flare);
                            // UPDATE STATUS
                            this.visible = false;
                        }
                        this.occlusionScale = (1f - this.occlusionTime);
                    }
                    if ( this.visible ) {
                        this.flare.setLocalScale(this.scale * this.occlusionScale, this.scale * this.occlusionScale, 1);
                        // MOVE LENSFLARE TO POSITION ON SCREEN
                        this.flare.setLocalTranslation(this.screenPos.x - (this.scale * this.occlusionScale) / 2, this.screenPos.y - (this.scale * this.occlusionScale) / 2, 0);
                    }
                }
            } else {                
                // CALCULATE OCCLUSION TIME
                if ( this.occlusionTime > 0 ) {
                    this.occlusionTime -= tpf;
                    if ( this.occlusionTime < 0 ) {
                        this.occlusionTime = 0;
                        this.occlusionScale = (1f - this.occlusionTime);
                    }
                }
                
                // UPDATE STATUS
                this.visible = true;

                // SHOW LENSFLARE IF HIDDEN
                this.space.getUINode().attachChild(this.flare);
                //((Node)getSpatial()).attachChild(this.flare);

                // SET LENSFLARE SCALE ACCORDING TO DISTANCE TO CENTER (IN CENTER ITS 100%)
                this.scale = Math.max(this.minScale, this.maxScale * (1 - (this.screenPos.subtract(this.center).length() / (Math.max(this.camera.getWidth(), this.camera.getHeight()) / 2))));

                // APPLY SCALE
                this.flare.setLocalScale(this.scale * this.occlusionScale, this.scale * this.occlusionScale, 1);

                // MOVE LENSFLARE TO POSITION ON SCREEN
                this.flare.setLocalTranslation(this.screenPos.x - (this.scale * this.occlusionScale) / 2, this.screenPos.y - (this.scale * this.occlusionScale) / 2, 0);
            }
            */
        // IF LIGHT IS OFF SCREEN
        } else {
            // HIDE LENSFLARE IF VISIBLE
            if ( this.visible ) {
                //((Node)getSpatial()).attachChild(this.flare);
                this.space.removeFromUI(this.flare);
                // SET LENSFLARE SIZE TO this.minScale IF LARGER
                this.flare.setLocalScale(this.minScale, this.minScale, 1);
                // UPDATE STATUS
                this.visible = false;
            }
        }
    }

    private boolean isOccluded() {
        this.occlusionResults.clear();
        this.camera.getWorldCoordinates(new Vector2f(this.screenPos.x, this.screenPos.y), 0f, this.screenWorld);
        this.occlusionVector.set(getSpatial().getWorldTranslation()).subtractLocal(this.screenWorld);
        if ( this.occlusionRay == null ) {
            this.occlusionRay = new Ray(this.screenWorld, this.occlusionVector.normalize());
        } else {
            this.occlusionRay.setOrigin(this.screenWorld);
            this.occlusionRay.setDirection(this.occlusionVector.normalize());
        }
                
        try {
            space.getSpace().collideWith(this.occlusionRay, this.occlusionResults); //FIXME CREATES UGLY NegativeArraySizeException (EngineTrail is suspect)
        } catch (NegativeArraySizeException e) {
            // IGNORE THIS STUPID BULLSHIT FUCKTARD EXCEPTION... THIS IS A DIRTY HACK!!!
        }
        return this.occlusionResults.size() > 0;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        LensFlareControl control = new LensFlareControl(this.space, this.camera, this.texture, this.maxScale, this.minScale, this.color);
        control.setSpatial(spatial);
        return control;
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if ( spatial == null ) {
            this.space.getGame().getGuiNode().detachChild(this.flare);
        }
    }
    
}
