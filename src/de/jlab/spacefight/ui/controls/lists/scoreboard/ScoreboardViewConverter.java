/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.controls.lists.scoreboard;

import de.lessvoid.nifty.controls.ListBox.ListBoxViewConverter;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;

/**
 *
 * @author rampage
 */
public class ScoreboardViewConverter implements ListBoxViewConverter<ScoreboardModel> {
    private static final String SCOREBOARD_LINE_NAME    = "#scoreboard-line-name";
    private static final String SCOREBOARD_LINE_KILLS   = "#scoreboard-line-kills";
    private static final String SCOREBOARD_LINE_DEATHS  = "#scoreboard-line-deaths";
    private static final String SCOREBOARD_LINE_SCORE   = "#scoreboard-line-score";

    /**
     * Default constructor.
     */
    public ScoreboardViewConverter() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void display(final Element listBoxItem, final ScoreboardModel item) {
        final Element nameText = listBoxItem.findElementByName(SCOREBOARD_LINE_NAME);
        final TextRenderer nameTextRenderer = nameText.getRenderer(TextRenderer.class);
        //int originWidth = ((originTextRenderer.getFont() == null) ? 0 : originTextRenderer.getFont().getWidth(item.getOrigin()));
        //originText.setWidth(originWidth);
        
        final Element killsText = listBoxItem.findElementByName(SCOREBOARD_LINE_KILLS);
        final TextRenderer killsTextRenderer = killsText.getRenderer(TextRenderer.class);
        //int damageWidth = ((damageTextRenderer.getFont() == null) ? 0 : damageTextRenderer.getFont().getWidth(item.getDamage()));
        //damageText.setWidth(damageWidth);
        
        final Element deathsText = listBoxItem.findElementByName(SCOREBOARD_LINE_DEATHS);
        final TextRenderer deathsTextRenderer = deathsText.getRenderer(TextRenderer.class);
        //int targetWidth = ((targetTextRenderer.getFont() == null) ? 0 : targetTextRenderer.getFont().getWidth(item.getTarget()));
        //targetText.setWidth(targetWidth);
        
        final Element scoreText = listBoxItem.findElementByName(SCOREBOARD_LINE_SCORE);
        final TextRenderer scoreTextRenderer = scoreText.getRenderer(TextRenderer.class);
        
        if (item != null) {
            nameTextRenderer.setColor(item.getItemColor());
            nameTextRenderer.setText(item.getName());
            killsTextRenderer.setText(Integer.toString(item.getKills()));
            deathsTextRenderer.setText(Integer.toString(item.getDeaths()));
            scoreTextRenderer.setText(Integer.toString(item.getScore()));
        } else {
            nameTextRenderer.setText("-");
            killsTextRenderer.setText("0");
            deathsTextRenderer.setText("0");
            scoreTextRenderer.setText("0");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getWidth(final Element listBoxItem, final ScoreboardModel item) {
        final Element nameText = listBoxItem.findElementByName(SCOREBOARD_LINE_NAME);
        final TextRenderer nameTextRenderer = nameText.getRenderer(TextRenderer.class);
        int nameWidth = ((nameTextRenderer.getFont() == null) ? 0 : nameTextRenderer.getFont().getWidth(item.getName()));
        //originText.setWidth(originWidth);
        
        final Element killsText = listBoxItem.findElementByName(SCOREBOARD_LINE_KILLS);
        final TextRenderer killsTextRenderer = killsText.getRenderer(TextRenderer.class);
        int killWidth = ((killsTextRenderer.getFont() == null) ? 0 : killsTextRenderer.getFont().getWidth(Integer.toString(item.getKills())));
        //damageText.setWidth(damageWidth);
        
        final Element deathsText = listBoxItem.findElementByName(SCOREBOARD_LINE_DEATHS);
        final TextRenderer deathsTextRenderer = deathsText.getRenderer(TextRenderer.class);
        int deathsWidth = ((deathsTextRenderer.getFont() == null) ? 0 : deathsTextRenderer.getFont().getWidth(Integer.toString(item.getDeaths())));
        //targetText.setWidth(targetWidth);
        
        final Element scoreText = listBoxItem.findElementByName(SCOREBOARD_LINE_SCORE);
        final TextRenderer scoreTextRenderer = scoreText.getRenderer(TextRenderer.class);
        int scoreWidth = ((scoreTextRenderer.getFont() == null) ? 0 : scoreTextRenderer.getFont().getWidth(Integer.toString(item.getScore())));
        
        return nameWidth + killWidth + deathsWidth + scoreWidth;
    }
}
