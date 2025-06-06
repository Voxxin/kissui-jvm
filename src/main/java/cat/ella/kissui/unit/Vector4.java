package cat.ella.kissui.unit;

public record Vector4(float x, float y, float z, float w) {
    public static final Vector4 ZERO = new Vector4(0, 0, 0, 0);
    public static final Vector4 ONE = new Vector4(1, 1, 1, 1);

}
