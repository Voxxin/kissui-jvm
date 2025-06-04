package cat.ella.kissui;

import cat.ella.kissui.component.util.Component;
import cat.ella.kissui.render.Renderer;

public class KissUI {
    /* TODO:
    * 0 - Setup testing enviroment to try out our library
    * 0 - Make KissUI creatable, and usable with other functions
    * 0 - Create renderer that interacts with NanoVG rendering class
    * 0 - Make text rendering possible
    * 0 - Start to implement components & layout
    */

    public KissUI(Component[] components, Renderer renderer) {
        renderer.init();
    }

}
