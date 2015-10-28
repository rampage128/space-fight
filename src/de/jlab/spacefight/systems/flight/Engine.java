/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.systems.flight;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.audio.AudioEffect;
import de.jlab.spacefight.effect.trail.LineControl;
import de.jlab.spacefight.effect.trail.TrailControl;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public class Engine implements XMLLoadable {
       
    private Geometry trailGeometry;
    private AudioEffect sound;
    private Vector3f offset;
    private String nodeName;
    
    private Node test;
    private TrailControl trailControl;
    private float width = 1f;
    private float endWidth = 1f;
    private float lifetime = 0.5f;
    private String materialName;
    
    private float widthFactor = 1f;
    
    private Spatial model;
    
    public Engine() {
        
    }
    
    public Engine(Element element, String path, GamedataManager gamedataManager) {
        loadFromElement(element, path, gamedataManager);
    }
    
    public void attachTo(Node node) {       
        Node engineNode = (Node)node.getChild(this.nodeName);
        if (engineNode == null) {
            engineNode = node;
        }
        
        //Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        //trail = new Geometry("Box", b);
        LineControl line = new LineControl(new LineControl.Algo2CamPosBBNormalized(), true);
        trailGeometry.addControl(line);
        trailControl = new TrailControl(line);
        engineNode.addControl(trailControl);
        trailControl.setStartWidth(this.endWidth * this.widthFactor);
        trailControl.setEndWidth(this.width * this.widthFactor);
        trailControl.setLifeSpan(this.lifetime);
        
        test = new Node();
        test.attachChild(trailGeometry);
        trailGeometry.setLocalTranslation(offset);
        test.setLocalTranslation(new Vector3f(0,2,0));  // without ignore transform this would offset the trail
        engineNode.attachChild(test);
        this.sound.attachTo(engineNode);
        
        if (this.model != null ) {
            engineNode.attachChild(this.model);
        }
        
        engineNode.setCullHint(Spatial.CullHint.Never);
        trailGeometry.setCullHint(Spatial.CullHint.Never);
    }
    
    public void setThrottle(float throttle) {
        this.trailControl.setLifeSpan(this.lifetime * throttle);
        if (this.model != null) {
            this.model.setLocalScale(1f, 1f, 3f * Math.max(0f, throttle));
        }
    }
    
    public void setWidthFactor(float widthFactor) {
        this.widthFactor = widthFactor;
        trailControl.setStartWidth(this.endWidth * this.widthFactor);
        trailControl.setEndWidth(this.width * this.widthFactor);
    }
    
    public void detach() {
        trailControl.getSpatial().removeControl(trailControl);
        test.removeFromParent();
        this.sound.detach();
        if (this.model != null ) {
            this.model.removeFromParent();
        }
    }

    public AudioEffect getSound() {
        return this.sound;
    }
    
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        this.offset = new Vector3f(XMLLoader.getVectorValue(element, "position", Vector3f.ZERO));
        
        this.nodeName = XMLLoader.getStringValue(element, "node", null);
        this.width = XMLLoader.getFloatValue(element, "width", this.width);
        this.endWidth = XMLLoader.getFloatValue(element, "endwidth", this.endWidth);
        this.lifetime = XMLLoader.getFloatValue(element, "lifetime", this.lifetime);
        this.materialName = XMLLoader.getStringValue(element, "material", null);
        
        String modelName = XMLLoader.getStringValue(element, "model", null);
        if (modelName != null) {
            this.model = gamedataManager.loadModel(modelName);
        }
        
        Material mat = gamedataManager.loadMaterial(this.materialName); //new Material(gamedataManager.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        //mat.setColor("Color", ColorRGBA.White);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        //mat.getAdditionalRenderState().setWireframe(true);
        trailGeometry = new Geometry();
        mat.getAdditionalRenderState().setAlphaTest(true);
        mat.getAdditionalRenderState().setAlphaFallOff(0.5f);
        trailGeometry.setMaterial(mat);
        //rootNode.attachChild(trail);  // either attach the trail geometry node to the root…
        trailGeometry.setIgnoreTransform(true); // or set ignore transform to true. this should be most useful when attaching nodes in the editor
        //trailGeometry.setQueueBucket(RenderQueue.Bucket.Translucent);
        
        Element audioElement = element.getChild("sound");
        this.sound = new AudioEffect(audioElement, path, gamedataManager);
        this.sound.setLocalTranslation(this.offset);
    }
    
    public void setColor(ColorRGBA color) {
        setColorOnSpatial(this.trailGeometry, color);
        //setColorOnSpatial(this.model, color);
    }
    
    private void setColorOnSpatial(Spatial spatial, ColorRGBA color) {
        if (spatial instanceof Node) {
            Node node = (Node)spatial;
            List<Spatial> childList = node.getChildren();
            for (Spatial child : childList) {
                setColorOnSpatial(child, color);
            }
        } else if (spatial instanceof Geometry) {
            Geometry gem = (Geometry)spatial;
            try {
                gem.getMaterial().setColor("Color", color);
            } catch (IllegalArgumentException e) {}
            try {
                gem.getMaterial().setColor("GlowColor", color);
            } catch (IllegalArgumentException e) {}
            if (gem.getMaterial().getAdditionalRenderState().isWireframe()) {
                gem.getMesh().setLineWidth(2);
            }
        }
    }
    
    @Override
    public Engine clone() {
        Engine engine = new Engine();
        engine.offset = offset;
        engine.nodeName = nodeName;
        engine.width = width;
        engine.endWidth = endWidth;
        engine.lifetime = lifetime;
        engine.materialName = materialName;
        engine.trailGeometry = new Geometry();
        engine.trailGeometry.setMaterial(trailGeometry.getMaterial());
        engine.trailGeometry.setIgnoreTransform(true);
        engine.sound = sound.clone();
        return engine;
    }
    
}
