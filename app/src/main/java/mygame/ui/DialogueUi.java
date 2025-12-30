package mygame.ui;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.material.RenderState;

import java.util.ArrayList;
import java.util.List;


public class DialogueUi {

    public interface SubmitListener {
        void onSubmit(String npcId, String text);
    }

    private final SimpleApplication app;
    private final AssetManager assets;
    private final Node guiNode;
    private final SubmitListener submitListener;

    private final Node root = new Node("DialogueUI");
    private Geometry background;
    private BitmapFont font;
    private BitmapText npcNameText;
    private BitmapText bodyText;
    private BitmapText inputText;

    private boolean active = false;
    private boolean typingMode = true;    // true = player typing, false = showing NPC reply

    private String npcId = "";
    private String npcDisplayName = "";

    // for NPC reply
    private String fullReplyText = "";
    private List<String> wrappedReplyLines = new ArrayList<>();
    private float revealChars = 0f;
    private float charsPerSecond = 40f;

    // for player input
    private StringBuilder typedBuffer = new StringBuilder();

    private RawInputListener keyListener;

    public DialogueUi(SimpleApplication app, SubmitListener submitListener) {
        this.app = app;
        this.assets = app.getAssetManager();
        this.guiNode = app.getGuiNode();
        this.submitListener = submitListener;

        initGraphics();
        initInputListener();
    }

    // Open dialogue panel for a specific NPC. 
    public void openForNpc(String npcId, String displayName) {
        if (active) {
            close();
        }
        this.npcId = npcId;
        this.npcDisplayName = displayName;

        typingMode = true;
        typedBuffer.setLength(0);
        fullReplyText = "";
        wrappedReplyLines.clear();
        revealChars = 0f;

        npcNameText.setText(displayName);
        bodyText.setText("");
        inputText.setText("> ");

        guiNode.attachChild(root);
        app.getInputManager().addRawInputListener(keyListener);
        active = true;
    }

    //Show the NPC's reply with typewriter effect.
     
    public void showNpcReply(String reply) {
        typingMode = false;
        fullReplyText = reply != null ? reply : "";
        wrappedReplyLines = wrapText(fullReplyText, bodyText, getInnerWidth());
        revealChars = 0f;
        updateBodyText(); // start from empty
    }

    public boolean isActive() {
        return active;
    }

   
    public void update(float tpf) {
        if (!active) return;

        if (!typingMode) {
            // typewriter reveal for NPC reply
            revealChars += charsPerSecond * tpf;
            updateBodyText();
        }

       
        updateInputText();
    }

    // Close/hide the panel.
    public void close() {
        if (!active) return;
        active = false;
        guiNode.detachChild(root);
        app.getInputManager().removeRawInputListener(keyListener);
    }

    // Graphics

    private void initGraphics() {
        int sw = app.getCamera().getWidth();
        int sh = app.getCamera().getHeight();

        float margin = 30f;
        float boxHeight = 250f;
        float x = margin;
        float y = margin;
        float w = sw - margin * 2;

        // background quad
        Quad quad = new Quad(w, boxHeight);
        background = new Geometry("dialog_bg", quad);
        Material mat = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.75f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        background.setMaterial(mat);
        background.setQueueBucket(RenderQueue.Bucket.Transparent);
        background.setLocalTranslation(x, y, 0);

        // font
        font = assets.loadFont("Interface/Fonts/Default.fnt");

        // NPC name
        npcNameText = new BitmapText(font);
        npcNameText.setSize(24f);
        npcNameText.setColor(ColorRGBA.White.mult(0.9f));
        npcNameText.setLocalTranslation(x + 20f, y + boxHeight - 20f, 1);

        // body text for NPC reply
        bodyText = new BitmapText(font);
        bodyText.setSize(18f);
        bodyText.setColor(ColorRGBA.White);
        bodyText.setLocalTranslation(x + 20f, y + boxHeight - 50f, 1);

        // input text line
        inputText = new BitmapText(font);
        inputText.setSize(18f);
        inputText.setColor(ColorRGBA.Cyan.mult(0.9f));
        inputText.setLocalTranslation(x + 20f, y + 30f, 1);

        root.attachChild(background);
        root.attachChild(npcNameText);
        root.attachChild(bodyText);
        root.attachChild(inputText);
    }

