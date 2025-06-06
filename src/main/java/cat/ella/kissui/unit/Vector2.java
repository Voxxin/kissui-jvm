package cat.ella.kissui.unit;

public record Vector2(Float x, Float y) {
    public static final Vector2 ZERO = new Vector2(0F, 0F);
    public static final Vector2 ONE = new Vector2(1F, 1F);

    public Float x() {
        return x;
    }

    public Float y() {
        return y;
    }

    public boolean isNegative() {
        return x() < 0 && y() < 0;
    }
    public boolean isPositive() {
        return x() > 0 && y() > 0;
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
        return this.x.equals(other.x) && this.y.equals(other.y);
    }
}