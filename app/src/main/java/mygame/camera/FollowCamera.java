package mygame.camera;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

public class FollowCamera {
    private final Camera cam;
    private final Spatial target;
    private Vector3f offset = new Vector3f(2f, 0.6f, 2f);
    private float smooth = 4f; // set to 4–8 for nice handheld easing

    private final Vector3f desired = new Vector3f();
    private final Vector3f current = new Vector3f();

    public FollowCamera(Camera cam, Spatial target) {
        this.cam = cam;
        this.target = target;
        current.set(target.getLocalTranslation()).addLocal(offset);
        cam.setLocation(current);
        cam.lookAt(target.getLocalTranslation(), Vector3f.UNIT_Y);
    }

    public void setOffset(Vector3f off) { this.offset = off; }
    public void setSmoothing(float factor) { this.smooth = Math.max(0, factor); }

    public void update(float tpf) {
        desired.set(target.getLocalTranslation()).addLocal(offset);

        if (smooth <= 0f) {
            cam.setLocation(desired);
        } else {
            float alpha = 1f - (float)Math.exp(-smooth * tpf);
            current.interpolateLocal(desired, alpha);
            cam.setLocation(current);
        }

        cam.lookAt(target.getLocalTranslation(), Vector3f.UNIT_Y);
    }
}
