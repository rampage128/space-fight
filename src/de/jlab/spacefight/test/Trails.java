package de.jlab.spacefight.test;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import de.jlab.spacefight.effect.trail.LineControl;
import de.jlab.spacefight.effect.trail.TrailControl;

public class Trails extends SimpleApplication {

    Node geom;

    public static void main(String[] args) {
        Trails app = new Trails();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        geom = new Node(); //new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        //mat.setTexture("ColorMap", assetManager.loadTexture("effects/trail.png"));

        Geometry trailGeometry = new Geometry();
        LineControl line = new LineControl(new LineControl.Algo1CamPosBB(), true);
        trailGeometry.addControl(line);
        TrailControl trailControl = new TrailControl(line);
        geom.addControl(trailControl);

//rootNode.attachChild(trail);  // either attach the trail geometry node to the root…

        trailGeometry.setIgnoreTransform(true); // or set ignore transform to true. this should be most useful when attaching nodes in the editor
        Node test = new Node();
        test.attachChild(trailGeometry);
        test.setLocalTranslation(new Vector3f(0, 2, 0));  // without ignore transform this would offset the trail
        rootNode.attachChild(test);

        mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        trailGeometry.setMaterial(mat);

        rootNode.attachChild(geom);
        initKeys();
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    private void initKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Rotate", new KeyTrigger(KeyInput.KEY_SPACE),
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(analogListener, new String[]{"Left", "Right", "Rotate"});

    }
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("Rotate")) {
                geom.rotate(0, value * speed, 0);
            }
            if (name.equals("Right")) {
                Vector3f v = geom.getLocalTranslation();
                geom.setLocalTranslation(v.x + value * speed * 5, v.y, v.z);
            }
            if (name.equals("Left")) {
                Vector3f v = geom.getLocalTranslation();
                geom.setLocalTranslation(v.x - value * speed * 5, v.y, v.z);
            }
        }
    };
}