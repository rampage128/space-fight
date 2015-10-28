/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.screens;

import de.jlab.spacefight.Game;
import de.jlab.spacefight.ui.TabController;
import de.jlab.spacefight.ui.controls.DropDownValue;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author rampage
 */
public class Options implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    
    private TabController tabs;

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
        
        this.tabs = new TabController(screen);
        this.tabs.addTab("button_pilot", "tab_pilot");
        this.tabs.addTab("button_controls", "tab_controls");
        this.tabs.addTab("button_video", "tab_video");
        this.tabs.addTab("button_audio", "tab_audio");
    }

    public void onStartScreen() {
        TextField pilotName = this.screen.findNiftyControl("pilot_name", TextField.class);
        pilotName.setText(Game.get().getPlayer().getNickname());
        
        DropDown<DropDownValue> videoShadowQuality = this.screen.findNiftyControl("video_shadow_quality", DropDown.class);
        videoShadowQuality.clear();
        videoShadowQuality.addItem(new DropDownValue("Off",     0));
        videoShadowQuality.addItem(new DropDownValue("Low",     1));
        videoShadowQuality.addItem(new DropDownValue("Medium",  2));
        videoShadowQuality.addItem(new DropDownValue("High",    3));
        videoShadowQuality.addItem(new DropDownValue("Ultra",   4));
        videoShadowQuality.selectItemByIndex(Game.get().getVideoManager().getShadowQuality());
        
        DropDown<DropDownValue> ssaoQuality = this.screen.findNiftyControl("video_ssao_quality", DropDown.class);
        ssaoQuality.addItem(new DropDownValue("Off", 0));
        ssaoQuality.addItem(new DropDownValue("On",  1));
        ssaoQuality.selectItemByIndex(Game.get().getVideoManager().getSSAOQuality());
        
        DropDown<DropDownValue> fxaaQuality = this.screen.findNiftyControl("video_fxaa_quality", DropDown.class);
        fxaaQuality.addItem(new DropDownValue("Off", 0));
        fxaaQuality.addItem(new DropDownValue("On",  1));
        fxaaQuality.selectItemByIndex(Game.get().getVideoManager().getFXAAQuality());
        
        Slider videoGamma  = this.screen.findNiftyControl("video_gamma", Slider.class);
        videoGamma.setValue(Game.get().getVideoManager().getGamma());
        
        Slider audioMaster  = this.screen.findNiftyControl("audio_master", Slider.class);
        Slider audioEffects = this.screen.findNiftyControl("audio_effects", Slider.class);
        Slider audioMusic   = this.screen.findNiftyControl("audio_music", Slider.class);
        Slider audioSpeech  = this.screen.findNiftyControl("audio_speech", Slider.class);
        
        audioMaster.setValue(Game.get().getAudioManager().getUserMasterVolume());
        audioEffects.setValue(Game.get().getAudioManager().getUserEffectVolume());
        audioMusic.setValue(Game.get().getAudioManager().getUserMusicVolume());
        audioSpeech.setValue(Game.get().getAudioManager().getUserSpeechVolume());
    }

    public void onEndScreen() {
        
    }

      //////////////////////////////////////////////////////////////////////////
     // TAB BUTTON CALLBACKS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void showPilot() {
        this.tabs.switchTab("tab_pilot");
    }
    
    public void showControls() {
        this.tabs.switchTab("tab_controls");
    }
    
    public void showVideo() {
        this.tabs.switchTab("tab_video");
    }
    
    public void showAudio() {
        this.tabs.switchTab("tab_audio");
    }
    
      //////////////////////////////////////////////////////////////////////////
     // MAIN BUTTON CONTROLS //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public void apply() {
        TextField pilotName = this.screen.findNiftyControl("pilot_name", TextField.class);
        Game.get().getPlayer().setNickname(pilotName.getText());
        Game.get().getPlayer().savePlayer();
                
        DropDown<DropDownValue> videoShadowQuality = this.screen.findNiftyControl("video_shadow_quality", DropDown.class);
        DropDown<DropDownValue> ssaoQuality = this.screen.findNiftyControl("video_ssao_quality", DropDown.class);
        DropDown<DropDownValue> fxaaQuality = this.screen.findNiftyControl("video_fxaa_quality", DropDown.class);
        Slider videoGamma  = this.screen.findNiftyControl("video_gamma", Slider.class);
        Game.get().getVideoManager().setShadowQuality(videoShadowQuality.getSelection().getIntValue());
        Game.get().getVideoManager().setSSAOQuality(ssaoQuality.getSelection().getIntValue());
        Game.get().getVideoManager().setFXAAQuality(fxaaQuality.getSelection().getIntValue());
        Game.get().getVideoManager().setGamma(videoGamma.getValue());
        Game.get().getVideoManager().saveConfig();
        Game.get().getVideoManager().reshape();
        
        Slider audioMaster  = this.screen.findNiftyControl("audio_master", Slider.class);
        Slider audioEffects = this.screen.findNiftyControl("audio_effects", Slider.class);
        Slider audioMusic   = this.screen.findNiftyControl("audio_music", Slider.class);
        Slider audioSpeech  = this.screen.findNiftyControl("audio_speech", Slider.class);        
        Game.get().getAudioManager().setUserMasterVolume(audioMaster.getValue());
        Game.get().getAudioManager().setUserEffectVolume(audioEffects.getValue());
        Game.get().getAudioManager().setUserMusicVolume(audioMusic.getValue());
        Game.get().getAudioManager().setUserSpeechVolume(audioSpeech.getValue());
        Game.get().getAudioManager().saveConfig();
        
        this.nifty.gotoScreen("mainmenu");
    }
    
    public void cancel() {
        this.nifty.gotoScreen("mainmenu");
    }
    
}