/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jlab.spacefight.ui;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.ViewPort;
import de.jlab.spacefight.Game;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;

/**
 * App state to maintain UI stuff. "UI" is only the menu and "real" deal.
 * Ingame HUD is maintained somewhere else and is highly context sensitive.
 * 
 * @author rampage
 */
public class UIAppState extends AbstractAppState {

    private NiftyJmeDisplay _niftyDisplay;
    private Nifty nifty;
    
    private Game game;
    //private SpaceAppState uispace;
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app); 
        game = (Game)app;          // cast to a more specific class

        //initNifty();
        //gotoMainMenu();
        
        //game.gotoMainMenu();
        initNifty();
    }
 
    private void initNifty() {
        ViewPort guiViewPort = game.getGuiViewPort();
        InputManager inputManager = game.getInputManager();
               
        if(guiViewPort.getProcessors().contains(_niftyDisplay))
            guiViewPort.removeProcessor(_niftyDisplay);
                
        // init stuff that is independent of whether state is PAUSED or RUNNING
        _niftyDisplay = new NiftyJmeDisplay(game.getAssetManager(),
                                                      inputManager,
                                                      game.getAudioRenderer(),
                                                      guiViewPort);
        nifty = _niftyDisplay.getNifty();

        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(_niftyDisplay);
        
        this.game.getGamedataManager().loadUI(this.nifty);
        
        this.game.gotoMainMenu();
    }
    
    @Override
    public void cleanup() {
      super.cleanup();
//      if ( _nifty != null )
//        _nifty.exit();
    }
  
    @Override
    public void update(float tpf) {

    }
    
    /*
    public void stopGame(SpaceAppState space) {
        _game.getStateManager().detach(space);
        gotoMainMenu();
    }
    */
    
    /*
    public void gotoMainMenu() {
        _game.getFlyByCamera().setEnabled(false);
        _game.getFlyByCamera().setRotationSpeed(2f);
        _game.getFlyByCamera().setDragToRotate(true);
        _game.getInputManager().setCursorVisible(true);        
        
        initNifty();
        _nifty.fromXml("ui/menu.xml", "start", new ScreenController[] { new MainMenuController(this), 
                                                                        new EditorController(_game), 
                                                                        new SelectionMenuController(this),
                                                                        new QuickMatchController(this),
                                                                        new SingleMissionController(this),
                                                                        new ServerBrowserController(this),
                                                                        new GameLobbyController(this)
                                                                      });
        
        _game.getAudioManager().setSoundVolume(0);
        
        startDemo();
        //_game.getAudioManager().playMusic("mainmenu.ogg", 0, true);
    }
    */
        
    /*
    public void startMission(SimpleConfig config) {
        startSpace(config);
    }
    */
    
    /*
    public void startSpace(SimpleConfig config) {
        _game.getStateManager().detach(_uispace);
        _uispace = null;
        
        SpaceAppState oldSpace = this._game.getStateManager().getState(SpaceAppState.class);
        if (oldSpace != null) {
            this._game.getStateManager().detach(oldSpace);
        }
        
        _game.getAudioManager().stopMusic(0);
        _game.getAudioManager().setSoundVolume(1);
        SpaceAppState space = new SpaceAppState(getGame(), true);
        space.loadMission(config);
        initNifty();
        _nifty.fromXml("ui/ingame.xml", "flightselect", new ScreenController[] {    new IngameMenuController(_game, this, space), 
                                                                            new IngameController(_game, this, space),
                                                                            new FactionSelectController(this, space)
                                                                       });

        _game.getStateManager().attach(space);
    }
    
    public void quit() {
        if ( _uispace != null ) {
            _uispace.setEnabled(false);
            _game.getStateManager().detach(_uispace);
        }
        _game.stop();
    }
    
    public ScreenController getController() {
        return _nifty.getCurrentScreen().getScreenController();
    }
    */
    
    public Game getGame() {
        return game;
    }
    
    /* DEMO PLAYBACK */
