package cat.ella.kissui.nanovg;

import cat.ella.kissui.KissUI;
import cat.ella.kissui.render.Window;

import java.util.Optional;

abstract class GLFWWindow extends Window {

    public GLFWWindow(String title, int height, int width) {
        this(title, height, width, false, true, true);
    }

    public GLFWWindow(String title, int height, int width, boolean openGL12, boolean resizable, boolean decorated) {
        super(height, width);
    }
}
