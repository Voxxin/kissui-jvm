package cat.ella.kissui.nanovg;

import cat.ella.kissui.color.Colors;
import cat.ella.kissui.color.KColor;
import cat.ella.kissui.data.Font;
import cat.ella.kissui.data.KImage;
import cat.ella.kissui.render.Renderer;
import cat.ella.kissui.unit.Vector2;
import cat.ella.kissui.util.IOUtility;
import cat.ella.kissui.util.Int2Map;
import cat.ella.kissui.util.MathHelper;
import cat.ella.kissui.util.Pair;
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

import static cat.ella.kissui.data.KImage.ImageType.Raster;
import static cat.ella.kissui.data.KImage.ImageType.Vector;
import static cat.ella.kissui.util.IOUtility.toDirectByteBuffer;
import static cat.ella.kissui.util.IOUtility.toDirectByteBufferNT;
import static cat.ella.kissui.util.MathHelper.fti;
import static org.lwjgl.nanovg.NanoSVG.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.stb.STBImage.*;

public class NanoVGManager implements Renderer {
    public final static Logger LOGGER = LogManager.getLogger("KISSUI/NVGRenderer");
    private static final ByteBuffer PIXELS = ((ByteBuffer) MemoryUtil.memAlloc(3).put((byte) 112).put((byte) 120).put((byte) 0).flip());

