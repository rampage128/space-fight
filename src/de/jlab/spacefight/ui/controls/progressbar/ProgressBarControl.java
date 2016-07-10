/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.controls.progressbar;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.Parameters;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;

/**
 *
 * @author rampage
 */
public class ProgressBarControl implements Controller {
    private Element progressBarElement;
    private Element progressTextElement;

    public void onStartScreen() {
    }

    public void onFocus(final boolean getFocus) {
    }

    public boolean inputEvent(final NiftyInputEvent inputEvent) {
        return false;
    }

    public void setProgress(final float progressValue) {
        float progress = progressValue;
        if (progress < 0.0f) {
            progress = 0.0f;
        } else if (progress > 1.0f) {
            progress = 1.0f;
        }
        final int MIN_WIDTH = 32; 
        int pixelWidth = (int)(MIN_WIDTH + (progressBarElement.getParent().getWidth() - MIN_WIDTH) * progress);
        progressBarElement.setConstraintWidth(new SizeValue(pixelWidth + "px"));
        progressBarElement.getParent().layoutElements();

        String progressText = String.format("%3.0f%%", progress * 100);
        progressTextElement.getRenderer(TextRenderer.class).setText(progressText);
    }

    public void bind(Nifty nifty, Screen screen, Element element, Parameters parameter) {
        progressBarElement = element.findElementById("progress");
        progressTextElement = element.findElementById("progress-text");
    }

    public void init(Parameters parameter) {
        
    }
}