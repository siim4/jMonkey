package mygame.core;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

import mygame.input.InputMapper;
import mygame.player.PlayerController;
import mygame.util.AnimUtils;
import mygame.world.Ground;
import mygame.world.Sky;
import mygame.npc.NpcManager;
import mygame.art.PanelField;
import mygame.dialogue.DialogueClient;
import mygame.art.GifSheetPanel;
import mygame.world.WaterSystem;
import java.util.Arrays;
import java.util.List;
import mygame.sense.ExpressionTracker;
import mygame.ui.DialogueUi;
import mygame.world.TerrainGround;







public class GameApp extends SimpleApplication {

    private Sky sky;
    private TerrainGround terrainGround;
    private Ground ground;
    private PlayerController player;
    private NpcManager npcs;
    private PanelField panelField;
    private WaterSystem water;
    private ChaseCamera chaseCam;
    private final java.util.List<GifSheetPanel> gifSheets = new java.util.ArrayList<>();
    private DialogueClient dialogueClient;
    private DialogueUi dialogueUi;
    private ExpressionTracker expressionTracker;



        private final ActionListener talkListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (!isPressed) return;
            if ("Talk".equals(name)) {
                if (!dialogueUi.isActive()) {
                    // hardcoded NPC id + display name TODO: handle this by detection if we grow implementation
                    dialogueUi.openForNpc("witch_of_mist", "Witch of Mist");
                }
            }
        }
    };

    public static void main(String[] args) {
        GameApp app = new GameApp();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("third-person decay");
        settings.setFullscreen(true);
        settings.setVSync(true);
        app.setSettings(settings);
        app.setShowSettings(false);

        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        setDisplayFps(false);
        setDisplayStatView(false);

        // world
        sky = new Sky(assetManager);
        rootNode.attachChild(sky.getSpatial());

        ground = new Ground(assetManager, 40f, 4, 0.3f);
        ground.attachTo(rootNode);

        // Player
        var model = assetManager.loadModel("Models/player.glb");
        rootNode.attachChild(model);
        var composer = AnimUtils.findComposer(model);
        player = new PlayerController(model, composer);

        // Camera: mouse-controlled orbit around player
        flyCam.setEnabled(false);

        chaseCam = new ChaseCamera(cam, player.getModel(), inputManager);

        // Distance (zoom)
        chaseCam.setDefaultDistance(5f);   // how far behind the player
        chaseCam.setMinDistance(2f);
        chaseCam.setMaxDistance(12f);

        // Height / where we look at on the model
        chaseCam.setLookAtOffset(new Vector3f(0f, 1.3f, 0f)); 

        // Vertical angle (pitch)
        chaseCam.setDefaultVerticalRotation(0.2f);   // slight downward
        chaseCam.setMaxVerticalRotation(0.9f);
        chaseCam.setMinVerticalRotation(-0.2f);

        // Mouse feel
        chaseCam.setRotationSpeed(2f);     // mouse sensitivity
        chaseCam.setZoomSensitivity(2f);

        // Motion style
        chaseCam.setSmoothMotion(true);
        chaseCam.setTrailingEnabled(true); // trailing behind rotate fast

        // Lights
        AmbientLight amb = new AmbientLight();
        amb.setColor(ColorRGBA.Green.mult(0.4f));
        rootNode.addLight(amb);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1f, -2f, -1f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(0.8f));
        rootNode.addLight(sun);

        // Input
        new InputMapper(inputManager, player);

        // NPCs
        npcs = new NpcManager(assetManager, rootNode);

        npcs.spawn("Models/angel.gltf", -8f,  -6f, 1.0f, 20f);
        npcs.spawn("Models/angel.gltf",  6f,   3f, 1.0f, 20f);
        npcs.spawn("Models/angel.gltf", -3f,  10f, 1.0f, 20f);
        npcs.spawn("Models/angel.gltf", 12f,  -9f, 1.0f, 20f);

        // Panels

        panelField = new PanelField();

        List<String> artSet = Arrays.asList(
      "Textures/random1.jpg",
            "Textures/random2.jpg",
            "Textures/random3.jpg",
            "Textures/random4.jpg",
            "Textures/random5.jpg",
            "Textures/random6.jpg",
            "Textures/random7.jpg",
            "Textures/random8.jpg",
            "Textures/random9.jpg",
            "Textures/random10.jpg",
            "Textures/random11.jpg",
            "Textures/random12.jpg",
            "Textures/random13.gif",
            "Textures/random14.jpg",
            "Textures/random15.jpg",
            "Textures/random16.jpg",
            "Textures/random17.gif",
            "Materials/lilMonster.png"

            
            
);

        panelField.addRandomPanels(assetManager, artSet, 3f, 4f, 10, 12f, 0.02f);
        panelField.attachTo(rootNode);

        //Gif
        gifSheets.add(new GifSheetPanel(assetManager, "Textures/gifs/teeth.png", 26, 2f, 2.5f).at(-8f, 0.02f, -6f));
        gifSheets.add(new GifSheetPanel(assetManager, "Textures/gifs/teeth.png", 26, 2f, 2.5f).at(  2f, 0.02f,  9f));

        for (var p : gifSheets) rootNode.attachChild(p.getNode());

        // Water
        water = new WaterSystem(assetManager, rootNode);

        // Dialogue
        dialogueClient = new DialogueClient("http://localhost:8080");
              dialogueUi = new DialogueUi(this, (npcId, text) -> {
                startNpcRequest(npcId, text);
        });

        initTalkKey();

        //Mountain Terrain
        terrainGround=new TerrainGround(assetManager);
        terrainGround.attachTo(rootNode);

        // Expression tracker
        expressionTracker = new ExpressionTracker();
   
    }

    
   


    @Override
    public void simpleUpdate(float tpf) {

if (expressionTracker != null) {
    expressionTracker.update(tpf);
    float motion = expressionTracker.getMotionAmount();
    ground.setExpression(motion);


}
        ground.update(tpf);
        player.update(tpf);
        npcs.update(tpf);
        sky.follow(player.getModel().getLocalTranslation());
        if (panelField != null) {
            panelField.update(tpf, cam);
            }
        for (var p : gifSheets) p.update(tpf, cam);
            if (water != null) {
            water.update(tpf);
        }
              if (dialogueUi != null) {
            dialogueUi.update(tpf);
        }

        if (terrainGround != null) {
    var model = player.getModel();
    Vector3f pos = model.getLocalTranslation();

    float terrainY = terrainGround.getHeight(pos.x, pos.z);
    model.setLocalTranslation(pos.x, terrainY, pos.z);
}

    }

     private void initTalkKey() {
        inputManager.addMapping("Talk", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(talkListener, "Talk");
    }

    private void startNpcRequest(String npcGameId, String playerMessage) {
        final String playerId = "player1";  // later you can derive from savegame / profile

        new Thread(() -> {
            try {
                String reply = dialogueClient.talkToNpc(npcGameId, playerId, playerMessage);
                System.out.println("NPC replied: " + reply);

                // Must update UI on JME render thread
                this.enqueue(() -> {
                    dialogueUi.showNpcReply(reply);
                    return null;
                });

            } catch (Exception e) {
                e.printStackTrace();
                this.enqueue(() -> {
                    dialogueUi.showNpcReply(
                            "(the witch coughs, static crackles in the air — something went wrong...)"
                    );
                    return null;
                });
            }
        }, "NpcDialogueThread").start();
    }
}
