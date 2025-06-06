package cat.ella.kissui.component;

import cat.ella.kissui.color.Colors;
import cat.ella.kissui.color.KColor;
import cat.ella.kissui.data.FrameBuffer;
import cat.ella.kissui.render.Renderer;
import cat.ella.kissui.unit.Vector2;

public class Drawn extends Scrollable implements Cloneable {

    public Drawn(Component[] children, Vector2 at, Vector2 size, Vector2 visibleSize, boolean canBeFocused) {
        super(at, size);

        for (Component child : children) {
            child.setParent(this);
        }

    }

//    @Override
//    public Vector2 getVisibleSize() {
//        return super.getVisibleSize().mul(new Vector2(scaleX, scaleY));
//    }
//
//    @Override
//    public void setVisibleSize(Vector2 value) {
//        super.setVisibleSize(value);
//    }
//
//    public Renderer getRenderer() {
//        return kissUI.getRenderer();
//    }

    private FrameBuffer framebuffer;

    private synchronized void setFramebuffer(FrameBuffer framebuffer) {
        this.framebuffer = framebuffer;
    }

    private short fbc = 0;

    private KColor _color = null;

    public void setColor(KColor value) {
        this._color = value;
    }

    public KColor getColor() {
        if (_color == null) {
            throw new IllegalStateException("Color is not initialized");
        }
        return _color;
    }

    public KColor getColorOrNull() {
        return _color;
    }

    private Colors.Palette _palette;

    public Colors.Palette getPalette() {
        if (_palette == null) {
            throw new IllegalStateException("Palette is not initialized");
        }
        return _palette;
    }

//    public void setPalette(Colors.Palette value) {
//        _palette = value;
//        if (_color instanceof KColor.Mut) {
//            ((KColor.Mut) _color).recolor(value.get(inputState));
//        } else {
//            _color = value.get(inputState);
//        }
//    }

    private boolean needsRedraw = true;

    public boolean isNeedsRedraw() {
        return needsRedraw;
    }

//    public void setNeedsRedraw(boolean value) {
//        if (value && !this.needsRedraw) {
//            if (_parent instanceof Drawn) {
//                ((Drawn) _parent).setNeedsRedraw(true);
//            }
//        }
//        this.needsRedraw = value;
//    }





    @Override
    public Drawn clone() {
        try {
            return (Drawn) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void render(Renderer renderer) {
    }
}
