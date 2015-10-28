/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.gamedata;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 *
 * @author rampage
 */
public class EntityCache<T extends CacheableEntity> {
    
    private HashMap<String, WeakReference<T>> entityMap = new HashMap<String, WeakReference<T>>();
    
    public T getEntity(String key) {
        WeakReference<T> ref = this.entityMap.get(key);
        if (ref == null) {
            return null;
        }
        return ref.get();
    }
    
    public T getEntityInstance(String key) {
        T entity = getEntity(key);
        if (entity == null) {
            return null;
        }
        return (T)entity.getInstance(key);
    }
    
    public void putEntity(String key, T entity) {
        this.entityMap.put(key, new WeakReference<T>(entity));
    }
    
}
