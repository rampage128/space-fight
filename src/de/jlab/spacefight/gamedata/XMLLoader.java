/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.gamedata;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author rampage
 */
public final class XMLLoader {
    
    /* XML RESOURCES */    
    public static Element loadXML(String path, String name) {
        Document doc = null;

        SAXBuilder sb = new SAXBuilder();
        
        File file = null;
        if (path != null) {
            file = new File(path, name + ".xml");
        } else {
            file = new File(name + ".xml");
        }
        
        try {
            doc = sb.build(file);
            return doc.getRootElement();
        } catch (JDOMException e) {
            throw new RuntimeException("cannot load XML " + name, e);
        } catch (IOException e) {
            throw new RuntimeException("cannot load XML " + name, e);
        }
    }
    
    public static List<Element> readElementList(String name, Element parentElement) {
        Element root = parentElement.getChild(name + "s");
        if (root != null) {
            return root.getChildren(name);
        } else {
            return new ArrayList<Element>();
        }
    }
    
    public static int getIntConstValue(Element element, String name, int[] intValues, String[] stringValues , int defaultValue) {
        // CREATE STRING VALUE STORAGE AND INT VALUE STORAGE
        String valueString = element.getChildTextTrim(name);
        int valueInt = defaultValue;
        
        // RETURN WITH DEFAULT VALUE IF STRING VALUE IS NOT SET AT ALL
        if (valueString == null) {
            return defaultValue;
        }
               
        // CHECK THE POSSIBLE VALUE LIST
        for (int i = 0; i < stringValues.length; i++) {
            String possibleValue = stringValues[i];
            if (possibleValue.equals(valueString)) {
                // IF POSSIBLE STRING EQUALS STRING VALUE RETURN THE KEY (INT)
                return intValues[i];
            } //else if (valueInt == intValues[i]) {
                // IF PREVIOUSLY PARSED DIRECT RESULT EQUALS KEY (INT) RETURN THE KEY
                //return valueInt;
            //}
        }
        
        // TRY TO DIRECTLY PARSE AN INT RESULT FROM STRING VALUE
        try {
            valueInt = Integer.valueOf(valueString);
            for (int i = 0; i < intValues.length; i++) {
                if (valueInt == intValues[i]) {
                    return valueInt;
                }
            }
        } catch (NumberFormatException e) {}
        
        // WE HAD NO SUCCESS IN PARSING ANYTHING ALLOWED AT ALL AND RETURN DEFAULT VALUE :-(
        return defaultValue;
    }
    
    public static String getStringValue(Element element, String name, String defaultValue) {
        String value = element.getChildTextTrim(name);
        if ( value == null ) {
            return defaultValue;
        }
        return value;
    }
    
    public static boolean getBooleanValue(Element element, String name, boolean defaultValue) {
        String valueString = element.getChildTextTrim(name);
        if (valueString != null) {
            try {
                return Boolean.parseBoolean(valueString);
            } catch (Exception e) {
                Logger.getLogger(XMLLoader.class.getSimpleName()).log(Level.WARNING, "Cannot get boolean value from " + name + "=" + valueString, e);
            }
        }
        return defaultValue;
    }
    
    public static float getFloatValue(Element element, String name, float defaultValue) {
        String valueString = element.getChildTextTrim(name);
        if (valueString != null) {
            try {
                return Float.parseFloat(valueString);
            } catch (Exception e) {
                Logger.getLogger(XMLLoader.class.getSimpleName()).log(Level.WARNING, "Cannot get float value from " + name + "=" + valueString, e);
            }
        }
        return defaultValue;
    }
    
    public static long getLongValue(Element element, String name, long defaultValue) {
        String valueString = element.getChildTextTrim(name);
        if (valueString != null) {
            try {
                return Long.parseLong(valueString);
            } catch (Exception e) {
                Logger.getLogger(XMLLoader.class.getSimpleName()).log(Level.WARNING, "Cannot get long value from " + name + "=" + valueString, e);
            }
        }
        return defaultValue;
    }
    
    public static int getIntValue(Element element, String name, int defaultValue) {
        String valueString = element.getChildTextTrim(name);
        if (valueString != null) {
            try {
                return Integer.parseInt(valueString);
            } catch (Exception e) {
                Logger.getLogger(XMLLoader.class.getSimpleName()).log(Level.WARNING, "Cannot get int value from " + name + "=" + valueString, e);
            }
        }
        return defaultValue;
    }
    
    public static Vector3f getVectorValue(Element element, String name, Vector3f defaultValue) {
        String valueString = element.getChildTextTrim(name);
        if (valueString != null) {
            try {
                String[] positions = valueString.replaceAll("[\\[\\]]", "").split(",");
                return new Vector3f(Float.parseFloat(positions[0].trim()), Float.parseFloat(positions[1].trim()), Float.parseFloat(positions[2].trim()));
            } catch (Exception e) {
                Logger.getLogger(XMLLoader.class.getSimpleName()).log(Level.WARNING, "Cannot get Vector value from " + name + "=" + valueString, e);
            }
        }
        return defaultValue;
    }
    
    public static Quaternion getAngleValue(Element element, String name, Quaternion defaultValue) {
        String valueString = element.getChildTextTrim(name);
        if (valueString != null) {
            try {
                String[] positions = valueString.replaceAll("[\\[\\]]", "").split(",");
                return new Quaternion().fromAngles(Float.parseFloat(positions[0].trim()) * FastMath.DEG_TO_RAD, Float.parseFloat(positions[1].trim()) * FastMath.DEG_TO_RAD, Float.parseFloat(positions[2].trim()) * FastMath.DEG_TO_RAD);
            } catch (Exception e) {
                Logger.getLogger(XMLLoader.class.getSimpleName()).log(Level.WARNING, "Cannot get Angle value from " + name + "=" + valueString, e);
            }
        }
        return defaultValue;
    }
    
    public static ColorRGBA getColorValue(Element element, String name, ColorRGBA defaultValue) {
        String valueString = element.getChildTextTrim(name);
        if (valueString != null) {
            try {
                String[] positions = valueString.replaceAll("[\\[\\]]", "").split(",");
                return new ColorRGBA(Float.parseFloat(positions[0].trim()), Float.parseFloat(positions[1].trim()), Float.parseFloat(positions[2].trim()), Float.parseFloat(positions[3].trim()));
            } catch (Exception e) {
                Logger.getLogger(XMLLoader.class.getSimpleName()).log(Level.WARNING, "Cannot get ColorRGBA value from " + name + "=" + valueString, e);
            }
        }
        return defaultValue;
    }
    
}
