package cat.ella.kissui.render;

import cat.ella.kissui.KissUI;

import java.util.Optional;

public abstract class Window {

    public Window(int height, int width) {
        this(height, width, 1);
    }

    public Window(int width, int height, float aspectRatio) {

    }

    public abstract Window open(KissUI Kui);

    public abstract void close();

    public abstract void preRender();

    public abstract void postRender();

    public abstract Optional<String> getClipboard();

    public abstract void setClipboard(String text);

    public abstract String getKeyName(int key);
}
