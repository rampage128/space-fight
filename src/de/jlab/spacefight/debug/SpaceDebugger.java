/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.debug;

import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.jlab.spacefight.SpaceAppState;
import java.util.HashMap;
import java.util.List;

/**
 * Debuggin class for Space.
 * It is designed as SINGLETON (Against all authority of subjects like Marco Hampe).
 * If you need to complain about the SINGLETON IDIOM here is a brief explenation of the reasons:
 * 
 * 1.: IT IS GLOBALLY ACCESSIBLE
 * 2.: IT REMAINS AN INSTANCE OF THE CURRENT SPACE TO ATTACH OBJECTS
 * 3.: NO NEED TO DRAG INSTANCES OF SPACE, GAME OR ANY SHIT THROUGH THE WHOLE METHODS 
 *     TO CALL A DEBUG FEATURE FROM SOME DEEP NESTED CODE
 * 
 * Now to the features:
 * 
 * SpaceDebugger can be used to draw visible vectors from computations, display "blips"
 * in 3D-space or just output some debug text. This is a really great way to debug 
 * "realtime"-Applications like games. If you don't like it you may try to debug
 * 100+ Frames per second with the java-debugger step-by-step (GL&HF).
 * 
 * Debuggin methods may be called anywhere in the game code with little to no cost.
 * They may also be kept in the code. Debugging is only enabled if the game is started
 * with the option "-debug" (see Menu "Run" -> "Set Project Configuration" -> "debug").
 * 
 * 
 * @author rampage
 */
public class SpaceDebugger {
    
    private static SpaceDebugger INSTANCE = new SpaceDebugger();
    
    private SpaceAppState space = null;
    
    private SpaceDebugger() {}
    public static SpaceDebugger getInstance() {
        return INSTANCE;
    }
    
    public void setSpace(SpaceAppState space) {
        if ( this.space != null ) {
            cleanup();
        }
        this.space = space;
        if ( this.space != null ) {
            init();
        }
    }
    
    public void cleanup() {
        this.debugnode.detachAllChildren();
        this.space.destroyObject(this.debugnode);
        //this.debugObjectMap.clear();
        //this.debugMap.clear();
        for (DebugContext context : this.itemMap.keySet()) {
            this.enableContext(context, false);
        }
        stopDebugText();
    }
    
    public void init() {
        this.debugnode = new Node("debug");
        this.space.getSpace().attachChild(this.debugnode);
        initDebugText();
    }
    
    public boolean hasSpace() {
        return this.space != null && this.space.isInitialized();
    }
    

    
    /* DEBUG TEXT */
    /*
    private LinkedHashMap<String, String> debugMap = new LinkedHashMap<String, String>();
    private BitmapText debugText;

    public void addText(String key, String value) {
        if (!this.space.getGame().isDebug()) {
            return;
        }

        debugMap.put(key, value);
        updateDebugText();
    }

    public void addTextFloat(String key, float value) {
        addText(key, Float.toString(value));
    }
    */

    private BitmapText debugText;
    
    public void debugPhysics(boolean value) {
        if (this.space.getGame().isDebug() && value) {
            this.space.getPhysics().setDebugEnabled(true);
            //this.space.getPhysics().getPhysicsSpace().enableDebug(this.space.getGame().getAssetManager());
        } else {
            this.space.getPhysics().setDebugEnabled(false);
            //this.space.getPhysics().getPhysicsSpace().disableDebug();
        }
    }
    
    private void updateDebugText() {
        if (!this.space.getGame().isDebug()) {
            return;
        }

        /*
        StringBuilder debugBuilder = new StringBuilder();
        for (Iterator<String> kit = debugMap.keySet().iterator(); kit.hasNext();) {
            String key = kit.next();
            String value = debugMap.get(key);
            debugBuilder.append(key).append(": ").append(value).append("\n");
        }
        debugText.setText(debugBuilder);
        */
        debugText.setLocalTranslation(0, this.space.getGame().getContext().getSettings().getHeight() - debugText.getHeight() / 2, 0); // position
    }

    private void initDebugText() {
        if (!this.space.getGame().isDebug()) {
            return;
        }

        BitmapFont guiFont = this.space.getGame().getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        debugText = new BitmapText(guiFont, false);

        debugText.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        debugText.setColor(ColorRGBA.White);                             // font color
        debugText.setText("DEBUG");                                     // the text
        this.space.getGame().getGuiNode().attachChild(debugText);
    }

    private void stopDebugText() {
        if (debugText != null) {
            this.space.getGame().getGuiNode().detachChild(debugText);
        }
    }
    
    /* DEBUG GEOMETRY */
    
    private Node debugnode;
    //private LinkedHashMap<String, Geometry> debugObjectMap = new LinkedHashMap<String, Geometry>();    
    
