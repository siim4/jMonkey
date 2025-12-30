package mygame.art;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.*;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.scene.VertexBuffer;

public class GifSheetPanel {
    private final Node node = new Node("GifSheetPanel");
    private final Geometry geom;
    private final Material mat;

    private final int totalFrames;
    private int index = 0;
    private float frameTime = 0.12f;
    private float timer = 0f;

    public GifSheetPanel(AssetManager assets, String sheetPath,
                         int totalFrames, float width, float height) {
        this.totalFrames = totalFrames;

        Quad q = new Quad(width, height);
        geom = new Geometry("GifSheetQuad", q);

        mat  = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assets.loadTexture(sheetPath);
        tex.setAnisotropicFilter(8);
        tex.setMagFilter(Texture.MagFilter.Bilinear);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        mat.setTexture("ColorMap", tex);

        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);

        geom.setMaterial(mat);
        geom.setLocalTranslation(-width/2f, 0f, 0f);
        node.attachChild(geom);

        setFrameUV(0);
    }

    public Node getNode() { return node; }
    public GifSheetPanel at(float x, float y, float z) { node.setLocalTranslation(x,y,z); return this; }
    public GifSheetPanel secondsPerFrame(float s) { frameTime = Math.max(0.01f, s); return this; }

    public void update(float tpf, Camera cam) {
       
        Vector3f toCam = cam.getLocation().subtract(node.getWorldTranslation());
        toCam.y = 0f;
        if (toCam.lengthSquared() > FastMath.ZERO_TOLERANCE) {
            float yaw = (float)Math.atan2(toCam.x, toCam.z);
            node.setLocalRotation(new Quaternion().fromAngles(0, yaw, 0));
        }

        timer += tpf;
        if (timer >= frameTime) {
            timer = 0f;
            index = (index + 1) % totalFrames;
            setFrameUV(index);
        }
    }

    private void setFrameUV(int i) {
        float frameW = 1f / totalFrames;
        float uMin = i * frameW;
        float uMax = uMin + frameW;

        float[] tc = new float[]{
            uMin, 0f,
            uMax, 0f,
            uMin, 1f,
            uMax, 1f
        };
        Mesh m = geom.getMesh();
        m.setBuffer(VertexBuffer.Type.TexCoord, 2, tc);
        m.updateBound();
    }
}
