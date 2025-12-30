package mygame.util;

import com.jme3.anim.AnimComposer;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public final class AnimUtils {
    private AnimUtils() {}

    public static AnimComposer findComposer(Spatial s) {
        if (s == null) return null;
        AnimComposer ac = s.getControl(AnimComposer.class);
        if (ac != null) return ac;
        if (s instanceof Node) {
            for (Spatial c : ((Node) s).getChildren()) {
                AnimComposer sub = findComposer(c);
                if (sub != null) return sub;
            }
        }
        return null;
    }
}
