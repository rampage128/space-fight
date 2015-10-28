/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.network;

import com.jme3.math.Quaternion;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author rampage
 */
public class QuaternionSerializer extends Serializer {

    @Override
    public Quaternion readObject(ByteBuffer data, Class c) throws IOException {
        Quaternion q = new Quaternion();
        q.set(data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat());
        return q;
    }

    @Override
    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        Quaternion q = (Quaternion)object;
        buffer.putFloat(q.getX());
        buffer.putFloat(q.getY());
        buffer.putFloat(q.getZ());
        buffer.putFloat(q.getW());
    }
    
    
    
}
