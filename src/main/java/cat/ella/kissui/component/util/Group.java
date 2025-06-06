package cat.ella.kissui.component.util;

import cat.ella.kissui.component.Component;
import cat.ella.kissui.component.Drawn;
import cat.ella.kissui.unit.Vector2;

public class Group extends Drawn {

    public Group(Component[] children, Vector2 at, Vector2 size, Vector2 visibleSize){
        super(children, at, size, visibleSize, false);
    };
}
