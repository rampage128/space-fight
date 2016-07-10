/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui;

import com.jme3.effect.ParticleEmitter;
import com.jme3.input.ChaseCamera;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.debug.Grid;
import de.jlab.spacefight.Game;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEvent;
import de.lessvoid.nifty.NiftyEventAnnotationProcessor;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.TreeBox;
import de.lessvoid.nifty.controls.TreeItem;
import de.lessvoid.nifty.controls.TreeItemSelectionChangedEvent;

import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Deprecated! Will use external application for editing gamedata!
 * 
 * @author rampage
 */
@Deprecated
public class EditorController implements ScreenController {

    private static final Logger LOGGER = Logger.getLogger(EditorController.class.getSimpleName());
    
    //private Node editorNode = null;
    private Game _game;
    private Nifty _nifty;
    
    private Node _editornode;
    private Node _ship;
    
    private Screen _screen;
    
    private NiftyImage _nodeImage;
    private NiftyImage _controlImage;
    private NiftyImage _geometryImage;
    private NiftyImage _meshImage;
    private NiftyImage _particleImage;
    
    private TreeBox _tree;
    
    private ChaseCamera _chaseCam;
        
    public EditorController(Game game) {
        _game = game;
        _editornode = new Node("editor");
    }
    
    public void bind(Nifty nifty, Screen screen) {
        _nifty = nifty;
        _screen = screen;

        NiftyEventAnnotationProcessor.process(this);
        
        _nodeImage = nifty.createImage("ui/editor/tree_node.png", true);
        _controlImage = nifty.createImage("ui/editor/tree_control.png", true);
        _geometryImage = nifty.createImage("ui/editor/tree_geometry.png", true);
        _meshImage = nifty.createImage("ui/editor/tree_mesh.png", true);
        _particleImage = nifty.createImage("ui/editor/tree_particle.png", true);

        _tree = _screen.findNiftyControl("shiptree", TreeBox.class);
    }

    public void onStartScreen() {
        //editorNode = new Node("editor");
        createGrid();
        newShip("fighter");
        setUpCamera();
    }

    public void onEndScreen() {
        //_app.getApp().getRenderManager().removeMainView("PiP");
        
        _game.getInputManager().removeListener(_chaseCam);
        //_game.getInputManager().clearMappings();
        _game.getRootNode().removeControl(_chaseCam);
        
        _game.getRootNode().detachChild(_editornode);
        /* IF CLEARMAPPINGS MAKES PROBLEMS WE NEED TO REMOVE FOLLOWING MAPPINGS
        "ChaseCamDown";
        "ChaseCamUp";
        "ChaseCamZoomIn";
        "ChaseCamZoomOut";
        "ChaseCamMoveLeft";
        "ChaseCamMoveRight";
        "ChaseCamToggleRotate";
         */
    }

    /* PRIVATE STUFF */
    
    private void setUpCamera() {
        // Setup second, smaller PiP view
        /*
        Camera cam2 = _app.getApp().getCamera().clone();
        cam2.setViewPort(.2f, .8f, 0.2f, 0.8f);
        ViewPort viewPort2 = _app.getApp().getRenderManager().createMainView("PiP", cam2);
        viewPort2.setClearFlags(true, true, true);

        viewPort2.attachScene(_app.getApp().getRootNode());
        viewPort2.setBackgroundColor(ColorRGBA.DarkGray);
        */
        //_flyCam = new FlyByCamera(cam2);
        //_flyCam.registerWithInput(_app.getApp().getInputManager());        
        _chaseCam = new ChaseCamera(_game.getCamera(), _game.getRootNode(), _game.getInputManager());
        _chaseCam.setSmoothMotion(true);
        _chaseCam.setDefaultDistance(20f);
        _chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        _chaseCam.setEnabled(true);
        
        _game.getCamera().lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
        //_app.getApp().getCamera().setRotation(Quaternion.ZERO);
    }
    
