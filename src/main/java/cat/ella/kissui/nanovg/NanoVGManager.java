package cat.ella.kissui.nanovg;

import cat.ella.kissui.color.Colors;
import cat.ella.kissui.color.KColor;
import cat.ella.kissui.data.Font;
import cat.ella.kissui.data.KImage;
import cat.ella.kissui.render.Renderer;
import cat.ella.kissui.unit.Vector2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.nanovg.*;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static cat.ella.kissui.util.MathHelper.fti;
import static org.lwjgl.nanovg.NanoVG.*;

public class NanoVGManager implements Renderer {
    public final static Logger LOGGER = LogManager.getLogger("KISSUI/NVGRenderer");
    private ByteBuffer BUFFER = MemoryUtil.memAlloc(3).put((byte) 112).put((byte) 120).put((byte) 0).flip();

    public Long VG = 0L;
    public Long RASTER = 0L;
    public boolean drawing = false;
    private final ArrayList<Runnable> queue = new ArrayList<>();
    private final Map<Font, Integer> fontHandles = new HashMap<>();
    private final Map<KImage, Integer> imageHandles = new HashMap<>();
    private final NVGColor nvgColor = NVGColor.calloc();

    private int nextFontId = 0;

    @Override
    public void init() {
        if (VG == 0L) VG = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS);
        if (RASTER == 0L) RASTER = NanoSVG.nsvgCreateRasterizer();

