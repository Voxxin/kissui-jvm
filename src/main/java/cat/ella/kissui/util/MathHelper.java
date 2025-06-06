package cat.ella.kissui.util;

public class MathHelper {
    public MathHelper() {}

    public static int fti(float f) { return (int) Math.floor(f); }

    public static float smallestInteger(float a, float b) {
        return Math.abs(a - 1f) <= Math.abs(b - 1f) ? a : b;
    }

}
