/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.audio;

import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.Game;
import de.jlab.spacefight.debug.DebugContext;
import de.jlab.spacefight.debug.SpaceDebugger;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.systems.PhysicsControl;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author rampage
 */
public class AudioManager {
    
    public static final String CONFIG_FILENAME = "audio.cfg";
    
    private Game _app;
    
    // TODO: DETERMINE CHANNELCOUNT, IMPLEMENT PRIORITY SYSTEM FOR SOUNDS (AudioSystem?)
    private int _channelCount = 0;
    
    private float musicVolume = 1f;
    private float soundVolume = 1f;
    
    private float userMasterVolume  = 1f;
    private float userEffectVolume  = 0.8f;
    private float userMusicVolume   = 0.5f;
    private float userSpeechVolume  = 1f;
    
    public AudioManager(Game app) {
        _app = app;
        
        SimpleConfig userConfig = new SimpleConfig();
        userConfig.loadFromFile(CONFIG_FILENAME);
        this.userMasterVolume = userConfig.getFloatValue("mastervolume", userMasterVolume);
        this.userEffectVolume = userConfig.getFloatValue("effectvolume", userEffectVolume);
        this.userMusicVolume  = userConfig.getFloatValue("musicvolume", userMusicVolume);
        this.userSpeechVolume = userConfig.getFloatValue("speechvolume", userSpeechVolume);
        if (!new File(CONFIG_FILENAME).exists()) {
            userConfig.saveToFile(CONFIG_FILENAME);
        }
    }

    public void saveConfig() {
        SimpleConfig userConfig = new SimpleConfig();
        userConfig.setValue("mastervolume", Float.toString(userMasterVolume));
        userConfig.setValue("effectvolume", Float.toString(userEffectVolume));
        userConfig.setValue("musicvolume", Float.toString(userMusicVolume));
        userConfig.setValue("speechvolume", Float.toString(userSpeechVolume));
        userConfig.saveToFile(CONFIG_FILENAME);
    }
    
    public void update() {
        updateSoundLoops();
        updateListener();
        updateMusic();
    }

    /* LISTENER */
    private Spatial _listenerSpatial;
    private PhysicsControl _listenerControl;
    
    private Vector3f _listenerPreviousLocation = Vector3f.ZERO.clone();

    public void setUserMasterVolume(float userMasterVolume) {
        this.userMasterVolume = userMasterVolume;
    }
    
    public void setUserEffectVolume(float userEffectVolume) {
        this.userEffectVolume = userEffectVolume;
    }
    
    public void setUserMusicVolume(float userMusicVolume) {
        this.userMusicVolume = userMusicVolume;
    }
    
    public void setUserSpeechVolume(float userSpeechVolume) {
        this.userSpeechVolume = userSpeechVolume;
    }
    
    public float getUserMasterVolume() {
        return this.userMasterVolume;
    }
    
    public float getUserEffectVolume() {
        return this.userEffectVolume;
    }
    
    public float getUserMusicVolume() {
        return this.userMusicVolume;
    }
    
    public float getUserSpeechVolume() {
        return this.userSpeechVolume;
    }
    
    private float getEffectVolume() {
        return this.userEffectVolume * this.userMasterVolume * this.soundVolume;
    }
    
    private float getMusicVolume() {
        return this.userMusicVolume * this.userMasterVolume * this.musicVolume;
    }
    
    private float getSpeechVolume() {
        return this.userSpeechVolume * this.userMasterVolume * this.soundVolume;
    }
          
    public void setListenerControl(PhysicsControl listenerControl) {
        _listenerControl = listenerControl;
    }
    
    public void setListenerSpatial(Spatial listenerSpatial) {
        _listenerSpatial = listenerSpatial;
    }
    
    public void setVolume(float volume) {
        _app.getListener().setVolume(volume);
    }
    
