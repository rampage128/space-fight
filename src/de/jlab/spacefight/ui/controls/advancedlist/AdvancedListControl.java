/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui.controls.advancedlist;

import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.tools.SizeValue;

/**
 *
 * @author rampage
 */
public class AdvancedListControl<T> extends ListBoxControl<T> {
      
    @Override
    public void refresh() {
        this.getElement().setConstraintHeight(SizeValue.percentHeight(100));
        super.refresh();
    }
    
}
