package mygame.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import mygame.player.PlayerController;

public class InputMapper {
    public InputMapper(InputManager input, PlayerController player) {
        input.addMapping("Left",  new KeyTrigger(KeyInput.KEY_A));
        input.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        input.addMapping("Up",    new KeyTrigger(KeyInput.KEY_W));
        input.addMapping("Down",  new KeyTrigger(KeyInput.KEY_S));

        input.addListener(new ActionListener() {
         @Override
public void onAction(String name, boolean isPressed, float tpf) {
    switch (name) {
        case "Left"  -> player.left  = isPressed;
        case "Right" -> player.right = isPressed;
        case "Up"    -> player.up    = isPressed;
        case "Down"  -> player.down  = isPressed;
        default      -> { /* no action */ }
    }
}

        }, "Left", "Right", "Up", "Down");
    }
}
