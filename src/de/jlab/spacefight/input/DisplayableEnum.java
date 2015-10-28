/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.input;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rampage
 */
public class DisplayableEnum {
    public interface Display {
        public String getDisplayValue();
    }
        
    public static <T extends Enum<T> & Display> String[] getDisplayValues(Class<T> pClass) {
      List<String> retval = new ArrayList<String>();
      for (Display thing : pClass.getEnumConstants()) {
        retval.add(thing.getDisplayValue());
      }
      return retval.toArray(new String[0]);
    }
        
}