/*    
    public void startDemo() {
        if ( _uispace != null ) {
            _uispace.setEnabled(false);
            _game.getStateManager().detach(_uispace);
        }
        
        // LOAD THE GENERATED DEMO
        _uispace = new SpaceAppState(getGame(), false);
        
        // CREATE RANDOM GENERATOR
        Random randomGenerator = new Random();
        // PICK A RANDOM LEVEL
        String[] levels = _game.getGamedataManager().listLevels();
        String level = levels[randomGenerator.nextInt(levels.length)];
        
        // LIST ALL SCRIPTED MISSIONS
        String[] missions = _game.getGamedataManager().listMissions();
        // CREATE A RANDOM FOR MISSION SELECTION 
        // (NUMBER OF SCRIPTED MISSIONS + NUMBER OF QUICKMATCH TYPES)
        int random = randomGenerator.nextInt(missions.length + 2);
        //AbstractMission mission = null;
        
        SimpleConfig config = new SimpleConfig();
        
        if ( random < missions.length ) {
            // SELECT A SCRIPTED MISSION IF RANDOM IS IN MISSIONLIST-BOUNDS
            config.setValue("type", "scripted");
            config.setValue("name", missions[random]);
            config.setValue("maxclients", "32");
            _uispace.loadMission(config);
            //mission = _game.getGamedataManager().loadMission(missions[random], 64);
        } else {
            // SELECT A QUICKMISSION IF RANDOM IS OUT OF RANGE
            random -= missions.length;
            System.out.println(random);
            if ( random == 0 ) {
                // LOAD DEATHMATCHMISSION IF RANDOM EQUALS MISSIONLIST-SIZE
                config.setValue("type", "deathmatch");
                config.setValue("level", level);
                config.setValue("maxclients", "64");
                config.setValue("teams", "64");
                config.setValue("limit", "20");
                _uispace.loadMission(config);
            } else {
                // LOAD TEAMDEATHMATCH IF RANDOM IS LARGER MISSIONLIST-SIZE
                int teams = 2 + 2 * randomGenerator.nextInt(2+1);
                
                config.setValue("type", "deathmatch");
                config.setValue("level", level);
                config.setValue("maxclients", "64");
                config.setValue("teams", Integer.toString(teams));
                config.setValue("limit", "100");
                _uispace.loadMission(config);
            }
        }
        
        _game.getStateManager().attach(_uispace);
    }
*/    
    
    /*
    public void initGameUI(SpaceAppState space) {
        this.initNifty();
                
        String screen = "spawnscreen";
        //if (NetworkAppState.isClient(space.getGame())) {
            //screen = null;
        //}
        try {
            this.nifty.validateXml("ui/ingame.xml");
        } catch (Exception ex) {
            Logger.getLogger(UIAppState.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.nifty.fromXml("ui/ingame.xml", screen, new ScreenController[] { 
            new SpawnScreenController(this, space),
            new IngameMenuController(game, this, space), 
            new IngameController(game, this, space),
            new FactionSelectController(this, space)
        });
        
        //if (screen != null) {
            //this._nifty.gotoScreen(screen);
        //}
    }
    
    public void initMenuUI() {
        this.initNifty();
        String startScreen = "start";
        if (NetworkAppState.isActive(this.game)) {
            startScreen = "mplobby";
        }
                
        try {
            this.nifty.validateXml("ui/menu.xml");
        } catch (Exception ex) {
            Logger.getLogger(UIAppState.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.nifty.fromXml("ui/menu.xml", startScreen, new ScreenController[] { 
            new MainMenuController(this), 
            new EditorController(game), 
            new SelectionMenuController(this),
            new QuickMatchController(this),
            new SingleMissionController(this),
            new ServerBrowserController(this),
            new GameLobbyController(this)
        });
    }
    */
    
    public void gotoScreen(String name) {
        this.nifty.gotoScreen(name);
    }
    
    public Screen getCurrentScreen() {
        return this.nifty.getCurrentScreen();
    }
    
      //////////////////////////////////////////////////////////////////////////
     // STATIC ACCESS METHODS /////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    
    public static void gotoScreen(String name, Game game) {
        UIAppState uiState = game.getStateManager().getState(UIAppState.class);
        if (uiState == null) {
            throw new NullPointerException("UIState not found!");
        }

        uiState.gotoScreen(name);
    }
        
    public static Screen currentScreen() {
        UIAppState uiState = Game.get().getStateManager().getState(UIAppState.class);
        if (uiState == null) {
           throw new NullPointerException("UIState not found!");
        }
        
        return uiState.getCurrentScreen();
    }
    
    /*
    public static void initGameUI(Game game) {
       UIAppState uiState = game.getStateManager().getState(UIAppState.class);
       if (uiState == null) {
           throw new NullPointerException("UIState not found!");
       }
       
       SpaceAppState space = game.getStateManager().getState(SpaceAppState.class);
       if (space == null) {
           throw new NullPointerException("SpaceAppState not found!");
       }

       uiState.initGameUI(space);           
    }
    
    public static void initMenuUI(Game game) {
       UIAppState uiState = game.getStateManager().getState(UIAppState.class);
       if (uiState == null) {
           throw new NullPointerException("UIState not found!");
       }
       
       uiState.initMenuUI();
    }
    */
    
}
