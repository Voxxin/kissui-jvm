package cat.ella.kissui.unit;

public record Vector2(long value) {

    public float x() {
        return Float.intBitsToFloat((int)(value >>> 32));
    }

    public float y() {
        return Float.intBitsToFloat((int)(value & 0xFFFFFFFFL));
    }

    public boolean isNegative() {
        return x() < 0 && y() < 0;
    }

    public boolean isZero() {
        return x() == 0 && y() == 0;
    }

    float get(int index) {
        return switch (index) {
            case 0 -> x();
            case 1 -> y();
            default -> throw new IndexOutOfBoundsException("Index out of bound for Vector2: " + index);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector2 other)) return false;
        return this.value == other.value;
    }

    public static class Constants {
        public static final Vector2 ZERO = of(0F, 0F);
        public static final Vector2 ONE = of(1F, 1F);

        public static Vector2 of(float x, float y) {
            long packed = ((long) Float.floatToRawIntBits(x) << 32) | (Float.floatToRawIntBits(y) & 0xFFFFFFFFL);
            return new Vector2(packed);
        }
    }
}