package cat.ella.kissui;

import cat.ella.kissui.color.KColor;
import cat.ella.kissui.component.Component;
import cat.ella.kissui.component.Drawn;
import cat.ella.kissui.data.FontFamily;
import cat.ella.kissui.data.KImage;
import cat.ella.kissui.input.InputManager;
import cat.ella.kissui.render.Renderer;
import cat.ella.kissui.render.Window;
import cat.ella.kissui.unit.Vector2;
import cat.ella.kissui.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class KissUI {
    protected Window window = null;
    public static final Logger LOGGER = LogManager.getLogger("KISSUI/");
    public final Renderer renderer;
    public final Component[] components;
    public Vector2 size = new Vector2(0F, 0F);
    public Drawn main;
    public final InputManager inputManager = new InputManager();
    public final FontFamily defaultFont = new FontFamily("JetBrainsMono", "assets/kissui/fonts/JetBrainsMono");
    public final KImage defaultImage = new KImage("/assets/kissui/images/kissui.png", new Vector2(900F, 900F));

    public KissUI(Component[] components, Renderer renderer) {
        this.components = components;
        this.renderer = renderer;
        this.main = new Drawn(components, new Vector2(0f, 0f), new Vector2(0F, 0F), new Vector2(0F, 0F), false);
        renderer.init();
    }

    public void resize(Float newHeight, Float newWidth) {
        this.resize(newHeight, newWidth, false);
    }

    public void resize(Float newHeight, Float newWidth, boolean force) {
        if (newWidth == 0f || newHeight == 0f) {
            LOGGER.error("Cannot resize to zero size: " + newWidth + "x" + newHeight);
            return;
        }

        this.size = new Vector2(newWidth, newHeight);
//        main.setSize(size);
    }

    public void render() {
        if (window == null) return;

        window.preRender();
        renderer.beginFrame(size.x(), size.y(), 1.0f);
        main.render(renderer);

        renderer.endFrame();
        window.postRender();
    }

    public void cleanup() {
        renderer.cleanup();
    }

    public void setWindow(Window window) {
        this.window = window;
    }
}