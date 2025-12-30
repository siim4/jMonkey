package mygame.util;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;

public final class SpriteSheetUvAnimator {

    private SpriteSheetUvAnimator() {}

     // Apply a spritesheet frame to all Geometry type meshes under a Spatial. Glitchy but stable
    public static void applyFrame(Spatial model, int frame, int totalFrames) {
        if (model == null || totalFrames <= 0) return;

        int safeFrame = Math.floorMod(frame, totalFrames);
        float frameWidth = 1f / totalFrames;
        float uMin = safeFrame * frameWidth;
        float uMax = uMin + frameWidth;

        model.depthFirstTraversal(s -> {
            if (s instanceof Geometry g) {
                applyFrame(g.getMesh(), uMin, uMax);
            }
        });
    }

   // Apply a spritesheet frame to a single Mesh
    public static void applyFrame(Mesh mesh, float uMin, float uMax) {
        if (mesh == null) return;

        int vc = mesh.getVertexCount();
        if (vc <= 0) return;

        float[] uvs = new float[vc * 2];

        for (int i = 0; i < vc; i++) {
            int c = i % 4;
            float u = (c == 1 || c == 3) ? uMax : uMin;
            float v = (c >= 2) ? 1f : 0f;

            uvs[i * 2] = u;
            uvs[i * 2 + 1] = v;
        }

        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, uvs);
        mesh.updateBound();
    }
}
