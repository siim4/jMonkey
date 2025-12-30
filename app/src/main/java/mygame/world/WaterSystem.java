package mygame.world;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.*;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;

import java.nio.FloatBuffer;

public class WaterSystem {

    private final AssetManager assets;
    private final Node root;

    private Geometry riverGeom;
    private Geometry waterfallGeom;

    private float riverOffsetU = 0f;
    private float waterfallOffsetV = 0f;

    private float riverSpeed = 0.4f;     
    private float waterfallSpeed = 1.0f; 

    private float[] riverBaseUV;
    private float[] waterfallBaseUV;

    private ParticleEmitter mistEmitter;

    public WaterSystem(AssetManager assets, Node root) {
        this.assets = assets;
        this.root   = root;

        createRiver();
        createWaterfall();
        createMist();
    }

    
    private void createRiver() {
        float length = 100f; // along X
        float width  = 6f;   // along Z

        // Quad in jME is in X/Y; we’ll rotate it to lie on X/Z.
        Quad q = new Quad(length, width);
        riverGeom = new Geometry("River", q);

        Material mat = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assets.loadTexture("Textures/water.jpg"); 
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", tex);
        mat.setColor("Color", new ColorRGBA(1,1,1,0.8f)); // opacity
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        riverGeom.setMaterial(mat);
        riverGeom.setQueueBucket(RenderQueue.Bucket.Transparent);

        // rotate to lie flat in ground.
        riverGeom.rotate(-FastMath.HALF_PI, 0, 0);
        // center position. We can tweak
        riverGeom.setLocalTranslation(-length / 2f, 0.5f, 20f);

        // Set initial tiling similar to Three.js repeat.set(10,1)
        setupRiverUV(length, width, 10f, 1f);

        root.attachChild(riverGeom);
    }

    private void setupRiverUV(float length, float width, float repeatU, float repeatV) {
        Mesh m = riverGeom.getMesh();

  
        riverBaseUV = new float[] {
            0f,       0f,
            repeatU,  0f,
            0f,       repeatV,
            repeatU,  repeatV
        };

        FloatBuffer buf = BufferUtils.createFloatBuffer(riverBaseUV);
        m.setBuffer(VertexBuffer.Type.TexCoord, 2, buf);
        m.updateBound();
    }

 
    private void createWaterfall() {
        float width  = 8f;
        float height = 18f;

        Quad q = new Quad(width, height);
        waterfallGeom = new Geometry("Waterfall", q);

        Material mat = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assets.loadTexture("Textures/water.jpg"); 
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", tex);
        mat.setColor("Color", new ColorRGBA(1,1,1,0.9f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        waterfallGeom.setMaterial(mat);
        waterfallGeom.setQueueBucket(RenderQueue.Bucket.Transparent);

     
        waterfallGeom.rotate(0, FastMath.HALF_PI, 0);
        waterfallGeom.setLocalTranslation(10f, 10f, -20f);

        setupWaterfallUV(width, height, 1f, 5f);
        root.attachChild(waterfallGeom);
    }

    private void setupWaterfallUV(float width, float height, float repeatU, float repeatV) {
        Mesh m = waterfallGeom.getMesh();

        waterfallBaseUV = new float[] {
            0f,       0f,
            repeatU,  0f,
            0f,       repeatV,
            repeatU,  repeatV
        };

        FloatBuffer buf = BufferUtils.createFloatBuffer(waterfallBaseUV);
        m.setBuffer(VertexBuffer.Type.TexCoord, 2, buf);
        m.updateBound();
    }
// Mist particle emitter

    private void createMist() {
        mistEmitter = new ParticleEmitter("Mist", ParticleMesh.Type.Triangle, 500);
        mistEmitter.setStartColor(new ColorRGBA(1f, 1f, 1f, 0.4f));
        mistEmitter.setEndColor  (new ColorRGBA(1f, 1f, 1f, 0.0f));
        mistEmitter.setStartSize(0.6f);
        mistEmitter.setEndSize(1.5f);
        mistEmitter.setGravity(0, 0.1f, 0);
        mistEmitter.setLowLife(1.0f);
        mistEmitter.setHighLife(3.0f);
        mistEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2f, 0));
        mistEmitter.getParticleInfluencer().setVelocityVariation(0.5f);

        Material pm = new Material(assets, "Common/MatDefs/Misc/Particle.j3md");
       
        mistEmitter.setMaterial(pm);

      
        mistEmitter.setLocalTranslation(10f, 1f, -20f);

        root.attachChild(mistEmitter);
    }

   
    public void update(float tpf) {
        if (riverGeom != null) {
            riverOffsetU += riverSpeed * tpf;
            scrollRiverUV();
        }

        if (waterfallGeom != null) {
            waterfallOffsetV -= waterfallSpeed * tpf; 
            scrollWaterfallUV();
        }

    }

    private void scrollRiverUV() {
        Mesh m = riverGeom.getMesh();
        float[] uv = new float[riverBaseUV.length];
        for (int i = 0; i < riverBaseUV.length; i += 2) {
            float u = riverBaseUV[i]   + riverOffsetU;
            float v = riverBaseUV[i+1];
            uv[i]   = u;
            uv[i+1] = v;
        }
        m.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(uv));
        m.updateBound();
    }

    private void scrollWaterfallUV() {
        Mesh m = waterfallGeom.getMesh();
        float[] uv = new float[waterfallBaseUV.length];
        for (int i = 0; i < waterfallBaseUV.length; i += 2) {
            float u = waterfallBaseUV[i];
            float v = waterfallBaseUV[i+1] + waterfallOffsetV;
            uv[i]   = u;
            uv[i+1] = v;
        }
        m.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(uv));
        m.updateBound();
    }
}
