/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.ingame.hud;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.SpaceAppState;
import java.util.HashMap;

/**
 *
 * @author Stefan
 */
public abstract class HudItem {

    private SpaceAppState space;
    private Camera camera;
    
    private Node itemNode;
    
    private float alignmentX = 0.5f;
    private float alignmentY = 0.5f;
                
    private HashMap<String, Spatial> pictureMap = new HashMap<String, Spatial>();
        
    public HudItem(String name, SpaceAppState space) {
        this.space = space;
        this.itemNode = new Node(name);
        this.space.addToUI(itemNode);
        this.camera = this.space.getGame().getCamera();
        //this.setPosition(0.5f, 0.5f);
        //this.setSize(0.125f);
        
        /* // DEBUG !!! 
        Box s = new Box(2, 2, 2);
        Geometry gem = new Geometry("pivot", s);
        Material mat = new Material(this.game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        gem.setMaterial(mat);
        gem.setLocalScale(0.01f, 0.01f, 0.01f);
        this.itemNode.attachChild(gem);
        */
    }
    
    public void cleanup() {
        this.space.removeFromUI(itemNode);
    }
    
    public abstract void initItem(Spatial spatial);
    public abstract void updateItem(float tpf, Spatial spatial);
    public abstract void renderItem(Spatial spatial, RenderManager rm, ViewPort vp);
    public abstract void cleanupItem(Spatial spatial);

    public Camera getCamera() {
        return this.camera;
    }
    
    /* ITEM RELATED TRANSFORMATIONS */
    public void move(float positionX, float positionY, float alignX, float alignY) {
        //float screenX = this.camera.getWidth() * positionX - this.camera.getWidth() * (this.itemNode.getLocalScale().getX() / this.camera.getWidth()) * (alignX - 0.5f);
        //float screenY = this.camera.getHeight() * positionY - this.camera.getHeight() * (this.itemNode.getLocalScale().getY() / this.camera.getHeight()) * (alignY - 0.5f);
        float screenX = this.camera.getWidth() * positionX - this.itemNode.getLocalScale().getX() * (alignX - 0.5f);
        float screenY = this.camera.getHeight() * positionY - this.itemNode.getLocalScale().getY() * (alignY - 0.5f);
        this.alignmentX = alignX;
        this.alignmentY = alignY;
        this.itemNode.setLocalTranslation(screenX, screenY, 0);
    }

    public void scale(float size) {
        this.itemNode.setLocalScale(this.camera.getHeight() * size, this.camera.getHeight() * size, this.camera.getHeight() * size);
    }
    
    public void scale(float sizeX, float sizeY) {
        this.itemNode.setLocalScale(this.camera.getWidth() * sizeX, this.camera.getHeight() * sizeY, this.camera.getHeight() * sizeY);
    }
        
    /* COMPONENT MANAGEMENT */
    protected void addPicture(String key, Spatial picture) {
        Node node = new Node(key);
        node.setQueueBucket(Bucket.Gui);
        node.attachChild(picture);
        picture.setLocalTranslation(picture.getLocalScale().getX() * -0.5f, picture.getLocalScale().getY() * -0.5f, 0);
        //node.setLocalScale(picture.getLocalScale());
        //picture.setLocalScale(1f, 1f, 1f);
        this.pictureMap.put(key, node);
        
        /* // DEBUG !!! 
        Box s = new Box(2, 2, 2);
        Geometry gem = new Geometry("pivot", s);
        Material mat = new Material(this.game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Yellow);
        gem.setMaterial(mat);
        gem.setLocalScale(0.01f, 0.01f, 0.01f);
        node.attachChild(gem);
        */
    }
    
    protected Spatial getPicture(String key) {
        return this.pictureMap.get(key);
    }
    
    protected void removePicture(String key) {
        this.hidePicture(key);
        this.pictureMap.remove(key);
    }
    
    protected void showPicture(String key) {   
        if ( !this.itemNode.hasChild(this.pictureMap.get(key)) ) {
            this.itemNode.attachChild(this.pictureMap.get(key));
        }
    }
    
    protected void hidePicture(String key) {    
        this.itemNode.detachChild(this.pictureMap.get(key));
    }
    
    /* RELATIVE PICTURE MOVEMENT */
    protected void rotatePicture(String key, float rotation) {
        Spatial picture = this.pictureMap.get(key);
        picture.rotate(0, 0, rotation);
    }
    
    protected void scalePicture(String key, float scale) {        
        float aspect = this.itemNode.getLocalScale().getY() / this.itemNode.getLocalScale().getX();
        float scaleX = scale * aspect;
        float scaleY = scale;
        scalePicture(key, scaleX, scaleY);
    }
    
    protected void scalePicture(String key, float scaleX, float scaleY) {
        Spatial picture = this.pictureMap.get(key);        
        //float screenX = (this.camera.getWidth() * this.itemNode.getLocalScale().getX() * scaleX);
        //float screenY = (this.camera.getHeight() * this.itemNode.getLocalScale().getY() * scaleY);        
        picture.setLocalScale(scaleX, scaleY, scaleX);
    }
    
    protected void movePicture(String key, float positionX, float positionY, float alignmentX, float alignmentY) {
        Spatial picture = this.pictureMap.get(key);
        
        float screenX = positionX - (alignmentX - 0.5f) * picture.getLocalScale().getX() - this.alignmentX;
        float screenY = positionY - (alignmentY - 0.5f) * picture.getLocalScale().getX() - this.alignmentY;
        
        picture.setLocalTranslation(screenX, screenY, 0);
    }
           
    /* OTHER STUFF */    
    public SpaceAppState getSpace() {
        return this.space;
    }
    
}
