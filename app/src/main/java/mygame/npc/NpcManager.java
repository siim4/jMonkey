package mygame.npc;

import com.jme3.anim.AnimComposer;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mygame.util.AnimUtils;

import java.util.ArrayList;
import java.util.List;

//Spawn and update a small crowd of NPCs.
public class NpcManager {
    private final AssetManager assets;
    private final Node root;
    private final List<Npc> npcs = new ArrayList<>();

    public NpcManager(AssetManager assets, Node root) {
        this.assets = assets;
        this.root = root;
    }

    public Npc spawn(String modelPath, float x, float z, float scale, float wanderHalfSize) {
        Spatial s = assets.loadModel(modelPath);
        AnimComposer ac = AnimUtils.findComposer(s);
        Npc npc = new Npc(s, ac).at(new com.jme3.math.Vector3f(x, 0f, z), scale).bounds(wanderHalfSize);
        npc.attachTo(root);
        npcs.add(npc);
        return npc;
    }

    public void update(float tpf) {
        for (Npc npc : npcs) npc.update(tpf);
    }
}
