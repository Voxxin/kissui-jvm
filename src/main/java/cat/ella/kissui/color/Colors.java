package cat.ella.kissui.color;

import java.awt.*;

public interface Colors {
    Page getPage();
    Brand getBrand();
    OnBrand getOnBrand();
    State getState();
    Component getComponent();
    Text getText();

    class Page {
        public final Palette bg;
        public final Color bgOverlay;
        public final Palette fg;
        public final Color fgOverlay;
        public final Color border20;
        public final Color border10;
        public final Color border5;

        public Page(Palette bg, Color bgOverlay, Palette fg, Color fgOverlay, Color border20, Color border10, Color border5) {
            this.bg = bg;
            this.bgOverlay = bgOverlay;
            this.fg = fg;
            this.fgOverlay = fgOverlay;
            this.border20 = border20;
            this.border10 = border10;
            this.border5 = border5;
        }
    }

    public static class Brand {
        public final Palette fg;
        public final Palette accent;

        public Brand(Palette fg, Palette accent) {
            this.fg = fg;
            this.accent = accent;
        }
    }

    class OnBrand {
        public final Palette fg;
        public final Palette accent;

        public OnBrand(Palette fg, Palette accent) {
            this.fg = fg;
            this.accent = accent;
        }
    }

    class State {
        public final Palette danger;
        public final Palette warning;
        public final Palette success;

        public State(Palette danger, Palette warning, Palette success) {
            this.danger = danger;
            this.warning = warning;
            this.success = success;
        }
    }

    class Component {
        public final Palette bg;
        public final Color bgDeselected;

        public Component(Palette bg, Color bgDeselected) {
            this.bg = bg;
            this.bgDeselected = bgDeselected;
        }
    }

    class Text {
        public final Palette primary;
        public final Palette secondary;

        public Text(Palette primary, Palette secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }
    }

    class Palette {
        public final Color normal;
        public final Color hovered;
        public final Color pressed;
        public final Color disabled;

        public Palette(Color normal, Color hovered, Color pressed, Color disabled) {
            this.normal = normal;
            this.hovered = hovered;
            this.pressed = pressed;
            this.disabled = disabled;
        }

        public Color get(byte state) {
            return switch (state) {
                case INPUT_HOVERED -> hovered;
                case INPUT_PRESSED -> pressed;
                default -> normal;
            };
        }
    }

    byte INPUT_NONE = 0;
    byte INPUT_HOVERED = 1;
    byte INPUT_PRESSED = 2;
}

