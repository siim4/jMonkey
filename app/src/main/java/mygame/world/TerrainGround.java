package mygame.world;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.texture.Texture;

public class TerrainGround {

    private final TerrainQuad terrain;

    public TerrainGround(AssetManager assetManager) {
        // height params
        int size = 513;                // must be 2^n + 1
        int iterations = 40;           // number of hills
        float heightScale = 25f;       // how tall the hills are
        float minRadius = 10f;
        float maxRadius = 50f;

        AbstractHeightMap heightMap;
        try {
            heightMap = new HillHeightMap(size, iterations, minRadius, maxRadius, (byte) 3);
            heightMap.load();
            heightMap.setHeightScale(heightScale);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create heightmap", e);
        }

        terrain = new TerrainQuad("terrain", 65, size, heightMap.getHeightMap());

        //Material
        Material mat = new Material(
                assetManager,
                "Common/MatDefs/Light/Lighting.j3md"
        );
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", ColorRGBA.Brown.mult(0.8f));
        mat.setColor("Ambient", ColorRGBA.Brown.mult(0.4f));

        //tiled ground texture
        try {
            Texture tex = assetManager.loadTexture("Materials/lilMonster.png");
            tex.setWrap(Texture.WrapMode.Repeat);
            mat.setTexture("DiffuseMap", tex);
            mat.setFloat("DiffuseMap_0_scale", 32f);
           
        } catch (Exception ignore) {
            // if we don't have a texture yet, material colors still work
        }

        terrain.setMaterial(mat);

        // Position so (0,0) is roughly center
        terrain.setLocalTranslation(0, 0, 0);
    }

    public void attachTo(Node root) {
        root.attachChild(terrain);
    }

    // Query terrain height at world (x,z)
    public float getHeight(float worldX, float worldZ) {
        Float h = terrain.getHeight(new Vector2f(worldX, worldZ));
        return (h != null) ? h : 0f;
    }

    public TerrainQuad getTerrain() {
        return terrain;
    }
}
