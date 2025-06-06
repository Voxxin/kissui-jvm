package cat.ella.kissui.test;

import cat.ella.kissui.KissUI;
import cat.ella.kissui.color.KColor;
import cat.ella.kissui.component.Component;
import cat.ella.kissui.nanovg.GLFWWindow;
import cat.ella.kissui.nanovg.NanoVGManager;

import java.awt.*;

public class Test {
    public static GLFWWindow window = new GLFWWindow("Test", 600, 800);
    public static NanoVGManager renderer = new NanoVGManager();
    public static KissUI UI = new KissUI(
            new Component[0],
            renderer
    );

    public static void main(String[] args) {
        window.open(UI);
//        renderer.push();
        renderer.line(
                0.0f, 0.0f,
                50.0f, 20.0f,
                new KColor(1f, 1f, 1f, 1f),
                1.0f
        );
//        renderer.pop();
    }
}
