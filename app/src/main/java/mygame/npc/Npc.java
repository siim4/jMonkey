package mygame.npc;

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Random;

public class Npc {
    private final Spatial model;        
    private final AnimComposer animator; 
    private final Vector3f dir = new Vector3f();
    private final Random rng = new Random();

    // Movement settings
    private float speed = 0.8f + rng.nextFloat() * 0.8f; 
    private float areaHalfSize = 20f;                    

    // Direction change timer
    private float changeEvery = 2.0f + rng.nextFloat() * 3.0f;
    private float timeAccum = 0f;

    public Npc(Spatial model, AnimComposer animator) {
        this.model = model;
        this.animator = animator;
        randomDirection();
        playLoop();
    }

    public Spatial getSpatial() { return model; }


    public void attachTo(Node root) {
        root.attachChild(model);
    }

    // Initial position & scale. 
    public Npc at(Vector3f pos, float uniformScale) {
        model.setLocalTranslation(pos);
        if (uniformScale != 1f) model.setLocalScale(uniformScale);
        return this;
    }

    // Bounds for wandering 
    public Npc bounds(float half) {
        this.areaHalfSize = half;
        return this;
    }

  
    public void update(float tpf) {
        // Change direction intervals
        timeAccum += tpf;
        if (timeAccum >= changeEvery) {
            timeAccum = 0f;
            changeEvery = 2.0f + rng.nextFloat() * 3.0f;
            randomDirection();
        }

        
        Vector3f step = dir.mult(speed * tpf);
        Vector3f p = model.getLocalTranslation().add(step);

        // Bounce at bounds
        if (p.x < -areaHalfSize || p.x > areaHalfSize) {
            dir.x *= -1f;
            p.x = FastMath.clamp(p.x, -areaHalfSize, areaHalfSize);
            faceMoveDir();
        }
        if (p.z < -areaHalfSize || p.z > areaHalfSize) {
            dir.z *= -1f;
            p.z = FastMath.clamp(p.z, -areaHalfSize, areaHalfSize);
            faceMoveDir();
        }

        model.setLocalTranslation(p);
    }

    

    private void randomDirection() {
        dir.set(rng.nextFloat() - 0.5f, 0f, rng.nextFloat() - 0.5f).normalizeLocal();
        faceMoveDir();
    }

    private void faceMoveDir() {
        float angleY = (float) Math.atan2(dir.x, dir.z);
        model.setLocalRotation(model.getLocalRotation().fromAngles(0, angleY, 0));
    }

private void playLoop() {
    if (animator == null) return;

    AnimClip clip = animator.getAnimClip("walk");
    if (clip == null) clip = animator.getAnimClip("Armature|mixamo.com|Layer0");

  
    if (clip == null) {
        for (AnimClip c : animator.getAnimClips()) {
            clip = c;
            break;
        }
    }

    if (clip != null) {
        animator.setCurrentAction(clip.getName());
    }
}

}
