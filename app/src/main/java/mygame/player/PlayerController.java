package mygame.player;

import com.jme3.anim.AnimComposer;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class PlayerController {
    private final Spatial model;
    private final AnimComposer animator;

    private final Vector3f moveDir = new Vector3f();
    private float moveSpeed = 4f;

    // Input flags
    public boolean left, right, up, down;

    private boolean walking = false;

    public PlayerController(Spatial model, AnimComposer animator) {
        this.model = model;
        this.animator = animator;
    }

    public void setMoveSpeed(float speed) {
    this.moveSpeed = speed;
}


    public void update(float tpf) {
        float x = 0, z = 0;
        if (left && !right)  x = -1f;
        if (right && !left)  x =  1f;
        if (up && !down)     z = -1f;
        if (down && !up)     z =  1f;

        moveDir.set(x, 0, z);
        boolean isMoving = moveDir.lengthSquared() > 0f;

        if (isMoving) {
            moveDir.normalizeLocal();
            Vector3f step = moveDir.mult(moveSpeed * tpf);
            model.setLocalTranslation(model.getLocalTranslation().add(step));

            float angleY = (float) Math.atan2(moveDir.x, moveDir.z);
            model.setLocalRotation(model.getLocalRotation().fromAngles(0, angleY, 0));

            playWalk();
        } else {
            stopWalk();
        }
    }

    private void playWalk() {
        if (animator == null || walking) return;
        animator.setCurrentAction("walk");
        walking = true;
    }

    private void stopWalk() {
        if (animator == null || !walking) return;
        animator.removeCurrentAction("Default");
        walking = false;
    }

    public Spatial getModel() { return model; }
}
