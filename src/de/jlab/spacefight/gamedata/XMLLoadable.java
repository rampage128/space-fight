/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.gamedata;

import org.jdom.Element;

/**
 *
 * @author rampage
 */
public interface XMLLoadable {
    
    /**
     * Loads parameters from an XML-Element
     * @param element the jdom-Element to load parameters from
     */
    public void loadFromElement(Element element, String path, GamedataManager gamedataManager);
    
}