    private float getInnerWidth() {
        int sw = app.getCamera().getWidth();
        float margin = 30f;
        float innerMargin = 40f;
        return (sw - margin * 2) - innerMargin;
    }


    private void initInputListener() {
        keyListener = new RawInputListener() {
            @Override public void onKeyEvent(KeyInputEvent evt) {
                if (!active || !evt.isPressed()) return;

                int keyCode = evt.getKeyCode();
                char c = evt.getKeyChar();

                // ESC closes
                if (keyCode == com.jme3.input.KeyInput.KEY_ESCAPE) {
                    close();
                    return;
                }

                // in reply-view mode: Enter or Space closes
                if (!typingMode) {
                    if (keyCode == com.jme3.input.KeyInput.KEY_RETURN ||
                        keyCode == com.jme3.input.KeyInput.KEY_SPACE) {
                        close();
                    }
                    return;
                }

                // typing mode:
                if (keyCode == com.jme3.input.KeyInput.KEY_BACK) {
                    if (typedBuffer.length() > 0) {
                        typedBuffer.deleteCharAt(typedBuffer.length() - 1);
                    }
                    return;
                }

                if (keyCode == com.jme3.input.KeyInput.KEY_RETURN) {
                    String msg = typedBuffer.toString().trim();
                    if (!msg.isEmpty() && submitListener != null) {
                        // send to game
                        submitListener.onSubmit(npcId, msg);
                    }
                    // clear input while we wait for reply
                    typedBuffer.setLength(0);
                    return;
                }

                // add letters / space / punctuation
                if (c >= 32 && c != 127) {
                    typedBuffer.append(c);
                }
            }

    
            @Override public void beginInput() {}
            @Override public void endInput() {}
            @Override public void onJoyAxisEvent(com.jme3.input.event.JoyAxisEvent evt) {}
            @Override public void onJoyButtonEvent(com.jme3.input.event.JoyButtonEvent evt) {}
            @Override public void onMouseMotionEvent(com.jme3.input.event.MouseMotionEvent evt) {}
            @Override public void onMouseButtonEvent(com.jme3.input.event.MouseButtonEvent evt) {}
            @Override public void onTouchEvent(com.jme3.input.event.TouchEvent evt) {}
        };
    }

   

    private List<String> wrapText(String text, BitmapText bmp, float width) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String w : words) {
            String test = current.length() == 0
                    ? w
                    : current.toString() + " " + w;

            bmp.setText(test);
            float lineWidth = bmp.getLineWidth();

            if (lineWidth <= width || current.length() == 0) {
                current.setLength(0);
                current.append(test);
            } else {
                lines.add(current.toString());
                current.setLength(0);
                current.append(w);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private void updateBodyText() {
        if (typingMode || fullReplyText == null) {
            bodyText.setText("");
            return;
        }

        int maxChars = Math.min((int)revealChars, fullReplyText.length());
        if (maxChars <= 0) {
            bodyText.setText("");
            return;
        }

        String visible = fullReplyText.substring(0, maxChars);
        List<String> visibleLines = wrapText(visible, bodyText, getInnerWidth());
        StringBuilder out = new StringBuilder();
        for (String line : visibleLines) {
            out.append(line).append("\n");
        }
        bodyText.setText(out.toString());
    }

    private void updateInputText() {
        if (!typingMode) {
            inputText.setText(""); // hide when only showing reply
            return;
        }
        String base = "> " + typedBuffer;
        // blinking caret
        boolean showCaret = (System.currentTimeMillis() / 400) % 2 == 0;
        inputText.setText(showCaret ? base + "_" : base + " ");
    }
}
