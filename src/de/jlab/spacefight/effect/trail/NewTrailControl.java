/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.effect.trail;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author rampage
 */
public class NewTrailControl extends AbstractControl {

    private float lifetime = 1f;
    private float startwidth = 1f;
    private float endwidth = startwidth;
    
    private float minDistance = 0.1f;
    
    private LinkedList<TrailPoint> trailPoints = new LinkedList<TrailPoint>();
    private Geometry trailGeometry;
    private Mesh trailMesh;
    
    private Vector3f mcPos = new Vector3f();
    private float mcMov = 0f;
    
    public NewTrailControl() {
        this.trailMesh = new Mesh();
        this.trailMesh.setDynamic();
        this.trailMesh.setMode(Mesh.Mode.TriangleStrip);
        
        this.trailGeometry = new Geometry("trail", trailMesh);
        this.trailGeometry.setIgnoreTransform(true);
    }
    
    public Material getTrailMaterial() {
        return this.trailGeometry.getMaterial();
    }
    
    public void setTrailMaterial(Material material) {
        this.trailGeometry.setMaterial(material);
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        for (Iterator<TrailPoint> pointIt = trailPoints.iterator(); pointIt.hasNext(); ) {
            TrailPoint point = pointIt.next();
            point.update(tpf);
            if (point.getAge() >= this.lifetime) {
                pointIt.remove();
                //trailPoints.remove(point);
            }
        }
        
        mcMov = mcPos.subtractLocal(getSpatial().getWorldTranslation()).length();
        mcPos.set(getSpatial().getWorldTranslation());
        
        if (mcMov >= minDistance) {
            trailPoints.add(0, new TrailPoint(mcPos));
        }
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            if (!(spatial instanceof Node)) {
                throw new IllegalArgumentException("TrailControl must be added to a Spatial of type Node! Found: " + spatial.getClass().getSimpleName());
            }
            ((Node)spatial).attachChild(trailGeometry);
        } else {
            trailGeometry.removeFromParent();
        }
    }
    
    public Control cloneForSpatial(Spatial spatial) {
        NewTrailControl newTrail = new NewTrailControl();
        return newTrail;
    }
    
    private class TrailPoint {
        
        private float age = 0;
        private Vector3f pos = new Vector3f();

        public TrailPoint(Vector3f pos) {
            pos.set(pos);
        }
        
        public void update(float tpf) {
            this.age += tpf;
        }
        
        public float getAge() {
            return this.age;
        }
        
        public Vector3f getPos() {
            return this.pos;
        }
        
        public void addToMesh(int index, Mesh mesh) {
            VertexBuffer pvb = mesh.getBuffer(VertexBuffer.Type.Position);
            FloatBuffer buf = (FloatBuffer)pvb.getData();
            
            buf.put(this.pos.x);
            buf.put(this.pos.y);
            buf.put(this.pos.z);
        }
        
        /*
        public void removeFromMesh(int index, Mesh mesh) {
            VertexBuffer pvb = mesh.getBuffer(VertexBuffer.Type.Position);
            FloatBuffer positions = (FloatBuffer) pvb.getData();
            positions.limit(positions.limit() - 6);
            if (positions.capacity() / 4 >= MINIMUM_POS_BUFFER_SIZE && positions.limit() <= positions.capacity() / 4) {
                FloatBuffer newBuffer = BufferUtils.createFloatBuffer(positions.capacity() / 2);
                newBuffer.limit(positions.limit());
                mesh.setBuffer(VertexBuffer.Type.Position, 3, newBuffer);    // TODO: is this necessary???
            } else {
                pvb.updateData(positions);    // TODO: is this necessary???
                //mesh.setBuffer(Type.Position, 3, positions);  // TODO: shouldn’t it work without this???
                //mesh.updateCounts();
            }

            // indexes
            pvb = mesh.getBuffer(VertexBuffer.Type.Index);
            IntBuffer indexes = (IntBuffer) pvb.getData();
            indexes.limit(indexes.limit() - 2);
            if (indexes.capacity() / 4 >= MINIMUM_INDEX_BUFFER_SIZE && indexes.limit() <= indexes.capacity() / 4) {
                IntBuffer newBuffer = BufferUtils.createIntBuffer(indexes.capacity() / 2);
                indexes.rewind();
                newBuffer.put(indexes);
                newBuffer.flip();
                mesh.setBuffer(VertexBuffer.Type.Index, 3, newBuffer);    // TODO: is this necessary???
            } else {
                pvb.updateData(indexes);    // TODO: is this necessary???
                //mesh.setBuffer(Type.Index, 3, indexes);  // TODO: shouldn’t it work without this???
                //mesh.updateCounts();
            }
            
            pvb = mesh.getBuffer(VertexBuffer.Type.TexCoord);
            FloatBuffer texCoord = (FloatBuffer) pvb.getData();
            texCoord.limit(texCoord.limit() - 4);
            if (texCoord.capacity() / 4 >= MINIMUM_TEXCOORD_BUFFER_SIZE && texCoord.limit() <= texCoord.capacity() / 4) {   // need to create new buffer
                FloatBuffer newBuffer = BufferUtils.createFloatBuffer(texCoord.capacity() / 2);  // reasonable assumption: 2 times the previous size
                newBuffer.limit(texCoord.limit());     // two new vertices, hence 4 new texture coordinates
                newBuffer.put(0.0f);
                newBuffer.put(0.0f);
                newBuffer.put(0.0f);
                newBuffer.put(1.0f);

                if (totalLength == 0) {
                    for (LineControl.FloatWrapper l : lengths) {
                        newBuffer.put(0.0f);
                        newBuffer.put(0.0f);
                        newBuffer.put(0.0f);
                        newBuffer.put(1.0f);
                    }
                } else {
                    for (LineControl.FloatWrapper l : lengths) {
                        float length = l.getValue() / totalLength;
                        newBuffer.put(length);
                        newBuffer.put(0.0f);
                        newBuffer.put(length);
                        newBuffer.put(1.0f);
                    }
                }

                newBuffer.flip();
                mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, newBuffer);   // TODO: is this necessary???
            } else {
                if (points.size() != 0) {
                    texCoord.rewind();
                    texCoord.put(0.0f);
                    texCoord.put(0.0f);
                    texCoord.put(0.0f);
                    texCoord.put(1.0f);

                    if (totalLength == 0) {
                        for (LineControl.FloatWrapper l : lengths) {
                            texCoord.put(0.0f);
                            texCoord.put(0.0f);
                            texCoord.put(0.0f);
                            texCoord.put(1.0f);
                        }
                    } else {
                        for (LineControl.FloatWrapper l : lengths) {
                            float length = l.getValue() / totalLength;
                            texCoord.put(length);
                            texCoord.put(0.0f);
                            texCoord.put(length);
                            texCoord.put(1.0f);
                        }
                    }
                    texCoord.flip();
                }
                pvb.updateData(texCoord);   // TODO: is this necessary???
                //mesh.setBuffer(Type.TexCoord, 2, texCoord);  // TODO: shouldn’t it work without this???
                //mesh.updateCounts();   // TODO: is this necessary???
            }
            mesh.updateCounts();
            
        }
        */
        
    }
    
}
