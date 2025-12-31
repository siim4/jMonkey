package mygame.world;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Map;

public class WorldMap {

    private final AssetManager ASSETS;
    private final Node ROOT;
    private final TerrainGround TERRAIN;
    private final Map<String, Spatial> OBJECTS = new HashMap<>();
    private float worldRadius = 35f;
    private float minY = -5f;
    private float maxY = 20f;

    public WorldMap(AssetManager assets, Node root, TerrainGround terrain) {
        this.ASSETS = assets;
        this.ROOT = root;
        this.TERRAIN = terrain;
    }

    public void setWorldRadius(float radius) { this.worldRadius = radius; }

    public Spatial addModel(String id, String modelPath, Vector3f position, float yawDegrees) {
      Spatial scene = ASSETS.loadModel(modelPath);
      scene.setLocalTranslation(clampToWorld(position));
      scene.rotate(0f, yawDegrees * FastMath.DEG_TO_RAD, 0f);
      if (TERRAIN != null) {
        Vector3f pos = scene.getLocalTranslation();
        float y = TERRAIN.getHeight(pos.x, pos.z);
        scene.setLocalTranslation(pos.x, y, pos.z);
      } 
      ROOT.attachChild(scene);
      OBJECTS.put(id, scene);
      return scene; 
    }

    public Spatial get(String id) { return OBJECTS.get(id); }

    public Vector3f clampToWorld(Vector3f position ) {
        Vector3f out = position.clone();
        Vector3f xz = new Vector3f(out.x, 0f, out.z);
        float length = xz.length();
        if (length > worldRadius) {
            xz.normalizeLocal().multLocal(worldRadius);
            out.x = xz.x;
            out.z = xz.z;
        }
        // Clamp Y
        out.y = FastMath.clamp(out.y, minY, maxY);
        return out;
    }
    
}
