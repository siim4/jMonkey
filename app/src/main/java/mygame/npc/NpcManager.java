package mygame.npc;

import com.jme3.anim.AnimComposer;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mygame.util.AnimUtils;

import java.util.ArrayList;
import java.util.List;

public class NpcManager {

    public record Entry(String id, String display, Npc npc) {}

    private final AssetManager assets;
    private final Node root;
    private final List<Entry> npcs = new ArrayList<>();

    public NpcManager(AssetManager assets, Node root) {
        this.assets = assets;
        this.root = root;
    }

    public void spawn(
            String id,
            String display,
            String modelPath,
            float x,
            float z,
            float scale,
            float wanderHalfSize
    ) {
        Spatial s = assets.loadModel(modelPath);
        AnimComposer ac = AnimUtils.findComposer(s);
        AnimUtils.printActions(ac, id);


       if (ac != null) {
    var clips = ac.getAnimClipsNames();

    String preferred = "walk";
    String chosen = clips.contains(preferred)
            ? preferred
            : (clips.isEmpty() ? null : clips.iterator().next());

    if (chosen != null) {
        System.out.println(id + " using anim: " + chosen);
        ac.setCurrentAction(chosen);
    } else {
        System.out.println(id + " has NO animations");
    }
}

        Npc npc = new Npc(s, ac)
                .at(new Vector3f(x, 0f, z), scale)
                .bounds(wanderHalfSize);

        npc.attachTo(root);
        npcs.add(new Entry(id, display, npc));
    }

    public Entry getNearestNpc(Vector3f playerPos, float radius) {
        float best = radius * radius;
        Entry bestNpc = null;

        for (Entry e : npcs) {
            Vector3f pos = e.npc().getSpatial().getLocalTranslation();
            float dx = playerPos.x - pos.x;
            float dz = playerPos.z - pos.z;
            float d2 = dx * dx + dz * dz;

            if (d2 <= best) {
                best = d2;
                bestNpc = e;
            }
        }
        return bestNpc;
    }

    public void update(float tpf) {
        for (Entry e : npcs) {
            e.npc().update(tpf);
        }
    }
}
