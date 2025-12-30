package mygame.sense;

import com.github.sarxos.webcam.Webcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
//Computes webcam motion into level 0-1 and is fed to ExpressionTracker
public class WebcamMotionTracker {

    private Webcam webcam;
    private Thread worker;
    private volatile boolean running = false;

    // 0..1 measure of motion
    private volatile float motionLevel = 0f;

    // previous frame grayscale
    private byte[] prevGray = null;
    private int prevWidth = 0;
    private int prevHeight = 0;

    public WebcamMotionTracker() {
        try {
            webcam = Webcam.getDefault();
            if (webcam != null) {
                // light resolution
                webcam.setViewSize(new Dimension(320, 240));
            } else {
                System.out.println("[WebcamMotionTracker] No webcam found.");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            webcam = null;
        }
    }

    public void start() {
        if (webcam == null) {
            System.out.println("[WebcamMotionTracker] Cannot start, no webcam.");
            return;
        }
        if (running) return;

        webcam.open();
        running = true;

        worker = new Thread(this::runLoop, "WebcamMotionTracker");
        worker.setDaemon(true);
        worker.start();
    }

    public void stop() {
        running = false;
        if (worker != null) {
            try {
                worker.join(500);
            } catch (InterruptedException ignored) {}
        }
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    private void runLoop() {
        final long sleepMillis = 80; // ~12.5 FPS

        while (running) {
            try {
                BufferedImage img = webcam.getImage();
                if (img != null) {
                    computeMotion(img);
                }
                Thread.sleep(sleepMillis);
            } catch (Exception e) {
                e.printStackTrace();
                // Don't kill the loop on a single error
            }
        }
    }

    private void computeMotion(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int totalPixels = w * h;

        if (totalPixels == 0) return;

        if (prevGray == null || prevWidth != w || prevHeight != h) {
            prevGray = new byte[totalPixels];
            prevWidth = w;
            prevHeight = h;
            // initialize prevGray from current image, but motion stays 0
            fillGray(img, prevGray);
            motionLevel = 0f;
            return;
        }

        byte[] currentGray = new byte[totalPixels];
        fillGray(img, currentGray);

        // Simple frame difference
        int changed = 0;
        int threshold = 18; // tweak for sensitivity

        for (int i = 0; i < totalPixels; i++) {
            int diff = (currentGray[i] & 0xFF) - (prevGray[i] & 0xFF);
            if (diff < 0) diff = -diff;
            if (diff > threshold) {
                changed++;
            }
        }

        System.arraycopy(currentGray, 0, prevGray, 0, totalPixels);

        float ratio = (float) changed / (float) totalPixels;
        // make it a bit more punchy 
        ratio = Math.min(1f, ratio * 8f);

        motionLevel = ratio;
    }

    private void fillGray(BufferedImage img, byte[] outGray) {
        int w = img.getWidth();
        int h = img.getHeight();
        int idx = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb) & 0xFF;
                int gray = (r + g + b) / 3;
                outGray[idx++] = (byte) gray;
            }
        }
    }

    // Returns a value in [0, 1] representing current motion.
   
    public float getMotionLevel() {
        return motionLevel;
    }
}
