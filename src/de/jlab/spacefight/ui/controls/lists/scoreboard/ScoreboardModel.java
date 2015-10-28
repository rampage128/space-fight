/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.controls.lists.scoreboard;

import de.lessvoid.nifty.tools.Color;

/**
 *
 * @author rampage
 */
public class ScoreboardModel {

    private String name;
    private int kills;
    private int deaths;
    private int score;
    private Color itemColor;

    public ScoreboardModel(String name, int kills, int deaths, int score, Color itemColor) {
        this.name   = name;
        this.kills  = kills;
        this.deaths = deaths;
        this.score  = score;
        this.itemColor = itemColor;
    }

    public String getName() {
        return this.name;
    }

    public int getKills() {
        return this.kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int getScore() {
        return this.score;
    }
    
    public Color getItemColor() {
        return this.itemColor;
    }

}
