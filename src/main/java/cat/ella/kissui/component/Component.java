package cat.ella.kissui.component;

import cat.ella.kissui.KissUI;
import cat.ella.kissui.render.Renderer;
import cat.ella.kissui.unit.Vector2;
import cat.ella.kissui.unit.Vector4;
import cat.ella.kissui.util.MathHelper;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

public class Component {
    protected KissUI UI;
    protected ArrayList<Component> children = null;
    protected Component _parent = null;
    protected float _x;
    protected float _y;
    protected float width;
    protected float height;
    protected Vector4 _padding = null;
    protected boolean renderAble = true;
    protected boolean isEnabled = true;
    protected String name = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());


    private ArrayList<Runnable> operations = null;


    public Component(Vector2 at, Vector2 size) {

    }

    public boolean isInitialized() {
        return UI != null;
    }


    public Component getParent() {
        if (_parent == null) throw new IllegalStateException("cannot move outside of component tree");
        return _parent;
    }

    public void setParent(Component value) {
        _parent = value;
    }

    public float getX() {
        return _x;
    }

    public void setX(float value) {
        if (value == _x) return;
        float delta = value - _x;
        _x = value;

        for (Component child : children) {
            child.setX(child.getX() + delta);
        }
    }

    public void setY(float value) {
        if (value == _y) return;
        float delta = value - _y;
        _y = value;

        for (Component child : children) {
            child.setY(child.getY() + delta);
        }
    }

    public float getY() {
        return _y;
    }

    public Vector2 getAt() {
        return new Vector2(_x, _y);
    }

    public void setAt(Vector2 value) {
        setX(value.x());
        setY(value.y());
    }

    public Vector2 getScreenAt() {
        return getAt();
    }

    public void at(int index, float value) {
        switch (index) {
            case 0 -> setX(value);
            case 1 -> setY(value);
            default -> throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    public Vector2 getSize() {
        return new Vector2(width, height);
    }

    public void setSize(Vector2 value) {
        width = value.x();
        height = value.y();
    }

    public boolean isSizeValid() {
        return width > 0f && height > 0f;
    }

    public Vector2 calculateSize() {
        return Vector2.ZERO;
    }

    public void size(int index, float value) {
        switch (index) {
            case 0 -> width = value;
            case 1 -> height = value;
            default -> throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    public void enable(boolean value) {
        this.isEnabled = value;
    }

    public Vector2 getVisibleSize() {
        return getSize();
    }

    public void fixVisibleSize() {
        Vector2 vs = getVisibleSize();
        width = Math.max(width, vs.x());
        height = Math.max(height, vs.y());
    }

    public Vector4 getPadding() {
        return _padding != null ? _padding : Vector4.ZERO;
    }

    public void setPadding(Vector4 value) {
        _padding = value;
    }

    protected byte layoutFlags = (byte) (((!isSizeValid() ? 0b00000000 : 0b00000010) |
            ((_x == 0f && _y == 0f) ? 0b00000000 : 0b00000100)));

    public boolean isRawResize() {
        return (layoutFlags & 0b00000001) == 0b00000001;
    }

    public void setRawResize(boolean value) {
        layoutFlags = value ? (byte) (layoutFlags | 0b00000001) : (byte) (layoutFlags & 0b11111110);
    }

    public boolean isLayoutIgnored() {
        return (layoutFlags & 0b00001000) == 0b00001000;
    }

    public void setLayoutIgnored(boolean value) {
        layoutFlags = value ? (byte) (layoutFlags | 0b00001000) : (byte) (layoutFlags & 0b11110111);
    }

    public boolean isCreatedWithSetPosition() {
        return (layoutFlags & 0b00000100) == 0b00000100;
    }

    public boolean isCreatedWithSetSize() {
        return (layoutFlags & 0b00000010) == 0b00000010;
    }

    public boolean isPositioned() {
        return (layoutFlags & 0b00010000) == 0b00010000;
    }

    public boolean isRenderAble() {
        return renderAble && isSizeValid();
    }

    public void setRenderAble(boolean value) {
        renderAble = value;
    }

    public boolean setup(KissUI UI) {
        if (isInitialized()) return false;
        this.UI = UI;
        if (children != null) {
            for (Component child : children) {
                child.setup(UI);
            }
        }
        position();
        return true;
    }

    public void draw() {
    }

    public void rescale(Vector2 scale) {
        rescale(scale.x(), scale.y());
    }

    public void rescale(float scaleX, float scaleY) {
        if (isRawResize()) {
            rescale0(scaleX, scaleY, true);
        } else {
            float s = MathHelper.smallestInteger(scaleX, scaleY);
            rescale0(s, s, true);
        }
    }

    public void rescale0(Vector2 scale, boolean withChildren) {
        rescale0(scale.x(), scale.y(), withChildren);
    }

    public void rescale0(float scaleX, float scaleY, boolean withChildren) {
        _x *= scaleX;
        _y *= scaleY;
        width *= scaleX;
        height *= scaleY;
        if (withChildren && children != null) {
            for (Component child : children) {
                child.rescale0(scaleX, scaleY, true);
            }
        }
    }

    public void clipChildren() {
        if (children == null) return;
        Vector2 staticPos = getScreenAt();
        Vector2 vs = getVisibleSize();
        _clipChildren(children, staticPos.x(), staticPos.y(), vs.x(), vs.y());
    }

    private void _clipChildren(ArrayList<? extends Component> children, float tx, float ty, float tw, float th) {
        for (Component child : children) {
            boolean p = child.intersects(tx, ty, tw, th);
            child.setRenderAble(p);
            if (child.isRenderAble()) {
                ArrayList<Component> grandChildren = child.children;
                if (grandChildren != null) {
                    child._clipChildren(grandChildren, tx, ty, tw, th);
                }
            }
        }
    }

    public boolean isInside(float x, float y) {
        Vector2 ta = getScreenAt();
        float tx = ta.x();
        float ty = ta.y();
        Vector2 vs = getVisibleSize();
        float tw = vs.x();
        float th = vs.y();
        return x >= tx && x <= tx + tw && y >= ty && y <= ty + th;
    }

    public boolean isInside(Vector2 point) {
        return isInside(point.x(), point.y());
    }

    public boolean intersects(Vector4 box) {
        return intersects(box.x(), box.y(), box.z(), box.w());
    }

    public boolean intersects(float x, float y, float width, float height) {
        Vector2 ta = getScreenAt();
        float tx = ta.x();
        float ty = ta.y();
        Vector2 vs = getVisibleSize();
        float tw = vs.x();
        float th = vs.y();
        return (x < tx + tw && tx < x + width) && (y < ty + th && ty < y + height);
    }

    public void position() {
//        UI.layoutController.layout(this);
        layoutFlags = (byte) (layoutFlags | 0b00010000);
        clipChildren();
    }

    public void recalculate() {
        if (children == null) return;
        float oldW = width;
        float oldH = height;
        if (!isCreatedWithSetSize()) {
            width = 0f;
            height = 0f;
        }
        position();
        if (!isCreatedWithSetSize()) {
            setX(getX() - (width - oldW) / 2f);
            setY(getY() - (height - oldH) / 2f);
        }
    }

    public Component get(int index) {
        if (children == null) throw new IndexOutOfBoundsException("index: " + index + ", length: 0");
        return children.get(index);
    }

    public Component get(String id) {
        if (children == null) throw new NoSuchElementException("no children on " + this);
        for (Component child : children) {
            if (id.equals(child.name)) return child;
        }
        throw new NoSuchElementException("no child with id " + id);
    }

    public Component get(float x, float y) {
        if (children == null) throw new NoSuchElementException("no children on " + this);
        for (Component child : children) {
            if (child.isInside(x, y)) return child;
        }
        throw new NoSuchElementException("no children on " + this);
    }

    public void set(int oldIndex, Component newComponent) {
        set(get(oldIndex), newComponent);
    }

    public void addChild(Component child, int index, boolean recalculate) {
        if (children == null) children = new ArrayList<>();
        ArrayList<Component> children = this.children;
        if (children == null) throw new ConcurrentModificationException("well, this sucks");
        child.setParent(this);
        child.enable(true);
        for (Component c : children) {
            if (c == child) throw new IllegalStateException("attempted to add the same component twice");
        }
        if (index < 0 || index >= children.size()) {
            children.add(child);
        } else {
            children.add(index, child);
        }
        if (isInitialized()) {
            child.setup(UI);
            if (!child.isCreatedWithSetPosition()) {
                if (recalculate) {
                    recalculate();
                } else {
                    child.setX(child.getX() + getX());
                    child.setY(child.getY() + getY());
                }
            }
//            if (child instanceof Input) {
//                ((Input) child).accept(Event.Lifetime.Added);
//            }
        }
    }

    public void addChild(Component child) {
        addChild(child, -1, true);
    }

    public void addChild(Component... children) {
        for (Component child : children) {
            addChild(child);
        }
    }

    public void removeChild(Component child, boolean recalculate) {
        if (children == null) throw new NoSuchElementException("no children on " + this);
        int i = children.indexOf(child);
        if (i == -1) throw new IllegalArgumentException("component " + child + " is not a child of " + this);
        removeChild(i, recalculate);
    }

    public synchronized void removeChild(int index, boolean recalculate) {
        if (children == null) throw new NoSuchElementException("no children on " + this);
        if (index < 0 || index >= children.size())
            throw new IndexOutOfBoundsException("index: " + index + ", length: " + children.size());
        Component child = children.get(index);
        child._parent = null;
        children.remove(child);
        child.enable(false);
        if (isInitialized()) {
            if (child instanceof Input) {
//                UI.inputManager.drop((Input) child);
//                ((Input) child).accept(Event.Lifetime.Removed);
            }
            if (recalculate) recalculate();
//            if (this instanceof Scrollable) {
//                accept(Event.Mouse.Scrolled);
//            }
        }
    }

    public void set(Component oldComp, Component newComp) {
        ArrayList<Component> children = this.children;
        if (children == null) throw new NoSuchElementException("no children on " + this);
        int index = children.indexOf(oldComp);
        if (index == -1) throw new IllegalArgumentException("component " + oldComp + " is not a child of " + this);

        if (newComp == null) {
            removeChild(index, true);
            return;
        }

        if (!isInitialized()) {
            removeChild(index, true);
            addChild(newComp, index, true);
            return;
        }

        newComp._parent = this;
        newComp.setup(UI);

        Vector2 oldStatic = oldComp.getScreenAt();
        newComp.setX(oldComp.getX() - (oldComp.getX() - oldStatic.x()));
        newComp.setY(oldComp.getY() - (oldComp.getY() - oldStatic.y()));

//        if (newComp instanceof Scrollable) {
//            ((Scrollable) newComp).resetScroll();
//        }

        if (this instanceof Input) {
//            UI.inputManager.drop((Input) this);
        }

        children.add(index, newComp);
    }

    public String debugString() {
        return null;
    }

    protected String extraToString() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(48);
        sb.append(name).append('(');
        String ex = extraToString();
        if (ex != null) {
            sb.append(ex).append(", ");
        }
        if (isInitialized()) {
            if (isSizeValid()) {
                sb.append(_x).append('x').append(_y).append(", ").append(getSize());
            } else {
                sb.append("being initialized");
            }
        } else {
            sb.append("not initialized");
        }
        sb.append(')');
        return sb.toString();
    }


    public void render(Renderer renderer) {

    }
}
