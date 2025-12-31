package mygame.world;

import com.jme3.math.Vector3f;

public final class MapPoints {
    private MapPoints() {}

    public static final Vector3f PLAYER_SPAWN = new Vector3f(0, 0, 0);
    public static final Vector3f MAZE_ENTRY   = new Vector3f(0, 0, 0);
    public static final Vector3f MAZE_EXIT    = new Vector3f(12, 0, -8);

    public static final Vector3f NPC_1 = new Vector3f(-8, 0, -6);
    public static final Vector3f NPC_2 = new Vector3f( 6, 0,  3);
    public static final Vector3f NPC_3 = new Vector3f(-3, 0, 10);
    public static final Vector3f NPC_4 = new Vector3f(12, 0, -9);
}