        if (VG == 0L) throw new IllegalStateException("Could not initialize NanoVG");
        if (RASTER == 0L) throw new IllegalStateException("Could not initialize NanoSVG");
    }

    @Override
    public void beginFrame(Float width, Float height, Float pixelRatio) {
        if (drawing) throw new IllegalStateException("NanoVG is already drawing");
        while (!queue.isEmpty()) {
            Runnable task = queue.removeLast();
            task.run();
        }
        NanoVG.nvgBeginFrame(VG, width, height, pixelRatio);
        drawing = true;
    }

    @Override
    public void endFrame() {
        if (!drawing) throw new IllegalStateException("NanoVG is not drawing");
        NanoVG.nvgEndFrame(VG);
        drawing = false;
    }

    @Override
    public void globalOpacity(Float opacity) {
        NanoVG.nvgGlobalAlpha(VG, opacity);
    }

    @Override
    public void translate(Float x, Float y) {
        NanoVG.nvgTranslate(VG, x, y);
    }

    @Override
    public void scale(Float sx, Float sy, Float px, Float py) {
        NanoVG.nvgTranslate(VG, px, py);
        NanoVG.nvgScale(VG, sx, sy);
        NanoVG.nvgTranslate(VG, -px, -py);
    }

    @Override
    public void rotate(Float angle, Float px, Float py) {
        NanoVG.nvgTranslate(VG, px, py);
        NanoVG.nvgRotate(VG, angle);
        NanoVG.nvgTranslate(VG, -px, -py);
    }

    @Override
    public void skewX(Float angle, Float px, Float py) {
        float skew = (float) Math.tan(angle);
        float[] t = new float[]{1, 0, skew, 1, 0, 0};
        NanoVG.nvgTranslate(VG, px, py);
        NanoVG.nvgTransform(VG, t[0], t[1], t[2], t[3], t[4], t[5]);
        NanoVG.nvgTranslate(VG, -px, -py);
    }

    @Override
    public void skewY(Float angle, Float px, Float py) {
        float skew = (float) Math.tan(angle);
        float[] t = new float[]{1, skew, 0, 1, 0, 0};
        NanoVG.nvgTranslate(VG, px, py);
        NanoVG.nvgTransform(VG, t[0], t[1], t[2], t[3], t[4], t[5]);
        NanoVG.nvgTranslate(VG, -px, -py);
    }

    @Override
    public void pushScissor(Float x, Float y, Float width, Float height) {
        NanoVG.nvgSave(VG);
        NanoVG.nvgScissor(VG, x, y, width, height);
    }

    @Override
    public void pushScissorIntersecting(Float x, Float y, Float width, Float height) {
        NanoVG.nvgSave(VG);
        NanoVG.nvgIntersectScissor(VG, x, y, width, height);
    }

    @Override
    public void popScissor() {
        NanoVG.nvgRestore(VG);
    }

    @Override
    public void push() {
        NanoVG.nvgSave(VG);
    }

    @Override
    public void pop() {
        NanoVG.nvgRestore(VG);
    }

    @Override
    public void text(Font font, Float x, Float y, String text, KColor color, Float size) {
        if (color.isTransparent()) return;

        NanoVG.nvgFontSize(VG, size);
        NanoVG.nvgFontFaceId(VG, getFont(font));
        nvgTextAlign(VG, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        color(color);
        nvgFillColor(VG, nvgColor);
        NanoVG.nvgText(VG, x, y, text);
    }

    @Override
    public Vector2 textBounds(Font font, String text, Float size) {
        int fontHandle = fontHandles.getOrDefault(font, -1);
        if (fontHandle == -1) return new Vector2(0F, 0F);

        NanoVG.nvgFontSize(VG, size);
        NanoVG.nvgFontFaceId(VG, fontHandle);
        float[] bounds = new float[4];
        NanoVG.nvgTextBounds(VG, 0, 0, text, bounds);
        return new Vector2(bounds[2] - bounds[0], bounds[3] - bounds[1]);
    }

    @Override
    public void initializeImage(KImage image, Vector2 size) {
        if (imageHandles.containsKey(image)) return;
        int flags = 0;
//        int handle = NanoVG.nvgCreateImageRGBA(VG, (int) size.x(), (int) size.y(), flags, image.getData());
        int handle = NanoVG.nvgCreateImageRGBA(VG, fti(size.x()), fti(size.y()), flags, null);
        imageHandles.put(image, handle);
    }

    @Override
    public void image(KImage image, Float x, Float y, Float width, Float height, int colorMask, Float bottomLeftRadius, Float topLeftRadius, Float topRightRadius, Float bottomRightRadius) {
        Integer handle = imageHandles.get(image);
        if (handle == null) {
            LOGGER.error("Image not initialized: " + image);
            return;
        }

        NanoVG.nvgSave(VG);
        NanoVG.nvgBeginPath(VG);
        if (bottomLeftRadius > 0 || topLeftRadius > 0 || topRightRadius > 0 || bottomRightRadius > 0) {
            NanoVG.nvgRoundedRectVarying(VG, x, y, width, height,
                    topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
        } else {
            NanoVG.nvgRect(VG, x, y, width, height);
        }

        NVGPaint nvgPaint = NVGPaint.create();

        NVGPaint imagePaint = NanoVG.nvgImagePattern(VG, x, y, width, height, 0, handle, 1.0f, nvgPaint);
        NanoVG.nvgFillPaint(VG, imagePaint);
        NanoVG.nvgFill(VG);
        NanoVG.nvgRestore(VG);
    }

    @Override
    public void rect(Float x, Float y, Float width, Float height, KColor color, Float bottomLeftRadius, Float topLeftRadius, Float topRightRadius, Float bottomRightRadius) {
        NanoVG.nvgBeginPath(VG);
        if (bottomLeftRadius > 0 || topLeftRadius > 0 || topRightRadius > 0 || bottomRightRadius > 0) {
            NanoVG.nvgRoundedRectVarying(VG, x, y, width, height,
                    topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
        } else {
            NanoVG.nvgRect(VG, x, y, width, height);
        }

        color(color);
        NanoVG.nvgFillColor(VG, nvgColor);
        NanoVG.nvgFill(VG);
    }

    @Override
    public void hollowRect(Float x, Float y, Float width, Float height, KColor color, int lineWidth, Float bottomLeftRadius, Float topLeftRadius, Float topRightRadius, Float bottomRightRadius) {
        NanoVG.nvgBeginPath(VG);
        if (bottomLeftRadius > 0 || topLeftRadius > 0 || topRightRadius > 0 || bottomRightRadius > 0) {
            NanoVG.nvgRoundedRectVarying(VG, x, y, width, height,
                    topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
        } else {
            NanoVG.nvgRect(VG, x, y, width, height);
        }

        color(color);
        NanoVG.nvgStrokeColor(VG, this.nvgColor);
        NanoVG.nvgStrokeWidth(VG, lineWidth);
        NanoVG.nvgStroke(VG);
    }

    @Override
    public void line(Float x1, Float y1, Float x2, Float y2, KColor color, Float width) {
        NanoVG.nvgBeginPath(VG);
        NanoVG.nvgMoveTo(VG, x1, y1);
        NanoVG.nvgLineTo(VG, x2, y2);
        color(color);
        NanoVG.nvgStrokeColor(VG, this.nvgColor);
        NanoVG.nvgStrokeWidth(VG, width);
        NanoVG.nvgStroke(VG);
    }

    @Override
    public void dropShadow(Float x, Float y, Float width, Float height, Float blur, Float spread, Float radius) {
        NVGPaint base = NVGPaint.create();
        NVGPaint shadowPaint = NanoVG.nvgBoxGradient(VG,
                x - spread, y - spread, width + 2*spread, height + 2*spread,
                radius, blur,
                this.nvgColor,
                this.nvgColor,
                base);

        NanoVG.nvgBeginPath(VG);
        NanoVG.nvgRect(VG, x - spread - blur, y - spread - blur,
                width + 2*(spread + blur), height + 2*(spread + blur));
        NanoVG.nvgFillPaint(VG, shadowPaint);
        NanoVG.nvgFill(VG);
    }

    @Override
    public boolean transformationPoint() {
        return false;
    }

    @Override
    public void delete(Font font) {
        fontHandles.remove(font);
    }

    @Override
    public void delete(KImage image) {
        Integer handle = imageHandles.remove(image);
        if (handle != null) {
            NanoVG.nvgDeleteImage(VG, handle);
        }
    }

    @Override
    public void cleanup() {
        if (VG != 0L) {
            NanoVGGL3.nvgDelete(VG);
            VG = 0L;
        }
        if (RASTER != 0L) {
            NanoSVG.nsvgDeleteRasterizer(RASTER);
            RASTER = 0L;
        }
        if (nvgColor != null) {
            nvgColor.free();
        }
    }

    private int getFont(Font font) {
        if (fontHandles.containsKey(font)) {
            return fontHandles.get(font);
        }

        // Create font handle synchronously
        String fontName = "font" + nextFontId++;
        ByteBuffer fontData = null;
        try (InputStream is = getClass().getResourceAsStream(font.path())) {
            if (is == null) {
                throw new RuntimeException("Font resource not found: " + font.path());
            }
            byte[] bytes = is.readAllBytes();
            fontData = MemoryUtil.memAlloc(bytes.length);
            fontData.put(bytes);
            fontData.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int handle = nvgCreateFontMem(VG, fontName, fontData, true);

        if (handle == -1) {
            LOGGER.error("Failed to create font: " + font);
            MemoryUtil.memFree(fontData);
            return -1;
        }

        fontHandles.put(font, handle);
        return handle;
    }


    private void color(KColor color) {
        nvgColor.r(color.getRed() / 255f);
        nvgColor.g(color.getGreen() / 255f);
        nvgColor.b(color.getBlue() / 255f);
        nvgColor.a(color.getAlpha() / 255f);
    }

    private void color(int colorMask) {
        nvgColor.r((colorMask & 0xFF) / 255f);
        nvgColor.g((colorMask >> 8 & 0xFF) / 255f);
        nvgColor.b((colorMask >> 16 & 0xFF) / 255f);
        nvgColor.a((colorMask >> 24 & 0xFF) / 255f);
    }
}