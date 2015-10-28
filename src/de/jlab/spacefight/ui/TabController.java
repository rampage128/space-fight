/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui;

import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import java.util.ArrayList;

/**
 *
 * @author rampage
 */
public class TabController {
    
    private Screen screen;
    
    private ArrayList<Tab> tabList = new ArrayList<Tab>();
    
    private Tab currentTab;
    
    public TabController(Screen screen) {
        this.screen = screen;
    }
    
    public void addTab(String buttonId, String panelId) {
        Tab newTab = new Tab(buttonId, panelId);
        for (Tab tab : this.tabList) {
            if (tab.isTab(newTab)) {
                return;
            }
        }

        if (this.tabList.size() > 0) {
            newTab.hide();
        } else {
            newTab.show();
            this.currentTab = newTab;
        }
        this.tabList.add(newTab);
    }
    
    public void switchTab(String tabId) {
        Tab selectedTab = null;
        for (Tab tab : this.tabList) {
            if (tab.isTab(tabId)) {
                selectedTab = tab;
                break;
            }
        }
        
        if (selectedTab == null) {
            return;
        }
        
        if (selectedTab == this.currentTab) {
            return;
        }
        
        if (!selectedTab.isEnabled()) {
            nextTab();
            return;
        }
        
        for (Tab tab : this.tabList) {
            if (tab != selectedTab) {
                tab.hide();
            } else {
                tab.show();
                this.currentTab = tab;
            }
        }
    }
    
    public void nextTab() {
        int selectionIndex = this.tabList.indexOf(this.currentTab) + 1;
        if (selectionIndex >= this.tabList.size() - 1) {
            selectionIndex = 0;
        }
        switchTab(this.tabList.get(selectionIndex).getId());
    }
    
    public String getCurrentTab() {
        if (this.currentTab == null) {
            return null;
        }
        return this.currentTab.getId();
    }
    
    public void toggleTab(String tabId, boolean state) {
        Tab selectedTab = null;
        for (Tab tab : this.tabList) {
            if (tab.isTab(tabId)) {
                selectedTab = tab;
                break;
            }
        }
        
        if (selectedTab == null) {
            return;
        }
        
        if (state) {
            selectedTab.enable();
        } else {
            selectedTab.disable();
            if (selectedTab == this.currentTab) {
                nextTab();
            }
        }
    }
    
    private class Tab {
        
        private Button button;
        private Element panel;
        
        private boolean enabled = true;
        
        private Tab(String buttonId, String panelId) {
            this.button = TabController.this.screen.findNiftyControl(buttonId, Button.class);
            this.panel  = TabController.this.screen.findElementByName(panelId);
        }
        
        private void show() {
            this.panel.setVisible(true);
        }
        
        private void hide() {
            this.panel.setVisible(false);
        }
        
        private void disable() {
            this.button.disable();
            this.panel.disable();
            this.enabled = false;
        }
        
        private void enable() {
            this.button.enable();
            this.panel.enable();
            this.enabled = true;
        }
        
        private boolean isEnabled() {
            return this.enabled;
        }
        
        private boolean isTab(String tabId) {
            return tabId.equalsIgnoreCase(this.panel.getId());
        }
        
        private boolean isTab(Tab tab) {
            return tab.panel.getId().equalsIgnoreCase(this.panel.getId());
        }
        
        private String getId() {
            return this.panel.getId();
        }
        
    }
    
}