    public void setSoundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
    }
    
    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
        if (this.musicPlaying) {
            this.music.setVolume(musicVolume);
        }
    }
    
    public float getVolume() {
        return _app.getListener().getVolume();
    }
    
    private void updateListener() {
        // UPDATE POSITION
        if ( _listenerSpatial != null ) {
            // USING SOURCE SPATIAL
            _app.getListener().setLocation(_listenerSpatial.getWorldTranslation());
            _app.getListener().setRotation(_listenerSpatial.getWorldRotation());
        } else {
            // USING CAMERA AS DEFAULT
            _app.getListener().setLocation(_app.getCamera().getLocation());
            _app.getListener().setRotation(_app.getCamera().getRotation());
        }
        
        // UPDATE VELOCITY
        if ( _listenerControl != null ) {
            // USING SOURCE RIGIDBODY
            _app.getListener().setVelocity(_listenerControl.getLinearVelocity());
        } else {
            // USING CAMERA POSITION AS DEFAULT
            _app.getListener().setVelocity(_app.getCamera().getLocation().subtract(_listenerPreviousLocation));
            _listenerPreviousLocation.set(_app.getListener().getLocation());
        }
        
        SpaceDebugger.getInstance().setBlip(DebugContext.AUDIO, "Listener", _app.getListener().getLocation(), 4, ColorRGBA.White);
    }
    
    /* SOUND EFFECTS */
    public void playSound(AudioEffect audio, Vector3f soundPos) {
        audio.setLocalTranslation(soundPos);
        Vector3f playerPos = _app.getListener().getLocation();
        if (audio.isAudible(playerPos, getEffectVolume())) {
            audio.playInstance(getEffectVolume());
        }
    }
    
    public void playSound(AudioEffect audio) {
        Vector3f playerPos = _app.getListener().getLocation();
        if (audio.isAudible(playerPos, getEffectVolume())) {
            audio.playInstance(getEffectVolume());
        }
    }
    
    /* LOOPING SOUND EFFECTS */
    private ArrayList<AudioEffect> soundLoopList = new ArrayList<AudioEffect>();
    
    private void updateSoundLoops() {
        for ( int i = 0; i < soundLoopList.size(); i++ ) {
            AudioEffect audio = soundLoopList.get(i);
            Vector3f playerPos = _app.getListener().getLocation();
            if (audio.isAudible(playerPos, getEffectVolume())) {
                if (audio.getStatus() != AudioSource.Status.Playing) {
                    audio.play(getEffectVolume());
                }
                SpaceDebugger.getInstance().setBlip(DebugContext.AUDIO, "Audio " + audio.hashCode(), audio.getWorldTranslation(), 3 * Math.max(0, 1 - (audio.getWorldTranslation().subtract(playerPos).length() / audio.getMaxDistance())), ColorRGBA.Yellow);
            } else {
                if ( audio.getStatus() == AudioSource.Status.Playing ) {
                    audio.stop();
                }
                SpaceDebugger.getInstance().removeItem(DebugContext.AUDIO, "Audio " + audio.hashCode());
            }
        }
    }
    
    public void playSoundLoop(AudioEffect audio) {
        if ( !soundLoopList.contains(audio) ) {
            soundLoopList.add(audio);
        }
    }
    
    public void stopSoundLoop(AudioEffect audio) {
        for ( int i = 0; i < soundLoopList.size(); i++ ) {
            AudioEffect originalNode = soundLoopList.get(i);
            if (originalNode == audio) {
                soundLoopList.remove(i).stop();
            }
        }
    }
    
    /* MUSIC */
    private String musicFile;
    private AudioEffect music;
    private boolean musicLooping = false;
    private boolean musicPlaying = false;
    
    private void updateMusic() {
        if (this.music != null) {
            if (this.musicVolume > 0 && this.musicPlaying && this.musicLooping && this.music.getStatus() != AudioSource.Status.Playing) {
                playMusic(this.musicFile, 0, this.musicLooping);
                //this.music.play(getMusicVolume());
                //this.musicPlaying = true;
            } else if (!musicPlaying && this.music.getStatus() == AudioSource.Status.Playing) {
                this.music.stop();
                this.musicPlaying = false;
            }
        }
    }
    
    public void playMusic(String name, float fadeTime, boolean looping) {
        AudioEffect newMusic = new AudioEffect(_app.getGamedataManager().loadMusic(name));
        if (this.music != null && this.music.getStatus() == AudioSource.Status.Playing) {
            this.music.stop();
        }
        this.music = newMusic;
        this.musicLooping = looping;
        this.musicFile = name;
        this.music.play(getMusicVolume());
        this.musicPlaying = true;
    }
    
    public void stopMusic(float fadeTime) {
        if (this.music != null && this.music.getStatus() == AudioSource.Status.Playing) {
            this.music.stop();
            this.musicFile = null;
            this.music = null;
            this.musicLooping = false;
            this.musicPlaying = false;
        }
    }
    
    public void cleanup() {
        _listenerSpatial = null;
        _listenerControl = null;
        while (!this.soundLoopList.isEmpty()) {
            AudioEffect node = this.soundLoopList.get(0);
            stopSoundLoop(node);
        }
        stopMusic(0f);
    }
    
}
