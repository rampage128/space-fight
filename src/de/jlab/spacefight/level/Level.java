/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.level;

import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LightControl;
import com.jme3.shadow.PssmShadowRenderer;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.SpaceAppState;
import de.jlab.spacefight.effect.LensFlareControl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author rampage
 */
public class Level {

    private SpaceAppState space;
    
    private String name = null;
    private Node level = null;
    
    private FilterPostProcessor bloomFilter = null;
    private FilterPostProcessor ssaoFilter = null;
    private ArrayList<PssmShadowRenderer> shadowList = new ArrayList<PssmShadowRenderer>();
    private ArrayList<FilterPostProcessor> scatterList = new ArrayList<FilterPostProcessor>();
    private FilterPostProcessor fxaaFilter = null;
    
    public Level(Node level, SpaceAppState space) {
        this.space = space;
        this.level = level;
    }
    
    public void attach(SpaceAppState space) {        
        Node sky = (Node)this.level.getChild("sky");
        Node levelspace = (Node)this.level.getChild("space");
        sky.setQueueBucket(Bucket.Sky); //makes certain the sky is rendered in the right order, behind everything else.
        sky.setCullHint(Spatial.CullHint.Never); // NEVER CULL IT?
        sky.setShadowMode(ShadowMode.Off);

        //space.getSpace().attachChild(levelspace);
        space.addToSky(sky);
        
        /* SETUP SPACE LIGHTS */
        ArrayList<DirectionalLight> lightDirections = new ArrayList<DirectionalLight>();
        LightList lightList = levelspace.getLocalLightList();
        for ( int i = 0; i < lightList.size(); ) {
            Light light = lightList.get(i);
            
            levelspace.removeLight(light);
            space.getSpace().addLight(light);           
                         
            if ( light instanceof DirectionalLight ) {
                lightDirections.add((DirectionalLight)light);
                /*
                System.out.println("Adding PSSM");
                PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(this.space.getGame().getAssetManager(), 1024, 3);
                pssmRenderer.setDirection(((DirectionalLight)light).getDirection().normalize()); // new Vector3f(1, 1, 1).normalizeLocal()
                pssmRenderer.setLambda(0.55f);
                pssmRenderer.setShadowZExtend(2000f);
                //pssmRenderer.setEdgesThickness(0);
                pssmRenderer.setShadowIntensity(0.4f);
                pssmRenderer.setCompareMode(PssmShadowRenderer.CompareMode.Software);
                pssmRenderer.setFilterMode(PssmShadowRenderer.FilterMode.Bilinear);
                //pssmRenderer.displayDebug();                
                this.space.getGame().getViewPort().addProcessor(pssmRenderer);
                this.shadowList.add(pssmRenderer);
                */
                
                /*
                PssmShadowRenderer pssm = new PssmShadowRenderer(this.game.getAssetManager(), 512, 3);
                pssm.setDirection(new Vector3f(1, 1, 1).normalizeLocal());
                pssm.setLambda(0.55f);
                pssm.setShadowIntensity(0.6f);
                pssm.setCompareMode(PssmShadowRenderer.CompareMode.Software);
                pssm.setFilterMode(PssmShadowRenderer.FilterMode.Bilinear);
                pssm.displayDebug();
                this.game.getViewPort().addProcessor(pssm);
                this.shadowList.add(pssm);
                 */
            }
            
            if (!lightDirections.isEmpty()) {
                Game.get().getVideoManager().createShadows(lightDirections.toArray(new DirectionalLight[0]));
            }
        }
        
        /* SETUP SKY STUFF */
        List<Spatial> nodeList = sky.getChildren();
        for ( int i = 0; i < nodeList.size(); i++ ) {
            Spatial child = nodeList.get(i);
            if ( "flare".equalsIgnoreCase(child.getName()) ) {
                String file = child.getUserData("file");
                Float maxScale = child.getUserData("maxScale");
                Float minScale = child.getUserData("minScale");
                LensFlareControl lfc = new LensFlareControl(space, this.space.getGame().getCamera(), file, maxScale, minScale, ColorRGBA.White);
                child.addControl(lfc);
                
            /* TEMPORARILY REMOVED BECAUSE IT WAS NOT VISIBLE?!
                FilterPostProcessor scatterFilter = new FilterPostProcessor(this.game.getAssetManager());
                LightScatteringFilter filter = new LightScatteringFilter(child.getLocalTranslation());
                //LightScatteringUI ui = new LightScatteringUI(inputManager, filter);
                scatterFilter.addFilter(filter);
                this.scatterList.add(scatterFilter);
             */
            }
        }
        
        lightList = sky.getLocalLightList();
        for ( Iterator<Light> lit = lightList.iterator(); lit.hasNext(); ) {
            Light light = lit.next();
            if ( light instanceof PointLight || light instanceof SpotLight ) {
                LightControl lc = new LightControl(light);
                sky.addControl(lc);
            }
        }
        
        // BLOOM FILTER
        /*
        this.bloomFilter = new FilterPostProcessor(this.space.getGame().getAssetManager());
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        bloom.setDownSamplingFactor(2.0f); 
        bloom.setBloomIntensity(4f);
        this.bloomFilter.addFilter(bloom);
        this.space.getGame().getViewPort().addProcessor(this.bloomFilter);       
        */
        
        // SSAO-FILTER
        /*
        this.ssaoFilter = new FilterPostProcessor(this.space.getGame().getAssetManager());
        SSAOFilter ssao = new SSAOFilter(12.94f, 43.92f, 0.33f, 0.61f);
        this.ssaoFilter.addFilter(ssao);
        this.space.getGame().getViewPort().addProcessor(this.ssaoFilter);
        */
        
        /*
        this.fxaaFilter = new FilterPostProcessor(this.space.getGame().getAssetManager());
        FXAAFilter fxaa = new FXAAFilter();
        fxaaFilter.addFilter(fxaa);
        this.space.getGame().getViewPort().addProcessor(fxaaFilter);
         */
    }
    
    public void cleanup() {
        //Game.get().getVideoManager().clearShadows();
        /*
        for ( int i = 0; i < this.shadowList.size(); i++ ) {
            PssmShadowRenderer shadow = this.shadowList.remove(i);
            this.space.getGame().getViewPort().removeProcessor(shadow);
        }
        for ( int i = 0; i < this.scatterList.size(); i++ ) {
            FilterPostProcessor scatterFilter = this.scatterList.remove(i);
            this.space.getGame().getViewPort().removeProcessor(scatterFilter);
        }
        */
        
        //this.space.getGame().getViewPort().removeProcessor(this.ssaoFilter);
        //this.space.getGame().getViewPort().removeProcessor(this.bloomFilter);
        //this.space.getGame().getViewPort().removeProcessor(this.fxaaFilter);
    }
    
    public static Level loadLevel(String path, SpaceAppState space) {
        Node level = space.getGame().getGamedataManager().loadLevel(path);
        
        return new Level(level, space);
    }
       
}
