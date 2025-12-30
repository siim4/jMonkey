package mygame.sense;

import com.jme3.math.FastMath;

//Smooths webcam motion into a nice 0..1 expression value
public class ExpressionTracker {

    private final WebcamMotionTracker webcamTracker;

    // smoothed value
    private float expression = 0f;

    // how quickly we react to changes (seconds to reach ~63% of the target)
    private final float smoothingTime = 0.5f;

    public ExpressionTracker() {
        this.webcamTracker = new WebcamMotionTracker();
        this.webcamTracker.start();
    }

    //Call from simpleUpdate(tpf) each frame
    public void update(float tpf) {
        if (tpf <= 0f) return;

        float target = webcamTracker.getMotionLevel(); // 0..1

        // simple exponential smoothing
        float tau = smoothingTime;
        float alpha = 1f - FastMath.exp(-tpf / tau);

        expression = FastMath.interpolateLinear(alpha, expression, target);
    }

    //Smoothed motion intensity in [0, 1]
    public float getMotionAmount() {
        return expression;
    }
}
