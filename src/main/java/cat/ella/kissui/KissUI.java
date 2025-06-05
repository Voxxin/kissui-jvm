package cat.ella.kissui;

import cat.ella.kissui.component.Component;
import cat.ella.kissui.component.Drawn;
import cat.ella.kissui.render.Renderer;
import cat.ella.kissui.render.Window;
import cat.ella.kissui.unit.Vector2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KissUI {
    public Window window = null;
    public Logger LOGGER = LogManager.getLogger("KISSUI/");
    public final Renderer renderer;
    public final Component[] components;
    public Vector2 size = Vector2.Constants.of(0, 0);
    public Drawn main;

    public KissUI(Component[] components, Renderer renderer) {
        this.components = components;
        this.renderer = renderer;
        this.main = new Drawn(components, Vector2.Constants.of(0, 0), Vector2.Constants.of(0, 0), Vector2.Constants.of(0, 0), false);
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

        this.size = Vector2.Constants.of(newWidth, newHeight);
//        main.setSize(size);
    }

    public void render() {
        if (window == null) return;

        window.preRender();
        renderer.beginFrame(size.x(), size.y(), 1.0f);
//        main.render(renderer);
        renderer.endFrame();
        window.postRender();
    }

    public void cleanup() {
        renderer.cleanup();
    }
}