    public Geometry createDebugShape(Mesh shape, ColorRGBA color, boolean solid) {
        Geometry g = new Geometry("debug geometry", shape);
        Material mat = new Material(this.space.getGame().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(!solid);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        this.debugnode.attachChild(g);
        return g;
    }
    
    /*
    public void drawVector(String name, Vector3f pos, Vector3f vec, ColorRGBA color) {
        if (!this.space.getGame().isDebug()) {
            return;
        }

        Geometry arrowNode;

        if (debugObjectMap.containsKey(name)) {
            arrowNode = debugObjectMap.get(name);
        } else {
            Arrow arrow = new Arrow(Vector3f.UNIT_Z);
            arrow.setLineWidth(4);
            arrowNode = createDebugShape(arrow, color, false);
            arrowNode.setQueueBucket(Bucket.Translucent);
            debugObjectMap.put(name, arrowNode);
        }

        arrowNode.setLocalTranslation(pos);
        //arrowNode.setLocalScale(new Vector3f(1, 1, 1).addLocal(vec));
        arrowNode.setLocalScale(vec.length());
        Quaternion rotation = new Quaternion().fromAxes(Vector3f.ZERO, Vector3f.ZERO, vec);
        rotation.lookAt(vec, new Vector3f(0, 1, 0));
        arrowNode.setLocalRotation(rotation);
        //setDebug(name, pos + " - " + vec);           
    }
    
    public void drawBlip(String name, Vector3f pos, float radius, ColorRGBA color) {
        if (!this.space.getGame().isDebug()) {
            return;
        }

        Geometry arrowNode;

        if (debugObjectMap.containsKey(name)) {
            arrowNode = debugObjectMap.get(name);
        } else {
            Box box = new Box(0.5f, 0.5f, 0.5f);
            box.setLineWidth(4);
            arrowNode = createDebugShape(box, color, true);
            arrowNode.setQueueBucket(Bucket.Translucent);
            debugObjectMap.put(name, arrowNode);
        }

        arrowNode.setLocalTranslation(pos);
        arrowNode.setLocalScale(radius);
        //setDebug(name, pos + " - " + vec);
    }
    
    public void removeShape(String name) {
        Geometry arrowNode = debugObjectMap.remove(name);
        this.space.destroyObject(arrowNode);
    }    
    */
    
    /* OTHER DEBUG FUNCTIONS */
    public void dumpGraph(Spatial spatial) {
        
        StringBuilder builder = new StringBuilder();
        
        Node parent = spatial.getParent();
        while ( parent != null ) {
            builder.append("-");
            parent = parent.getParent();
        }
        
        builder.append(spatial.toString()).append(" (S:").append(spatial.getShadowMode()).append("/C:").append(spatial.getCullHint()).append("/B:").append(spatial.getQueueBucket()).append("/L:").append(spatial.getLocalLightList().size()).append(")");
        System.out.println(builder.toString());
                
        if ( spatial instanceof Node ) {
            List<Spatial> childList = ((Node)spatial).getChildren();
            for ( Spatial child : childList ) {
                dumpGraph(child);
            }
        }
    }
 
    /* NEW DEBUG CODE */
    
    private HashMap<DebugContext, Boolean> contextMap = new HashMap<DebugContext, Boolean>();
    
    public void enableContext(DebugContext context, boolean state) {
        this.contextMap.put(context, state);
        if (state && itemMap.get(context) == null) {
            itemMap.put(context, new HashMap<String, AbstractDebugItem>());
        } else if (!state && itemMap.get(context) != null) {
            HashMap<String, AbstractDebugItem> map = itemMap.get(context);
            String[] keys = map.keySet().toArray(new String[0]);
            for (String key : keys) {
                removeItem(context, key);
            }
        }
        if (DebugContext.PHYSICS == context) {
            debugPhysics(state);
        }
    }
    
    public boolean isContextEnabled(DebugContext context) {
        return this.space != null && this.space.getGame().isDebug() && this.contextMap.containsKey(context) && this.contextMap.get(context);
    }
    
    private HashMap<DebugContext, HashMap<String, AbstractDebugItem>> itemMap = new HashMap<DebugContext, HashMap<String, AbstractDebugItem>>();
    
    public void setVector(DebugContext context, String name, Vector3f origin, Vector3f vector, ColorRGBA color) {
        if (!isContextEnabled(context)) {
            return;
        }
        AbstractDebugItem item = getDebugItem(context, name);
        if ( item != null && item instanceof VectorDebugItem) {
            ((VectorDebugItem)item).setValue(origin, vector, color);
        } else {
            item = new VectorDebugItem(origin, vector, color);
            setDebugItem(context, name, item);
        }
    }
    
    public void setBlip(DebugContext context, String name, Vector3f position, float radius, ColorRGBA color) {
        if (!isContextEnabled(context)) {
            return;
        }
        AbstractDebugItem item = getDebugItem(context, name);
        if ( item != null && item instanceof BlipDebugItem) {
            ((BlipDebugItem)item).setValue(position, radius, color);
        } else {
            item = new BlipDebugItem(position, radius, color);
            setDebugItem(context, name, item);
        }
    }
    
    public void setText(DebugContext context, String name, String text) {
        if (!isContextEnabled(context)) {
            return;
        }
        AbstractDebugItem item = getDebugItem(context, name);
        if ( item != null && item instanceof TextDebugItem) {
            ((TextDebugItem)item).setValue(text);
        } else {
            item = new TextDebugItem(name, text);
            setDebugItem(context, name, item);
        }
        updateDebugText();
    }
    
    public void setTextFloat(DebugContext context, String name, float value) {
        setText(context, name, Float.toString(value));
    }
    
    private void setDebugItem(DebugContext context, String name, AbstractDebugItem item) {
        this.itemMap.get(context).put(name, item);
    }
    
    private AbstractDebugItem getDebugItem(DebugContext context, String name) {
        if (!isContextEnabled(context)) {
            return null;
        }
        return this.itemMap.get(context).get(name);
    }
 
    public void removeItem(DebugContext context, String name) {
        HashMap<String, AbstractDebugItem> map = this.itemMap.get(context);
        if (map != null) {
            AbstractDebugItem item = map.remove(name);
            if ( item != null ) {
                item.cleanup();
            }
        }
    }
    
}
