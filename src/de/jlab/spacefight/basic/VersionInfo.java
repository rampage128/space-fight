/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.basic;

/**
 *
 * @author rampage
 */
public class VersionInfo {
    
    public static final int STATE_STABLE = 0;
    public static final int STATE_RELEASECANDIDATE = 1;
    public static final int STATE_BETA  = 2;
    public static final int STATE_ALPHA = 3;
    
    private int major;
    private int minor;
    private int revision;
    private int state;
    
    public VersionInfo(int major, int minor, int revision, int state) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.state = state;
    }
    
    @Override
    public String toString() {
        StringBuilder versionString = new StringBuilder("V ");       
        versionString.append(major).append(".").append(minor).append(".");
        switch (this.state) {
            case STATE_ALPHA:
                versionString.append("A");
                break;
            case STATE_BETA:
                versionString.append("B");
                break;
            case STATE_RELEASECANDIDATE:
                versionString.append("RC");
                break;
        }
        versionString.append(revision);        
        return versionString.toString();
    }
    
}
