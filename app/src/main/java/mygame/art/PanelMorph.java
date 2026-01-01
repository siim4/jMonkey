package mygame.art;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import java.util.List;

public class PanelMorph {

    private final AssetManager assets;
    private final Node node = new Node("PanelMorph");
    private final Geometry front;
    private final Geometry back;
    private final Material mFront;
    private final Material mBack;

    private final List<String> imagePaths;
    private int currIndex = 0;
    private int nextIndex = 1;

    private float fadeTimer = 0f;
    private float fadeDuration = 8f; // Seconds per crossfade

    @SuppressWarnings("unused")
    private final float width;
    @SuppressWarnings("unused")
    private final float height;

    public PanelMorph(AssetManager assets, List<String> imagePaths, float width, float height) {
        this.assets = assets;
        this.imagePaths = imagePaths;
        this.width = width;
        this.height = height;

        Quad q = new Quad(width, height);
        front = new Geometry("PanelFront", q);
        back  = new Geometry("PanelBack",  q);

        mFront = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mBack  = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");

        // Transparency 
        mFront.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mBack .getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mFront.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mBack .getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        front.setQueueBucket(RenderQueue.Bucket.Transparent);
        back .setQueueBucket(RenderQueue.Bucket.Transparent);


        setTexture(mBack,  imagePaths.get(currIndex));
        setTexture(mFront, imagePaths.get(nextIndex));
        mBack.setColor ("Color", new ColorRGBA(1,1,1,1));   // back = visible
        mFront.setColor("Color", new ColorRGBA(1,1,1,0));   // front = transparent

        front.setMaterial(mFront);
        back .setMaterial(mBack);

     
        Node offset = new Node();
        offset.attachChild(back);
        offset.attachChild(front);
        back.setLocalTranslation(-width/2f, 0f, 0f);
        front.setLocalTranslation(-width/2f, 0f, 0.001f);
        node.setLocalTranslation(node.getLocalTranslation().add(0, 0.02f, 0));

        node.attachChild(offset);
    }

    public Node getNode() { return node; }


    public PanelMorph at(float x, float y, float z) {
        node.setLocalTranslation(x, y, z);
        return this;
    }

    //Seconds per crossfade.
    public PanelMorph fadeDuration(float seconds) {
        this.fadeDuration = Math.max(0.1f, seconds);
        return this;
    }

 
    public void update(float tpf, Camera cam) {
        
        Vector3f toCam = cam.getLocation().subtract(node.getWorldTranslation());
        toCam.y = 0f;
        if (toCam.lengthSquared() > FastMath.ZERO_TOLERANCE) {
            toCam.normalizeLocal();
            float yaw = (float)Math.atan2(toCam.x, toCam.z);
            node.setLocalRotation(new Quaternion().fromAngles(0, yaw, 0));
        }

        // Crossfade
        fadeTimer += tpf;
        float a = FastMath.clamp(fadeTimer / fadeDuration, 0f, 1f);
        mFront.setColor("Color", new ColorRGBA(1,1,1, a));
        mBack .setColor("Color", new ColorRGBA(1,1,1, 1f - a));

        if (fadeTimer >= fadeDuration) {
         
            fadeTimer = 0f;
            currIndex = nextIndex;
            nextIndex = (nextIndex + 1) % imagePaths.size();

           
            setTexture(mBack,  imagePaths.get(currIndex));
            setTexture(mFront, imagePaths.get(nextIndex));
            mBack.setColor ("Color", ColorRGBA.White);
            mFront.setColor("Color", new ColorRGBA(1,1,1,0));
        }
    }

    private void setTexture(Material m, String path) {
        var tex = assets.loadTexture(path);
        tex.setAnisotropicFilter(8);
        tex.setMagFilter(com.jme3.texture.Texture.MagFilter.Bilinear);
        tex.setMinFilter(com.jme3.texture.Texture.MinFilter.Trilinear);
        m.setTexture("ColorMap", tex);
    }
}
