/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.gamedata;

import com.jme3.network.serializing.Serializable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rampage
 */
@Serializable()
public class SimpleConfig {
    
    private LinkedHashMap<String, String> lineMap = new LinkedHashMap<String, String>();
    
    public Class<?> getClassValue(String name, Class<?> defaultValue) {
        String value = lineMap.get(name);
        if ( value == null ) {
            return defaultValue;
        }
        try {
            return Class.forName(value);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SimpleConfig.class.getName()).log(Level.SEVERE, "Cannot get class from config (" + name + " = " + value + ")", ex);
            return defaultValue;
        }
    }
    
    public int getIntValue(String name, int defaultValue) {
        String value = lineMap.get(name);
        if ( value == null ) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            Logger.getLogger(SimpleConfig.class.getName()).log(Level.SEVERE, "Cannot get int from config (" + name + " = " + value + ")", ex);
            return defaultValue;
        }
    }
    
    public float getFloatValue(String name, float defaultValue) {
        String value = lineMap.get(name);
        if ( value == null ) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ex) {
            Logger.getLogger(SimpleConfig.class.getName()).log(Level.SEVERE, "Cannot get float from config (" + name + " = " + value + ")", ex);
            return defaultValue;
        }
    }

    public String getValue(String name, String defaultValue) {
        String value = lineMap.get(name);
        if ( value == null ) {
            return defaultValue;
        }
        return value;
    }
    
    public void setValue(String name, String value) {
        this.lineMap.put(name, value);
    }
    
    public void clear() {
        this.lineMap.clear();
    }
    
    public void saveToFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            Logger.getLogger(SimpleConfig.class.getName()).log(Level.WARNING, "Replacing existing file (" + fileName + ").");
            if (!file.delete()) {
                Logger.getLogger(SimpleConfig.class.getName()).log(Level.WARNING, "Cannot save config to existing file (" + fileName + "): Cannot delete old file!");    
                return;
            }
        }
        
        if (!file.exists()) {
            RandomAccessFile raf = null;
            try {
                if (file.createNewFile()) {
                    raf = new RandomAccessFile(file, "rw");
                    for ( String name : this.lineMap.keySet() ) {
                        String value = getValue(name, "");
                        String line = new StringBuilder(name).append(" = ").append(value).append("\n").toString();
                        raf.writeBytes(line);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(SimpleConfig.class.getName()).log(Level.SEVERE, "Cannot save config to file (" + fileName + "): " + ex.getMessage(), ex);
            }
            
            if ( raf != null ) {
                try {
                    raf.close();
                } catch (IOException ex) {}
            }
        }
    }
    
    public boolean loadFromFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            Logger.getLogger(SimpleConfig.class.getName()).log(Level.WARNING, "Cannot load config from file (" + fileName + "): File does not exist!");
            return false;
        }
        
        if (!file.isFile()) {
            Logger.getLogger(SimpleConfig.class.getName()).log(Level.SEVERE, "Cannot load config from file (" + fileName + "): File is a directory!");
            return false;
        }
        
        boolean result = false;
        RandomAccessFile raf = null;
        int lineNumber = 0;
        try {
            raf = new RandomAccessFile(file, "r");
            String line = raf.readLine();
            lineNumber++;
            while ( line != null ) {
                if ( line.indexOf("=") > 0 ) {
                    String[] tokens = line.split("=");
                    setValue(tokens[0].trim(), tokens[1].trim());
                }
                line = raf.readLine();
                lineNumber++;
            }
            result = true;
        } catch (Exception ex) {
            Logger.getLogger(SimpleConfig.class.getName()).log(Level.SEVERE, "Error loading config from file (" + fileName + ":" + lineNumber + "): " + ex.getMessage(), ex);
        }
        
        if ( raf != null ) {
            try {
                raf.close();
            } catch (IOException ex) {}
        }
        
        return result;
    }
    
    public static SimpleConfig createFromFile(String fileName) {
        SimpleConfig config = new SimpleConfig();
        config.loadFromFile(fileName);
        return config;
    }
    
    public void copy(HashMap<String, String> targetMap) {
        for (String key : this.lineMap.keySet()) {
            targetMap.put(key, this.lineMap.get(key));
        }
    }
    
    public void putAll(HashMap<String, String> sourceMap) {
        for (String key : sourceMap.keySet()) {
            this.lineMap.put(key, sourceMap.get(key));
        }
    }
    
}
