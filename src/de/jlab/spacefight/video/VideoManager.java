/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.video;

import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.filters.GammaCorrectionFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.shadow.CompareMode;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.shadow.PssmShadowRenderer;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.gamedata.SimpleConfig;
import java.util.ArrayList;

/**
 *
 * @author rampage
 */
public final class VideoManager {
    
    public static final String CONFIG_FILENAME = "video.cfg";
    
    public static final String PROPERTY_SHADOW_QUALITY = "shadow_quality";
    public static final String PROPERTY_FXAA_QUALITY = "fxaa_quality";
    public static final String PROPERTY_SSAO_QUALITY = "ssao_quality";
    public static final String PROPERTY_GAMMA = "gamma";
        
    private Game game;
    private SimpleConfig videoConfig;
    private FilterPostProcessor filterPostProcessor = null;
    
    private ArrayList<DirectionalLightShadowRenderer> shadowList = new ArrayList<DirectionalLightShadowRenderer>();
    private SSAOFilter ssaoFilter;
    private FXAAFilter fxaaFilter;
    private BloomFilter bloomFilter;
    private GammaCorrectionFilter gammaFilter;
    private MotionBlurFilter motionBlurFilter;
        
    public VideoManager(Game game) {
        this.game = game;
        this.videoConfig = new SimpleConfig();
        this.loadConfig();
    }
    
    public void loadConfig() {
        this.videoConfig.loadFromFile(CONFIG_FILENAME);
    }
    
    public void saveConfig() {
        this.videoConfig.saveToFile(CONFIG_FILENAME);
    }
    
    public void init() {
        initEffects();
    }
    
    public void cleanup() {
        this.clearEffects();
        this.clearShadows();
    }
    
    public void reshape() {
        this.recreateShadows();
    }
    
    public void setShadowQuality(int quality) {
        this.videoConfig.setValue(PROPERTY_SHADOW_QUALITY, Integer.toString(quality));
    }
    
    public int getShadowQuality() {
        return this.videoConfig.getIntValue(PROPERTY_SHADOW_QUALITY, 2);
    }
    
    public void setSSAOQuality(int quality) {
        this.videoConfig.setValue(PROPERTY_SSAO_QUALITY, Integer.toString(quality));
    }
    
    public int getSSAOQuality() {
        return this.videoConfig.getIntValue(PROPERTY_SSAO_QUALITY, 1);
    }
    
    public void setFXAAQuality(int quality) {
        this.videoConfig.setValue(PROPERTY_FXAA_QUALITY, Integer.toString(quality));
    }
    
    public int getFXAAQuality() {
        return this.videoConfig.getIntValue(PROPERTY_FXAA_QUALITY, 1);
    }
    
    public void setGamma(float gamma) {
        this.videoConfig.setValue(PROPERTY_GAMMA, Float.toString(gamma));
    }

    public float getGamma() {
        return this.videoConfig.getFloatValue(PROPERTY_GAMMA, 1);
    }
    
    public void createShadows(DirectionalLight[] lights) {
        clearShadows();
        
        int resolution  = 1024;
        int amount      = 4;
        float range     = 2000f;
        float fadeout   = 500f;
        
        int quality = getShadowQuality();
        if (quality >= 4) {
            resolution  = 2048;
            amount      = 4;
            range       = 6000f;
            fadeout     = 2000f;
        } else if (quality >= 3) {
            resolution  = 2048;
            amount      = 3;
            range       = 4000f;
            fadeout     = 1000f;
        } else if (quality >= 2) {
            resolution  = 1024;
            amount      = 2;
            range       = 2000f;
            fadeout     = 500f;
        } else if (quality >= 1) {
            resolution  = 512;
            amount      = 1;
            range       = 1000f;
            fadeout     = 250f;
        } else {
            return;
        }
        
        this.clearEffects();
        for (DirectionalLight light : lights) {
            DirectionalLightShadowRenderer sRenderer = new DirectionalLightShadowRenderer(this.game.getAssetManager(), resolution, amount);
            //PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(this.game.getAssetManager(), resolution, amount);
            sRenderer.setLight(light);
            sRenderer.setLambda(0.55f);
            sRenderer.setShadowZExtend(range);
            sRenderer.setShadowZFadeLength(fadeout);
            //pssmRenderer.setEdgesThickness(0);
            sRenderer.setShadowIntensity(0.4f);
            sRenderer.setShadowCompareMode(CompareMode.Software);
            sRenderer.setEdgeFilteringMode(EdgeFilteringMode.Bilinear);
            //pssmRenderer.displayDebug();                
            this.game.getViewPort().addProcessor(sRenderer);
            this.shadowList.add(sRenderer);
        }
        this.initEffects();
    }
    
    private void recreateShadows() {
        ArrayList<DirectionalLight> lightDirections = new ArrayList<DirectionalLight>();
        while (!this.shadowList.isEmpty()) {
            DirectionalLightShadowRenderer shadow = this.shadowList.remove(0);
            this.game.getViewPort().removeProcessor(shadow);
            lightDirections.add(shadow.getLight());
        }
        if (!lightDirections.isEmpty()) {
            createShadows(lightDirections.toArray(new DirectionalLight[0]));
        }
    }
    
    public void clearShadows() {
        while (!this.shadowList.isEmpty()) {
            DirectionalLightShadowRenderer shadow = this.shadowList.remove(0);
            this.game.getViewPort().removeProcessor(shadow);
        }
    }
    
    private void initEffects() {
        this.filterPostProcessor = new FilterPostProcessor(this.game.getAssetManager());
        createSSAO();
        createBloom();
        //createMotionBlur();
        createFXAA();
        createGamma();
        game.getViewPort().addProcessor(filterPostProcessor);
    }
    
    private void clearEffects() {
        if (this.filterPostProcessor == null) {
            return;
        }
        
        filterPostProcessor.removeAllFilters();
        game.getViewPort().removeProcessor(filterPostProcessor);
    }
    
    private void createSSAO() {
        int quality = getSSAOQuality();        
        if (quality >= 1) {
            this.ssaoFilter = new SSAOFilter(12.94f, 43.92f, 0.33f, 0.61f);
            this.filterPostProcessor.addFilter(this.ssaoFilter);
        }
    }
    
    private void createFXAA() {
        int quality = getFXAAQuality();        
        if (quality >= 1) {
            this.fxaaFilter = new FXAAFilter();
            this.filterPostProcessor.addFilter(this.fxaaFilter);
        }
    }
    
    private void createBloom() {
        this.bloomFilter = new BloomFilter(BloomFilter.GlowMode.Objects);
        bloomFilter.setDownSamplingFactor(2.0f); 
        bloomFilter.setBloomIntensity(4f);
        this.filterPostProcessor.addFilter(this.bloomFilter);
    }
    
    private void createMotionBlur() {
        this.motionBlurFilter = new MotionBlurFilter();
        this.filterPostProcessor.addFilter(this.motionBlurFilter);
    }
    
    private void createGamma() {
        this.gammaFilter = new GammaCorrectionFilter(getGamma());
        this.filterPostProcessor.addFilter(this.gammaFilter);
    }
    
}
