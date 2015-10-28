/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network;

import com.jme3.network.AbstractMessage;

/**
 *
 * @author rampage
 */
public abstract class NetworkMessage extends AbstractMessage {
    
    public abstract boolean mustBeReliable();
    
}
