package mygame.world;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.asset.AssetManager;

public class Sky {
    private final Geometry skyGeom;

    public Sky(AssetManager assets) {
        Box skyBoxMesh = new Box(100f, 100f, 100f);
        skyGeom = new Geometry("SkyBox", skyBoxMesh);

        Material skyMat = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        skyMat.setTexture("ColorMap", assets.loadTexture("Materials/sky.jpg"));
        skyMat.setColor("Color", ColorRGBA.White.mult(0.9f));
        skyMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        skyGeom.setMaterial(skyMat);
        skyGeom.setQueueBucket(RenderQueue.Bucket.Sky);
        skyGeom.setCullHint(Geometry.CullHint.Never);
    }

    public Geometry getSpatial() { return skyGeom; }

    public void follow(Vector3f targetPos) {
        skyGeom.setLocalTranslation(targetPos);
    }
}
