package mygame.world;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Box;

import java.util.ArrayList;
import java.util.List;

public class Ground {

    // panel + its base Y so we can lift it with "expression" 
    private static class Panel {
        Geometry geom;
        float baseY;

        Panel(Geometry geom) {
            this.geom = geom;
            this.baseY = geom.getLocalTranslation().y;
        }
    }

    private final Geometry base;
    private final Material mat;

    private float animTime = 0f;
    private int currentFrame = 0;

    private final int totalFrames;
    private final float frameDuration;

    private final List<Panel> panels = new ArrayList<>();
    private final List<Geometry> hills = new ArrayList<>();

    // expression → panel lift
    private float currentExpression = 0f; // 0-1
    private final float maxLift = 0.8f;   // max Y offset

    public Ground(AssetManager assets, float size, int totalFrames, float frameDuration) {
        this.totalFrames = totalFrames;
        this.frameDuration = frameDuration;

        // Sprite material using sprite sheet
        mat = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assets.loadTexture("Materials/lilMonster.png"));
        mat.setColor("Color", ColorRGBA.White.mult(0.8f));
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        // Base ground
        Quad q = new Quad(size, size);
        base = new Geometry("GroundBase", q);
        base.setMaterial(mat);
        base.rotate(-FastMath.HALF_PI, 0, 0);
        base.setLocalTranslation(-size / 2f, 0f, size / 2f);
        base.setQueueBucket(RenderQueue.Bucket.Opaque);
        base.setCullHint(Geometry.CullHint.Never);

        // panels
        addPanel(12f, 12f, 0f, 0f);
        addPanel(6f, 6f, -10f, 5f);
        addPanel(4f, 8f, 8f, -6f);
        addPanel(5f, 5f, -4f, -12f);

        //mountains
        createHills(size);

        // Apply initial UV frame
        setFrame(0);
    }

    public void attachTo(Node root) {
        root.attachChild(base);
        for (Panel p : panels) {
            root.attachChild(p.geom);
        }
        for (Geometry h : hills) {
            root.attachChild(h);
        }
    }

    public Geometry getBase() {
        return base;
    }

    //per frame update-----------------------
    public void update(float tpf) {
        // animate sprite frame
        animTime += tpf;
        if (animTime >= frameDuration) {
            animTime = 0f;
            currentFrame = (currentFrame + 1) % totalFrames;
            setFrame(currentFrame);
        }

        // apply expression-driven lift to panels
        applyExpressionToPanels();
    }

    // Add panels
    private void addPanel(float w, float h, float worldX, float worldZ) {
        Quad q = new Quad(w, h);
        Geometry g = new Geometry("GroundPanel", q);
        g.setMaterial(mat);

        g.rotate(-FastMath.HALF_PI, 0, 0);
        g.setLocalTranslation(worldX - w / 2f, 0.01f, worldZ + h / 2f);
        g.setQueueBucket(RenderQueue.Bucket.Opaque);
        g.setCullHint(Geometry.CullHint.Never);

        panels.add(new Panel(g));
    }

    // Add mountains
    private void createHills(float size) {
        addHill(3f, 1.4f, -size * 0.25f, -size * 0.10f);
        addHill(2.5f, 1.2f, size * 0.20f, size * 0.15f);
        addHill(4f, 1.8f, 0f, -size * 0.30f);
        addHill(2f, 1.0f, -size * 0.15f, size * 0.30f);
    }

    private void addHill(float radiusXZ, float height, float worldX, float worldZ) {
        Box box = new Box(radiusXZ, height, radiusXZ);
        Geometry hill = new Geometry("Hill", box);
        hill.setMaterial(mat);

        hill.setLocalTranslation(worldX, height, worldZ);
        hill.setQueueBucket(RenderQueue.Bucket.Opaque);
        hill.setCullHint(Geometry.CullHint.Never);

        hills.add(hill);
    }

    // Spritesheet animation
    private void setFrame(int index) {
        float frameWidth = 1f / totalFrames;
        float uMin = index * frameWidth;
        float uMax = uMin + frameWidth;

        applyUV(base.getMesh(), uMin, uMax);
        for (Panel p : panels) applyUV(p.geom.getMesh(), uMin, uMax);
        for (Geometry h : hills) applyUV(h.getMesh(), uMin, uMax);
    }

    // Assign glitchy UVs to sprite frame
    private static void applyUV(Mesh m, float uMin, float uMax) {
        int vertexCount = m.getVertexCount();
        if (vertexCount == 0) return;

        float[] uvs = new float[vertexCount * 2];

        for (int i = 0; i < vertexCount; i++) {
            int corner = i % 4;

            float u = (corner == 1 || corner == 3) ? uMax : uMin;
            float v = (corner == 2 || corner == 3) ? 1f   : 0f;

            uvs[i * 2]     = u;
            uvs[i * 2 + 1] = v;
        }

        m.setBuffer(VertexBuffer.Type.TexCoord, 2, uvs);
        m.updateBound();
    }

    //Expression lift
    public void setExpression(float intensity) {
        currentExpression = FastMath.clamp(intensity, 0f, 1f);
    }

    private void applyExpressionToPanels() {
        float lift = currentExpression * maxLift;

        for (Panel p : panels) {
            var t = p.geom.getLocalTranslation();
            p.geom.setLocalTranslation(t.x, p.baseY + lift, t.z);
        }
    }
}
