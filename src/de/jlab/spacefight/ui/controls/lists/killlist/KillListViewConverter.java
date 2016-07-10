/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.controls.lists.killlist;

import de.lessvoid.nifty.controls.ListBox.ListBoxViewConverter;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.tools.Color;

/**
 *
 * @author rampage
 */
public class KillListViewConverter implements ListBoxViewConverter<KillListModel> {
    private static final String KILL_LINE_ORIGIN = "#kill-line-origin";
    private static final String KILL_LINE_DAMAGE = "#kill-line-damage";
    private static final String KILL_LINE_TARGET = "#kill-line-target";

    /**
     * Default constructor.
     */
    public KillListViewConverter() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void display(final Element listBoxItem, final KillListModel item) {
        final Element originText = listBoxItem.findElementById(KILL_LINE_ORIGIN);
        final TextRenderer originTextRenderer = originText.getRenderer(TextRenderer.class);
        //int originWidth = ((originTextRenderer.getFont() == null) ? 0 : originTextRenderer.getFont().getWidth(item.getOrigin()));
        //originText.setWidth(originWidth);
        
        final Element damageText = listBoxItem.findElementById(KILL_LINE_DAMAGE);
        final TextRenderer damageTextRenderer = damageText.getRenderer(TextRenderer.class);
        //int damageWidth = ((damageTextRenderer.getFont() == null) ? 0 : damageTextRenderer.getFont().getWidth(item.getDamage()));
        //damageText.setWidth(damageWidth);
        
        final Element targetText = listBoxItem.findElementById(KILL_LINE_TARGET);
        final TextRenderer targetTextRenderer = targetText.getRenderer(TextRenderer.class);
        //int targetWidth = ((targetTextRenderer.getFont() == null) ? 0 : targetTextRenderer.getFont().getWidth(item.getTarget()));
        //targetText.setWidth(targetWidth);
        
        if (item != null) {
            originTextRenderer.setColor(item.getOriginColor());
            originTextRenderer.setText(item.getOrigin());
            damageTextRenderer.setColor(Color.WHITE);
            damageTextRenderer.setText(item.getDamage());
            targetTextRenderer.setColor(item.getTargetColor());
            targetTextRenderer.setText(item.getTarget());
        } else {
            originTextRenderer.setText("");
            
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getWidth(final Element listBoxItem, final KillListModel item) {
        final Element originText = listBoxItem.findElementById(KILL_LINE_ORIGIN);
        final TextRenderer originTextRenderer = originText.getRenderer(TextRenderer.class);
        int originWidth = ((originTextRenderer.getFont() == null) ? 0 : originTextRenderer.getFont().getWidth(item.getOrigin()));
        //originText.setWidth(originWidth);
        
        final Element damageText = listBoxItem.findElementById(KILL_LINE_DAMAGE);
        final TextRenderer damageTextRenderer = damageText.getRenderer(TextRenderer.class);
        int damageWidth = ((damageTextRenderer.getFont() == null) ? 0 : damageTextRenderer.getFont().getWidth(item.getDamage()));
        //damageText.setWidth(damageWidth);
        
        final Element targetText = listBoxItem.findElementById(KILL_LINE_TARGET);
        final TextRenderer targetTextRenderer = targetText.getRenderer(TextRenderer.class);
        int targetWidth = ((targetTextRenderer.getFont() == null) ? 0 : targetTextRenderer.getFont().getWidth(item.getTarget()));
        //targetText.setWidth(targetWidth);
        
        return originWidth + damageWidth + targetWidth;
    }
}
