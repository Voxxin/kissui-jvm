package cat.ella.kissui.nanovg;

import cat.ella.kissui.component.KImage;
import cat.ella.kissui.data.Font;
import cat.ella.kissui.render.Renderer;
import cat.ella.kissui.unit.Vector2;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class NanoVGManager implements Renderer {

    public Long VG = 0L;
    public Long RASTER = 0L;
    public boolean drawing = false;
    private final ArrayList<Runnable> queue = new ArrayList<>();

    @Override
    public void init() {
        if (VG == 0L) VG = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS);
        if (RASTER == 0L) RASTER = NanoSVG.nsvgCreateRasterizer();

        if (VG != 0L) throw new IllegalStateException("Could not initialize NanoVG");
        if (RASTER != 0L) throw new IllegalStateException("Could not initialize NanoSVG");
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

    }

    @Override
    public void scale(Float sx, Float sy, Float px, Float py) {

    }

    @Override
    public void rotate(Double angle, Float px, Float py) {

    }

    @Override
    public void skewX(Float angle, Float px, Float py) {

    }

    @Override
    public void skewY(Float angle, Float px, Float py) {

    }

    @Override
    public void pushScissor(Float x, Float y, Float width, Float height) {

    }

    @Override
    public void pushScissorIntersecting(Float x, Float y, Float width, Float height) {

    }

    @Override
    public void popScissor() {

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
    public void text(Font font, Float x, Float y, String text, Color color, Float size) {

    }

    @Override
    public Vector2 textBounds(Font font, String text, Float size) {
        return null;
    }

    @Override
    public void initializeImage(KImage image, Vector2 size) {

    }

    @Override
    public void image(KImage image, Float x, Float y, Float width, Float height, int colorMask, Float bottomLeftRadius, Float topLeftRadius, Float topRightRadius, Float bottomRightRadius) {

    }

    @Override
    public void rect(Float x, Float y, Float width, Float height, int colorMask, Float bottomLeftRadius, Float topLeftRadius, Float topRightRadius, Float bottomRightRadius) {

    }

    @Override
    public void hollowRect(Float x, Float y, Float width, Float height, Color color, int lineWidth, Float bottomLeftRadius, Float topLeftRadius, Float topRightRadius, Float bottomRightRadius) {

    }

    @Override
    public void line(Float x1, Float y1, Float x2, Float y2, Color color, Float width) {

    }

    @Override
    public void dropShadow(Float x, Float y, Float width, Float height, Float blur, Float spread, Float radius) {

    }

    @Override
    public boolean transformationPoint() {
        return false;
    }

    @Override
    public void delete(Font font) {

    }

    @Override
    public void delete(KImage image) {

    }

    @Override
    public void cleanup() {

        NanoVGGL3.nvgDelete(VG);
    }

//    private class NVGFont(String id, ByteBuffer data);
}
