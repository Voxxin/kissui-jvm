package cat.ella.kissui.color;

import java.awt.*;

public class KColor {
    public KColor() {}

    private static Float hue, saturation, brightness, alpha;
    private static int argb;

    public int getRgba() {
        return (argb << 8) | ((argb >>> 24) & 0xFF);
    }

    public boolean isTransparent() {
        return alpha == 0f;
    }

    public int getRed() {
        return (argb >>> 16) & 0xFF;
    }

    public int getGreen() {
        return (argb >>> 8) & 0xFF;
    }

    public int getBlue() {
        return argb & 0xFF;
    }

    public int getAlpha() {
        return (argb >>> 24) & 0xFF;
    }

    public interface Dynamic {
        boolean update(long deltaTimeNanos);
    }

    public interface AbleToMutate {
        AbleToMutate recolor(KColor to);
    }

    public static class Static extends KColor {
        public final Float hue, saturation, brightness, alpha;

        public final int argb;

        public Static(Float hue, Float saturation, Float brightness, Float alpha) {
            this.hue = hue % 360f;
            this.saturation = Math.min(Math.max(saturation, 0f), 1f);
            this.brightness = Math.min(Math.max(brightness, 0f), 1f);
            this.alpha = Math.min(Math.max(alpha, 0f), 1f);
            this.argb = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
        }
    }
}
