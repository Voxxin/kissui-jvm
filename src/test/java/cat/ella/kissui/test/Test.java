package cat.ella.kissui.test;

import cat.ella.kissui.KissUI;
import cat.ella.kissui.component.Component;
import cat.ella.kissui.nanovg.GLFWWindow;
import cat.ella.kissui.nanovg.NanoVGManager;

public class Test {
    public static GLFWWindow window = new GLFWWindow("Test", 600, 800);
    public static NanoVGManager renderer = new NanoVGManager();
    public static KissUI UI = new KissUI(
            new Component[0],
            renderer
    );

    public static void main(String[] args) {
        window.open(UI);
    }
}