    private void createGrid() {
          Geometry g = new Geometry("wireframe grid", new Grid(21, 21, 1f) );
          Material mat = new Material(_game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
          mat.getAdditionalRenderState().setWireframe(true);
          mat.setColor("Color", new ColorRGBA(0.5f, 0.0f, 0.0f, 0.25f));
          mat.setTransparent(true);
          g.setMaterial(mat);
          g.center().move(Vector3f.ZERO);
          g.setQueueBucket(Bucket.Transparent);
          _editornode.attachChild(g);
          
          g = new Geometry("wireframe grid", new Grid(21, 21, 1f) );
          mat = new Material(_game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
          mat.getAdditionalRenderState().setWireframe(true);
          mat.setColor("Color", new ColorRGBA(0.0f, 0.5f, 0.0f, 0.25f));
          g.setMaterial(mat);
          g.center().move(new Vector3f(0, 10f, 10f));
          _editornode.attachChild(g);
          g.setLocalRotation(new Quaternion().fromAngleAxis(0.5f * (float)Math.PI, Vector3f.UNIT_X));
          
          g = new Geometry("wireframe grid", new Grid(21, 21, 1f) );
          mat = new Material(_game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
          mat.getAdditionalRenderState().setWireframe(true);
          mat.setColor("Color", new ColorRGBA(0.0f, 0.0f, 0.5f, 0.25f));
          g.setMaterial(mat);
          g.center().move(new Vector3f(10f, -10f, 0));
          _editornode.attachChild(g);
          g.setLocalRotation(new Quaternion().fromAngleAxis(0.5f * (float)Math.PI, Vector3f.UNIT_Z));
    }
    
    private TreeItem<Object> refreshTree(Spatial userObject, TreeItem parent) {
        
        NiftyImage itemImage = _nodeImage;
        
        if ( userObject instanceof Geometry )
            itemImage = _geometryImage;
        if ( userObject instanceof ParticleEmitter )
            itemImage = _particleImage;
        if ( userObject instanceof Control )
            itemImage = _controlImage;
        
        TreeItem<Object> item = new TreeItem<Object>(userObject); // parent, userObject, userObject.getName(), itemImage, itemImage
        item.setParentItem(parent);
        //node.setValue(userObject);
        //node.setDisplayCaption(userObject.toString());
        
        if ( userObject instanceof Node ) {
            addItemsFromNode((Node)userObject, item);
        }
        
        return item;
    }
    
    private void addItemsFromNode(Node node, TreeItem<Object> parent) {
        List<Spatial> childList = node.getChildren();
        for ( int i = 0; i < childList.size(); i++ ) {
            Spatial s = childList.get(i);
            parent.setExpanded(true);
            parent.addTreeItem(refreshTree(s, parent));
        }
    }
    
    /* CONTROLLING */
    
    /**
     * When the selection of the ListBox changes this method is called.
     */
    @NiftyEventSubscriber(id="shiptree")
    public void onShiptreeTreeItemSelectedEvent(final String id, final TreeItemSelectionChangedEvent event) {
        List selection = event.getSelection();
        if (!selection.isEmpty()) {
            TreeItem item = (TreeItem)selection.get(0);
            LOGGER.warning(((Spatial)item.getValue()).getName());
        }
    }
        
    public void quitEditor() {
        _game.getRootNode().detachAllChildren();
        _nifty.gotoScreen("start");
    }
    
    public void newShip(String shipType) {
        if ( _ship != null )
            _editornode.detachChild(_ship);
        _ship = new Node(shipType);
        _ship.setShadowMode(ShadowMode.CastAndReceive);
        
        _ship.attachChild(new Node("hull"));
        _ship.attachChild(new Node("engine"));
        _ship.attachChild(new Node("sensors"));
        _ship.attachChild(new Node("weapons"));
        _ship.attachChild(new Node("collision"));
        
        _editornode.attachChild(_ship);
        
        //_app.getApp().getAssetManager().registerLocator("gamedata/base/ships/" + name + "/", FileLocator.class);
        
        //Spatial hull = (Spatial)assetManager.loadModel("ships/" + name + "/" + name + ".obj");
        //ship.attachChild(hull);
        
        //FlightControl fc = new FlightControl(this);
        //ship.addControl(fc);
        //fc.setCollisionShape(new SphereCollisionShape(3));
        //BulletAppState physics = _app.getStateManager().getState(BulletAppState.class);
        //physics.getPhysicsSpace().add(fc);
                        
        //ship.setUserData("hull_hp", 500f);
        
        //ship.addControl(new WeaponSystemControl(this));
        //ship.addControl(new DamageControl(this));
                
        //assetManager.unregisterLocator("gamedata/base/ships/" + name + "/", FileLocator.class);
        TreeItem rootItem = new TreeItem();
        TreeItem shipItem = refreshTree(_ship, rootItem);
        rootItem.addTreeItem(shipItem);
        _tree.setTree(rootItem);
    }
    
}
