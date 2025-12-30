package mygame.art;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PanelField {
    private final List<PanelMorph> panels = new ArrayList<>();
    private final Random rng = new Random();

    public PanelField() {}

    public PanelMorph addPanel(AssetManager assets, List<String> paths,
                               float w, float h, float x, float y, float z) {
        PanelMorph p = new PanelMorph(assets, paths, w, h).at(x, y, z);
        panels.add(p);
        return p;
    }

    public void addRandomPanels(AssetManager assets, List<String> paths,
                                float w, float h, int count, float halfArea, float baseY) {
        for (int i = 0; i < count; i++) {
            float x = (rng.nextFloat()*2f - 1f) * halfArea;
            float z = (rng.nextFloat()*2f - 1f) * halfArea;
            float y = baseY; // Ground position.
            PanelMorph p = new PanelMorph(assets, paths, w, h).at(x, y, z);
           
            p.fadeDuration(6f + rng.nextFloat()*6f);
            panels.add(p);
        }
    }

    public void attachTo(Node root) {
        for (PanelMorph p : panels) root.attachChild(p.getNode());
    }

    public void update(float tpf, Camera cam) {
        for (PanelMorph p : panels) p.update(tpf, cam);
    }
}
