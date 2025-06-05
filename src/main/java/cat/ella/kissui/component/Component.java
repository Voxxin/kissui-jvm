package cat.ella.kissui.component;

import cat.ella.kissui.unit.Vector2;

public class Component {
    public Component parent = null;

    public Component(Vector2 at, Vector2 size) {

    }

    public void setParent(Component parent) {
        this.parent = parent;
    }
}
