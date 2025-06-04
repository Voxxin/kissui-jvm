package cat.ella.kissui.render;

import cat.ella.kissui.component.KImage;
import cat.ella.kissui.data.Font;
import cat.ella.kissui.unit.Vector2;

import java.awt.*;

public interface Renderer extends AutoCloseable{

    void init();

    void beginFrame(Float width, Float height, Float pixelRatio);

    void endFrame();

    void globalOpacity(Float opacity);

    default void resetGlobalOpacity() {
        globalOpacity(1f);
    }

    void translate(Float x, Float y);

    void scale(Float sx, Float sy, Float px, Float py);

    void rotate(Double angle, Float px, Float py);

    void skewX(Float angle, Float px, Float py);

    void skewY(Float angle, Float px, Float py);

    void pushScissor(Float x, Float y, Float width, Float height);

    void pushScissorIntersecting(Float x, Float y, Float width, Float height);

    void popScissor();

    void push();

    void pop();

    void text(
            Font font,
            Float x, Float y,
            String text,
            Color color,
            Float size
    );

    Vector2 textBounds(Font font, String text, Float size);

    void initializeImage(KImage image, Vector2 size);

    void image(
            KImage image,
            Float x, Float y,
            Float width, Float height,
            int colorMask,
            Float bottomLeftRadius, Float topLeftRadius,
            Float topRightRadius, Float bottomRightRadius
    );

    void rect(
            Float x, Float y,
            Float width, Float height,
            int colorMask,
            Float bottomLeftRadius, Float topLeftRadius,
            Float topRightRadius, Float bottomRightRadius
    );

    void hollowRect(
            Float x, Float y,
            Float width, Float height,
            Color color,
            int lineWidth,
            Float bottomLeftRadius, Float topLeftRadius,
            Float topRightRadius, Float bottomRightRadius
    );


    default void image(
            KImage image,
            Float x, Float y,
            Float width, Float height,
            Float radius,
            int colorMask
    ) {
        image(image, x, y, width, height, colorMask, radius, radius, radius, radius);
    }

    default void image(
            KImage image,
            Float x, Float y,
            Float width, Float height,
            Float[] radiusArr,
            int colorMask
    ) {
        image(image, x, y, width, height, colorMask, radiusArr[0], radiusArr[1], radiusArr[2], radiusArr[3]);
    }

    default void rect(
            Float x, Float y,
            Float width, Float height,
            Color color,
            Float radius) {
        rect(x, y, width, height, color.getRGB(), radius, radius, radius, radius);
    }

    default void rect(
            Float x, Float y,
            Float width, Float height,
            Color color,
            Float[] radiusArr) {
        rect(x, y, width, height, color.getRGB(), radiusArr[0], radiusArr[1], radiusArr[2], radiusArr[3]);
    }

    default void hollowRect(
            Float x, Float y,
            Float width, Float height,
            Color color,
            int lineWidth,
            Float radius
    ) {
        hollowRect(x, y, width, height, color, lineWidth, radius, radius, radius, radius);
    };

    default void hollowRect(
            Float x, Float y,
            Float width, Float height,
            Color color,
            int lineWidth,
            Float[] radiusArr
    ) {
        hollowRect(x, y, width, height, color, lineWidth, radiusArr[0], radiusArr[1], radiusArr[2], radiusArr[3]);
    };

    void line(
            Float x1, Float y1,
            Float x2, Float y2,
            Color color,
            Float width
    );

    void dropShadow(
            Float x, Float y,
            Float width, Float height,
            Float blur,
            Float spread,
            Float radius
    );

    boolean transformationPoint();

    void delete(Font font);

    void delete(KImage image);

    void cleanup();

    @Override
    default void close() {
        cleanup();
    };
}
