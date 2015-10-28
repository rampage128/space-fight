/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.audio;

import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.XMLLoadable;
import de.jlab.spacefight.gamedata.XMLLoader;
import org.jdom.Element;

/**
 *
 * @author rampage
 */
public final class AudioEffect implements XMLLoadable {
    
    private float volume = 1f;
    private AudioNode audio = null;
    
    public AudioEffect(AudioNode audio) {
        this.audio = audio;
    }
    
    public AudioEffect(Element element, String path, GamedataManager gamedataManager) {
        loadFromElement(element, path, gamedataManager);
    }

    public void loadFromElement(Element element, String path, GamedataManager gamedataManager) {
        String soundFileName = XMLLoader.getStringValue(element, "file", null);
        this.volume      = XMLLoader.getFloatValue(element, "volume", 1);
        float maxDistance = XMLLoader.getFloatValue(element, "maxdistance", 200);
        float refDistance = XMLLoader.getFloatValue(element, "refdistance", 100);
        Vector3f position = new Vector3f(XMLLoader.getVectorValue(element, "position", Vector3f.ZERO));
        this.audio = gamedataManager.loadSound(soundFileName);
        //this.audio.setLooping(!TYPE_ONCE.equalsIgnoreCase(type));
        this.audio.setLocalTranslation(position);
        this.audio.setVolume(this.volume);
        this.audio.setMaxDistance(maxDistance);
        this.audio.setRefDistance(refDistance);
        this.audio.setPositional(true);
        this.audio.setDirectional(false);
    }
    
    public void setVelocity(Vector3f velocity) {
        this.audio.setVelocity(velocity);
    }
    
    public boolean isLooping() {
        return this.audio.isLooping();
    }
    
    public void setLocalTranslation(Vector3f translation) {
        this.audio.setLocalTranslation(translation);
    }
    
    public Vector3f getWorldTranslation() {
        return this.audio.getWorldTranslation();
    }
    
    public AudioSource.Status getStatus() {
        return this.audio.getStatus();
    }
    
    public void setVolume(float mixerVolume) {
        this.audio.setVolume(this.volume * mixerVolume);
    }
    
    public void setPitch(float pitch) {
        this.audio.setPitch(pitch);
    }
    
    public float getMaxDistance() {
        return this.audio.getMaxDistance();
    }
    
    public void play(float mixerVolume) {
        this.audio.setVolume(this.volume * mixerVolume);
        this.audio.play();
    }
    
    public void playInstance(float mixerVolume) {
        this.audio.setVolume(this.volume * mixerVolume);
        this.audio.playInstance();
    }
    
    public void stop() {
        this.audio.stop();
    }
    
    public boolean isPositional() {
        return this.audio.isPositional();
    }
    
    public boolean isAudible(Vector3f listenerPosition, float mixerVolume) {
        boolean inRange = !audio.isPositional() || getWorldTranslation().subtract(listenerPosition).length() <= audio.getMaxDistance();
        boolean loudEnough = mixerVolume * this.audio.getVolume() > 0;
        return inRange && loudEnough;
    }
    
    public void attachTo(Node parent) {
        parent.attachChild(this.audio);
    }
    
    public void detach() {
        this.audio.removeFromParent();
    }
 
    public AudioEffect clone() {
        return new AudioEffect(this.audio.clone());
    }
    
}
