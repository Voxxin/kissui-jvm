package cat.ella.kissui.color;

import java.awt.*;

public record KColor(Float hue, Float saturation, Float brightness, Float alpha) {
    public static final KColor WHITE = new KColor(0f, 0f, 1f, 1f);
    public static final KColor BLACK = new KColor(0f, 0f, 0f, 1f);
    public static final KColor RED = new KColor(0f, 1f, 1f, 1f);
    public static final KColor GREEN = new KColor(120f, 1f, 1f, 1f);
    public static final KColor BLUE = new KColor(240f, 1f, 1f, 1f);
    public static final KColor TRANSPARENT = new KColor(0f, 0f, 0f, 0f);

    public int getArgb() {
        float h = hue % 360f;
        float s = Math.min(Math.max(saturation, 0f), 1f);
        float b = Math.min(Math.max(brightness, 0f), 1f);
        float a = Math.min(Math.max(alpha, 0f), 1f);

        float hueFraction = h / 360f;
        int rgb = Color.HSBtoRGB(hueFraction, s, b);
        int alphaInt = (int) (a * 255) & 0xFF;
        return (alphaInt << 24) | (rgb & 0x00FFFFFF);
    }

    public int getRgba() {
        int argb = getArgb();
        return (argb << 8) | ((argb >>> 24) & 0xFF);
    }

    public boolean isTransparent() {
        return alpha == 0f;
    }

    public int getRed() {
        int argb = getArgb();
        return (argb >>> 16) & 0xFF;
    }

    public int getGreen() {
        int argb = getArgb();
        return (argb >>> 8) & 0xFF;
    }

    public int getBlue() {
        int argb = getArgb();
        return argb & 0xFF;
    }

    public int getAlpha() {
        int argb = getArgb();
        return (argb >>> 24) & 0xFF;
    }

    public interface Dynamic {
        boolean update(long deltaTimeNanos);
    }

    public interface AbleToMutate {
        AbleToMutate recolor(KColor to);
    }

    public static class Static {
        public final Float hue, saturation, brightness, alpha;
        public final int argb;

        public Static(Float hue, Float saturation, Float brightness, Float alpha) {
            // Normalize HSBA values
            this.hue = hue % 360f;
            this.saturation = Math.min(Math.max(saturation, 0f), 1f);
            this.brightness = Math.min(Math.max(brightness, 0f), 1f);
            this.alpha = Math.min(Math.max(alpha, 0f), 1f);

            // Convert HSB to RGB and incorporate alpha
            int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
            int alphaInt = (int) (this.alpha * 255) & 0xFF;
            this.argb = (alphaInt << 24) | (rgb & 0x00FFFFFF);
        }
    }
}