    public Long VG = 0L;
    public Long RASTER = 0L;
    public boolean drawing = false;
    private final ArrayList<Runnable> queue = new ArrayList<>();
    private final Map<Font, Integer> fontHandles = new HashMap<>();
    private final Map<KImage, Integer> images = new HashMap<>();
    private final Map<KImage, Pair<NSVGImage, Int2Map>> svgs = new HashMap<>();
    private final NVGColor nvgColor = NVGColor.calloc();
    private final NVGPaint nvgPaint = NVGPaint.calloc();

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
        getImage(image, size);
    }

    @Override
    public void image(KImage image, Float x, Float y, Float width, Float height, KColor color, Float bottomLeftRadius, Float topLeftRadius, Float topRightRadius, Float bottomRightRadius) {
        NanoVG.nvgSave(VG);
        NanoVG.nvgBeginPath(VG);
        if (bottomLeftRadius > 0 || topLeftRadius > 0 || topRightRadius > 0 || bottomRightRadius > 0) {
            NanoVG.nvgRoundedRectVarying(VG, x, y, width, height,
                    topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius);
        } else {
            NanoVG.nvgRect(VG, x, y, width, height);
        }

        NVGPaint imagePaint = NanoVG.nvgImagePattern(VG, x, y, width, height, 0, getImage(image, new Vector2(width, height)), 1.0f, nvgPaint);
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
        NanoVG.nvgBoxGradient(VG,
                x - spread, y - spread, width + 2*spread, height + 2*spread,
                radius, blur,
                nvgColor,
                nvgColor,
                nvgPaint);

        NanoVG.nvgBeginPath(VG);
        NanoVG.nvgRect(VG, x - spread - blur, y - spread - blur,
                width + 2*(spread + blur), height + 2*(spread + blur));
        NanoVG.nvgFillPaint(VG, nvgPaint);
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
        if (image.getImageType() == Vector) {
            Pair<NSVGImage, Int2Map> entry = svgs.remove(image);
            if (entry != null) {
                nsvgDelete(entry.first);
                int array = 0;
                while(entry.second.iterator().hasNext()) {
                    nvgDeleteImage(VG, entry.second.remove(array));
                    array++;
                }
            }
        } else {
            Integer handle = images.remove(image);
            if (handle != null) {
                nvgDeleteImage(VG, handle);
            }
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
        nvgColor.free();
        nvgPaint.free();
    }

    private int getFont(Font font) {
        if (fontHandles.containsKey(font)) {
            return fontHandles.get(font);
        }

        // Create font handle synchronously
        String fontName = "font" + fontHandles.size()+1;
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

    public int getDefaultImage(Vector2 size) {
        return getImage(new KImage("assets/kissui/images/kissui.png"), size);
    }

    private int widthHash(float width, float height) {
        return Float.floatToIntBits(width) * 31 + Float.floatToIntBits(height);
    }

    private int getImage(KImage image, Vector2 size) {
        return getImage(image, fti(size.x()), fti(size.y()));
    }

    private int getImage(KImage image, float width, float height) {
        switch (image.getImageType()) {
            case Vector -> {
                Pair<NSVGImage, Int2Map> entry = svgs.get(image);
                if (entry == null) {
                    try (InputStream is = image.getData()) {
                        byte[] bytes = IOUtility.toByteArray(is);
                        ByteBuffer data = IOUtility.toDirectByteBufferNT(bytes);
                        return svgLoad(image, data);
                    } catch (IOException e) {
                        LOGGER.error("Failed to load SVG: " + image.getPath(), e);
                        return getDefaultImage(new Vector2(width, height));
                    }
                }
                var svg = entry.first;
                var map = entry.second;
                if (!image.getSize().isPositive()) {
                    KImage.setSize(image, new Vector2(svg.width(), svg.height()));
                }
                return map.getOrPut(widthHash(width, height), () -> svgResize(svg, width, height));
            }
            case Raster -> {
                return images.computeIfAbsent(image, key -> {
                    try (InputStream is = image.getData()) {
                        byte[] bytes = IOUtility.toByteArray(is);
                        ByteBuffer data = IOUtility.toDirectByteBuffer(bytes);
                        return loadRasterImage(image, data);
                    } catch (IOException e) {
                        LOGGER.error("Failed to load raster image: " + image.getPath(), e);
                        return getDefaultImage(new Vector2(width, height));
                    }
                });
            }
            default -> throw new IllegalStateException("Unsupported image type: " + image.getImageType());
        }
    }

    private int svgLoad(KImage image, ByteBuffer data) {
        NSVGImage svg = nsvgParse(data, PIXELS, 96f);
        if (svg == null) {
            throw new IllegalStateException("Failed to parse SVG: " + image.getPath());
        }
        var map = new Int2Map(4);
        if (!image.getSize().isPositive()) {
            KImage.setSize(image, new Vector2(svg.width(), svg.height()));
        }
        int id = svgResize(svg, svg.width(), svg.height());
        map.put(image.getSize().hashCode(), id);
        svgs.put(image, Pair.of(svg, map));
        return id;
    }

    private int svgResize(NSVGImage svg, float width, float height) {
        int wi = (int) ((width == 0f ? svg.width() : width) * 2f);
        int hi = (int) ((height == 0f ? svg.height() : height) * 2f);
        ByteBuffer dst = MemoryUtil.memAlloc(wi * hi * 4);
        float scale = MathHelper.smallestInteger(width / svg.width(), height / svg.height()) * 2f;
        nsvgRasterize(RASTER, svg, 0f, 0f, scale, dst, wi, hi, wi * 4);
        int handle = nvgCreateImageRGBA(VG, wi, hi, 0, dst);
        MemoryUtil.memFree(dst);
        return handle;
    }

    private int loadRasterImage(KImage image, ByteBuffer data) {
        int[] w = new int[1], h = new int[1], comp = new int[1];
        ByteBuffer d = stbi_load_from_memory(data, w, h, comp, 4);
        if (d == null) {
            throw new IllegalStateException("Failed to load image: " + stbi_failure_reason());
        }
        if (!image.getSize().isPositive()) {
            KImage.setSize(image, new Vector2((float)w[0], (float)h[0]));
        }
        int handle = nvgCreateImageRGBA(VG, w[0], h[0], 0, d);
        stbi_image_free(d);
        return handle;
    }

    private NVGColor colorR(KColor color) {
        NVGColor nvgColor = NVGColor.create();
        nvgColor.r(color.getRed() / 255f);
        nvgColor.g(color.getGreen() / 255f);
        nvgColor.b(color.getBlue() / 255f);
        nvgColor.a(color.getAlpha() / 255f);
        return nvgColor;
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