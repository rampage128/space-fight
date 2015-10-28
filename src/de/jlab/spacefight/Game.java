package de.jlab.spacefight;

import de.jlab.spacefight.debug.SpaceDebugger;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.renderer.RenderManager;
import de.jlab.spacefight.audio.AudioManager;
import de.jlab.spacefight.basic.VersionInfo;
import de.jlab.spacefight.basic.camera.CameraManager;
import de.jlab.spacefight.gamedata.GamedataManager;
import de.jlab.spacefight.gamedata.SimpleConfig;
import de.jlab.spacefight.network.NetworkAppState;
import de.jlab.spacefight.player.Player;
import de.jlab.spacefight.ui.UIAppState;
import de.jlab.spacefight.video.VideoManager;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic game class. It just initializes the important stuff. 
 * Game-Logic is mainly maintained by SpaceAppState, UIAppState and some other classes.
 * 
 * @author rampage
 */
public class Game extends AdvancedSimpleApplication {
    
    /* VOID MAIN */
    public static void main(String[] args) {      
        Game app = new Game(args);
        // TODO IMPLEMENT DEDICATED SERVER MODE!
        app.start();
    }

    public static final VersionInfo VERSION = new VersionInfo(0, 4, 1, VersionInfo.STATE_ALPHA);
    private static Game INSTANCE;
    
    public static Game get() {
        if (INSTANCE == null) {
            throw new RuntimeException("Game was not started yet!");
        }
        return INSTANCE;
    }
    
    /* GAME */
    //private AbstractNetworkInstance network;
    private GamedataManager _gamedataManager;
    private AudioManager _audioManager;
    private VideoManager videoManager;
    private CameraManager cameraManager;
    private UIAppState _ui;
    private boolean _debug = false;
    
    private Player player = null;
    
    private Game(String[] args) {
        super();

        for ( int i = 0; i < args.length; i++ ) {
            if ( args[i].equalsIgnoreCase("-debug") )
                _debug = true;
        }
    }
    
    public boolean isDebug() {
        return _debug;
    }
    
    public void setDebug(boolean debug) {
        this._debug = debug;
        if ( SpaceDebugger.getInstance().hasSpace() ) {
            SpaceDebugger.getInstance().init();
        }
    }
        
    @Override
    public void simpleInitApp() {
        // FILL SINGLETON INSTANCE
        INSTANCE = this;
        // LOCATOR FOR CURRENT MOD FILES
        //assetManager.registerLocator("gamedata/" + currentMod + "/", FileLocator.class);
        // FALLBACK BASEMOD LOCATOR
        
        this.player = Player.loadPlayer();
        
        this.setPauseOnLostFocus(false);
        //this.getFlyByCamera().setEnabled(false);
        
        // REMOVE DEFAULT INPUT
        //getInputManager().deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        
        Logger.getLogger("").setLevel(Level.SEVERE);
        Logger.getLogger("de.lessvoid.nifty").setLevel(Level.WARNING);

        // INIT UI
        _ui = new UIAppState();
        
        assetManager.registerLocator("gamedata/base/", FileLocator.class);
        assetManager.unregisterLocator("/", FileLocator.class);
        stateManager.attach(_ui);

        // INIT GLOBAL AUDIO
        _audioManager = new AudioManager(this);
        
        // INIT VIDEO MANAGER
        this.videoManager = new VideoManager(this);
        this.videoManager.init();
        
        // INIT GAMEDATA HANDLING
        _gamedataManager = new GamedataManager(this);
        
        // CREATE CAMERA
        this.cameraManager = new CameraManager(getCamera());
        //this.game.getRootNode().addControl(this.cameraManager);
               
        // CHECK DEFAULT DEBUG OPTIONS
        if ( !_debug ) {
            this.setDisplayStatView(false);
            //this.setDisplayFps(false);
        }
    }
    
    /* RUNTIME METHODS */   
    @Override
    public void simpleUpdate(float tpf) {
        // MOVED TO AUDIOCONTROL TRYING TO PREVENT AUDIO "FLICKERING" IN ENGINE SOUND!
        //_audioManager.update();
    }
    
    @Override
    public void postUpdate(float tpf) {
        this.cameraManager.update(tpf);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //System.out.println("-----------------------");
    }
    
    /* ACCESS METHODS */
    public void startGame(SimpleConfig missionConfig) {
        SpaceAppState.startGame(missionConfig, this);
    }
    
    public void gotoMainMenu() {
        SpaceAppState.startDemo(this);
    }
    
    public void quit() {
        NetworkAppState.leave(this);
        SpaceAppState.leaveSpace(this);
        this.stop();
    }
    
    /* GETTERS AND SETTERS */
    public CameraManager getCameraManager() {
        return this.cameraManager;
    }

    public Player getPlayer() {
        return this.player;
    }
    
    public AudioManager getAudioManager() {
        return _audioManager;
    }
    
    public VideoManager getVideoManager() {
        return this.videoManager;
    }
    
    public GamedataManager getGamedataManager() {
        return _gamedataManager;
    }
    
    public UIAppState getUI() {
        return _ui;
    }
        
    public void joinServer(final String host, final int port) {
        enqueue(new Callable() {
            public Object call() throws Exception {
                NetworkAppState.join(Game.this, host, port);
                return null;
            }
        });
    }
    
    public void createServer(final int port) {
        enqueue(new Callable() {
            public Object call() throws Exception {
                NetworkAppState.host(Game.this, port);
                return null;
            }
        });
    }
    
    public void disconnect() {
        NetworkAppState.leave(Game.this);
    }
    
    @Override
    public void destroy() {
        disconnect();
        this.videoManager.cleanup();
        super.destroy();
    }
    
    
    
}
